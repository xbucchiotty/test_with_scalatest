import java.util.Currency

case class Instrument(basis: Currency, counterpart: Currency) {
  def inverse = this.copy(basis = counterpart, counterpart = basis)
}

trait Exchanges extends ((Amount, Currency) => Amount)

object Exchanges {
  def apply(rates: Map[Instrument, Double]): Exchanges = new Exchanges {
    def apply(amount: Amount, counterpart: Currency): Amount =
      Amount(amount.value * findRate(Instrument(amount.currency, counterpart)), counterpart)

    private def findRate(instrument: Instrument): Double = {
      rates
        .get(instrument)
        .orElse(rates.get(instrument.inverse).map(1 / _))
        .getOrElse {
        throw new IllegalArgumentException(s"Unknonw instrument $instrument")
      }
    }
  }

  val noop = new Exchanges {
    def apply(amount: Amount, counterpart: Currency): Amount = amount.copy(currency = counterpart)
  }

}
