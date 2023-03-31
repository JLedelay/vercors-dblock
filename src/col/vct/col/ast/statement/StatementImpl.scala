package vct.col.ast.statement

import vct.col.ast.Statement
import vct.col.ast.node.NodeFamilyImpl
import vct.col.print._

trait StatementImpl[G] extends NodeFamilyImpl[G] { this: Statement[G] =>
  def layoutAsBlock(implicit ctx: Ctx): Doc =
    Text("{") <>>
      { this.layout } <+/>
    "}"
}
