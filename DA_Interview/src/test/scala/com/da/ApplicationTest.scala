package com.da

import com.da.Application.WeightedSplit.Beneficiary
import utest.{Tests, test}
import utest._

object ApplicationTest extends TestSuite {
  val tests = Tests {
    test("search", {
      val input = List(4.0, 8.0, 10.0, 12.0)
      val window = 2
      val output = List(6, 9, 11)
      pprint.pprintln(
      Application.MovingAverage.average(window, input)
      )
      Application.MovingAverage.average(window, input) ==> output
    })
    test({
            val candyUnits = 100
            val bill: (Beneficiary, Int) = ("bill",  10)
            val simon: (Beneficiary, Int) = ("simon",  10)
            Application.WeightedSplit.split(candyUnits, List(bill, simon)) ==> List( ("bill", 50), ("simon", 50))
      //      "some result"
    })

//    test("weightUnits", {
//    })
  }
}
