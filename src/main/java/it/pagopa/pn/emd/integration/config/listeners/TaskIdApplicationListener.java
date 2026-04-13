package it.pagopa.pn.emd.integration.config.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

@Slf4j
public class TaskIdApplicationListener implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        String ecsAgentUri = System.getenv("ECS_AGENT_URI");
        if (ecsAgentUri != null && !ecsAgentUri.isBlank()) {
            // ECS_AGENT_URI format: http://<task-id>/...
            // Extract task ID: last path segment before /task or the host part
            String taskId = extractTaskId(ecsAgentUri);
            if (taskId != null) {
                System.setProperty("TASK_ID", taskId);
                log.debug("ECS Task ID set to: {}", taskId);
            }
        }
    }

    private String extractTaskId(String uri) {
        try {
            // ECS_AGENT_URI: http://169.254.170.2/v2/metadata/<task-id>
            // or ECS_CONTAINER_METADATA_URI_V4: http://169.254.170.2/v4/<task-id>/...
            String[] parts = uri.split("/");
            for (int i = parts.length - 1; i >= 0; i--) {
                String part = parts[i];
                if (part != null && part.length() > 8 && !part.isEmpty()
                        && !part.equals("v2") && !part.equals("v3") && !part.equals("v4")
                        && !part.equals("metadata") && !part.equals("task")) {
                    return part;
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract task ID from ECS_AGENT_URI: {}", uri);
        }
        return null;
    }
}
