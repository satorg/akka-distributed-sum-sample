package satorg.sample.akka.dsum

import akka.actor.ActorSystem

import scala.io.StdIn

object MainApp {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("distributed-sum")
    try {
      val nums = 100L to 110L

      for ((num, idx) <- nums.zipWithIndex) {
        val id = idx + 1
        system.actorOf(WorkerActor.props(id, nums.size, num), WorkerActor.makeName(id))
      }

      // TODO: wait until the work is done, then terminate
      println("Press <Enter> to exit")
      StdIn.readLine()
    }
    finally {
      system.terminate()
    }
  }
}
