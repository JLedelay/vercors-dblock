package vct.col.rewrite;

import vct.col.ast.ASTClass;
import vct.col.ast.ASTSpecial;
import vct.col.ast.BlockStatement;
import vct.col.ast.Contract;
import vct.col.ast.ContractBuilder;
import vct.col.ast.DeclarationStatement;
import vct.col.ast.Method;
import vct.col.ast.OperatorExpression;
import vct.col.ast.ProgramUnit;
import vct.col.ast.StandardOperator;
import vct.col.ast.ASTSpecial.Kind;
import vct.col.ast.PrimitiveType.Sort;
import vct.col.util.FeatureScanner;

/**
 * This class encodes the built-in operations of PVL.
 * 
 * This encoding must be run before and pass that renames methods
 * and/or adds argument is run. E.g. JavaEncoder.
 * 
 * @author Stefan Blom
 *
 */
public class PVLEncoder extends AbstractRewriter {

  private static String INV="lock_invariant";
  private static String HELD="lock_held";

  public PVLEncoder(ProgramUnit source) {
    super(source);
  }
  
  public void visit(ASTSpecial s){
    switch(s.kind){
    case Fork:
      result=create.invokation(rewrite(s.args[0]),null,"forkOperator");
      break;
    case Join:
      result=create.invokation(rewrite(s.args[0]),null,"joinOperator");
      break;      
    case Lock:
      currentBlock.add(create.special(ASTSpecial.Kind.Inhale,create.invokation(rewrite(s.args[0]),null,INV)));
      currentBlock.add(create.special(Kind.Unfold,create.invokation(rewrite(s.args[0]),null,INV)));
      currentBlock.add(create.special(ASTSpecial.Kind.Inhale,create.invokation(rewrite(s.args[0]),null,HELD)));
      result=null;
      break;
    case Unlock:
      currentBlock.add(create.special(ASTSpecial.Kind.Exhale,create.invokation(rewrite(s.args[0]),null,HELD)));
      currentBlock.add(create.special(Kind.Fold,create.invokation(rewrite(s.args[0]),null,INV)));
      currentBlock.add(create.special(ASTSpecial.Kind.Exhale,create.invokation(rewrite(s.args[0]),null,INV)));
      result=null;
      break;
    case Wait:
      currentBlock.add(create.special(Kind.Fold,create.invokation(rewrite(s.args[0]),null,INV)));
      currentBlock.add(create.special(ASTSpecial.Kind.Exhale,create.invokation(rewrite(s.args[0]),null,INV)));
      currentBlock.add(create.special(ASTSpecial.Kind.Assert,create.invokation(rewrite(s.args[0]),null,HELD)));
      currentBlock.add(create.special(ASTSpecial.Kind.Inhale,create.invokation(rewrite(s.args[0]),null,INV)));
      currentBlock.add(create.special(Kind.Unfold,create.invokation(rewrite(s.args[0]),null,INV)));
      result=null;
      break;
    case Notify:
      currentBlock.add(create.special(ASTSpecial.Kind.Assert,create.invokation(rewrite(s.args[0]),null,HELD)));
      result=null;
      break;
    default:
      super.visit(s);
    }
  }

  @Override
  public void visit(OperatorExpression e){
    switch (e.operator()) {
    case PVLidleToken:
      result=create.invokation(rewrite(e.arg(0)),null,"idleToken");
      break;
    case PVLjoinToken:
      result=create.invokation(rewrite(e.arg(0)),null,"joinToken");
      break;
    case Held:
      result = create.invokation(rewrite(e.arg(0)), null, HELD);
      break;
    default:
      super.visit(e);
    }
  }
  
  
  @Override
  public void visit(Method m){
    if(m.name().equals(INV)){
      currentTargetClass.add_dynamic(create.predicate(HELD,null));
    }
    if (m.name().equals("run")) {
      Contract c=m.getContract();
      FeatureScanner features=new FeatureScanner();
      c.post_condition.accept(features);
      if(features.usesOperator(StandardOperator.Old)){
        Fail("The post-condition of a run method is not allowed to use the \\old operator.");
      }
      ContractBuilder cb=new ContractBuilder();
      cb.requires(rewrite(c.pre_condition));
      cb.requires(create.invokation(null,null,"idleToken"));
      cb.ensures(create.invokation(null,null,"joinToken"));
      Method fork=create.method_decl(
          create.primitive_type(Sort.Void),
          cb.getContract(),
          "forkOperator",
          new DeclarationStatement[0],
          null
      );
      currentTargetClass.add_dynamic(fork);
      cb=new ContractBuilder();
      cb.requires(create.invokation(null,null,"joinToken"));
      cb.ensures(create.invokation(null,null,"idleToken"));
      cb.ensures(rewrite(c.post_condition));
      Method join=create.method_decl(
          create.primitive_type(Sort.Void),
          cb.getContract(),
          "joinOperator",
          new DeclarationStatement[0],
          null
      );
      currentTargetClass.add_dynamic(join);
      Method token=create.predicate("joinToken",null);
      currentTargetClass.add_dynamic(token);
      token=create.predicate("idleToken",null);
      currentTargetClass.add_dynamic(token);
    }
    if(m.kind==Method.Kind.Constructor){
      ASTClass parent=(ASTClass)m.getParent();
      boolean runnable=false;
      for(Method method:parent.dynamicMethods()){
        if (method.name().equals("run")){
          runnable=true; 
          break;
        }
      }
      if (runnable){
        currentContractBuilder=new ContractBuilder();
        currentContractBuilder.ensures(create.invokation(null,null,"idleToken"));
        super.visit(m);
        Method method=(Method)result;
        BlockStatement block=(BlockStatement)method.getBody();
        block.append(
            create.special(ASTSpecial.Kind.Inhale,
            create.invokation(null,null,"idleToken")));
        return;
      }
    }
    super.visit(m);
  }

}
