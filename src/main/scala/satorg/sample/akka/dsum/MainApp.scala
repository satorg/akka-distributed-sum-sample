package satorg.sample.akka.dsum

import akka.actor.ActorSystem
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

object MainApp {
  def main(args: Array[String]): Unit = {
    val maxWorkDuration = 1.minute
    implicit val workTimeout: Timeout = maxWorkDuration

    val system = ActorSystem("distributed-sum")
    try {
      val managerActorRef = system.actorOf(ManagerActor.props, "manager")

      val nums = 100L to 110L
      val workFuture = (managerActorRef ? ManagerActor.StartWork(nums)).mapTo[ManagerActor.ReturnResult]

      val result = Await.result(workFuture, maxWorkDuration)
      println(s"Result: ${result.num}")
    }
    finally {
      system.terminate()
    }
  }
}
