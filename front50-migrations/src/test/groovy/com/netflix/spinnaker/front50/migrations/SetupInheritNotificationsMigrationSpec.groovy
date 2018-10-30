package com.netflix.spinnaker.front50.migrations

import com.netflix.spinnaker.front50.model.pipeline.Pipeline
import com.netflix.spinnaker.front50.model.pipeline.PipelineDAO
import spock.lang.Specification
import spock.lang.Subject

class SetupInheritNotificationsMigrationSpec extends Specification {
  def pipelineDAO = Mock(PipelineDAO)

  @Subject
  def migration = new SetupInheritNotificationsMigration(pipelineDAO)

  def "should set configuration for inherit notifications"() {
    given:
    def pipeline = new Pipeline([
      application: "test",
      type: "templatedPipeline",
      config: []
    ])

    when:
    migration.run()

    then:
    1 * pipelineDAO.all() >> { return [pipeline] }

    def config = (Map) pipeline.get("config")
    def configuration = (Map) config.get("configuration")
    def inherit = (List) configuration.get("inherit")
    inherit.contains("notifications")
  }

}
