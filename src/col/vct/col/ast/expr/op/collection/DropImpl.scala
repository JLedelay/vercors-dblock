package vct.col.ast.expr.op.collection

import vct.col.ast.{Drop, Type}
import vct.col.print.{Ctx, Doc, Precedence}

trait DropImpl[G] { this: Drop[G] =>
  override def t: Type[G] = xs.t

  override def precedence: Int = Precedence.POSTFIX
  override def layout(implicit ctx: Ctx): Doc = assoc(xs) <> "[" <> count <> "..]"
}