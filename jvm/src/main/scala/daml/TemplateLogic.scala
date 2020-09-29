package daml

import zio.Task

class TemplateLogic(templateRepository: TemplateRepository) {
  def insert(template: Template): Task[Unit] =
    templateRepository.createTableFor(template)

}
