package vct.col.ast.statement.nonexecutable

import vct.col.ast.SpecIgnoreEnd
import vct.col.print.{Ctx, Doc, Text}

trait SpecIgnoreEndImpl[G] { this: SpecIgnoreEnd[G] =>
  override def layout(implicit ctx: Ctx): Doc =
    Doc.inlineSpec(Text("spec_ignore }"))
}