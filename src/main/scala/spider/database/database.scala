package spider
package database

import slick.jdbc.JdbcBackend._
import slick.jdbc.MySQLProfile.api.{ Database ⇒ _, DBIOAction ⇒ _, _ }
import slick.dbio.DBIO
import slick.dbio._

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.Duration.Inf

import com.typesafe.scalalogging.Logger

object FarmDB {

  lazy val logger = Logger("spider.database")

  def prepareDbAndTable(config: DBConfig): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    if (config.db.isEmpty) return
    val db = getConnection(config.copy(db = None))
    val actions = Seq(
      createDatabaseSQL(config.db.get),
      createProductTable,
      createCategoryTable)
    actions map { action ⇒
      Await.result(db.run(action) recover {
        case e ⇒ logger.info(e.getMessage)
      }, Inf)
    }
  }

  def getConnection(config: DBConfig): Database = {
    prepareDbAndTable(config)
    Database.forURL(mysqlURL(config), driver = "com.mysql.jdbc.Driver")
  }

  private def createDatabaseSQL(db: String) = {
    // NO INJECTION PREVENTION
    sqlu"CREATE DATABASE IF NOT EXISTS #$db"
  }

  private def mysqlURL(config: DBConfig): String = {
    val builder = new StringBuilder(s"jdbc:mysql://${config.host}:${config.port}")
    for (db ← config.db) builder ++= s"/$db"
    builder ++= s"?user=${config.username}"
    for (pass ← config.password) builder ++= s"&password=$pass"
    for ((key, value) ← config.properties) builder ++= s"&$key=$value"
    builder.toString
  }

  def createProductTable = FarmTable.schema.create
  def createCategoryTable = CategoryTable.schema.create

  implicit class SyncDB(db: Database) {
    def runSync[R](a: DBIOAction[R, NoStream, Nothing])(
      implicit ec: ExecutionContext): R = {
      Await.result(db.run(a), Inf)
    }
  }
}