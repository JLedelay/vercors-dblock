package vct.col.newrewrite.util

import vct.col.ast.{Declaration, Expr, TVar, Type, Variable}
import vct.col.origin.Origin
import vct.col.ref.{LazyRef, Ref}
import vct.col.rewrite.{NonLatchingRewriter, Rewriter}

import scala.reflect.ClassTag

/**
 * Apply a substitution map to expressions
 */
case class Substitute[G](subs: Map[Expr[G], Expr[G]],
                         typeSubs: Map[TVar[G], Type[G]] = Map.empty[TVar[G], Type[G]],
                         bindingSubs: Map[Variable[G], Variable[G]] = Map.empty[Variable[G], Variable[G]],
                         originTrafo: Origin => Origin = identity)
  extends NonLatchingRewriter[G, G] {

  override def lookupSuccessor: Declaration[G] => Option[Declaration[G]] = {
    val here = super.lookupSuccessor
    decl => here(decl).orElse(Some(decl))
  }

  override def dispatch(o: Origin): Origin = originTrafo(o)

  override def dispatch(e: Expr[G]): Expr[G] = e match {
    case expr if subs.contains(expr) => subs(expr)
    case other => rewriteDefault(other)
  }

  override def dispatch(t: Type[G]): Type[G] = t match {
    case v @ TVar(_) if typeSubs.contains(v) => dispatch(typeSubs(v))
    case other => rewriteDefault(other)
  }

  override def dispatch(v: Declaration[G]): Unit = v match {
    case decl: Variable[G] if bindingSubs.contains(decl) => dispatch(bindingSubs(decl))
    case other => rewriteDefault(other)
  }
}
