package com.komlan.lab.market.utils

import org.scalatest.FunSuite

import java.util.Date

class DateUtilsTest extends FunSuite {
  test("Date conversion from string using default formatter"){
    import com.komlan.lab.market.utils.Implicits._
    val result = DateUtils.getDateFromString("01/05/2018")
    println(result)
    assert(result.getYear + 1900 == 2018)
  }
}
