package com.aluscent

import com.typesafe.config.{Config, ConfigFactory}

import java.io.File
import java.nio.file.{Files, Paths}
import scala.collection.JavaConverters._

class EnvironmentSpecificConfig(
    environment: Map[String, String] = sys.env,
    fileConfig: Option[Config] = None
) {

  val selectedProfile: String =
    environment.getOrElse("ACTIVE_PROFILE", "local")

  private val rootConfigFilePath =
    environment.getOrElse("ROOT_CONFIG_PATH", "/conf/default.conf")

  private val rootConfigFile: Option[File] =
    if (Files.exists(Paths.get(rootConfigFilePath))) {
      println(s"[${getClass.getSimpleName}] Root config file is set.")
      Some(Paths.get(rootConfigFilePath).toFile)
    } else {
      println(s"[${getClass.getSimpleName}] No '.conf' files exist in '$rootConfigFilePath' directory.")
      None
    }

  println(s"[${getClass.getSimpleName}] Profile set to $selectedProfile.")

  private val configsInFiles: Config = fileConfig.getOrElse((rootConfigFile match {
    case Some(file: File) =>
      ConfigFactory.parseFile(file)
        .withFallback(ConfigFactory.load(selectedProfile))
    case None =>
      ConfigFactory.load(selectedProfile)
  })
    .withFallback(ConfigFactory.load()).resolve())

  private val configsFromEnvs: Config = ConfigFactory.parseMap(environment.asJava)

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

  /** Environment overrides apply to the existing leaves of a config object too.
    * Environment-only paths remain available through scalar getters; underscores
    * cannot unambiguously describe new nested object paths.
    */
  def getConfig(name: String): Config = {
    val base = configsInFiles.getConfig(name)
    base.entrySet().asScala.foldLeft(base) { (resolved, entry) =>
      val envKey = configOfFileToConfigOfEnv(s"$name.${entry.getKey}")
      if (configsFromEnvs.hasPathOrNull(envKey))
        resolved.withValue(entry.getKey, configsFromEnvs.getValue(envKey))
      else resolved
    }
  }

  private def contains(name: String): Boolean =
    configsFromEnvs.hasPathOrNull(configOfFileToConfigOfEnv(name)) ||
      configsInFiles.hasPathOrNull(name)

  def confOrElse(name: String, other: String): String =
    if (contains(name))
      getString(name)
    else other

  def confOrElse(name: String, other: Int): Int =
    if (contains(name))
      getInt(name)
    else other
}
