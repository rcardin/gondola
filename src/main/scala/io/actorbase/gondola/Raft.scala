package io.actorbase.gondola

import java.util.concurrent.TimeUnit

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, SpawnProtocol}

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Raft {

  private case object TimerKey

  sealed trait RaftProtocol
  final case class RequestVote(term: Int) extends RaftProtocol
  final case class Voted(term: Int) extends RaftProtocol
  final case class AppendEntries(term: Int) extends RaftProtocol
  final case class EntriesAppended(term: Int) extends RaftProtocol
  private[gondola] final case object CheckHeartbeat extends RaftProtocol

  def candidate: Behavior[RaftProtocol] =
    Behaviors.receivePartial {
      case (ctx, Voted(term)) => Behaviors.same
    }

  def leader: Behavior[RaftProtocol] =
    Behaviors.receivePartial {
      case (ctx, EntriesAppended(term)) => Behaviors.same
    }

  def follower(heartbeat: Long, lastHeartbeat: Long): Behavior[RaftProtocol] =
    Behaviors.withTimers { timers =>
      timers.startPeriodicTimer(TimerKey, CheckHeartbeat, FiniteDuration.apply(heartbeat, TimeUnit.MILLISECONDS))
      Behaviors.receivePartial {
        case (ctx, RequestVote(term)) => Behaviors.same
        case (ctx, AppendEntries(term)) => Behaviors.same
        case (ctx, CheckHeartbeat) =>
          if (now() - lastHeartbeat > heartbeat) {
            candidate
          }
          Behaviors.same
      }
    }

  val main: Behavior[SpawnProtocol] =
    Behaviors.setup { ctx =>
      val followerActor = ctx.spawn(follower(randomHeartbeat(), now()), "follower-1")
      SpawnProtocol.behavior
    }

  val system = ActorSystem(main, "Raft")

  def now(): Long = {
    System.currentTimeMillis()
  }

  def randomHeartbeat(): Long = {
    150 + Random.nextInt(150)
  }
}
