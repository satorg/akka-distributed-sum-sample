package satorg.sample.akka.dsum

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object WorkerActor {
  def props: Props = Props[WorkerActor]

  case class StartWork(selfNum: Long, workers: Vector[ActorRef])

  case class ReceiveNum(num: Long)

}

import satorg.sample.akka.dsum.WorkerActor._

class WorkerActor private() extends Actor with ActorLogging {

  private var resNum: Long = 0

  override def receive: Receive = ready()

  private def ready(): Receive = {
    case StartWork(selfNum, workers) =>
      val selfIndex = workers.indexOf(self)

      // Check input data.
      if (!workers.isDefinedAt(selfIndex)) {
        throw new NoSuchElementException(s"not in workers: $self")
      }

      resNum += selfNum

      val subIndex1 = selfIndex * 2 + 1
      val subIndex2 = subIndex1 + 1

      // TODO: this can be optimized (no need to evaluate workers.lift(subIndex2) if workers.lift(subIndex1) evaluates to None).
      val subWorkers = Set.empty[ActorRef] ++ workers.lift(subIndex1) ++ workers.lift(subIndex2)
      val resWorker = if (selfIndex == 0) sender() else workers((selfIndex - 1) / 2)

      context.become(awaitSubWorkers(resWorker, subWorkers))
  }

  private def awaitSubWorkers(resWorker: ActorRef, subWorkers: Set[ActorRef]): Receive = {
    if (subWorkers.isEmpty) {
      log.debug("sending {} to {}", resNum, resWorker)
      resWorker ! ReceiveNum(resNum)

      // My work is done, exit
      context.stop(self)
      Actor.emptyBehavior
    }
    else {
      case ReceiveNum(num) =>
        log.debug("receiving {} from {}", num, sender())

        if (!subWorkers.contains(sender())) {
          throw new NoSuchElementException(s"not a sub-worker: ${sender()}")
        }

        resNum += num

        context.become(awaitSubWorkers(resWorker, subWorkers - sender()))
    }
  }
}
