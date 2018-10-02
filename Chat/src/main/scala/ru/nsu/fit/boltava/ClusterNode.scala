package ru.nsu.fit.boltava

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, SubscribeAck, Unsubscribe, UnsubscribeAck}

import ru.nsu.fit.boltava.io.MessageWriter

class ClusterNode(config: NodeConfig, messageWriter: MessageWriter) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  var state = 0

  override def preStart(): Unit = {
    cluster.subscribe(self, InitialStateAsEvents, classOf[MemberEvent])
    mediator ! Subscribe(config.subscriptionTopic, self)
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
    cluster.leave(cluster.selfAddress)
  }

  override def receive: Receive = {
    case MemberUp(member) if member.address == cluster.selfAddress =>
      joinChat()
    case SubscribeAck(Subscribe(config.subscriptionTopic, _, `self`)) =>
      joinChat()
    case UnsubscribeAck(Unsubscribe(config.subscriptionTopic, _, `self`)) =>
      context.stop(self)

    case envelope: Envelope =>
      writeMessage(envelope)
    case SendLeave =>
      leaveChat()
      mediator ! Unsubscribe(config.subscriptionTopic, self)
    case SendText(text) =>
      publish(Envelope(config.name, TextMessage(text)))
  }

  private def joinChat(): Unit = {
    state += 1
    if (state == 2) {
      publish(Envelope(config.name, Join))
    }
  }

  private def leaveChat(): Unit = {
    publish(Envelope(config.name, Leave))
  }

  private def writeMessage(envelope: Envelope): Unit = envelope match {
    case Envelope(sender, message) =>
      message match {
        case TextMessage(text) =>
          messageWriter.textMessage(sender, text)
        case Join if sender != config.name =>
          messageWriter.joinMessage(sender)
        case Leave if sender != config.name =>
          messageWriter.leaveMessage(sender)
        case _ =>
      }
    case _ => log.info(s"Unsupported message: $envelope")
  }

  private def publish(msg: Any): Unit = {
    mediator ! Publish(config.subscriptionTopic, msg)
  }

}
