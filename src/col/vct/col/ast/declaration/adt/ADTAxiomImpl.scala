package vct.col.ast.declaration.adt

import vct.col.ast.{ADTAxiom, TBool}
import vct.col.check.{CheckContext, CheckError}
import vct.col.print._

trait ADTAxiomImpl[G] { this: ADTAxiom[G] =>
  override def check(context: CheckContext[G]): Seq[CheckError] = axiom.checkSubType(TBool())

  override def layout(implicit ctx: Ctx): Doc =
    Text("axiom") <+> axiom
}