package ru.nsu.fit.boltava.io

trait MessageWriter {
  def textMessage(sender: String, msg: String): Unit
  def joinMessage(sender: String): Unit
  def leaveMessage(sender: String): Unit
  def list(users: List[String]): Unit
}

class ConsoleMessageWriter extends MessageWriter {
  override def textMessage(sender: String, msg: String): Unit =
    println(s"$sender: $msg")

  override def joinMessage(sender: String): Unit = println(s"$sender joined chat")

  override def leaveMessage(sender: String): Unit = println(s"$sender left chat")

  override def list(users: List[String]): Unit = println(
    s"""
       |Active users:
       |${users.map("-- " + _).mkString("\n")}
     """.stripMargin)
}
