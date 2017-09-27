package satorg.sample.akka.dsum

import akka.actor.{Actor, ActorLogging, Props}

object WorkerActor {
  def props(id: Int, idMax: Int, num: Long): Props = Props(new WorkerActor(id, idMax, num))

  def makeName(id: Int) = s"worker-$id"

  case class ReceiveNum(senderId: Int, senderNum: Long)

}

import satorg.sample.akka.dsum.WorkerActor._

class WorkerActor(myId: Int, maxId: Int, myNum: Long) extends Actor with ActorLogging {

  private var resultNum: Long = myNum

  private var childIds: Set[Int] = {
    val childId1 = myId * 2
    val childId2 = childId1 + 1

    (childId1 :: childId2 :: Nil).iterator.takeWhile(_ <= maxId).toSet
  }

  override def preStart(): Unit = {
    handleResult()
  }

  override def receive: Receive = {
    case ReceiveNum(senderId, senderNum) if childIds.contains(senderId) =>
      log.debug("received {} from child ID={}", senderNum, senderId)
      resultNum += senderNum
      childIds -= senderId
      handleResult()

    case ReceiveNum(senderId, _) =>
      log.warning("unexpected sender ID={}", senderId)
  }

  private def handleResult(): Unit = {
    if (childIds.nonEmpty)
      return

    if (myId == 1) {
      log.debug(s"sending {} to manager", resultNum)
      context.parent ! ReceiveNum(myId, resultNum)
      return
    }

    val parentId: Int = myId / 2
    val parentPath = context.self.path.parent / makeName(parentId)

    log.debug(s"sending {} to parent ID={}", resultNum, parentId)
    context.actorSelection(parentPath) ! ReceiveNum(myId, resultNum)
    context.stop(self) // the work is complete, exiting
  }
}
