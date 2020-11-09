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

case class ExerciseGenericWithReps(
  id: String,
  humanFriendlyName: String,
  repsPerSet: Int,
  setsPerSession: Int,
  sessionDailyGoal: Int
) extends Exercise {
  val dailyGoal: Int = (repsPerSet * setsPerSession) * sessionDailyGoal
}

object Exercises {

  val QuadSets = ExerciseGeneric(
    id = "QuadSets",
    humanFriendlyName = "QuadSets",
    dailyGoal = 100
  )

  val supineShoulderExternalRotation =
    ExerciseGenericWithReps(
      id = "supine_shoulder_external_rotation_with_aid",
      humanFriendlyName = "Lying External Rotation with aid",
      repsPerSet = 10,
      setsPerSession = 1,
      sessionDailyGoal = 2
    )

  val manuallyCountedExercises: Seq[ExerciseGenericWithReps] = List(
    ExerciseGenericWithReps(
      id = "face_down_t_lift",
      humanFriendlyName = "Face-down T lift",
      repsPerSet = 10,
      setsPerSession = 3,
      sessionDailyGoal = 2
    ),
    ExerciseGenericWithReps(
      id = "face_down_i_lift",
      humanFriendlyName = "Face-down I lift",
      repsPerSet = 10,
      setsPerSession = 3,
      sessionDailyGoal = 2
    ),
    ExerciseGenericWithReps(
      id = "leaning_shoulder_taps",
      humanFriendlyName = "Leaning Shoulder Taps",
      repsPerSet = 20,
      setsPerSession = 1,
      sessionDailyGoal = 2
    ),
    ExerciseGenericWithReps(
      id = "rows",
      humanFriendlyName = "Rows",
      repsPerSet = 10,
      setsPerSession = 3,
      sessionDailyGoal = 2
    ),
    ExerciseGenericWithReps(
      id = "kettle_style_lifts",
      humanFriendlyName = "Kettle Style Lifts",
      repsPerSet = 10,
      setsPerSession = 3,
      sessionDailyGoal = 2
    ),
    supineShoulderExternalRotation,
    ExerciseGenericWithReps(
      id = "supine_shoulder_flexion_with_aid",
      humanFriendlyName = "Lying Shoulder Flexion with aid",
      repsPerSet = 10,
      setsPerSession = 1,
      sessionDailyGoal = 2
    ),
    ExerciseGenericWithReps(
      id = "band_rotating_internal_rotation",
      humanFriendlyName = "Banded Rotation (Internal)",
      repsPerSet = 15,
      setsPerSession = 3,
      sessionDailyGoal = 2
    ),
    ExerciseGenericWithReps(
      id = "band_rotating_external_rotation",
      humanFriendlyName = "Banded Rotation (External)",
      repsPerSet = 15,
      setsPerSession = 3,
      sessionDailyGoal = 2
    ),
    ExerciseGenericWithReps(
      id = "straight_forward_arm_raises",
      humanFriendlyName = "Arm Raises (Straight into Cross)",
      repsPerSet = 10,
      setsPerSession = 3,
      sessionDailyGoal = 2
    ),
    ExerciseGenericWithReps(
      id = "45_degree_arm_raises",
      humanFriendlyName = "Arm Raises (45 Degrees)",
      repsPerSet = 10,
      setsPerSession = 3,
      sessionDailyGoal = 2
    ),
    ExerciseGenericWithReps(
      id = "wax_on_wax_off",
      humanFriendlyName = "Wax-On Wax-Off (w/ towel)",
      repsPerSet = 20,
      setsPerSession = 2,
      sessionDailyGoal = 2
    )
  ).sortBy(_.humanFriendlyName)

}
