package com.netflix.spinnaker.front50.migrations;

import com.netflix.spinnaker.front50.model.ItemDAO;
import com.netflix.spinnaker.front50.model.pipeline.Pipeline;
import com.netflix.spinnaker.front50.model.pipeline.PipelineDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

      if (!"templatedPipeline".equals(type)) {
        return false;
      }

      Map config = (Map) p.getConfig();

      return Optional.ofNullable((Map) config.get("configuration"))
        .map(configuration -> (List) configuration.get("inherit"))
        .map(inherit -> !inherit.contains("notifications"))
        .orElse(true);
    };

    pipelineDAO.all().stream()
      .filter(missingInheritNotifications)
      .forEach(this::migrate);
  }

  @SuppressWarnings("unchecked")
  private <V> V putOrGet(Map<String, V> map, String key, V valueIfMissing) {
    V oldValue = map.putIfAbsent(key, valueIfMissing);
    return oldValue == null ? valueIfMissing : oldValue;
  }

  @SuppressWarnings("unchecked")
  private void migrate(Pipeline pipeline) {
    log.info("Added inherit notification configuration (application: {}, pipelineId: {}, config: {})",
        pipeline.getApplication(),
        pipeline.getId(),
        pipeline.get("config")
    );

    Map<String, Map> config = (Map<String, Map>) pipeline.get("config");
    Map<String, List> configuration = putOrGet(config, "configuration", new HashMap<String, List>());
    List<String> inherit = putOrGet(configuration, "inherit", new ArrayList<String>());
    if (!inherit.contains("notifications")) {
      inherit.add("notifications");
    }
    pipelineDAO.update(pipeline.getId(), pipeline);
  }
}
