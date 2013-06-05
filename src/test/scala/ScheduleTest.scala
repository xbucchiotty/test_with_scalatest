import org.scalatest.FunSpec
import org.scalatest.matchers.{MatchResult, BeMatcher, ClassicMatchers, Matcher, ShouldMatchers, HavePropertyMatchResult, HavePropertyMatcher}
import org.scalatest.prop.TableDrivenPropertyChecks

class ScheduleTest extends FunSpec with ShouldMatchers with AmountImplicits with TableDrivenPropertyChecks with TableMatchers {

  import Assertions.{balance, cashDate, flow}

  describe("A schedule") {
    it("should describe a generate 5 good cash flows given following properties") {
      implicit val EUR = "EUR"

      val results: Schedule = Schedule(Amount(10000, "EUR"), 5, "01/01/2013")

      results should matchTable(
        (cashDate, flow, balance),
        ("01/01/2013", 2000.0, 8000.0),
        ("01/02/2013", 2000.0, 6000.0),
        ("01/03/2013", 2000.0, 4000.0),
        ("01/04/2013", 2000.0, 2000.0),
        ("01/05/2013", 2000.0, 0.0)
      )
    }
  }
}

object Assertions {

  import TableMatchers.CheckColumn

  def cashDate: CheckColumn[CashFlow, String] = (expectedValue => {
    new HavePropertyMatcher[CashFlow, String] {
      def apply(actual: CashFlow): HavePropertyMatchResult[String] = HavePropertyMatchResult(actual.date.toString == expectedValue, "date", expectedValue, actual.date.toString)
    }
  })

  def flow: CheckColumn[CashFlow, Double] = (expectedValue => {
    new HavePropertyMatcher[CashFlow, Double] {
      def apply(actual: CashFlow): HavePropertyMatchResult[Double] = HavePropertyMatchResult(actual.flow.value == expectedValue, "flow", expectedValue, actual.flow.value)
    }
  })

  def balance: CheckColumn[CashFlow, Double] = (expectedValue => {
    new HavePropertyMatcher[CashFlow, Double] {
      def apply(actual: CashFlow): HavePropertyMatchResult[Double] = HavePropertyMatchResult(actual.balance.value == expectedValue, "balance", expectedValue, actual.balance.value)
    }
  })
}