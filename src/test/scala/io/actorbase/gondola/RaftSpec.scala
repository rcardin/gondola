package io.actorbase.gondola

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, BehaviorTestKit, ManualTime, TestProbe}
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
/*
  "A timer" must {
    "schedule a message to check heartbeats" in {
      val testKit = BehaviorTestKit(follower(200L, now(), 0))
      val raftActor = spawn(follower(200L, now(), 0))
      // val inbox = raftActor.
      // manualTime.timePasses(200.millis)
      // inbox.expectMessage(CheckHeartbeat)
    }
  }
*/
  "A follower" must {
    "respond to leader heartbeat" in {
      val probe = TestProbe[RaftProtocol]()
      val followerActor = spawn(follower(Nil, 200L, now(), 0))
      followerActor ! AppendEntries(0, probe.ref)
      probe.expectMessage(HeartbeatResponse(0))
    }
  }
}
