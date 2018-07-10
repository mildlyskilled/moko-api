package com.mokocharlie.infrastructure.repository.common

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime, ZoneOffset}

import com.mokocharlie.domain.common.MokoCharlieServiceError.{
  MaximumNumberOfAttemptsReached,
  UnknownError
}
import com.mokocharlie.domain.common.ServiceResponse.RepositoryResponse
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import scalikejdbc.TxBoundary.Either._
import scalikejdbc._

import scala.util.Left

object JdbcParameters {

  def fromConfig(cfg: Config = ConfigFactory.load()): JdbcParameters = {
    val config = cfg.getConfig("mokocharlie")
    JdbcParameters(
      protocol = config.getString("db.protocol"),
      host = config.getString("db.host"),
      port = config.getInt("db.port"),
      dbName = config.getString("db.dbName"),
      user = config.getString("db.user"),
      password = config.getString("db.password"),
      socketTimeout = config.getInt("db.socket-timeout")
    )
  }
}

case class JdbcParameters(
    protocol: String,
    host: String,
    port: Int,
    dbName: String,
    user: String,
    password: String,
    socketTimeout: Int = 0) {

  val url = s"$protocol://$host:$port/$dbName?autoReconnect=true"
}

object JdbcRepository {
  // Doing the direct instant to timestamp conversion does not give us what we need:
  // Time.fromInstant(ins) adds an hour on in DST periods WTF!
  // There is probably a better way than the below
  def toTimestamp(instant: Instant): Timestamp =
    Timestamp.valueOf(LocalDateTime.ofInstant(instant, ZoneOffset.UTC))
}

trait JdbcRepository extends StrictLogging {

  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
    enabled = true,
    singleLineMode = true,
    printUnprocessedStackTrace = false,
    stackTraceDepth = 15,
    logLevel = 'debug,
    warningEnabled = false,
    warningThresholdMillis = 3000L,
    warningLogLevel = 'warn
  )

  val writePool = "write"

  // initialize JDBC driver & connection pool
  Class.forName("com.mysql.jdbc.Driver")

  if (!ConnectionPool.isInitialized(ConnectionPool.DEFAULT_NAME)) {
    ConnectionPool.add(
      name = ConnectionPool.DEFAULT_NAME,
      url = parameters.url,
      user = parameters.user,
      password = parameters.password,
      settings = ConnectionPoolSettings(
        initialSize = config.getInt("mokocharlie.db.connection-pool.initial-size"),
        maxSize = config.getInt("mokocharlie.db.connection-pool.max-size"))
    )
  }

  if (!ConnectionPool.isInitialized(writePool)) {
    ConnectionPool.add(
      name = writePool,
      url = parameters.url,
      user = parameters.user,
      password = parameters.password,
      settings = ConnectionPoolSettings(
        initialSize = config.getInt("mokocharlie.db.write-connection-pool.initial-size"),
        maxSize = config.getInt("mokocharlie.db.write-connection-pool.max-size"))
    )
  }

  def config: Config

  //todo - eliminate this. The default should be good for most uses
  def parameters: JdbcParameters = JdbcParameters.fromConfig(config)

  def readOnlyTransaction[T](f: (DBSession) ⇒ RepositoryResponse[T]): RepositoryResponse[T] =
    using(DB(ConnectionPool.get(ConnectionPool.DEFAULT_NAME).borrow())) {
      _.readOnly { implicit session ⇒
        f(session)
      }
    }

  def writeTransaction[T](f: DBSession ⇒ RepositoryResponse[T]): RepositoryResponse[T] =
    writeTransaction[T](1, "No retries allowed. This should't be ever logged")(f)

  def writeTransaction[T](maxAttempts: Int, logMessage: String)(
      f: (DBSession) ⇒ RepositoryResponse[T]): RepositoryResponse[T] = {
    executeAndRetryOnFail(maxAttempts, logMessage, {
      using(DB(ConnectionPool.get(writePool).borrow())) {
        _.localTx { implicit session ⇒
          f(session)
        }
      }
    })
  }

  private[common] def executeAndRetryOnFail[T](
      numberOfAttempts: Int,
      logMessage: String,
      f: ⇒ RepositoryResponse[T],
      attempt: Int = 0): RepositoryResponse[T] = {

    val result = try {
      f
    } catch {
      case e: Exception ⇒
        logger.error(s"Retry mechanism expected either but exception was thrown", e)
        Left(UnknownError(e))
    }

    // todo - all of this can be tidied up
    if (result.isLeft && attempt < numberOfAttempts - 1) {
      logger.warn(s"$logMessage , attempt ${attempt + 1} of $numberOfAttempts")
      executeAndRetryOnFail(numberOfAttempts, logMessage, f, attempt + 1)
    }
    else if (result.isLeft && attempt == numberOfAttempts - 1) {

      val reason = result.left.toOption
        .map {
          case error @ UnknownError(ex) ⇒
            MaximumNumberOfAttemptsReached(ex.getMessage, Some(error), Some(ex))
          case error ⇒ MaximumNumberOfAttemptsReached(error.msg, Some(error))
        }
        .getOrElse(MaximumNumberOfAttemptsReached("Unknown error"))

      logger.error(
        s"Last attempt of transaction execution has failed: ${attempt + 1} of $numberOfAttempts, reason: ${reason.msg}")
      Left(reason)
    }
    else
      result
  }

  protected def read[T](queryName: String)(query: DBSession ⇒ T): RepositoryResponse[T] =
    readOnlyTransaction { implicit session ⇒
      perform(queryName, query)
    }

  protected def write[T](queryName: String)(update: DBSession ⇒ T): RepositoryResponse[T] =
    writeTransaction { implicit session ⇒
      perform(queryName, update)
    }

  protected def writeInExistingTx[T](queryName: String)(update: DBSession ⇒ T)(
      implicit session: DBSession): RepositoryResponse[T] =
    perform(queryName, update)

  private def perform[T](queryName: String, op: DBSession ⇒ T)(
      implicit session: DBSession): RepositoryResponse[T] =
    try {
      Right(op(session))
    } catch {
      case e: Exception ⇒
        Left(UnknownError(e))
    }
}
