package vct.col.feature

sealed trait Feature

case object DynamicallyTypedCollection extends Feature
case object ContextSensitiveNode extends Feature
case object CurrentThread extends Feature
case object StarSubscript extends Feature
case object AxiomaticLibraryType extends Feature
case object WildcardReadPermission extends Feature
case object SequenceRange extends Feature
case object BuiltinArrayOperators extends Feature
case object NumericReductionOperator extends Feature
case object InlineQuantifierPattern extends Feature
case object Classes extends Feature
case object Models extends Feature
case object Pointers extends Feature
case object Arrays extends Feature
case object BitOperators extends Feature
case object AmbiguousOperators extends Feature
case object Exponents extends Feature
case object MagicWand extends Feature
case object SugarPermissionOperator extends Feature
case object SugarCollectionOperator extends Feature
case object PermutationOperator extends Feature
case object MatrixVector extends Feature
case object TypeValuesAndGenerics extends Feature
case object ExpressionWithSideEffects extends Feature
case object NonMethodInvocationEvaluation extends Feature
case object IntrinsicLocks extends Feature
case object JavaThreads extends Feature
case object UnscopedDeclaration extends Feature
case object NonVoidReturn extends Feature
case object LoopIterationContract extends Feature
case object NonTrivialBranch extends Feature
case object SwitchStatement extends Feature
case object TryCatchStatement extends Feature
case object NonWhileLoop extends Feature
case object ParallelRegion extends Feature
case object SendRecv extends Feature
case object SpecIgnore extends Feature
case object Exceptions extends Feature
case object WaitNotify extends Feature
case object ExceptionalLoopControl extends Feature
case object ExoticTypes extends Feature
case object TextTypes extends Feature
case object TermRewriteRules extends Feature
case object ApplicableToBeInlined extends Feature