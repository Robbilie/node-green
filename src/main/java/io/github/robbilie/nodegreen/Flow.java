package io.github.robbilie.nodegreen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

public class Flow implements IFlow {
    public static Flow create(IFlow parent, ObjectNode global, ObjectNode config) {
        return new Flow(parent, global, config);
    }
    public static Flow create(IFlow parent, ObjectNode global) {
        return new Flow(parent, global);
    }

    IFlow parent;

    ObjectNode global;
    ObjectNode flow;
    Map<String, INode> activeNodes = new HashMap<>();

    Boolean isGlobalFlow;
    public Flow(IFlow parent, ObjectNode global) {
        this.parent = parent;
        this.isGlobalFlow = true;
        this.global = global;
        this.flow = global;
    }
    public Flow(IFlow parent, ObjectNode global, ObjectNode flow) {
        this.parent = parent;
        this.isGlobalFlow = false;
        this.global = global;
        this.flow = flow;
    }

    public void start() {
        if (this.flow.has("configs")) {
            for (Iterator<String> it = this.flow.get("configs").fieldNames(); it.hasNext(); ) {
                String id = it.next();
                if (this.flow.get("configs").has(id)) {
                    JsonNode node = this.flow.get("configs").get(id);
                    if (!node.has("d")) {
                        if (!this.activeNodes.containsKey(id)) {
                            INode newNode = FlowUtil.createNode(this, node);
                            this.activeNodes.put(id, newNode);
                        }
                    }
                }
            }
        }
        if (this.flow.has("nodes")) {
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
    }

    public void send(List<SendEvent> sendEvents) {
        Flow.handleOnSend(this, sendEvents);
    }

    public static void handleOnSend(Flow flow, List<SendEvent> sendEvents) {
        for (SendEvent sendEvent : sendEvents) {
            Flow.handlePreRoute(flow, sendEvent);
        }
    }

    public static void handlePreRoute(Flow flow, SendEvent sendEvent) {
        sendEvent.setDestinationNode(flow.getNode(sendEvent.getDestinationId()));
        if (sendEvent.getDestinationNode() != null) {
            if (sendEvent.shouldCloneMessage()) {
                sendEvent.setMsg(sendEvent.getMsg().deepCopy());
            }
            Flow.handlePreDeliver(flow, sendEvent);
        }
    }

    public static void handlePreDeliver(Flow flow, SendEvent sendEvent) {
        if (sendEvent.getDestinationNode() != null) {
            sendEvent.getDestinationNode().receive(sendEvent.getMsg());
        }
    }

    public INode getNode(String id) {
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
        } else {
            return this.parent.getNode(id);
        }
    }

    public void stop() {
        this.stop(this.activeNodes.keySet(), new HashSet<String>());
    }

    public void stop(Set<String> stopList, Set<String> removedList) {
        Map<String, Boolean> removedMap = new HashMap<>();
        removedList.forEach(id -> {
            removedMap.put(id, true);
        });
        List<String> nodesToStop = new ArrayList<>();
        List<String> configsToStop = new ArrayList<>();
        stopList.forEach(id -> {
            if (this.flow.has("configs") && this.flow.get("configs").has(id)) {
                configsToStop.add(id);
            } else {
                nodesToStop.add(id);
            }
        });
        List<String> newStopList = new ArrayList<>();
        newStopList.addAll(nodesToStop);
        newStopList.addAll(configsToStop);

        newStopList.forEach(id -> {
            if (this.activeNodes.containsKey(id)) {
                INode node = this.activeNodes.get(id);
                this.activeNodes.remove(id);
                Boolean removed = removedMap.get(id);
                Flow.stopNode(node, removed);
            }
        });
    }

    public Map<String, INode> getActiveNodes() {
        return this.activeNodes;
    }

    public static void stopNode(INode node, Boolean removed) {
        node.close(removed);
    }

}
