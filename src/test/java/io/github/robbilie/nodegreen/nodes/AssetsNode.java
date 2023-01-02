package io.github.robbilie.nodegreen.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.robbilie.nodegreen.Flow;
import io.github.robbilie.nodegreen.Node;

import java.util.List;

public class AssetsNode extends Node {
    public List<JsonNode> assets;
    public AssetsNode(Flow flow, JsonNode config) {
        super(flow, config);
        this.assets = config.findValues("assets");
    }
}
