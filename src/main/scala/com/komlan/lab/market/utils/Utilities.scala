package com.komlan.lab.market.utils

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

object DateUtils {
  val formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)
  def getDateFromString(str: String):Date = formatter.parse(str)
}