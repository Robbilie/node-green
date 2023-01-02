package io.github.robbilie.nodegreen.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.robbilie.nodegreen.Flow;
import io.github.robbilie.nodegreen.Node;

import java.util.List;

public class CompositionsNode extends Node {
    public List<JsonNode> compositions;
    public CompositionsNode(Flow flow, JsonNode config) {
        super(flow, config);
        this.compositions = config.findValues("compositions");
    }
}
