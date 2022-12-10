package io.github.robbilie.nodegreen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.robbilie.nodegreen.nodes.CompositionNode;
import io.github.robbilie.nodegreen.nodes.SelectThemeNode;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MainTest {

    @Test
    void mainTest() {
        String json = "[{\"x\":0,\"y\":0,\"z\":\"global\",\"id\":\"1234\",\"type\":\"select_theme\",\"wires\":[[\"5678\"]]},{\"x\":0,\"y\":0,\"z\":\"global\",\"id\":\"5678\",\"type\":\"composition\",\"composition\":\"007\",\"wires\":[]}]";

        try {
            List<JsonNode> config = new ObjectMapper().readValue(json, new TypeReference<List<JsonNode>>() {});

            RED.nodes.registerType("select_theme", SelectThemeNode::new);
            RED.nodes.registerType("composition", CompositionNode::new);

            FlowConfig fc = FlowUtil.parseConfig(config);

            System.out.println(fc);

            Flow flow = Flow.create(fc, fc.flows.get("global"));
            flow.start();


            for (String id : SelectThemeNode.nodes.keySet()) {
                ObjectNode msg = new ObjectMapper().createObjectNode();
                SelectThemeNode.nodes.get(id).receive(msg);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
