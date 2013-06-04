# DSL with Scala

## Objective
What is the value of a program relative to? It's Domain! So, let's make it be valuable.

How can we use **Scala syntax** to create a **DSL** (*Domain Specific Language*)?
We'll also see that Scala is well suited for **BDD** (*Behavior Driven Development*) and **DDD** (*Domain Driven Design*).

During this hand's on session, you can use **SBT** and its wonderful command: **~test**


##Step 1: one baby step
Create a **case class Amount** with *value* as *double* and a *currency*.

Before adding method, ie behavior, let's create a test clas AmountTest using the framework **ScalaTest** and **FEST-Assert**.

	private val EUR = Currency.getInstance("EUR")

	describe("An amount"){
		it("can be added to another amount of the same currency"){
			val _15_EUR = Amount(15.,EUR)
			val _20_EUR = Amouunt(20.,EUR)
			
			val result = left.add(right)
			
			assertThat(_15_EUR.add(_20_EUR)).isEqualTo(Amount(35.,EUR))
		}	
	}
	
Implements the amount method that make this test all green.

	case class Amount(value: Double, currency: Currency) {
		def add(right: Amount): Amount = ???
	}

##Step 2: should and have matchers in Scalatest
The first test make the works be it can be detailed. Here is what are the business expectation after the operation (Then clause):

* result should have value 35
* result should be in EUR

Let's explore the ScalaTest API about the keywords **should** and **have** for refactoring the first assertion (about the value).

The test can now be written

	private val EUR = Currency.getInstance("EUR")
	private val _15_EUR = Amount(15d,EUR)
	private val _20_EUR = Amount(20d,EUR)

	describe("An amount"){
    	it("can be added to another amount of the same currency"){
    	    val result = _15_EUR.add(_20_EUR)
	
			result should have('value(35))
			assertThat(result).isEqualTo(Amount(35d,EUR))
	    }   
	}

But there is not keyword in Scala test for Amount and Currency. Let's create it!

###Step 3 : creating our own Scalatest BeMatcher
In AmountTest file, create a trait named **AmountImplicits**. Now **AmountTest** class should extends **AmountImplicits**

	implicit def in(expectation: Currency): BeMatcher[Amount] = new BeMatcher[Amount] {
    	def apply(left: Amount): MatchResult = MatchResult(

      	left.currency == expectation,
		      s"given amount $left should be in $expectation",
		      s"given amount $left shouldn't be in $expectation")
  	}
  	
Now the test can be written:

	private val EUR = Currency.getInstance("EUR")
	private val _15_EUR = Amount(15d,EUR)
	private val _20_EUR = Amount(20d,EUR)	

	describe("An amount"){
    	it("can be added to another amount of the same currency"){
    	    val result = _15_EUR.add(_20_EUR)
	
			result should have('value(	35d))
			result should be(in("EUR"))
	    }   
	}
	
###Step 4 : implicit conversion
How can we get rid of the following line which is not important for the test?
	
	val EUR = Currency.getInstance("EUR")
	
	
The answer is: **implicit conversion**.

In *AmountImplicits*, let's create a function that can tranform a string into a currency:

	def stringToCurrency(currencyCode: String): Currency = ???
	
Make a replace of all EUR into "EUR". Then delete the val *EUR*. It doesn't compile because Scala compiler doesn't know that it case use our brand new function. To do that, just place the keyword **implicit** before *def*.  And it's compiling!

The test code is now like this:

	private val _15_EUR = Amount(15d,EUR)
	private val _20_EUR = Amount(20d,EUR)	

	describe("An amount") {
		it("can be added to another amount of the same currency") {
			val result = _15_EUR.add(_20_EUR)

			result should have('value(35d))
			result should be(in("EUR"))
		}	
	}

###Step 5 : adding behavior to existing class
Let's create a new behavior to our Amount, the multiplication by a coefficient. The test is like this:
	
	it("can be multiplied by a coefficient") {
		val result = _15_EUR.multipliedBy(2d)

		result should have('value(30d))
		result should be(in("EUR"))
    }

The Amount has now the following design:
	
	case class Amount(value: Double, currency: Currency) {

		def add(right: Amount): Amount = ???

		def multipliedBy(coefficient: Double): Amount = ???
	}
	
But what happens if I want to write this ?

    it("can be multiplied by a coefficient 2") {
      val _15_EUR = Amount(15d, "EUR")

      val result = 2d.multiply(_15_EUR)

      result should have('value(30d))
      result should be(in("EUR"))
    }
	
It doesn't compile because this method doesn't exist. Let's create it!

In *LeasingImplicits*, let's create an other implicit function that transform a Double into something with a method named multiply(right:Amount)

It should look like something like this:
	
	implicit def extendedDouble(coefficient: Double) = new {
    	def multiply(right: Amount): Amount = right.multipliedBy(coefficient)
	}
	
And it now compile. The content assist of the IDE propose you the method! Great! To improve the compiled code, we can ask Scala compiler to inline the code. For that,we have to create an implicit class from our previous code:

	object AmountImplicits {

		implicit class ExtendedDouble(val coefficient: Double) extends AnyVal {
			def multiply(right: Amount): Amount = right.multipliedBy(coefficient)
	  }
	}
	
Note that the class is in an object Named AmountImplicits and not in the trait. The important difference is that the class extends AnyVal! That's how Scala compiler will then inline the code.
Other difference, you need to explicitly import the implicit class at the beginning of the test.

	import AmountImplicits._



###Step 6 : operators
Let's now add methods for replacement of add and multiplied with operators. Why wouldn't be abe to write left + write with amounts?

	/* for Amount*/
	def + = add(_)
	//or def +(right:Amount) = add(right)
	
	def * = multipliedBy(_)
	//or def *(right:Amount) = multipliedBy(right)
	
	/*and for the ExtendedDouble*/
	def * = multiply(_)
	
We can now first write the following tests

    it("can be added to another amount of the same currency") {
      val result = _15_EUR.+(_20_EUR)

      result should have('value(35d))
      result should be(in("EUR"))
    }

    it("can be multiplied by a coefficient") {
      val result = _15_EUR.*(2d)

      result should have('value(30d))
      result should be(in("EUR"))
    }

    it("can be multiplied by a coefficient 2") {
      val result = 2d.*(_15_EUR)

      result should have('value(30d))
      result should be(in("EUR"))
    }
    
But you can also infix the expression and removing '.' '(' and ')' from the lines (IntelliJ makes it very easy with ALT+ENTER). Now we have:

	it("can be added to another amount of the same currency") {
      val result = _15_EUR + _20_EUR

      result should have('value(35d))
      result should be(in("EUR"))
    }

    it("can be multiplied by a coefficient") {
      val result = _15_EUR * 2d

      result should have('value(30d))
      result should be(in("EUR"))
    }

    it("can be multiplied by a coefficient 2") {
      val result = 2d * _15_EUR

      result should have('value(30d))
      result should be(in("EUR"))
    }


###Step 7 : currency exchanges
Amount is a Value Object in the DDD world. It represents some measure of the world, is immutable. But what happends if I want to implements currency change? 
In a naked POJO world with Spring Services, I should have created an ExchangesServices that take the amount and process the change. But let's try to put it in the ValueObject.

First is first, here is the test

    it("can be changed in a given counterpart currency") {
      val exchanges = Exchanges(Map(Instrument("USD", "EUR") -> .5))

      _15_EUR.changedIn("USD", exchanges) should have('value(7.5))
      _15_EUR.changedIn("USD", exchanges) should be(in("USD"))
    }

And the implementation:

	case class Amount(value: Double, currency: Currency) {
		def add(right: Amount): Amount = ???
		def + = add(_)
		def multipliedBy(coefficient: Double): Amount = ???
		def * = multipliedBy(_)
		def changedIn(currency: Currency, exchanges: Exchanges): Amount = ???
	}

	case class Instrument(basis: Currency, counterpart: Currency)

	trait Exchanges extends ((Amount, Currency) => Amount)

	object Exchanges {
		def apply(rates: Map[Instrument, Double]): Exchanges = new Exchanges {
			def apply(amount: Amount, currency: Currency): Amount = ???
		}
	}

As a Value Object, the Amount asks for the dependency Exchanges to make the convertion operation. Now, how to improve the signature of changedIn method? By currying and implicits once again!

	def changedIn(currency: Currency)(implicit exchanges: Exchanges): Amount = ???
	
	//and for the test
	
    it("can be changed in a given counterpart currency") {
      implicit val exchanges = Exchanges(Map(Instrument("USD", "EUR") -> .5))

      _15_EUR changedIn("USD") should have('value(7.5))
      _15_EUR changedIn("USD") should be(in("USD"))
    }

    
We've made it, and with Inversion Of Control (IOC) pattern. It's now the caller which is responsible for giving an instance of Exchanges. 


###Step 8 : sum and exchanges
How first baby step was to create an addition without taking care of the currency. Now that we have the exchanges feature, we can add the feature of summing amount of different currencies.

Let's make the following test pass!

