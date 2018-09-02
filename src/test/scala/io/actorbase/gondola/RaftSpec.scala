package io.actorbase.gondola

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class RaftSpec extends WordSpec with ActorTestKit with Matchers with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    shutdownTestKit()
  }

  "A timer" must {
    "schedule a message to check heartbeats" in {
      // TODO
    }
  }
}
