package vct.col.ast.expr.literal.build

import vct.col.ast.{Range, TInt, TSeq, Type}
import vct.col.print.{Ctx, Doc, Precedence, Text}

trait RangeImpl[G] { this: Range[G] =>
  override def t: Type[G] = TSeq(TInt())

  override def precedence: Int = Precedence.ATOMIC
  override def layout(implicit ctx: Ctx): Doc = Text("{") <> from <+> ".." <+> to <> "}"
}