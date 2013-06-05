case class CashFlow(date: CashDate, flow: Amount, balance: Amount) {
  def next: CashFlow = CashFlow(
    date = date.next,
    flow = flow,
    balance = Amount(balance.value - flow.value, balance.currency)
  )

  override def toString = s"$date\t, $flow\t, $balance"
}

object CashFlow {
  def first(initialAmount: Amount, numberOfPeriod: Int, startDate: CashDate): CashFlow = {
    assert(numberOfPeriod > 0)

    val flow = initialAmount.value / numberOfPeriod

    CashFlow(
      date = startDate,
      flow = Amount(flow, initialAmount.currency),
      balance = Amount(initialAmount.value - flow, initialAmount.currency)
    )
  }
}

