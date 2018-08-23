package io.actorbase.gondola

import akka.NotUsed
import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors

object Raft {
  sealed trait RaftProtocol
  final case class RequestVote(term: Int) extends RaftProtocol
  final case class Voted(term: Int) extends RaftProtocol
  final case class AppendEntries(term: Int) extends RaftProtocol
  final case class EntriesAppended(term: Int) extends RaftProtocol

  private def candidate: Behavior[RaftProtocol] =
    Behaviors.receivePartial {
      case (ctx, Voted(term)) => Behaviors.same
    }

  private def leader: Behavior[RaftProtocol] =
    Behaviors.receivePartial {
      case (ctx, EntriesAppended(term)) => Behaviors.same
    }

  private def follower: Behavior[RaftProtocol] =
    Behaviors.receivePartial {
      case (ctx, RequestVote(term)) => Behaviors.same
      case (ctx, AppendEntries(term)) => Behaviors.same
    }

  val main: Behavior[NotUsed] =
    Behaviors.setup { ctx =>
      val followerActor = ctx.spawn(follower, "follower-1")
      Behaviors.receiveSignal {
        case (_, Terminated(_)) => Behaviors.stopped
      }
    }

  val system = ActorSystem(main, "Raft")
}
