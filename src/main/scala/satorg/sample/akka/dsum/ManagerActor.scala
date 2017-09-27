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
      for ((num, idx) <- nums.zipWithIndex) {
        val id = idx + 1
        context.actorOf(WorkerActor.props(id, nums.size, num), WorkerActor.makeName(id))
      }
      context.become(started(sender()))
  }

  private def started(outputRef: ActorRef): Receive = {
    case WorkerActor.ReceiveNum(1, num) =>
      outputRef ! ReturnResult(num)
      context.become(idle())
  }
}
