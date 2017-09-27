package satorg.sample.akka.dsum

import akka.actor.ActorSystem
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object MainApp {
  def main(args: Array[String]): Unit = {
    val nums = readNums()

    val maxWorkDuration = 1.minute
    implicit val workTimeout: Timeout = maxWorkDuration

    val system = ActorSystem("distributed-sum")
    try {
      val managerActorRef = system.actorOf(ManagerActor.props, "manager")

      val workFuture = (managerActorRef ? ManagerActor.StartWork(nums)).mapTo[ManagerActor.ReturnResult]
      val result = Await.result(workFuture, maxWorkDuration)
      println(s"Result: ${result.num}")
    }
    finally {
      system.terminate()
    }
  }

  private def readNums(): Seq[Long] = {
    Iterator.continually(StdIn.readLine()).takeWhile(_ != null).map(_.toLong).toVector
  }
}
