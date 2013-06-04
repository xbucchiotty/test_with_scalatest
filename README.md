# DSL with Scala

## Objective
What is the value of a program relative to? It's Domain! So, let's make it be valuable.

How can we use **Scala syntax** to create a **DSL** (*Domain Specific Language*)?
We'll also see that Scala is well suited for **BDD** (*Behavior Driven Development*) and **DDD** (*Domain Driven Design*).

During this hand's on session, you can use **SBT** and its wonderful command: **~test**


##Step 1: one baby step
Create a **case class Amount** with *value* as *double* and a *currency*.

Before adding method, ie behavior, let's create a test class AmountTest using the framework **ScalaTest** and **FEST-Assert**.

	private val EUR = Currency.getInstance("EUR")

	describe("An amount"){
		it("can be added to another amount of the same currency"){
			val _15_EUR = Amount(15d,EUR)
			val _20_EUR = Amount(20d,EUR)
			
			Assertions.assertThat(_15_EUR.add(_20_EUR)).isEqualTo(Amount(35d,EUR))
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
	
			//TODO result should have a of 35d
			Assertions.assertThat(result).isEqualTo(Amount(35d,EUR))
	    }   
	}

But there is not keyword in Scala test for Amount and Currency. Let's create it!

###Step 3 : creating our own Scalatest BeMatcher
In AmountTest file, create a trait named **AmountImplicits**. Now **AmountTest** class should extends **AmountImplicits**

	def in(expectation: Currency): BeMatcher[Amount] = new BeMatcher[Amount] {
    	def apply(left: Amount): MatchResult = ???
    }
  	
Now the test can be written:

	private val EUR = Currency.getInstance("EUR")
	private val _15_EUR = Amount(15d,EUR)
	private val _20_EUR = Amount(20d,EUR)	

	describe("An amount"){
    	it("can be added to another amount of the same currency"){
    	    val result = _15_EUR.add(_20_EUR)
	
			result should have('value(	35d))
			result should be(in(EUR))
	    }   
	}
	
###Step 4 : implicit conversion
How can we get rid of the following line which is not important for the test?
	
	val EUR = Currency.getInstance("EUR")
	
	
The answer is: **implicit conversion**.

In *AmountImplicits*, let's create a function that can tranform a string into a currency:

	def stringToCurrency(currencyCode: String): Currency = ???
	
Make a replace of all EUR into "EUR". Then delete the val *EUR*.

The test code is now like this:

	private val _15_EUR = Amount(15d,"EUR")
	private val _20_EUR = Amount(20d,"EUR")

	describe("An amount") {
		it("can be added to another amount of the same currency") {
			val result = _15_EUR.add(_20_EUR)

			result should have('value(35d))
			result should be(in("EUR"))
		}	
	}

It doesn't compile because Scala compiler doesn't know that it case use our brand new function. To do that, just place the keyword **implicit** before *def*.  And it's compiling!

###Step 5 : adding behavior to existing class
Let's create a new behavior to our Amount, the multiplication by a coefficient. The test is like this:
	
	it("can multiply a coefficient") {
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

    it("can be multiplied by a coefficient") {
      val result = 2d.multiply(_15_EUR)

      result should have('value(30d))
      result should be(in("EUR"))
    }
	
It doesn't compile because this method doesn't exist. Let's create it!

In *AmountImplicits*, let's create an other implicit function that transform a Double into something with a method named multiply(right:Amount)

It should look like something like this:
	
	implicit def extendedDouble(coefficient: Double) = new {
    	def multiply(right: Amount): Amount = ???
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
    def *(right: Amount): Amount = right.multipliedBy(coefficient)

	
We can now first write the following tests

    it("can be added to another amount of the same currency") {
      val result = _15_EUR.+(_20_EUR)

      result should have('value(35d))
      result should be(in("EUR"))
    }

    it("can multiply a coefficient") {
      val result = _15_EUR.*(2d)

      result should have('value(30d))
      result should be(in("EUR"))
    }

    it("can be multiplied by a coefficient") {
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
        implicit val exchanges = Exchanges(Map(Instrument("EUR", "USD") -> 2))

       _15_EUR changedIn ("USD") should have('value(30))
       _15_EUR changedIn ("USD") should be(in("USD"))
     }

And the implementation:

	case class Amount(value: Double, currency: Currency) {
		def add(right: Amount): Amount = ???
		def + = add(_)
		def multipliedBy(coefficient: Double): Amount = ???
		def * = multipliedBy(_)
		def changedIn(currency: Currency, exchanges: Exchanges): Amount = ???
	}


As a Value Object, the Amount asks for the dependency Exchanges to make the convertion operation. Now, how to improve the signature of changedIn method? By currying and implicits once again!

	def changedIn(currency: Currency)(implicit exchanges: Exchanges): Amount = ???
	
	//and for the test
	
    it("can be changed in a given counterpart currency") {
          implicit val exchanges = Exchanges(Map(Instrument("EUR", "USD") -> 2))

          _15_EUR changedIn ("USD") should have('value(30))
          _15_EUR changedIn ("USD") should be(in("USD"))
        }

    
We've made it, and with Inversion Of Control (IOC) pattern. It's now the caller which is responsible for giving an instance of Exchanges. 


###Step 8 : sum and exchanges
How first baby step was to create an addition without taking care of the currency. Now that we have the exchanges feature, we can add the feature of summing amount of different currencies.

Let's make the following test pass!

    it("can be added to another amount of different currency given exchanges") {
        implicit val exchanges = Exchanges(Map(Instrument("EUR", "USD") -> 2))
        val result = _15_EUR + _20_USD

        result should be(in("EUR"))
        result should have('value(25d))
    }

###Step 9 : generator driven checks
Those tests are working but only test operation with two numbers, 15 and 20. ThScalatest provides a way to have a more dynamic way.
Following this url http://www.scalatest.org/user_guide/generator_driven_property_checks and change the test to get rid of the values! (forAll clause)


###Step 10 : table driven checks
If you have a function that returns a list, it's great to have a quick and efficient way to tests values. Scalatest tries to answer this question with Table Driven checks. It can also be used to check multiple combinaison in one test.
http://www.scalatest.org/user_guide/table_driven_property_checks

Assuming that it's only in monthly period, here is a new business definition:

* A **cash flow** is composed of an amount and a cash date
* A **schedule** is an ordered list of cash flows.
* A **linear progression** split an initial amount on equally flows over a given period of time


Here is the Excel file from the business analyst. Let's implement it with ScalaTest.

	For initialAmount= 10000.0 EUR
        numberOfPeriod = 5
        startDate = 01/01/2013

	"cashDate" , "flow" , "balance"
	01/01/2013 , 2000.0 , 8000.0 
	01/02/2013 , 2000.0 , 6000.0
	01/03/2013 , 2000.0 , 4000.0
	01/04/2013 , 2000.0 , 2000.0
	01/05/2013 , 2000.0 , 0.0
	
