package vct.col.ast.lang.smt

import vct.col.ast.{SmtlibReAll, TSmtlibRegLan, Type}
import vct.col.print._

trait SmtlibReAllImpl[G] { this: SmtlibReAll[G] =>
  override def t: Type[G] = TSmtlibRegLan()
  // def layout(implicit ctx: Ctx): Doc = ???
}