// -*- tab-width:2 ; indent-tabs-mode:nil -*-

package vct.main;

import hre.ast.Context;
import hre.ast.MessageOrigin;
import hre.debug.HeapDump;
import hre.io.PrefixPrintStream;
import hre.util.TestReport;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import vct.col.annotate.DeriveModifies;
import vct.col.ast.*;
import vct.col.rewrite.AssignmentRewriter;
import vct.col.rewrite.ConstructorRewriter;
import vct.col.rewrite.DefineDouble;
import vct.col.rewrite.ExplicitPermissionEncoding;
import vct.col.rewrite.FinalizeArguments;
import vct.col.rewrite.Flatten;
import vct.col.rewrite.GlobalizeStatics;
import vct.col.rewrite.GuardedCallExpander;
import vct.col.rewrite.ReorderAssignments;
import vct.col.rewrite.ResolveAndMerge;
import vct.col.rewrite.ReferenceEncoding;
import vct.col.rewrite.SimplifyCalls;
import vct.col.rewrite.VoidCalls;
import vct.col.util.SimpleTypeCheck;
import vct.options.VerCorsToolOptionStore;
import vct.options.VerCorsToolSettings;
import static hre.System.*;
import static hre.ast.Context.globalContext;

class Main
{  
  private static ASTClass program;
  static {
    program=new ASTClass();
    program.setOrigin(new MessageOrigin("root class"));
  }
  
  private static void parseFile(String name){
    int dot=name.lastIndexOf('.');
    if (dot<0) {
      Fail("cannot deduce language of %s",name);
    }
    String lang=name.substring(dot+1);
    if (lang.equals("pvl")){
      //TODO: fix this kludge.
      vct.col.ast.ASTNode.pvl_mode=true;
    }
    Progress("Parsing %s file %s",lang,name);
    program.add_static(Parser.parse(lang,name));
    Progress("Read %s succesfully",name);
  }

  private static int parseFilesFromFileList(String fileName)
  {
      LineNumberReader str = null;
      int cnt = 0;
      try
      {
         str = new LineNumberReader(new FileReader(new File(fileName)));
         String s;

         while ((s = str.readLine()) != null) {
          cnt++;
          parseFile(s);
         }
      }
      catch(Exception e) { e.printStackTrace(); }
      finally { if (str != null) try { str.close(); } catch(Exception e) {}  }
      return cnt;
   }

  public static void main(String[] args) throws Throwable
  {
    PrefixPrintStream out=new PrefixPrintStream(System.out);
    VerCorsToolSettings.parse(args);
    VerCorsToolOptionStore options = VerCorsToolSettings.getOptionStore();
    if(options.isDebugSet()){
      for(String name:options.getDebug()){
        hre.System.EnableDebug(name,java.lang.System.err,"vct("+name+")");
      }
    }
    hre.System.setProgressReporting(options.isProgressSet());
    Hashtable<String,CompilerPass> defined_passes=new Hashtable<String,CompilerPass>();
    Hashtable<String,ValidationPass> defined_checks=new Hashtable<String,ValidationPass>();
    defined_passes.put("java",new CompilerPass("print AST in java syntax"){
      public ASTClass apply(ASTClass arg){
        vct.java.printer.JavaPrinter.dump(System.out,arg);
        return arg;
      }
    });
    defined_passes.put("dump",new CompilerPass("dump AST"){
      public ASTClass apply(ASTClass arg){
        PrefixPrintStream out=new PrefixPrintStream(System.out);
        HeapDump.tree_dump(out,arg,ASTNode.class);
        return arg;
      }
    });
    defined_passes.put("assign",new CompilerPass("change inline assignments to statements"){
      public ASTClass apply(ASTClass arg){
        return (ASTClass)arg.apply(new AssignmentRewriter());
      }
    });
    defined_checks.put("boogie",new ValidationPass("verify with Boogie"){
      public TestReport apply(ASTClass arg){
        return vct.boogie.Main.TestBoogie(arg);
      }
    });
    defined_checks.put("chalice",new ValidationPass("verify with Chalice"){
      public TestReport apply(ASTClass arg){
        return vct.boogie.Main.TestChalice(arg);
      }
    });
    defined_passes.put("check",new CompilerPass("run a type check"){
      public ASTClass apply(ASTClass arg){
        new SimpleTypeCheck(arg).check(arg);
        return arg;
      }
    });
    defined_passes.put("define_double",new CompilerPass("Rewrite double as a non-native data type."){
      public ASTClass apply(ASTClass arg){
        return DefineDouble.rewrite(arg);
      }
    });
    defined_passes.put("expand",new CompilerPass("expand guarded method calls"){
      public ASTClass apply(ASTClass arg){
        return (ASTClass)arg.apply(new GuardedCallExpander());
      }
    });
    defined_passes.put("explicit_encoding",new CompilerPass("encode required and ensured permission as ghost arguments"){
      public ASTClass apply(ASTClass arg){
        return (ASTClass)arg.apply(new ExplicitPermissionEncoding());
      }
    });
    defined_passes.put("finalize_args",new CompilerPass("???"){
      public ASTClass apply(ASTClass arg){
        return (ASTClass)arg.apply(new FinalizeArguments());
      }
    });
    defined_passes.put("flatten",new CompilerPass("remove nesting of expression"){
      public ASTClass apply(ASTClass arg){
        return (ASTClass)arg.apply(new Flatten());
      }
    });
    defined_passes.put("globalize",new CompilerPass("split classes into static and dynamic parts"){
      public ASTClass apply(ASTClass arg){
        return (ASTClass)arg.apply(new GlobalizeStatics());
      }
    });
    defined_checks.put("hoare_logic",new ValidationPass("Check Hoare Logic Proofs"){
      public TestReport apply(ASTClass arg){
        Brain.main(arg);
        return null;
      }
    });
    defined_passes.put("modifies",new CompilerPass("Derive modifies clauses for all contracts"){
      public ASTClass apply(ASTClass arg){
        new DeriveModifies().annotate(arg);
        return arg;
      }
    });
    defined_passes.put("reorder",new CompilerPass("reorder statements (e.g. all declarations at the start of a bock"){
      public ASTClass apply(ASTClass arg){
        return (ASTClass)arg.apply(new ReorderAssignments());
      }
    });
    defined_passes.put("refenc",new CompilerPass("apply reference encoding"){
      public ASTClass apply(ASTClass arg){
        return (ASTClass)arg.apply(new ReferenceEncoding());
      }
    });
    defined_passes.put("resolv",new CompilerPass("resolv all names"){
      public ASTClass apply(ASTClass arg){
        return (ASTClass)arg.apply(new ResolveAndMerge());
      }
    });
    defined_passes.put("rm_cons",new CompilerPass("???"){
      public ASTClass apply(ASTClass arg){
        return (ASTClass)arg.apply(new ConstructorRewriter());
      }
    });
    defined_passes.put("simplify_calls",new CompilerPass("???"){
      public ASTClass apply(ASTClass arg){
        return (ASTClass)arg.apply(new SimplifyCalls());
      }
    });
    defined_passes.put("voidcalls",new CompilerPass("???"){
      public ASTClass apply(ASTClass arg){
        return (ASTClass)arg.apply(new VoidCalls());
      }
    });
    if (options.isHelpPassesSet()) {
      System.out.println("The following passes are available:"); 
      for (Entry<String, CompilerPass> entry:defined_passes.entrySet()){
        System.out.printf(" %-12s : %s%n",entry.getKey(),entry.getValue().getDescripion());
      }
      for (Entry<String, ValidationPass> entry:defined_checks.entrySet()){
        System.out.printf(" %-12s : %s%n",entry.getKey(),entry.getValue().getDescripion());
      }
      System.exit(0);
    }
    Progress("parsing inputs...");
    int cnt = 0;
    long startTime = System.currentTimeMillis();
    for(File f : options.getFiles()){
      if (!options.isSkipContextSet()){
        globalContext.add(f.toString());
      }
      parseFile(f.getPath());
      cnt++;
    }
    Progress("Parsed %d files in: %dms",cnt,System.currentTimeMillis() - startTime);
    List<String> passes=null;
    if (options.isPassesSet()){
    	passes=options.getPasses();
    } else if (options.isBoogieSet()) {
    	passes=new ArrayList<String>();
      passes.add("resolv");
      passes.add("check");
      passes.add("flatten");
      passes.add("assign");
      if (!options.isHoareLogicSet()) {
        passes.add("finalize_args");
      }
      passes.add("reorder");
      passes.add("simplify_calls");
      if (options.isInferModifiesSet()) {
        passes.add("resolv");
        passes.add("check");
        passes.add("modifies");
      }
      passes.add("resolv");
      passes.add("check");
      passes.add("voidcalls");
      passes.add("resolv");
      passes.add("check");
      passes.add("flatten");
      passes.add("resolv");
      passes.add("check");
    	passes.add("boogie");
    } else if (options.isChaliceSet()) {
    	passes=new ArrayList<String>();
    	passes.add("resolv");
    	passes.add("check");
      if (options.isExplicitPermissionsSet()){
        passes.add("java");
        passes.add("explicit_encoding");
        passes.add("java");
        passes.add("resolv");
        passes.add("check");
      }
      passes.add("flatten");
      passes.add("resolv");
      passes.add("check");
      if (options.isReferenceEncodingSet()){
        passes.add("java");
        passes.add("refenc");
        passes.add("resolv");
        passes.add("java");
        passes.add("check");
      }
      passes.add("globalize");
      passes.add("resolv");
      passes.add("check");
      passes.add("define_double");
      passes.add("resolv");
      passes.add("check");     
    	passes.add("assign");
      passes.add("reorder");
    	passes.add("expand");
    	passes.add("resolv");
    	passes.add("check");
      passes.add("rm_cons");
      passes.add("resolv");
      passes.add("check");
      passes.add("voidcalls");
      passes.add("resolv");
      //passes.add("java");
      passes.add("check");
      passes.add("flatten");
      passes.add("resolv");
      passes.add("check");
    	passes.add("chalice");
    } else if (options.isHoareLogicSet()) {
    	passes=new ArrayList<String>();
    	passes.add("resolv");
    	passes.add("assign");
    	passes.add("hoare_logic");
    } else {
    	Abort("no back-end or passes specified");
    }
    {
      TestReport res=null;
      for(String pass:passes){
        if (res!=null){
          Progress("Ignoring intermediate verdict %s",res.getVerdict());
          res=null;
        }
        CompilerPass task=defined_passes.get(pass);
        if (task!=null){
          Progress("Applying %s ...", pass);
          startTime = System.currentTimeMillis();
          program=task.apply(program);
          Progress(" ... pass took %d ms",System.currentTimeMillis()-startTime);
        } else {
          ValidationPass check=defined_checks.get(pass);
          if (check!=null){
            Progress("Applying %s ...", pass);
            startTime = System.currentTimeMillis();
            res=check.apply(program);
            Progress(" ... pass took %d ms",System.currentTimeMillis()-startTime);
          } else {
            Fail("unknown pass %s",pass);
          }
        }
      }
      if (res!=null) {
        Output("The final verdict is %s",res.getVerdict());
      } else {
        Fail("No overall verdict has been set. Check the output carefully!");
      }
    }
  }
}

