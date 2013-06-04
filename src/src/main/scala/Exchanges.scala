import java.util.Currency

case class Instrument(basis: Currency, counterpart: Currency)

trait Exchanges extends ((Amount, Currency) => Amount)

object Exchanges {
  def apply(rates: Map[Instrument, Double]): Exchanges = new Exchanges {
    def apply(amount: Amount, currency: Currency): Amount = ???
  }
}
