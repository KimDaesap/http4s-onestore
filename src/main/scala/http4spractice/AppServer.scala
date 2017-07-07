package http4spractice

import java.util.concurrent.Executors

import com.typesafe.config.ConfigFactory
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.middleware.GZip
import org.http4s.server.{Server, ServerApp}
import slick.jdbc.H2Profile.api._

import scala.util.Properties.envOrNone
import scalaz.concurrent.Task
import http4spractice.service.{PurchaseService, UserService}


object AppServer extends ServerApp {

  val config         = ConfigFactory.load()
  val appId          = config.getString("appId")
  val port           = envOrNone("HTTP_PORT") map (_.toInt) getOrElse 9010
  val ip             = "0.0.0.0"
  val database       = Database.forConfig("database")
  val excutorService = Executors.newCachedThreadPool()
  val client         = PooledHttp1Client()

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder
      .bindHttp(port, ip)
      .mountService(GZip(PurchaseService.service), "/api/purchase")
//      .mountService(GZip(UserService.service), "api/user")
      .withServiceExecutor(excutorService)
      .start

  override def shutdown(server: Server): Task[Unit] = {
    database.close
    client.shutdownNow
    super.shutdown(server)
  }

}
