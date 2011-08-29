package vct.col.util;

import vct.col.ast.ASTClass;
import vct.col.ast.ASTNode;
import vct.col.ast.ASTWith;
import vct.col.ast.AbstractScanner;
import vct.col.ast.AssignmentStatement;
import vct.col.ast.DeclarationStatement;
import vct.col.ast.Method;

public class DefinitionScanner extends AbstractScanner {

  private ClassDefinition root;
  private ClassDefinition current;
  
  public DefinitionScanner(ClassDefinition root){
    this.root=root;
  }

  public void visit(ASTClass c) {
    if (c.getParentClass()==null){
      current=root;
    }
    ClassDefinition tmp=current;
    if (c.getName()!=null) {
      current=current.addNested(c.getName());
    }
    int N;
    N=c.getStaticCount();
    for(int i=0;i<N;i++){
      ASTNode n=c.getStatic(i);
      n.accept(this);
    }
    N=c.getDynamicCount();
    for(int i=0;i<N;i++){
      ASTNode n=c.getDynamic(i);
      n.accept(this);
    }
    if (c.getName()!=null) {
      current=tmp;
    }
  }

  public void visit(DeclarationStatement s){
    current.addField(s.getName());
  }

  public void visit(AssignmentStatement s){
    // TODO: scan for nested classes.
  }

  public void visit(Method m){
    current.addMethod(m.getName());
    // TODO: scan body for nested classes.
  }
  
}
