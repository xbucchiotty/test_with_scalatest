import java.util.Currency
import org.scalatest.FunSpec
import org.scalatest.matchers.{MatchResult, BeMatcher, ShouldMatchers}
import AmountImplicits._
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class AmountTest extends FunSpec with ShouldMatchers with AmountImplicits with GeneratorDrivenPropertyChecks {

  describe("An amount") {
    it("can be added to another amount of the same currency") {
      implicit val noExchanges = Exchanges.noop

      forAll {
        (a: Double, b: Double) => {
          val result = Amount(a, "EUR") + Amount(b, "EUR")
          result should have('value(a + b))
          result should be(in("EUR"))
        }
      }
    }

    it("can multiply a coefficient") {
      implicit val noExchanges = Exchanges.noop

      forAll {
        (a: Double, b: Double) => {
          val result = Amount(a, "EUR") * b
          result should have('value(a * b))
          result should be(in("EUR"))
        }
      }


    }

    it("can be multiplied by a coefficient") {
      forAll {
        (a: Double, b: Double) => {
          val result = a * Amount(b, "EUR")
          result should have('value(a * b))
          result should be(in("EUR"))
        }
      }
    }

    it("can be changed in a given counterpart currency") {
      implicit val exchanges = Exchanges(Map(Instrument("EUR", "USD") -> 2))

      forAll {
        a: Double => {
          Amount(a, "EUR") changedIn ("USD") should have('value(a * 2))
          Amount(a, "EUR") changedIn ("USD") should be(in("USD"))
        }
      }
    }

    it("can be added to another amount of different currency given exchanges") {
      implicit val exchanges = Exchanges(Map(Instrument("EUR", "USD") -> 2))

      forAll {
        (a: Double, b: Double) => {
          val left = Amount(a, "EUR")
          val right = Amount(b, "USD")

          val result = left + right
          result should be(in("EUR"))
          result should have('value(a + b / 2))
        }
      }
    }
  }
}

trait AmountImplicits {
  def in(expectation: Currency): BeMatcher[Amount] = new BeMatcher[Amount] {
    def apply(left: Amount): MatchResult = MatchResult(left.currency == expectation, s"Expected currency $expectation for :$left ", s"Expected currency different from $expectation for :$left ")
  }

  implicit def stringToCurrency(currencyCode: String): Currency = Currency.getInstance(currencyCode)

}

object AmountImplicits {

  implicit class ExtendedDouble(val coefficient: Double) extends AnyVal {
    def multiply(right: Amount): Amount = right.multipliedBy(coefficient)

    def *(right: Amount): Amount = right.multipliedBy(coefficient)

  }

}