package billding

sealed trait SoundStatus {}
case object FULL extends SoundStatus
case object OFF extends SoundStatus
