/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package viper.silicon.state

import scala.collection.mutable
import viper.silicon.interfaces.state.{Heap, Store, State}
import viper.silicon.state.terms._
import viper.silicon.supporters.qps.SummarisingFvfDefinition

package object utils {
  /** Note: the method accounts for `ref` occurring in `σ`, i.e. it will not generate the
    * unsatisfiable constraint `ref != ref`.
    */
  def computeReferenceDisjointnesses[ST <: Store[ST], H <: Heap[H], S <: State[ST, H, S]]
                                    (σ: S, ref: Term)
                                    : Seq[Term] = {

    val refs = mutable.HashSet[Term]()
    val refSets = mutable.HashSet[Term]()
    val refSeqs = mutable.HashSet[Term]()

    def collect(t: Term) {
      t.sort match {
        case sorts.Ref => if (t != ref) refs += t
        case sorts.Set(sorts.Ref) => refSets += t
        case sorts.Seq(sorts.Ref) => refSeqs += t
        case _ =>
      }
    }

    /* Collect all Ref/Set[Ref]/Seq[Ref]-typed values from the store */
    σ.γ.values.values foreach collect

    /* Collect all Ref/Set[Ref]/Seq[Ref]-typed terms from heap chunks */
    σ.h.values.foreach {
      case bc: BasicChunk =>
        bc.args foreach collect
        collect(bc.snap)
      case qch: QuantifiedChunk =>
        /* Terms from quantified chunks contain the implicitly quantified receiver `?r`,
         * hence, they can only be used under quantifiers that bind `?r`.
         * An exception are quantified chunks that (definitely) provide permissions to
         * a single location (i.e. for a single receiver) only.
         */
        qch.singletonRcvr.foreach(rcvr => {
          collect(rcvr)
          collect(qch.valueAt(rcvr))
        })
      case _ =>
    }

    val disjointnessAssumptions = mutable.ListBuffer[Term]()

    refs foreach (r => disjointnessAssumptions += (ref !== r))
    refSets foreach (rs => disjointnessAssumptions += Not(SetIn(ref, rs)))
    refSeqs foreach (rs => disjointnessAssumptions += Not(SeqIn(rs, ref)))

    disjointnessAssumptions.result()
  }

  def partitionAuxiliaryTerms(ts: Iterable[Term]): (Iterable[Term], Iterable[Term]) =
    ts.partition {
      case   _: FvfAfterRelation
           | _: Definition
           => true
      case _ => false
    }

  def detectQuantificationProblems(quantification: Quantification): Seq[String] = {
    var problems: List[String] = Nil

    quantification.q match {
      case Exists =>
        /* No checks yet */
      case Forall =>
        /* 1. Check that triggers are present */
        if (quantification.triggers.isEmpty)
          problems ::= s"No triggers given"

        /* 2. Check that each trigger set mentions all quantified variables */
        quantification.triggers.foreach(trigger => {
          val vars =
            trigger.p.foldLeft(Set[Var]()){case (varsAcc, term) =>
              varsAcc ++ term.deepCollect{case v: Var => v}}

          if (!quantification.vars.forall(vars.contains))
            problems ::= s"Trigger set $trigger does not contain all quantified variables"
        })

        /* 3. Check that all triggers are valid */
        quantification.triggers.foreach(trigger => trigger.p.foreach{term =>
          if (!TriggerGenerator.isPossibleTrigger(term))
            problems ::= s"Trigger $term is not a possible trigger"

          term.deepCollect{case t if TriggerGenerator.isForbiddenInTrigger(t) => t}
              .foreach(term => problems ::= s"Term $term may not occur in triggers")
        })
    }

    problems.reverse
  }

  def subterms(t: Term): Seq[Term] = t match {
    case _: Symbol | _: Literal | _: MagicWandChunkTerm => Nil
    case op: BinaryOp[Term@unchecked] => List(op.p0, op.p1)
    case op: UnaryOp[Term@unchecked] => List(op.p)
    case ite: Ite => List(ite.t0, ite.t1, ite.t2)
    case and: And => and.ts
    case or: Or => or.ts
    case _: NoPerm | _: FullPerm => Nil
    case wcp: WildcardPerm => List(wcp.v)
    case fp: FractionPerm => List(fp.n, fp.d)
    case ivp: IsValidPermVar => List(ivp.v)
    case irp: IsReadPermVar => List(irp.v, irp.ub)
    case app: Application[_] => app.args
    case sr: SeqRanged => List(sr.p0, sr.p1)
    case ss: SeqSingleton => List(ss.p)
    case su: SeqUpdate => List(su.t0, su.t1, su.t2)
    case ss: SingletonSet => List(ss.p)
    case ss: SingletonMultiset => List(ss.p)
    case sw: SortWrapper => List(sw.t)
    case d: Distinct => Seq.empty // d.ts.toList
    case q: Quantification => q.vars ++ List(q.body) ++ q.triggers.flatMap(_.p)
    case l: Let =>
      val (vs, ts) = l.bindings.toSeq.unzip
      vs ++ ts :+ l.body
    case Domain(_, fvf) => fvf :: Nil
    case Lookup(_, fvf, at) => fvf :: at :: Nil
    case FvfAfterRelation(_, fvf2, fvf1) => fvf2 :: fvf1 :: Nil
    case SummarisingFvfDefinition(_, fvf, rcvr, _) => Seq(fvf, rcvr)
  }

  /** @see [[viper.silver.ast.utility.Transformer.transform()]] */
  def transform[T <: Term](term: T,
                           pre: PartialFunction[Term, Term] = PartialFunction.empty)
                          (recursive: Term => Boolean = !pre.isDefinedAt(_),
                           post: PartialFunction[Term, Term] = PartialFunction.empty)
                          : T = {

    def go[D <: Term](term: D): D = transform(term, pre)(recursive, post)

    def goTriggers(trigger: Trigger) = Trigger(trigger.p map go)

    def recurse(term: Term): Term = term match {
      case _: Var | _: Function | _: Literal | _: MagicWandChunkTerm | _: Distinct => term
      case q: Quantification => Quantification(q.q, q.vars map go, go(q.body), q.triggers map goTriggers)
      case Plus(t0, t1) => Plus(go(t0), go(t1))
      case Minus(t0, t1) => Minus(go(t0), go(t1))
      case Times(t0, t1) => Times(go(t0), go(t1))
      case Div(t0, t1) => Div(go(t0), go(t1))
      case Mod(t0, t1) => Mod(go(t0), go(t1))
      case Not(t) => Not(go(t))
      case Or(ts) => Or(ts map go : _*)
      case And(ts) => And(ts map go : _*)
      case Implies(t0, t1) => Implies(go(t0), go(t1))
      case Iff(t0, t1) => Iff(go(t0), go(t1))
      case Ite(t0, t1, t2) => Ite(go(t0), go(t1), go(t2))
      case BuiltinEquals(t0, t1) => Equals(go(t0), go(t1))
      case CustomEquals(t0, t1) => Equals(go(t0), go(t1))
      case Less(t0, t1) => Less(go(t0), go(t1))
      case AtMost(t0, t1) => AtMost(go(t0), go(t1))
      case Greater(t0, t1) => Greater(go(t0), go(t1))
      case AtLeast(t0, t1) => AtLeast(go(t0), go(t1))
      case _: NoPerm | _: FullPerm  => term
      case FractionPerm(n, d) => FractionPerm(go(n), go(d))
      case WildcardPerm(v) => WildcardPerm(go(v))
      case IsValidPermVar(v) => IsValidPermVar(go(v))
      case IsReadPermVar(v, ub) => IsReadPermVar(go(v), go(ub))
      case PermTimes(p0, p1) => PermTimes(go(p0), go(p1))
      case IntPermTimes(p0, p1) => IntPermTimes(go(p0), go(p1))
      case PermIntDiv(p0, p1) => PermIntDiv(go(p0), go(p1))
      case PermPlus(p0, p1) => PermPlus(go(p0), go(p1))
      case PermMinus(p0, p1) => PermMinus(go(p0), go(p1))
      case PermLess(p0, p1) => PermLess(go(p0), go(p1))
      case PermAtMost(p0, p1) => PermAtMost(go(p0), go(p1))
      case PermMin(p0, p1) => PermMin(go(p0), go(p1))
      case App(f, ts) => App(f, ts map go)
      case SeqRanged(t0, t1) => SeqRanged(go(t0), go(t1))
      case SeqSingleton(t) => SeqSingleton(go(t))
      case SeqAppend(t0, t1) => SeqAppend(go(t0), go(t1))
      case SeqDrop(t0, t1) => SeqDrop(go(t0), go(t1))
      case SeqTake(t0, t1) => SeqTake(go(t0), go(t1))
      case SeqLength(t) => SeqLength(go(t))
      case SeqAt(t0, t1) => SeqAt(go(t0), go(t1))
      case SeqIn(t0, t1) => SeqIn(go(t0), go(t1))
      case SeqUpdate(t0, t1, t2) => SeqUpdate(go(t0), go(t1), go(t2))
      case SingletonSet(t) => SingletonSet(go(t))
      case SetAdd(t0, t1) => SetAdd(go(t0), go(t1))
      case SetUnion(t0, t1) => SetUnion(go(t0), go(t1))
      case SetIntersection(t0, t1) => SetIntersection(go(t0), go(t1))
      case SetSubset(t0, t1) => SetSubset(go(t0), go(t1))
      case SetDifference(t0, t1) => SetDifference(go(t0), go(t1))
      case SetIn(t0, t1) => SetIn(go(t0), go(t1))
      case SetCardinality(t) => SetCardinality(go(t))
      case SetDisjoint(t0, t1) => SetDisjoint(go(t0), go(t1))
      case SingletonMultiset(t) => SingletonMultiset(go(t))
      case MultisetUnion(t0, t1) => MultisetUnion(go(t0), go(t1))
      case MultisetIntersection(t0, t1) => MultisetIntersection(go(t0), go(t1))
      case MultisetSubset(t0, t1) => MultisetSubset(go(t0), go(t1))
      case MultisetDifference(t0, t1) => MultisetDifference(go(t0), go(t1))
      case MultisetCardinality(t) => MultisetCardinality(go(t))
      case MultisetCount(t0, t1) => MultisetCount(go(t0), go(t1))
      case MultisetAdd(t1, t2) => MultisetAdd(go(t1), go(t2))
      case Combine(t0, t1) => Combine(go(t0), go(t1))
      case First(t) => First(go(t))
      case Second(t) => Second(go(t))
      case SortWrapper(t, s) => SortWrapper(go(t), s)
//      case Distinct(ts) => Distinct(ts map go)
      case Let(bindings, body) => Let(bindings map (p => go(p._1) -> go(p._2)), go(body))
      case Domain(f, fvf) => Domain(f, go(fvf))
      case Lookup(f, fvf, at) => Lookup(f, go(fvf), go(at))
      case FvfAfterRelation(f, fvf2, fvf1) => FvfAfterRelation(f, go(fvf2), go(fvf1))
    }

    val beforeRecursion = pre.applyOrElse(term, identity[Term])

    val afterRecursion =
      if (recursive(term)) recurse(beforeRecursion)
      else beforeRecursion

    post.applyOrElse(afterRecursion, identity[Term]).asInstanceOf[T]
  }
}