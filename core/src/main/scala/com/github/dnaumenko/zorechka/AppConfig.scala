package com.github.dnaumenko.zorechka

import java.util.concurrent.{Executors, ThreadPoolExecutor}

import com.github.dnaumenko.zorechka.utils.concurrent.NamedThreadFactory
import com.github.dnaumenko.zorechka.HasAppConfig.Cfg
import zio.internal.Executor

import scala.concurrent.ExecutionContext

case class AppConfig(reposFile: String)

trait HasAppConfig {
  val cfg: Cfg
}

object HasAppConfig {
  trait Cfg {
    val config: AppConfig
    val blockingCtx: ExecutionContext
  }

  trait Live extends HasAppConfig {
    import pureconfig.generic.auto._

    val cfg: Cfg = new Cfg {
      override val config: AppConfig = pureconfig.loadConfigOrThrow[AppConfig]

      override val blockingCtx: ExecutionContext = {
        val factory = NamedThreadFactory(name = "blocking-pool", daemon = true)
        Executor
          .fromThreadPoolExecutor(_ => Int.MaxValue)(Executors.newCachedThreadPool(factory).asInstanceOf[ThreadPoolExecutor]).asEC
      }
    }
  }
}
