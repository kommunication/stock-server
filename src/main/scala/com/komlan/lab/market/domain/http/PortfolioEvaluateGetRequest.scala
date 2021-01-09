package com.komlan.lab.market.domain.http

import com.twitter.finatra.http.annotations.{QueryParam, RouteParam}
import com.twitter.finatra.validation.constraints.PastTime
import org.joda.time.DateTime

import java.time.LocalDate

case class PortfolioEvaluateGetRequest(
                                        @RouteParam("userId")
                                        userId: Int,
                                        @PastTime
                                        @QueryParam("from")
                                        dateFrom: Option[DateTime],
                                        @QueryParam("to")
                                        dateTo: DateTime
                                      )
