package com.aluscent

import com.typesafe.config.{ConfigException, ConfigFactory}
import org.scalatest.funsuite.AnyFunSuite

class PrecedenceSpec extends AnyFunSuite {
  private def config(env: (String, String)*) = new EnvironmentSpecificConfig(
    env.toMap, Some(ConfigFactory.parseString("""
      service { host = "file-host", port = 5432 }
      empty = null
    """)))

  test("environment-only values take priority over supplied defaults") {
    val c = config("service_token" -> "secret", "workers" -> "8")
    assert(c.confOrElse("service.token", "default") == "secret")
    assert(c.confOrElse("workers", 2) == 8)
  }
  test("scalar and object getters share environment precedence") {
    val c = config("service_host" -> "env-host", "service_port" -> "6543")
    assert(c.getString("service.host") == "env-host")
    assert(c.getInt("service.port") == 6543)
    assert(c.getConfig("service").getString("host") == "env-host")
    assert(c.getConfig("service").getInt("port") == 6543)
  }
  test("missing values use defaults, existing file values do not") {
    val c = config()
    assert(c.confOrElse("service.host", "default") == "file-host")
    assert(c.confOrElse("missing", "default") == "default")
    assert(c.confOrElse("workers", 2) == 2)
  }
  test("invalid overrides and explicit nulls fail instead of hiding bad configuration") {
    intercept[ConfigException.WrongType](config("service_port" -> "invalid").confOrElse("service.port", 1))
    intercept[ConfigException.Null](config().confOrElse("empty", "default"))
  }
  test("instances do not leak environment values into each other") {
    assert(config("service_host" -> "a").getString("service.host") == "a")
    assert(config().getString("service.host") == "file-host")
  }
}
