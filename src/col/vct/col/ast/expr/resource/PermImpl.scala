package vct.col.ast.expr.resource

import vct.col.ast.{Perm, TResource, Type}
import vct.col.print.{Ctx, Doc, Group, Precedence, Text}

trait PermImpl[G] { this: Perm[G] =>
  override def t: Type[G] = TResource()

  override def precedence: Int = Precedence.POSTFIX
  override def layout(implicit ctx: Ctx): Doc =
    Group(Text("Perm(") <> Doc.args(Seq(loc, perm)) <> ")")
}