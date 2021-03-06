package spider

import scala.math.ceil
import scala.util.{ Try, Success, Failure }

import java.io.{ StringWriter, PrintWriter }
import java.nio.file.Paths
import java.security.MessageDigest
import java.sql.Date

import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.Logger

object Util {
  def md5Hash(s: String): String = {
    val md5 = MessageDigest.getInstance("md5")
    md5.digest(s.getBytes).map(b ⇒ "%02X".format(b)).mkString
  }

  def splitSeq[A](as: Seq[A], n: Int): List[Seq[A]] = {
    if (as.isEmpty) Nil
    else (as grouped ceil(as.size.toDouble / n).toInt).toList
  }

  def tryOption[A](a: ⇒ A): Option[A] = Try(a).toOption

  def cwd: String = Paths.get(".").toAbsolutePath.toString

  object DateTimeUtil {
    def fromFormatString(date: String, format: String): DateTime = {
      DateTimeFormat.forPattern(format).parseDateTime(date)
    }
    def toFormatString(
      date: DateTime, format: String = "yyyy-MM-dd"): String = {
      DateTimeFormat.forPattern(format).print(date)
    }
    def trivialInterval: Interval = {
      val date = DateTime.now
      date to date
    }
    def toSQLDate(date: DateTime): Date = {
      new Date(date.getMillis)
    }
    def stripHourMinSec(date: DateTime): DateTime = {
      date.hour(0).minute(0).second(0)
    }
  }

  def getStackTraceString(e: Throwable): String = {
    val stringWriter = new StringWriter()
    val printWriter = new PrintWriter(stringWriter)
    e.printStackTrace(printWriter)
    stringWriter.toString
  }

  def errorExit(msg: String, code: Int = 1): Nothing = {
    System.err.println(s"Error: $msg")
    sys.exit(code)
  }
}