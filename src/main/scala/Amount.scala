import java.util.Currency

case class Amount(value: Double, currency: Currency) {
  def add(right: Amount)(implicit exchanges: Exchanges): Amount = if (right.currency == this.currency) {
    copy(value = value + right.value)
  } else {
    this.add(right.changedIn(this.currency))
  }

  def multipliedBy(coefficient: Double): Amount = copy(value = value * coefficient)

  def +(right: Amount)(implicit exchanges: Exchanges) = add(right)

  def * = multipliedBy(_)

  def changedIn(counterpart: Currency)(implicit exchanges: Exchanges): Amount = exchanges(this, counterpart)

}