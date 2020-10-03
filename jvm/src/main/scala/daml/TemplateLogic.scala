package daml

import zio.Task

class TemplateLogic(templateRepository: TemplateRepository) {
  def insert(template: Template): Task[Unit] =
    templateRepository.createTableFor(template)

  def insert(contract: Contract): Task[Unit] =
    templateRepository.insert(contract)

}
