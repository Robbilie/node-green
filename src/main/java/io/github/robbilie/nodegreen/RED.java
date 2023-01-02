package io.github.robbilie.nodegreen;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.stream.Collectors;

public class RED {

    public static Nodes nodes = new Nodes();
    public static Map<String,Flow> activeFlows = new HashMap<>();
    public static void start(ObjectNode activeFlowConfig) {
        RED.stop();
        RED.activeFlows = new HashMap<>();
        RED.activeFlows.put("global", Flow.create(activeFlowConfig));
        for (Iterator<String> it = activeFlowConfig.get("flows").fieldNames(); it.hasNext(); ) {
            String id = it.next();
            if (!RED.activeFlows.containsKey(id)) {
                RED.activeFlows.put(id, Flow.create(RED.activeFlows.get("global"), activeFlowConfig, (ObjectNode) activeFlowConfig.get("flows").get(id)));
            }
        }
        for (Iterator<String> it = RED.activeFlows.keySet().iterator(); it.hasNext(); ) {
            String id = it.next();
            RED.activeFlows.get(id).start();
        }
    }

    public static void stop() {
        List<String> activeFlowIds = RED.activeFlows.keySet().stream().filter(id -> !id.equals("global")).collect(Collectors.toList());
        if (RED.activeFlows.containsKey("global")) {
            activeFlowIds.add("global");
        }
        for (Iterator<String> it = activeFlowIds.iterator(); it.hasNext(); ) {
            String id = it.next();
            RED.activeFlows.get(id).stop();
        }
        RED.activeFlows = new HashMap<>();
    }
}
