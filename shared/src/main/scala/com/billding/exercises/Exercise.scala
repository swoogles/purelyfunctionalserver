package com.billding.exercises

sealed trait Exercise {
  val id: String
  val humanFriendlyName: String
  val dailyGoal: Int
}

case class ExerciseGeneric(
  id: String,
  humanFriendlyName: String,
  dailyGoal: Int
) extends Exercise

object Exercises {

  val QuadSets = ExerciseGeneric(
    id = "QuadSets",
    humanFriendlyName = "QuadSets",
    dailyGoal = 100
  )

  val supineShoulderExternalRotation =
    ExerciseGeneric(
      id = "supine_shoulder_external_rotation_with_aid",
      humanFriendlyName = "Supine Shoulder External Rotation with aid",
      dailyGoal = 2
    )

  val manuallyCountedExercises = List(
    ExerciseGeneric(
      id = "shoulder_stretches",
      humanFriendlyName = "Shoulder Stretches",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "shoulder_squeezes",
      humanFriendlyName = "Shoulder Squeezes",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "door_radial_isometrics",
      humanFriendlyName = "Door Radial Isometrics",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "prone_shoulder_presses",
      humanFriendlyName = "Prone Shoulder Presses",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "kettle_style_lifts",
      humanFriendlyName = "Kettle Style Lifts",
      dailyGoal = 2
    ),
    supineShoulderExternalRotation,
    ExerciseGeneric(
      id = "supine_shoulder_flexion_with_aid",
      humanFriendlyName = "Supine Shoulder Flexion with aid",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "sitting_shoulder_flexion_with_aid",
      humanFriendlyName = "Sitting Shoulder Flexion With Aid",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "isometric_shoulder_external_rotation",
      humanFriendlyName = "Isometric Shoulder External Rotation",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "isometric_shoulder_internal_rotation",
      humanFriendlyName = "Isometric Shoulder Internal Rotation",
      dailyGoal = 2
    )
  ).sortBy(_.humanFriendlyName)

}
