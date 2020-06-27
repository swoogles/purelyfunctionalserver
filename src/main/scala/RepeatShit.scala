import cats.effect.{ContextShift, Fiber, IO, Timer}
import cats.implicits._

import scala.concurrent.duration._

object RepeatShit {

  def infiniteIO(id: Int)(implicit cs: ContextShift[IO], timer: Timer[IO]): IO[Fiber[IO, Unit]] = {
    def repeat: IO[Unit] =
      IO(println("I should periodically retrieve Github info!"))
        .flatMap(_ => IO.shift *> IO.sleep(15.minutes) *> repeat)

    repeat.start
  }

  def infiniteWeatherCheck(implicit cs: ContextShift[IO], timer: Timer[IO]): IO[Fiber[IO, Unit]] = {
    def repeat: IO[Unit] =
      IO(println("I should get the weather data every few minutes."))
        .flatMap(_ => IO.shift *> IO.sleep(10.minutes) *> repeat)

    repeat.start
  }
}
