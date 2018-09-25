package ru.nsu.fit.boltava

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{
  Publish,
  Subscribe,
  SubscribeAck,
  Unsubscribe,
  UnsubscribeAck
}

import ru.nsu.fit.boltava.io.MessageWriter

class ClusterNode(config: NodeConfig, messageWriter: MessageWriter) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator
  var users = Set.empty[String]

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
    cluster.leave(cluster.selfAddress)
  }

  override def receive: Receive = {
    case MemberUp(member) if member.address == cluster.selfAddress =>
      mediator ! Subscribe(config.subscriptionTopic, self)
    case SubscribeAck(Subscribe(config.subscriptionTopic, _, `self`)) =>
      joinChat()
    case UnsubscribeAck(Unsubscribe(config.subscriptionTopic, _, `self`)) =>
      context.stop(self)

    case envelope: Envelope => writeMessage(envelope)
    case SendLeave =>
      leaveChat()
      mediator ! Unsubscribe(config.subscriptionTopic, self)
    case SendText(text) => publish(Envelope(config.name, TextMessage(text)))
    case ListActive     => messageWriter.list(users.toList)
  }

  private def joinChat(): Unit = {
    users += config.name
    publish(Envelope(config.name, Join))
  }

  private def leaveChat(): Unit = {
    users -= config.name
    publish(Envelope(config.name, Leave))
  }

  private def writeMessage(envelope: Envelope): Unit = envelope match {
    case Envelope(sender, message) =>
      message match {
        case TextMessage(text) =>
          messageWriter.textMessage(sender, text)
        case Join if sender != config.name =>
          users = users + sender
          messageWriter.joinMessage(sender)
        case Leave if sender != config.name =>
          users = users - sender
          messageWriter.leaveMessage(sender)
        case _ =>
      }
    case _ => log.info(s"Unsupported message: $envelope")
  }

  private def publish(msg: Any): Unit = {
    mediator ! Publish(config.subscriptionTopic, msg)
  }

}
