package com.billding.exercises

sealed trait Exercise {
  val id: String
  val humanFriendlyName: String
}

case class ExerciseGeneric(
  id: String,
  humanFriendlyName: String
) extends Exercise

object Exercises {

  val QuadSets = ExerciseGeneric(
    id = "QuadSets",
    humanFriendlyName = "QuadSets"
  )

  val manuallyCountedExercises = List(
    ExerciseGeneric(
      id = "shoulder_stretches",
      humanFriendlyName = "Shoulder Stretches"
    ),
    ExerciseGeneric(
      id = "shoulder_squeezes",
      humanFriendlyName = "Shoulder Squeezes"
    ),
    ExerciseGeneric(
      id = "door_radial_isometrics",
      humanFriendlyName = "Door Radial Isometrics"
    ),
    ExerciseGeneric(
      id = "prone_shoulder_presses",
      humanFriendlyName = "Prone Shoulder Presses"
    ),
    ExerciseGeneric(
      id = "kettle_style_lifts",
      humanFriendlyName = "Kettle Style Lifts"
    ),
    ExerciseGeneric(
      id = "supine_shoulder_external_rotation_with_aid",
      humanFriendlyName = "Supine Shoulder External Rotation with aid"
    ),
    ExerciseGeneric(
      id = "supine_shoulder_flexion_with_aid",
      humanFriendlyName = "Supine Shoulder Flexion with aid"
    ),
    ExerciseGeneric(
      id = "sitting_shoulder_flexion_with_aid",
      humanFriendlyName = "Sitting Shoulder Flexion With Aid"
    ),
    ExerciseGeneric(
      id = "isometric_shoulder_external_rotation",
      humanFriendlyName = "Isometric Shoulder External Rotation"
    ),
    ExerciseGeneric(
      id = "isometric_shoulder_internal_rotation",
      humanFriendlyName = "Isometric Shoulder Internal Rotation"
    ),
    ExerciseGeneric(
      id = "inclined_row_unweighted",
      humanFriendlyName = "Inclined Row (unweighted)"
    )
  )

}
