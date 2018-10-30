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
      id: "1337",
      type: "templatedPipeline",
      config: [
        someOtherThing: [
          hello: "world"
        ]
      ]
    ])

    when:
    migration.run()

    then:
    1 * pipelineDAO.all() >> { return [pipeline] }
    1 * pipelineDAO.update("1337", pipeline)

    def config = (Map) pipeline.get("config")
    def configuration = (Map) config.get("configuration")
    def inherit = (List) configuration.get("inherit")
    inherit.contains("notifications")
  }

  def "should not add inherit configuration if it is already there"() {
    given:
    def pipeline = new Pipeline([
      application: "test",
      type: "templatedPipeline",
      config: [
        configuration: [
          inherit: [
            "notifications"
          ],
          someOtherThing: [
            hello: "world"
          ]
        ]
      ]
    ])

    when:
    migration.run()

    then:
    1 * pipelineDAO.all() >> { return [pipeline] }
    0 * pipelineDAO.update(_, _)

    def config = (Map) pipeline.get("config")
    def configuration = (Map) config.get("configuration")
    def inherit = (List) configuration.get("inherit")
    inherit == ["notifications"]
  }

  def "should add inherit configuration if it is not already there"() {
    given:
    def pipeline = new Pipeline([
      application: "test",
      id: "1337",
      type: "templatedPipeline",
      config: [
        configuration: [
          inherit: [
            "triggers",
            "parameters"
          ]
        ]
      ]
    ])

    when:
    migration.run()

    then:
    1 * pipelineDAO.all() >> { return [pipeline] }
    1 * pipelineDAO.update("1337", pipeline)

    def config = (Map) pipeline.get("config")
    def configuration = (Map) config.get("configuration")
    def inherit = (List) configuration.get("inherit")
    inherit == ["triggers", "parameters", "notifications"]
  }

}
