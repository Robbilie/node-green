package io.github.robbilie.nodegreen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.function.Consumer;

public class Node implements INode {
    JsonNode config;
    public String id;
    String type;
    String z;
    String g;
    List<Consumer<ObjectNode>> inputCallbacks = new ArrayList<>();
    List<Consumer<INode>> closeCallbacks = new ArrayList<>();
    String name;
    Flow flow;
    List<List<String>> wires = new ArrayList<>();
    Integer wireCount = 0;
    public Node(Flow flow, JsonNode config) {
        this.config = config;
        this.id = config.get("id").asText();
        this.type = config.get("type").asText();
        if (config.has("z")) {
            this.z = config.get("z").asText();
        } else {
            this.z = "global";
        }
        if (config.has("g")) {
            this.g = config.get("g").asText();
        }
        if (config.has("name")) {
            this.name = config.get("name").asText();
        }
        this.flow = flow;
        if (config.has("wires")) {
            this.updateWires(config.get("wires"));
        }
    }

    void updateWires(JsonNode wires) {
        this.wires = new ArrayList<>();
        this.wireCount = 0;
        for (JsonNode wire : wires) {
            List<String> links = new ArrayList<>();
            for (JsonNode link : wire) {
                links.add(link.asText());
                this.wireCount++;
            }
            this.wires.add(links);
        }
    }

    public void onInput(Consumer<ObjectNode> callback) {
        this.inputCallbacks.add(callback);
    }

    public void onClose(Consumer<INode> callback) {
        this.closeCallbacks.add(callback);
    }

    void emitInput(ObjectNode arg) {
        int c = this.inputCallbacks.size();
        for (Consumer<ObjectNode> cb : this.inputCallbacks) {
            cb.accept(arg);
        }
    }

    void removeInputListener(Consumer<JsonNode> callback) {
        this.inputCallbacks.remove(callback);
    }

    void removeCloseListener(Consumer<JsonNode> callback) {
        this.closeCallbacks.remove(callback);
    }

    void removeAllInputListeners() {
        this.inputCallbacks = new ArrayList<>();
    }

    void removeAllCloseListeners() {
        this.closeCallbacks = new ArrayList<>();
    }

    public void send(ObjectNode msg) {
        this.send(Collections.singletonList(msg));
    }

    public void send(List<ObjectNode> msgs) {
        boolean msgSent = false;
        List<SendEvent> sendEvents = new ArrayList<>();
        int numOutputs = this.wires.size();
        for (int i = 0; i < numOutputs; i++) {
            List<String> wires = this.wires.get(i);
            if (i < msgs.size()) {
                ObjectNode msg = msgs.get(i);
                for (String wire : wires) {
                    sendEvents.add(new SendEvent(msg, this.id, this, i, wire, null, msgSent));
                    msgSent = true;
                }
            }
        }
        this.flow.send(sendEvents);
    }

    public void receive(ObjectNode msg) {
        this.emitInput(msg);
    }

    public void close() {
        this.close(false);
    }

    public void close(Boolean removed) {
        for (Consumer<INode> callback : this.closeCallbacks) {
            callback.accept(this);
        }
        this.removeAllInputListeners();
    }

}
