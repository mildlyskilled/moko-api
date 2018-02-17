package com.mokocharlie.domain.common

import java.time._

class SettableClock(var dateTime: LocalDateTime) extends Clock {

  /**
    * Adjust the time of the clock, keeping the date unchanged.
    *
    * @param time time to set the clock to
    */
  def adjustTime(time: LocalTime): Unit = dateTime = LocalDateTime.of(dateTime.toLocalDate, time)

  /**
    * Adjust the date of the clock, keeping the time unchanged.
    *
    * @param date date to set the clock to
    */
  def adjustDate(date: LocalDate): Unit = {
    dateTime = LocalDateTime.of(date, dateTime.toLocalTime)
  }

  def addSeconds(secs: Int): Unit = { dateTime = dateTime.plusSeconds(secs) }

  def addMinutes(minutes: Int): Unit = { dateTime = dateTime.plusMinutes(minutes) }

  def addYears(years: Int): Unit = { dateTime = dateTime.plusYears(years) }

  def addHours(hours: Int): Unit = { dateTime = dateTime.plusHours(hours) }

  override def getZone: ZoneId = ZoneOffset.UTC

  override def instant: Instant = dateTime.toInstant(ZoneOffset.UTC)

  override def withZone(zone: ZoneId): Clock = ???
}
