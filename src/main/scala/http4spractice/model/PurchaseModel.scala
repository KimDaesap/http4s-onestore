package http4spractice.model

import http4spractice.AppServer
import http4spractice.protocol._
import slick.dbio.DBIOAction
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scalaz.{-\/, \/-}


case class Purchase(id: Option[Long] = None,
                    tid: String,
                    userId: String,
                    produnctId: String,
                    createdTime: Long,
                    state: String,
                    txid: Option[String] = None,
                    receipt: Option[String] = None,
                    verifiedTime: Option[Long] = None)


class PurchaseTable(tag: Tag) extends Table[Purchase](tag, "purchase") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def tid = column[String]("tid")
  def userId = column[String]("userId")
  def productId = column[String]("produnctId")
  def createdTime = column[Long]("createdTime")
  def state = column[String]("state")
  def txid = column[Option[String]]("txid")
  def receipt = column[Option[String]]("receipt")
  def verifiedTime = column[Option[Long]]("verifiedTime")

  // index
  def userIdIndex = index("userIdIndex", userId)

  def * = (id.?, tid, userId, productId, createdTime,
    state, txid, receipt, verifiedTime) <> (Purchase.tupled, Purchase.unapply)
}


object PurchaseModel extends Model {
  import AppServer.database
  import UserModel.userQuery

  implicit val ec = ExecutionContext.fromExecutorService(AppServer.excutorService)
  val purchaseQuery = TableQuery[PurchaseTable]

  // Schema init
  Await.result(database.run(DBIOAction.seq(purchaseQuery.schema.create)), Duration.Inf)

  def getFromTid(tid: String) = task {
    purchaseQuery.filter(_.tid === tid).result.headOption
  }

  def getAllFromUserId(userId: String) = task {
    purchaseQuery.filter(_.userId === userId).result
  }

  def requestTid(userId: String, productId: String) = task {
    import java.time.Instant

    val tid = java.util.UUID.randomUUID().toString.filter(_.isLetterOrDigit)
    val purchase = Purchase(
      None,
      tid,
      userId,
      productId,
      Instant.now.getEpochSecond,
      "CREATED"
    )

    (purchaseQuery returning purchaseQuery.map(_.id) += purchase)
      .map(id => purchase.copy(id = Some(id)))
  }

  def preCheckReceipt(receipt: ReqVerifyReceipt) = task {

    if (receipt.txid == None) DBIO.failed(throw new Exception("pa"))

    purchaseQuery.filter(_.tid === receipt.tid).result
      .headOption.flatMap {
      case None =>
        DBIO.failed(throw new Exception("not found tid in purchase"))
      case Some(p) =>
        if (receipt.product.exists(_.id == p.produnctId) == false)
          DBIO.failed(throw new Exception("invalid product id"))
        else if (receipt.userid != p.userId)
          DBIO.failed(throw new Exception("invalid user id"))
        else
          purchaseQuery.update(p.copy(
            state = "RECEIPT_RECEIVED",
            txid = Some(receipt.txid),
            receipt = Some(receipt.receipt))
          ) map(_ > 0)
    }
  }

  def verifiedReceipt(txid: String, storeRes: ResStoreVerifyReceipt) = task {
    import java.time.Instant

    if (storeRes.status != 0)
      DBIO.failed(throw new Exception(s"verify failed: code ${storeRes.detail}"))
    else purchaseQuery.filter(_.txid === txid).map(p =>
      (p.state, p.verifiedTime)).update("VERIFIED", Some(Instant.now.getEpochSecond))
  }

}
