package com.komlan.lab.market.domain.http

import com.twitter.finatra.http.annotations.{QueryParam, RouteParam}
import com.twitter.finatra.validation.{MethodValidation, ValidationResult}
import com.twitter.finatra.validation.constraints.PastTime
import org.joda.time.{DateTime, Days}

case class StockQuoteGetRequest(
    @RouteParam("symbol")
    symbol: String,
    @PastTime
    @QueryParam("from")
    dateFrom: Option[DateTime],
    @QueryParam("to")
    dateTo: Option[DateTime]
) {
  @MethodValidation(fields = Array("from", "to"))
  def ensureMinimumDelta: ValidationResult = {
    ValidationResult.validate((dateFrom.isEmpty || dateTo.isEmpty) || (Days.daysBetween(dateFrom.get, dateTo.get).getDays() >= 1), "dates must be at least a day apart")
  }
}
