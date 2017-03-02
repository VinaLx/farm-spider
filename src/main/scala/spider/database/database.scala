package spider
package database

import slick.jdbc.JdbcBackend._
import slick.jdbc.MySQLProfile.api.{ Database ⇒ _, _ }
import slick.dbio.DBIO
import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf

object FarmDB {
  def prepareDbAndTable(config: DBConfig): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    if (config.db.isEmpty) return
    val db = getConnection(config.copy(db = None))
    val actions = DBIO.seq(
      createDatabaseSQL(config.db.get), createProductTable)
    Await.result(db.run(actions) recover { case e ⇒ () }, Inf)
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
}