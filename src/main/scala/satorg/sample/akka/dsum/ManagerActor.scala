package satorg.sample.akka.dsum

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object ManagerActor {
  def props: Props = Props[ManagerActor]

  case class StartWork(nums: Seq[Long])

  case class ReturnResult(num: Long)

}

import satorg.sample.akka.dsum.ManagerActor._

class ManagerActor extends Actor with ActorLogging {

  override def receive: Receive = idle()

  private def idle(): Receive = {
    case StartWork(nums) =>
      if (nums.isEmpty) {
        throw new IllegalArgumentException("empty input data set")
      }

      val workers =
        (1 to nums.size).iterator.
          map { idx => context.actorOf(WorkerActor.props, s"worker-$idx") }.
          toVector

      for ((num, worker) <- nums.iterator.zip(workers.iterator)) {
        worker ! WorkerActor.StartWork(num, workers)
      }

      context.become(started(sender()))
  }

  private def started(outputRef: ActorRef): Receive = {
    case WorkerActor.ReceiveNum(num) =>
      outputRef ! ReturnResult(num)
      context.become(idle())
  }
}
