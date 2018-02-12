import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import org.scalatest._

@DoNotDiscover
class PhotoEndpointTest
    extends AsyncFlatSpec
    with HttpScaffold
    with Matchers
    with BeforeAndAfterAll {

  implicit override val ec = system.dispatcher

  override def beforeAll(): Unit = start()

  "The photo end point" should "return a list of images" in {
    sendRequest("http://localhost/photos").map { res ⇒
      res.status shouldBe StatusCodes.OK
    }
  }

  override def afterAll(): Unit = stop()

}
