package ru.nsu.fit.boltava.io

import scala.io.StdIn
import scala.util.control.NoStackTrace

import ru.nsu.fit.boltava.{Command, SendLeave, SendText}

trait MessageReader {
  def read(): String
}

object CommandParser {
  def parse(command: String): Either[UnsupportedCommand, Command] = command match {
    case _: String if command.startsWith("/") =>
      command.drop(1) match {
        case "leave" => Right(SendLeave)
        case _       => Left(UnsupportedCommand(command))
      }

    case _ => Right(SendText(command))
  }

  final case class UnsupportedCommand(command: String)
      extends Exception(s"Command $command is not supported")
      with NoStackTrace

}

class ConsoleMessageReader extends MessageReader {
  override def read(): String = StdIn.readLine()
}
