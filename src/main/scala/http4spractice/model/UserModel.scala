package http4spractice.model

import http4spractice.AppServer

import slick.jdbc.H2Profile.api._
import slick.dbio.DBIOAction

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

import scalaz.concurrent.Task


case class User(id: Option[Long], uid: Option[String],
                name: Option[String], cash: Long)


class UserTable(tag: Tag) extends Table[User](tag, "user") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def googleId = column[Option[String]]("googleId")
  def name = column[Option[String]]("name")
  def cash = column[Long]("gold")

  // index
  def googleIdIndex = index("googleIdIndex", googleId)

  def * = (id.?, googleId, name, cash) <> (User.tupled, User.unapply)
}


object UserModel extends Model {
  import AppServer.database
  implicit val ec = ExecutionContext.fromExecutorService(AppServer.excutorService)

  val userQuery = TableQuery[UserTable]

  // Schema init
  Await.result(database.run(DBIOAction.seq(userQuery.schema.create)), Duration.Inf)


  def create(googleId: String): Task[User] = task {
    val user = User(None, Some(googleId), None, 0L)
    (userQuery returning userQuery.map(_.id) += user)
      .map(id => user.copy(id = Some(id)))
  }

  def update(user: User): Task[Int] = task {
    val query = for (u <- userQuery if u.id === user.id) yield u
    query.update(user)
  }

  def read(googleId: String) = task {
    userQuery.filter(_.googleId === googleId).result.headOption
  }

  def delete(googleId: String) = task {
    userQuery.filter(_.googleId === googleId).delete
  }
}