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
