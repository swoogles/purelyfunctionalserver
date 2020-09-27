package daml

import zio.Task

class TemplateLogic(templateRepository: TemplateRepository) {
  def insert[T](template: Template[T]): Task[Unit] =
    templateRepository.createTableFor(template)

}
