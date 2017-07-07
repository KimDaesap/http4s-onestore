package http4spractice.protocol

import scala.xml.{Elem, Node}


case class ReqRequestTid(userid: String, productid: String)

case class ReqVerityTid(tids: Seq[String])

case class ResVerifiedTid(status: Int, detail: String, message: String,
                          appid: String, billinglog: Seq[StoreItem])

case class StoreItem(tid: String, projectid: String, logtime:String,
                     chargingid: String, chargeamount: Int, detailpname: String,
                     bpinfo: String, tcashflag: String)

object ResVerifiedTid {
  def apply(xml: Elem): ResVerifiedTid = {
    val result = xml \ "result"

    def item(node: Node): StoreItem = StoreItem(
      node \ "tid" text,
      node \ "product_id" text,
      node \ "log_time" text,
      node \ "charging_id" text,
      (node \ "charge_amount" text).toInt,
      node \ "detail_pname" text,
      node \ "bp_info" text,
      node \ "tcash_flag" text
    )

    ResVerifiedTid(
      (result \ "status" text).toInt,
      result \ "detail" text,
      result \ "message" text,
      result \ "appid" text,
      result \ "billing_log" map (item)
    )
  }
}


case class ReqVerifyReceipt(userid: String, tid: String, txid: String,
                            receipt: String, product: Seq[StoreProduct])

case class ResVerifyReceipt()

case class StoreProduct(id: String, name: String, `type`: String,
                        kind: String, validity: Int, price: Int,
                        startDat: String, endDate: String, status: StoreStatus)

case class StoreStatus(code: String, message: String)

case class ReqStoreVerifyReceipt(appid: String, txid: String, signdata: String)

case class ResStoreVerifyReceipt(status: Int, detail: String, message: String, product: Seq[StoreProduct])



