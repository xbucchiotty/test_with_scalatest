import collection.immutable.IndexedSeq
import org.scalatest.matchers.{HavePropertyMatchResult, MatchResult, Matcher, ClassicMatchers, HavePropertyMatcher}

trait TableMatchers {

  def matchTable[Actual] = new MatchTableWord[Actual]

}

object TableMatchers {
  type CheckColumn[Actual, P] = (P => HavePropertyMatcher[Actual, P])
}

final class MatchTableWord[Actual] extends ClassicMatchers {

  private type CheckColumn[P] = (P => HavePropertyMatcher[Actual, P])

  def apply[A, B, C](properties: (CheckColumn[A], CheckColumn[B], CheckColumn[C]), expectations: (A, B, C)*): Matcher[Traversable[Actual]] = new Matcher[Traversable[Actual]] {
    def apply(results: Traversable[Actual]): MatchResult = {
      val haveSize = have(size(expectations.size))(results)

      if (!haveSize.matches) {
        haveSize
      } else {
        val allExpectations: Seq[Seq[HavePropertyMatcher[Actual, Any]]] = expectations.map(expectation => {
          for (i <- 0 until properties.productArity) yield {
            val property = properties.productElement(i).asInstanceOf[CheckColumn[Any]]
            val propertyValue = expectation.productElement(i)

            property(propertyValue)
          }
        })

        val expectationsIt = allExpectations.iterator
        val evaluations: Traversable[(Actual, HavePropertyMatchResult[Any])] = results.flatMap(result => {
          expectationsIt.next().map(exp => (result, exp.apply(result)))
        })

        val failures = evaluations.filter(!_._2.matches)

        if (failures.isEmpty) {
          MatchResult(true, "", "")
        } else {
          val (result, failure) = failures.head

          MatchResult(false, s"Expected value <${failure.expectedValue}>, actual <${failure.actualValue}> for property '${failure.propertyName}' in [$result]", s"Expected value not be <${failure.expectedValue}> for property '${failure.propertyName}' in [$result]")
        }
      }
    }
  }
}
