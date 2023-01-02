package io.github.robbilie.nodegreen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

public class Flows implements IFlow {

    public Map<String,Flow> activeFlows = new HashMap<>();
    public List<String> activeFlowIds = new ArrayList<>();
    ObjectNode activeFlowConfig;
    Map<String, String> activeNodesToFlow = new HashMap<>();

    public void setFlows(List<JsonNode> flows) {
        this.activeFlowConfig = FlowUtil.parseConfig(flows);
    }
    public void startFlows() {
        this.stop();
        this.activeFlows = new HashMap<>();
        this.activeFlowIds = new ArrayList<>();
        this.activeNodesToFlow = new HashMap<>();
        Flow global = Flow.create(this, this.activeFlowConfig);
        this.activeFlows.put("global", global);
        this.activeFlowIds.add("global");
        for (Iterator<String> it = this.activeFlowConfig.get("flows").fieldNames(); it.hasNext(); ) {
            String id = it.next();
            if (!this.activeFlows.containsKey(id)) {
                this.activeFlows.put(id, Flow.create(this, this.activeFlowConfig, (ObjectNode) this.activeFlowConfig.get("flows").get(id)));
                this.activeFlowIds.add(id);
            }
        }
        for (String id : this.activeFlowIds) {
            this.activeFlows.get(id).start();
            Map<String, INode> activeNodes = this.activeFlows.get(id).getActiveNodes();
            for(String nid : activeNodes.keySet()) {
                this.activeNodesToFlow.put(nid, id);
            }
        }
    }

    public void stop() {
        List<String> reverseActiveFlowIds = this.activeFlowIds.subList(0, this.activeFlowIds.size());
        Collections.reverse(reverseActiveFlowIds);
        for (String id : reverseActiveFlowIds) {
            this.activeFlows.get(id).stop();
        }
        this.activeFlows = new HashMap<>();
        this.activeFlowIds = new ArrayList<>();
        this.activeNodesToFlow = new HashMap<>();
    }

    public INode getNode(String id) {
        if (this.activeNodesToFlow.containsKey(id) && this.activeFlows.containsKey(this.activeNodesToFlow.get(id))) {
            return this.activeFlows.get(this.activeNodesToFlow.get(id)).getNode(id);
        }
        for (String flowId : this.activeFlows.keySet()) {
            INode node = activeFlows.get(flowId).getNode(id);
            if (node != null) {
                return node;
            }
        }
        return null;
    }
}
