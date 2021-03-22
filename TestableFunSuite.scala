package tests

import org.scalatest.FunSuite
import org.scalatest.exceptions.TestFailedException
import scalaj.http.{Http, HttpOptions}
import play.api.libs.json._

object TestableFunSuite {
  var breakPointMarkCount: Int = 0
  var testUniqueId: String = randomString(10)

  def getCurBreakPointMarkCount (): Int = {
    breakPointMarkCount += 1
    breakPointMarkCount
  }
  def randomString(length: Int) = scala.util.Random.alphanumeric.take(length).mkString
}

class TestableFunSuite(
  feedbackUrl: String
) extends FunSuite{
  /**
   * testableAssert
   *
   * @param condition the test result from testcase
   * @param clue clue
   * @param breakPointMark a string to indicate what assertion this is.
   * */
  def testableAssert (condition: Boolean, clue: Any = "", breakPointMark: String = ""): Unit ={
    var exception: RuntimeException = new RuntimeException()

    var stackTrace: JsArray = JsArray()
    for (trace <- exception.getStackTrace) {
      stackTrace = stackTrace :+ JsString(trace.toString)
    }

    val data : String = Json.stringify(JsObject(Seq(
      "test_type" -> JsNumber(0),
      "assert_result" -> JsBoolean(condition),
      "clue" -> JsString(clue.toString),
      "break_point_mark" -> JsString(breakPointMark),
      "assertion_count" -> JsNumber(TestableFunSuite.getCurBreakPointMarkCount()),
      "stack_trace" -> stackTrace,
      "unique_id" -> JsString(TestableFunSuite.testUniqueId)
    )))
    Http(feedbackUrl)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .postData(data)
      .asString

    assert(condition, clue)

    /*try {
      assert(condition, clue)
    } catch {
      // `case` keyword is banned in this project
      case cause: TestFailedException => {
        println(cause)
        println(cause.getMessage)
        println(cause.failedCodeFileNameAndLineNumberString)

        val data : String = Json.stringify(JsObject(Seq(
          "assertResult" -> JsBoolean(condition),
          "clue" -> JsString(clue.toString),
          "breakPointMark" -> JsString(breakPointMark),
          "TestFailedException.message" -> JsString(cause.getMessage),
          "TestFailedException.failedCodeFileNameAndLineNumberString" -> JsString(cause.failedCodeFileNameAndLineNumberString.toString)
        )))
        Http(feedbackUrl)
          .postData(data)
          .asString

        throw cause
      }
    }*/
  }
}
