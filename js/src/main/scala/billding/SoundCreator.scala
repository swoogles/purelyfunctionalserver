package billding

import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLAudioElement

class SoundCreator {
  val startSound = sound("/resources/audio/startQuadSet/metronome_tock.wav");
  val endSound = sound("/resources/audio/completeQuadSet/metronome_tink.wav");

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
