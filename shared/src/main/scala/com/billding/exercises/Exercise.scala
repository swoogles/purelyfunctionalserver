package com.billding.exercises

sealed trait Exercise {
  val id: String
  val humanFriendlyName: String
}

case object QuadSets extends Exercise {
  val id: String = "QuadSets"
  val humanFriendlyName = "QuadSets"
}

case object ShoulderStretches extends Exercise {
  val id: String = "shoulder_stretches"
  val humanFriendlyName = "Shoulder Stretches"
}

case object ShoulderSqueezes extends Exercise {
  val id: String = "shoulder_squeezes"
  val humanFriendlyName = "Shoulder Squeezes"
}

case object DoorRadialIsometrics extends Exercise {
  val id: String = "door_radial_isometrics"
  val humanFriendlyName = "Door Radial Isometrics"
}

case object ProneShoulderPresses extends Exercise {
  val id: String = "prone_shoulder_presses"
  val humanFriendlyName = "Prone Shoulder Presses"
}

case object KettleStyleLifts extends Exercise {
  val id: String = "kettle_style_lifts"
  val humanFriendlyName = "Kettle Style Lifts"
}
