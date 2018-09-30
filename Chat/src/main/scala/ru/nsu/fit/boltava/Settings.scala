package ru.nsu.fit.boltava

final case class NodeConfig(name: String, subscriptionTopic: String)
final case class ClusterConfig(name: String, subscriptionTopic: String)
final case class Settings(cluster: ClusterConfig)
