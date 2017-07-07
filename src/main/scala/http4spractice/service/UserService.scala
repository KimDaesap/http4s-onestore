package http4spractice.service

import org.http4s._
import org.http4s.dsl._
import org.http4s.circe._
import org.http4s.HttpService
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext
import http4spractice.AppServer
import http4spractice.model.{User, UserModel}
import http4spractice.protocol.ReqLogin


object UserService {
  implicit val ec = ExecutionContext.fromExecutorService(AppServer.excutorService)

//  val service = HttpService {
//    case req @ POST -> Root / "api" / "login" =>
//      for {
//        param <- req.as(jsonOf[ReqLogin])
//        user <- UserModel.read(param.uid).map(_.getOrElse(UserModel.create(param.uid)))
//        resp <- Ok(user.asJson)
//      } yield resp
//
//  }
}
