package io.github.robbilie.nodegreen.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.robbilie.nodegreen.Context;
import io.github.robbilie.nodegreen.Flow;
import io.github.robbilie.nodegreen.Node;

public class CompositionNode extends Node {
    public CompositionNode(Flow flow, JsonNode config) {
        super(flow, config);
        this.onInput((ObjectNode msg) -> {
            Context context = this.getContext();
            System.out.println("Composition: " + msg.toString());
            msg.put("payload", "test");
            this.send(msg);
        });
    }
}
