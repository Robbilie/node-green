package io.github.robbilie.nodegreen.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.robbilie.nodegreen.Flow;
import io.github.robbilie.nodegreen.INode;
import io.github.robbilie.nodegreen.Node;

import java.util.HashMap;
import java.util.Map;

public class SelectThemeNode extends Node {

    public static Map<String, SelectThemeNode> nodes = new HashMap<>();
    public SelectThemeNode(Flow flow, JsonNode config) {
        super(flow, config);
        SelectThemeNode.nodes.put(this.id, this);
        this.onInput((ObjectNode msg) -> {
            msg.put("payload", "test");
            this.send(msg);
        });
        this.onClose((INode node) -> {
            System.out.println("select theme node close");
            SelectThemeNode.nodes.remove(this.id);
        });
    }
}
