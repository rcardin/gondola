package io.actorbase.gondola

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SpawnProtocol}

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Raft {

  private case object TimerKey

  sealed trait RaftProtocol
  final case class RequestVote(term: Int, replyTo: ActorRef[RaftProtocol]) extends RaftProtocol
  final case class Voted(voted: Boolean, term: Int) extends RaftProtocol
  final case class AppendEntries(term: Int, replyTo: ActorRef[RaftProtocol]) extends RaftProtocol
  final case class HeartbeatResponse(term: Int) extends RaftProtocol
  final case class EntriesAppended(term: Int) extends RaftProtocol
  private[gondola] final case object CheckHeartbeat extends RaftProtocol

  // TODO INSERT Election timeout
  def candidate(nodes: List[ActorRef[RaftProtocol]], currentTerm: Int): Behavior[RaftProtocol] = {
    val quorum: Int = nodes.size / 2 + 1
    Behaviors.setup[RaftProtocol] { ctx =>
      var requestVoteResponses: List[Voted] = Nil
      nodes.foreach(node => node ! RequestVote(currentTerm + 1, ctx.self))

      def nextBehavior: Behavior[RaftProtocol] = {
        if (requestVoteResponses.count(_.voted) >= quorum) {
          leader(nodes)
        } else {
          Behaviors.same
        }
      }

      Behaviors.receivePartial {
        case (_, Voted(voted, term)) =>
          requestVoteResponses = Voted(voted, term) :: requestVoteResponses
          nextBehavior
      }
    }
  }

  def leader(nodes: List[ActorRef[RaftProtocol]]): Behavior[RaftProtocol] =
    Behaviors.receivePartial {
      case (_, EntriesAppended(term)) => Behaviors.same
    }

  def follower(nodes: List[ActorRef[RaftProtocol]],
               heartbeat: Long,
               lastHeartbeat: Long,
               currentTerm: Int): Behavior[RaftProtocol] =
    Behaviors.withTimers { timers =>
      timers.startPeriodicTimer(TimerKey, CheckHeartbeat, FiniteDuration.apply(heartbeat, TimeUnit.MILLISECONDS))
      Behaviors.receivePartial {
        case (ctx, RequestVote(term, replyTo)) => Behaviors.same
        case (ctx, AppendEntries(term, leader)) =>
          if (term == currentTerm) {
            leader ! HeartbeatResponse(currentTerm)
            Behaviors.same
          } else {
            // TODO
            Behaviors.same
          }
        case (ctx, CheckHeartbeat) =>
          if (now() - lastHeartbeat > heartbeat) {
            candidate(nodes, currentTerm)
          }
          Behaviors.same
      }
    }

  val main: Behavior[SpawnProtocol] =
  // FIXME Nil is not a correct initial value
    Behaviors.setup { ctx =>
      val followerActor = ctx.spawn(follower(Nil, randomHeartbeat(), now(), 0), "follower-1")
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
