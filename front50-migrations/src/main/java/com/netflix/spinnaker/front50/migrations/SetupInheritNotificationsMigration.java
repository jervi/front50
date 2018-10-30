package com.netflix.spinnaker.front50.migrations;

import com.netflix.spinnaker.front50.model.ItemDAO;
import com.netflix.spinnaker.front50.model.pipeline.Pipeline;
import com.netflix.spinnaker.front50.model.pipeline.PipelineDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
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
      String type = (String) p.get("type");

      if(!type.equals("templatedPipeline")) {
        return false;
      }

      Map config = (Map) p.getConfig();

      return Optional.of((Map) config.get("configuration"))
        .map(configuration -> (List) configuration.get("inherit"))
        .map(inherit -> inherit.contains("notifications"))
        .orElse(false);
    };

    pipelineDAO.all().stream()
      .filter(missingInheritNotifications)
      .forEach(pipeline -> migrate(pipelineDAO, pipeline));
  }

  private void migrate(ItemDAO<Pipeline> dao, Pipeline pipeline) {
    log.info(
      "Added inherit notification configuration (application: {}, pipelineId: {}, config: {})",
      pipeline.getApplication(),
      pipeline.getId(),
      pipeline.get("config")
    );

    Map<String, Object> config = (Map<String, Object>) pipeline.get("config");
    config.putIfAbsent("configuration", new HashMap<String, Object>());

    Map<String, Object> configuration = (Map<String, Object>) config.get("configuration");
    configuration.putIfAbsent("inherit", new ArrayList<String>());

    List<String> inherit = (List<String>) configuration.get("inherit");
    if (!inherit.contains("notifications")) {
      inherit.add("notifications");
    }
    dao.update(pipeline.getId(), pipeline);
  }
}
