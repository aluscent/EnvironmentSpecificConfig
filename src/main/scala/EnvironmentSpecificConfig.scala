package com.aluscent

import com.typesafe.config.{Config, ConfigFactory}

import java.io.File
import java.nio.file.{Files, Paths}
import scala.util.Properties

class EnvironmentSpecificConfig {

  val selectedProfile: String =
    Properties.envOrElse("ACTIVE_PROFILE", "local")

  private val rootConfigFilePath =
    Properties.envOrElse("ROOT_CONFIG_PATH", "/conf/default.conf")

  private val rootConfigFile: Option[File] =
    if (Files.exists(Paths.get(rootConfigFilePath))) {
      println(s"[${getClass.getSimpleName}] Root config file is set.")
      Some(Paths.get(rootConfigFilePath).toFile)
    } else {
      println(s"[${getClass.getSimpleName}] No '.conf' files exist in '$rootConfigFilePath' directory.")
      None
    }

  println(s"[${getClass.getSimpleName}] Profile set to $selectedProfile.")

  private val configsInFiles: Config = (rootConfigFile match {
    case Some(file: File) =>
      ConfigFactory.parseFile(file)
        .withFallback(ConfigFactory.load(selectedProfile))
    case None =>
      ConfigFactory.load(selectedProfile)
  })
    .withFallback(ConfigFactory.load())

  private val configsFromEnvs: Config = ConfigFactory.systemEnvironment()

  private def configOfFileToConfigOfEnv(name: String): String =
    name.replace(".", "_")

  def apply(name: String): String = getString(name)

  def getString(name: String): String =
    if (configsFromEnvs.hasPathOrNull(configOfFileToConfigOfEnv(name)))
      configsFromEnvs.getString(configOfFileToConfigOfEnv(name))
    else configsInFiles.getString(name)

  def getInt(name: String): Int =
    if (configsFromEnvs.hasPathOrNull(configOfFileToConfigOfEnv(name)))
      configsFromEnvs.getInt(configOfFileToConfigOfEnv(name))
    else configsInFiles.getInt(name)

  def getConfig(name: String): Config = configsInFiles.getConfig(name)

  def confOrElse(name: String, other: String): String =
    if (configsInFiles.hasPathOrNull(name))
      getString(name)
    else other

  def confOrElse(name: String, other: Int): Int =
    if (configsInFiles.hasPathOrNull(name))
      getInt(name)
    else other
}
