package io.github.robbilie.nodegreen.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.robbilie.nodegreen.Flow;
import io.github.robbilie.nodegreen.Node;
import io.github.robbilie.nodegreen.RED;

public class CompositionNode extends Node {
    public CompositionNode(Flow flow, JsonNode config) {
        super(flow, config);
        System.out.println(((CompositionsNode) RED.flows.getNode(config.get("compositions").asText())).compositions);
        this.onInput((ObjectNode msg) -> {
            System.out.println("Composition: " + msg.toString());
            msg.put("payload", "test");
            this.send(msg);
        });
    }
}
