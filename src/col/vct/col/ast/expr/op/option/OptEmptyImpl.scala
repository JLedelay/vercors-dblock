package vct.col.ast.expr.op.option

import vct.col.ast.{OptEmpty, TBool, Type}
import vct.col.print.{Ctx, Doc, Precedence}

trait OptEmptyImpl[G] { this: OptEmpty[G] =>
  override def t: Type[G] = TBool()

  override def precedence: Int = Precedence.POSTFIX
  override def layout(implicit ctx: Ctx): Doc = assoc(opt) <> ".isEmpty"
}