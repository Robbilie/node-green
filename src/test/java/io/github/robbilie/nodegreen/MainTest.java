package io.github.robbilie.nodegreen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.robbilie.nodegreen.nodes.AssetsNode;
import io.github.robbilie.nodegreen.nodes.CompositionNode;
import io.github.robbilie.nodegreen.nodes.CompositionsNode;
import io.github.robbilie.nodegreen.nodes.SelectThemeNode;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainTest {

    @Test
    void mainTest() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("flows.json").getFile());
        String json = null;
        try {
            json = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FlowsJson flowsJson = new ObjectMapper().readValue(json, FlowsJson.class);

            RED.nodes.registerType("assets", AssetsNode::new);
            RED.nodes.registerType("compositions", CompositionsNode::new);
            RED.nodes.registerType("composition", CompositionNode::new);
            RED.nodes.registerType("select_theme", SelectThemeNode::new);

            ObjectNode config = FlowUtil.parseConfig(flowsJson.flows);

            System.out.println(config);

            RED.start(config);

            assertEquals(1, SelectThemeNode.nodes.size());

            for (String id : SelectThemeNode.nodes.keySet()) {
                ObjectNode msg = new ObjectMapper().createObjectNode();
                SelectThemeNode.nodes.get(id).receive(msg);
            }

            RED.stop();

            for (String id : SelectThemeNode.nodes.keySet()) {
                ObjectNode msg = new ObjectMapper().createObjectNode();
                SelectThemeNode.nodes.get(id).receive(msg);
            }

            assertEquals(0, SelectThemeNode.nodes.size());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
