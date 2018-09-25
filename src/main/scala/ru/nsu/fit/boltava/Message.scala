package ru.nsu.fit.boltava

sealed trait Message extends Serializable

sealed trait ChatMessage extends Message
case object Join extends ChatMessage
case object Leave extends ChatMessage
final case class TextMessage(text: String) extends ChatMessage

final case class Envelope(name: String, userMessage: ChatMessage) extends Message

sealed trait Command extends Message
final case class SendText(text: String) extends Command
case object SendLeave extends Command
case object ListActive extends Command
