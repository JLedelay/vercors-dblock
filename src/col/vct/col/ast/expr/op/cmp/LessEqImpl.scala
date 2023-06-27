package vct.col.ast.expr.op.cmp

import vct.col.ast.LessEq
import vct.col.print.{Ctx, Doc, Precedence}

trait LessEqImpl[G] { this: LessEq[G] =>
  override def precedence: Int = Precedence.RELATIONAL
  override def layout(implicit ctx: Ctx): Doc = lassoc(left, "<=", right)
}