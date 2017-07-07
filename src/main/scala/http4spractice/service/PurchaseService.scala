package http4spractice.service

import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar

import scala.concurrent.ExecutionContext
import scala.xml.Elem

import scalaz.concurrent.Task

import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.scalaxml._

import http4spractice.AppServer
import http4spractice.model._
import http4spractice.protocol._

import scala.util.{Success, Failure, Try}


object PurchaseService {
  import AppServer.client
  implicit val ec = ExecutionContext.fromExecutorService(AppServer.excutorService)


  val service = HttpService {
    // tid 발급 요청
    case req @ POST -> Root / "requesttid" =>
      for {
        param <- req.as(jsonOf[ReqRequestTid])
        purchase <- PurchaseModel.requestTid(param.userid, param.productid)
        resp <- Ok(purchase.asJson)
      } yield resp

    // tid 검증 (테스트)
    case req @ POST -> Root / "verifytid" =>
      for {
        param <- req.as(jsonOf[ReqVerityTid])
        uri = Uri.uri("http://iapdev.tstore.co.kr:8082/billIntf/billinglog/billloginquiry.action")
        date = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance.getTime)
        query = s"?DATE=${date}" +
              s"&APPID=${AppServer.appId}" +
              s"&TIDCNT=${param.tids.length}" +
              s"&TID=${URLEncoder.encode(param.tids.mkString("|"), "UTF-8")}"
        xml <- client.expect[Elem](uri + query)
        resp <- Ok(ResVerifiedTid(xml).asJson)
      } yield resp

    // 영수증 검증
    case req @ POST -> Root / "verifyreceipt" =>
      {
        for {
          param <- req.as(jsonOf[ReqVerifyReceipt])
          _ <- PurchaseModel.preCheckReceipt(param)
          uri = Uri.uri("https://iapdev.tstore.co.kr/digitalsignconfirm.iap")
          storeReq = Request(POST, uri).withBody(
            ReqStoreVerifyReceipt(AppServer.appId, param.txid, param.receipt))(jsonEncoderOf[ReqStoreVerifyReceipt])
          storeRes <- client.expect[ResStoreVerifyReceipt](storeReq)(jsonOf[ResStoreVerifyReceipt])
          isSuccess <- PurchaseModel.verifiedReceipt(param.txid, storeRes)
          resp <- Ok("Success")
        } yield resp
      }.handleWith {
        case e: Throwable => BadRequest(e.getMessage)
      }
    }

}


