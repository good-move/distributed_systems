import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import ru.nsu.fit.boltava.io.{CommandParser, ConsoleMessageReader, ConsoleMessageWriter}
import ru.nsu.fit.boltava.{ClusterNode, NodeConfig, SendLeave, Settings}

object ChatNode {
  def main(args: Array[String]): Unit = {
    pureconfig
      .loadConfig[Settings]
      .map { localConfig =>
        val cluster = localConfig.cluster
        val akkaClusterConfig = ConfigFactory.load()
        val nodeConfig = NodeConfig(cluster.nodeName, cluster.subscriptionTopic)
        val system = ActorSystem(localConfig.cluster.name, akkaClusterConfig)

        val node =
          system.actorOf(Props(new ClusterNode(nodeConfig, new ConsoleMessageWriter)), nodeConfig.name)

        new Thread(() => {
          val messageReader = new ConsoleMessageReader
          try {
            while (!Thread.interrupted()) {
              CommandParser.parse(messageReader.read()) match {
                case Right(command) =>
                  node ! command
                  if (command == SendLeave) throw new InterruptedException
                case Left(error) => println(error.getMessage)
              }
            }
          } catch {
            case _: InterruptedException =>
          }
        }).start()

        sys.addShutdownHook {
          node ! SendLeave
        }
      }

  }

}
