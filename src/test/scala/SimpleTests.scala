package com.aluscent

import org.scalatest.flatspec.AnyFlatSpec

class SimpleTests extends AnyFlatSpec {

  val config = new EnvironmentSpecificConfig

  "Environment" should "be local (the default)" in assert(config.selectedProfile === "local")

  "Version" should "be 0.1.0" in assert(config("version") === "0.1.0")

  "Int amount" should "be 123357" in assert(config.getInt("int-amount") === 123357)

  "Multiple configs" should "have multiple configs in it" in {
    val testConfig = Map("value1" -> "test1", "value2" -> 1234)
    val configInFile = config.getConfig("multiple-config")

    for {
      pair <- testConfig
      key = pair._1
      value = pair._2.toString
    } yield assert(configInFile.getString(key) === value)
  }

  "Replaced version" should "get value 0.5.0" in
    assert(config.confOrElse("replaced-version", "0.5.0") === "0.5.0")

  "Replaced int amount" should "get value 453256" in
    assert(config.confOrElse("replaced-int-amount", "453256") === "453256")

  "Common property" should "fallback to common-property-1" in
    assert(config("common-property") === "common-property-1")
}
