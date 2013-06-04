
case class Schedule(initialAmount: Amount, numberOfPeriod: Int, startDate: CashDate) extends Iterable[CashFlow] {

  assert(numberOfPeriod > 0)

  private val flows: List[CashFlow] = {
    def loop(previous: CashFlow, remainingPeriod: Int): List[CashFlow] = {
      if (remainingPeriod < 0) {
        Nil
      } else {
        previous :: loop(previous.next, remainingPeriod - 1)
      }
    }

    val firstCashFlow = CashFlow.first(initialAmount, numberOfPeriod, startDate)

    loop(firstCashFlow, numberOfPeriod - 1)
  }

  def iterator: Iterator[CashFlow] = flows.iterator
}
