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
      humanFriendlyName = "Lying External Rotation with aid",
      dailyGoal = 2
    )

  val manuallyCountedExercises = List(
    ExerciseGeneric(
      id = "shoulder_squeezes",
      humanFriendlyName = "Shoulder Squeezes",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "prone_shoulder_presses",
      humanFriendlyName = "Lying Shoulder Presses",
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
      humanFriendlyName = "Lying Shoulder Flexion with aid",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "sitting_shoulder_flexion_with_aid",
      humanFriendlyName = "Standing Shoulder Flexion With Aid",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "isometric_shoulder_external_rotation",
      humanFriendlyName = "Banded Anti-Rotation (External )",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "isometric_shoulder_internal_rotation",
      humanFriendlyName = "Banded Anti-Rotation (Internal)",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "band_rotating_internal_rotation",
      humanFriendlyName = "Banded Rotation (Internal)",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "band_rotating_external_rotation",
      humanFriendlyName = "Banded Rotation (External)",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "straight_forward_arm_raises",
      humanFriendlyName = "Arm Raises (Straight Forward)",
      dailyGoal = 2
    ),
    ExerciseGeneric(
      id = "45_degree_arm_raises",
      humanFriendlyName = "Arm Raises (45 Degrees)",
      dailyGoal = 2
    )
  ).sortBy(_.humanFriendlyName)

}
