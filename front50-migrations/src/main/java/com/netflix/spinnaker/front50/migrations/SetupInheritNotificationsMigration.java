package com.netflix.spinnaker.front50.migrations;

import com.netflix.spinnaker.front50.model.pipeline.Pipeline;
import com.netflix.spinnaker.front50.model.pipeline.PipelineDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class SetupInheritNotificationsMigration implements Migration {
  private static final Logger log = LoggerFactory.getLogger(SetupInheritNotificationsMigration.class);

  private final PipelineDAO pipelineDAO;

  @Autowired
  public SetupInheritNotificationsMigration(PipelineDAO pipelineDAO) {
    this.pipelineDAO = pipelineDAO;
  }

  @Override
  public boolean isValid() {
    return false;
  }

  @Override
  public void run() {
    log.info("Starting inherit notifications migration");

    /*
    "config": {
    "configuration": {
      "inherit": [
        "notifications"
      ]
    }
     */

    Predicate<Pipeline> missingInheritNotifications = p -> {
      Map config = (Map) p.get("config");
      if(config..containsKey("configuration"))


        return ("cron".equalsIgnoreCase(type) && (id == null || id.isEmpty()));
      });
    };

    pipelineDAO.all().stream()
      .filter(missingInheritNotifications)
      .forEach(pipeline -> migrate(pipelineDAO, pipeline));

  }
}
