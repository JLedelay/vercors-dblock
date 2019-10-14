package vct.main;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import vct.antlr4.parser.Parsers;
import vct.col.ast.stmt.decl.ProgramUnit;
import vct.col.rewrite.RewriteSystem;
import vct.util.Configuration;

public class RewriteSystems {

  static Map<File,ProgramUnit> systems=new ConcurrentHashMap<File,ProgramUnit>();
  
  public static RewriteSystem getRewriteSystem(String name){
    File f=new File(name+".jspec");
    if (!f.exists()){
      f=Configuration.getConfigFile(name + ".jspec");
    }
    ProgramUnit unit=systems.get(f);
    if (unit==null) synchronized(systems){
      unit=systems.get(f);
      if (unit==null){
        unit=Parsers.getParser("jspec").parse(f);
        systems.put(f, unit);
      }
    }
    return new RewriteSystem(unit,name);
  }

}
