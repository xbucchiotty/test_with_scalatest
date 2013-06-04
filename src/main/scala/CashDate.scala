import java.util.Date
import org.joda.time.{Period, DateMidnight}

case class CashDate(date: DateMidnight) {

  import CashDate.ExtendedInt

  def next: CashDate = CashDate(date plus (1 month))

  override def toString = date.toString("dd/MM/yyyy")
}

object CashDate {

  def apply(year: Int, month: Int, day: Int): CashDate = CashDate(new DateMidnight(year, month, day))

  def apply(date: Date): CashDate = CashDate(new DateMidnight(date))

  private[CashDate] implicit class ExtendedInt(val period: Int) extends AnyVal {
    def month: Period = Period.months(period)
  }

}