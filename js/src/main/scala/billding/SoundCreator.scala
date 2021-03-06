package billding

import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLAudioElement

class SoundCreator {
  val startSound = sound("/resources/audio/startQuadSet/metronome_tock.wav");
  val endSound = sound("/resources/audio/completeQuadSet/metronome_tink.wav");
  val goalReached = sound("/resources/audio/goalReached/109662__grunz__success.wav")
  val addExerciseSet = sound("/resources/audio/455181__screamr__dink-1.wav")

  def sound(src: String): HTMLAudioElement = {
    val sound: HTMLAudioElement = document.createElement("audio").asInstanceOf[HTMLAudioElement]
    sound.src = src
    sound.setAttribute("preload", "auto")
    sound.setAttribute("controls", "none")
    sound.style.display = "none"
    document.body.appendChild(sound)
    sound
  }
}
