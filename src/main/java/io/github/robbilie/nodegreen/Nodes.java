package io.github.robbilie.nodegreen;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Nodes {
    public void registerType(String name, BiFunction<Flow, JsonNode, INode> fn) {
        this.registry.put(name, fn);
    }

    public Map<String, BiFunction<Flow, JsonNode, INode>> registry = new HashMap<>();

    public boolean containsKey(String type) {
        return registry.containsKey(type);
    }
}
