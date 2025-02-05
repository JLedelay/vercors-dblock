package vct.col.ast.lang

import vct.col.ast.{JavaNewDefaultArray, TArray, Type}
import vct.col.print.{Ctx, Doc, Precedence, Text}

trait JavaNewDefaultArrayImpl[G] { this: JavaNewDefaultArray[G] =>
  override lazy val t: Type[G] = (0 until (specifiedDims.size + moreDims)).foldLeft(baseType)((t, _) => TArray(t))

  override def precedence: Int = Precedence.POSTFIX
  override def layout(implicit ctx: Ctx): Doc =
    Text("new") <+> baseType <> Doc.fold(specifiedDims.map(Text("[") <> _ <> "]"))(_ <> _) <> "[]".repeat(moreDims)
}