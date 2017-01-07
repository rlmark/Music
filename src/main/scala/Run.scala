import compose.core.{Duration, Note, Pitch => ComposePitch, SeqScore}
import compose.player.{ScalaColliderPlayer, Tempo}
import de.sciss.synth.ugen.{Out, SinOsc}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


object Run {
  def main(args: Array[String]) {

    import de.sciss.synth._

    val cfg = Server.Config()
    cfg.program = "/opt/homebrew-cask/Caskroom/supercollider/3.6.6/SuperCollider/SuperCollider.app/Contents/Resources/scsynth"

    val song: SeqScore = {
      Note(ComposePitch.C3, Duration.Quarter) ~
        Note(ComposePitch.E3, Duration.Quarter) ~
        Note(ComposePitch.G3, Duration.Quarter) ~
        Note(ComposePitch.G3, Duration.Whole)
    }

    val Freq = "freq"
    val Amp = "amp"

    val sine = SynthDef("sine") {
      val freq = Freq.kr(440)
      val amp = Amp.kr(0.0)
      val osc = SinOsc.ar(freq, 0.0) * amp
      Out.ar(0, List(osc, osc))
    }

    import scala.concurrent.ExecutionContext.Implicits.global

    def func: (ScalaColliderPlayer) => Future[ScalaColliderPlayer.State]  = {
      player: ScalaColliderPlayer =>
        player.play(song, Tempo(180))(global)
    }

    Server.run(cfg) { server =>
      val player = new ScalaColliderPlayer(4, sine, server)
      try {
        Await.result(func(player), 50.seconds)
      }
      finally {
        player.free
      }
    }
  }
}
