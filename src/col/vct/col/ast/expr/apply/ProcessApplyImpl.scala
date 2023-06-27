package vct.col.ast.expr.apply

import vct.col.ast.{ProcessApply, TProcess, Type}
import vct.col.print._

trait ProcessApplyImpl[G] { this: ProcessApply[G] =>
  override def t: Type[G] = TProcess()

  override def precedence: Int = Precedence.POSTFIX
  override def layout(implicit ctx: Ctx): Doc =
    Group(Text(ctx.name(process)) <> "(" <> Doc.args(args) <> ")")
}