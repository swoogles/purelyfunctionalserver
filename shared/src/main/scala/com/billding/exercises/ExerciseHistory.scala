package com.billding.exercises

import exercises.DailyQuantizedExercise

case class ExerciseHistory(exercises: List[DailyQuantizedExercise])

object ExerciseHistoryConstruction {

  def apply(exercises: List[DailyQuantizedExercise], otherStuff: Int) = {
    assert(exercises.forall(_.name == exercises.head.name)) // assert that all exercises are the same. TODO This would be better applied on the data type before it gets here.
    val allExercises: Seq[Exercise] = Exercises.manuallyCountedExercises :+ Exercises.QuadSets
    val typedExercise = // TODO Wouldn't it be great if this could be validated earlier? eg as records are being pulled from the DB?
      allExercises
        .find(_.id == exercises.head.name)
        .getOrElse(throw new RuntimeException("Unknown exercise id: " + exercises.head.name))
  }
}
