package io.github.robbilie.nodegreen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Flow {
    public static Flow create(FlowConfig global, ObjectNode config) {
        return new Flow(global, config);
    }

    FlowConfig global;
    ObjectNode flow;
    Map<String, INode> activeNodes = new HashMap<>();
    public Flow(FlowConfig global, ObjectNode flow) {
        this.global = global;
        this.flow = flow;
    }

    public void start() {
        for (Iterator<String> it = this.flow.get("nodes").fieldNames(); it.hasNext(); ) {
            String id = it.next();
            if (this.flow.get("nodes").has(id)) {
                JsonNode node = this.flow.get("nodes").get(id);
                if (!node.has("d")) {
                    if (!node.has("subflow")) {
                        if (!this.activeNodes.containsKey(id)) {
                            INode newNode = FlowUtil.createNode(this, node);
                            this.activeNodes.put(id, newNode);
                        }
                    }
                }
            }

        }
    }

    public void send(List<SendEvent> sendEvents) {
        this.handleOnSend(this, sendEvents);
    }

    void handleOnSend(Flow flow, List<SendEvent> sendEvents) {
        for (int i = 0; i < sendEvents.size(); i++) {
            this.handlePreRoute(flow, sendEvents.get(i));
        }
    }

    void handlePreRoute(Flow flow, SendEvent sendEvent) {
        sendEvent.setDestinationNode(flow.getNode(sendEvent.getDestinationId()));
        if (sendEvent.getDestinationNode() != null) {
            if (sendEvent.shouldCloneMessage()) {
                sendEvent.setMsg(sendEvent.getMsg().deepCopy());
            }
            this.handlePreDeliver(flow, sendEvent);
        }
    }

    void handlePreDeliver(Flow flow, SendEvent sendEvent) {
        if (sendEvent.getDestinationNode() != null) {
            sendEvent.getDestinationNode().receive(sendEvent.getMsg());
        }
    }

    INode getNode(String id) {
        if (id == null) {
            return null;
        }
        if (this.flow.has("configs") &&
                this.flow.get("configs").has(id) ||
                (this.flow.has("nodes") &&
                        this.flow.get("nodes").has(id) &&
                        this.flow.get("nodes").get(id).has("type") &&
                        !this.flow.get("nodes").get(id).get("type").asText().startsWith("subflow:"))) {
            return this.activeNodes.get(id);
        } else if (this.activeNodes.containsKey(id)) {
            return this.activeNodes.get(id);
        }
        return null;
    }

}
