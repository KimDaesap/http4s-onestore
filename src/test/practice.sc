import scalaz.-\/
import scalaz.concurrent.Task

//val i: Task[Int] = Task.fail(new Exception("example")) or Task.now(1)
//val j: Task[Int] = Task.now(None) or Task.now(1)
//
//i.unsafePerformSync
//j.unsafePerformSync


(Task { -\/("Error") } or Task.now(1)).unsafePerformSync

(Task { throw new OutOfMemoryError("die") } or Task.now(1)).unsafePerformSync


Task(Option.empty[Int]).map(a => a.orElse(Some(1))).unsafePerformSync

Task(throw new OutOfMemoryError("die")).flatMap { x => Task.now(println("앙?")) }
  .unsafePerformSync