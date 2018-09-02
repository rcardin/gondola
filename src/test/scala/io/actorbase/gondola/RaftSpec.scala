package io.actorbase.gondola

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, ManualTime, TestProbe}
import com.typesafe.config.Config
import io.actorbase.gondola.Raft._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.duration._

class RaftSpec extends WordSpec with ActorTestKit with Matchers with BeforeAndAfterAll {

  override def config: Config = ManualTime.config
  val manualTime: ManualTime = ManualTime()

  override protected def afterAll(): Unit = {
    shutdownTestKit()
  }

  "A timer" must {
    "schedule a message to check heartbeats" in {
      val probe = TestProbe[CheckHeartbeat.type]()
      spawn(follower(200L, now()))
      manualTime.expectNoMessageFor(200 millis, probe)
      manualTime.timePasses(100 millis)
      // FIXME probe.expectMessage(CheckHeartbeat)
    }
  }
}
