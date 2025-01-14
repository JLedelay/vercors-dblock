package vct.col.ast.expr.op.collection

import vct.col.ast.{RemoveAt, Type}
import vct.col.print.{Ctx, Doc, Precedence, Group}

trait RemoveAtImpl[G] { this: RemoveAt[G] =>
  override def t: Type[G] = xs.t

  override def precedence: Int = Precedence.POSTFIX
  override def layout(implicit ctx: Ctx): Doc = Group(assoc(xs) <> ".removeAt(" <> Doc.arg(i) <> ")")
}