package vct.col.ast.expr.op.bool

import vct.col.ast.{ComputationalAnd, TBool, Type}
import vct.col.print.{Ctx, Doc, Precedence}

trait ComputationalAndImpl[G] { this: ComputationalAnd[G] =>
  override def t: Type[G] = TBool()

  override def precedence: Int = Precedence.BIT_AND
  override def layout(implicit ctx: Ctx): Doc = lassoc(left, "&", right)
}