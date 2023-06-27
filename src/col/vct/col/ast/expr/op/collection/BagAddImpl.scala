package vct.col.ast.expr.op.collection

import vct.col.ast.{BagAdd, TBag}
import vct.col.print.{Ctx, Doc, Precedence}
import vct.col.typerules.Types

trait BagAddImpl[G] { this: BagAdd[G] =>
  def xsType: TBag[G] = xs.t.asBag.get
  def ysType: TBag[G] = ys.t.asBag.get
  override lazy val t: TBag[G] = TBag(Types.leastCommonSuperType(xsType.element, ysType.element))

  override def precedence: Int = Precedence.ADDITIVE
  override def layout(implicit ctx: Ctx): Doc = lassoc(xs, "+", ys)
}