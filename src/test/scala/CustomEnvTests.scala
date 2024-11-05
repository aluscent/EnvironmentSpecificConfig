package com.aluscent

import org.scalatest.flatspec.AnyFlatSpec

class CustomEnvTests extends AnyFlatSpec {

  def setEnv(key: String, value: String): String = {
    val field = System.getenv().getClass.getDeclaredField("m")
    field.setAccessible(true)
    val map = field.get(System.getenv()).asInstanceOf[java.util.Map[java.lang.String, java.lang.String]]
    map.put(key, value)
  }

  setEnv("ACTIVE_PROFILE", "dev")

  val config = new EnvironmentSpecificConfig

  "Environment" should "be dev" in assert(config.selectedProfile === "dev")

  "Version" should "be 0.3.0" in assert(config("version") === "0.3.0")

  "Int amount" should "be 654652" in assert(config.getInt("int-amount") === 654652)

  "Multiple configs" should "have multiple configs in it" in {
    val testConfig = Map("value1" -> "test2", "value2" -> 4654)
    val configInFile = config.getConfig("multiple-config")

    for {
      pair <- testConfig
      key = pair._1
      value = pair._2.toString
    } yield assert(configInFile.getString(key) === value)
  }

  "Replaced version" should "get value 0.7.0" in
    assert(config.confOrElse("replaced-version", "0.7.0") === "0.7.0")

  "Replaced int amount" should "get value 65165" in
    assert(config.confOrElse("replaced-int-amount", "65165") === "65165")

  "Common property" should "fallback to common-property-1" in
    assert(config("common-property") === "common-property-1")
}
