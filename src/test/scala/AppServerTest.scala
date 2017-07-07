import org.scalatest.{Matchers, FunSuite}

class AppServerTest extends FunSuite with Matchers {

  test("Only true") {
    true should be(true)
  }

}