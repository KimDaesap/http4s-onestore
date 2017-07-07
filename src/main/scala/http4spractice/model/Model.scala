package http4spractice.model

import slick.dbio._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}


trait Model {

  import http4spractice.AppServer.{database, excutorService}

  protected def task[R](action: DBIO[R]): Task[R] = {
    implicit val executionContext = ExecutionContext.fromExecutorService(excutorService)
    Task.async { f =>
      database.run(action) onComplete {
        case Success(a) => f(\/-(a))
        case Failure(e) => f(-\/(e))
      }
    }
  }

}
