package io.github.robbilie.nodegreen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlowUtil {
    private static final Pattern subflowInstanceRE = Pattern.compile("^subflow:(.+)$");

    public static FlowConfig parseConfig(List<JsonNode> input) throws JsonProcessingException {
        FlowConfig flow = new FlowConfig();
        flow.allNodes = new HashMap<>();
        flow.subflows = new HashMap<>();
        flow.configs = new HashMap<>();
        flow.flows = new HashMap<>();
        flow.groups = new HashMap<>();
        flow.missingTypes = new ArrayList<>();

        List<ObjectNode> config = new ArrayList<>();
        for (JsonNode n : input) {
            config.add(n.deepCopy());
        }

        for (ObjectNode n : config) {
            flow.allNodes.put(n.get("id").asText(), n.deepCopy());
            if (n.get("type").asText().equals("tab")) {
                flow.flows.put(n.get("id").asText(), n);
                flow.flows.get(n.get("id").asText()).putObject("subflows");
                flow.flows.get(n.get("id").asText()).putObject("configs");
                flow.flows.get(n.get("id").asText()).putObject("nodes");
            }
            if (n.get("type").asText().equals("group")) {
                flow.groups.put(n.get("id").asText(), n);
            }
        }

        for (ObjectNode n : config) {
            if (n.get("type").asText().equals("subflow")) {
                flow.subflows.put(n.get("id").asText(), n);
                flow.subflows.get(n.get("id").asText()).putObject("configs");
                flow.subflows.get(n.get("id").asText()).putObject("nodes");
                flow.subflows.get(n.get("id").asText()).putArray("instances");
            }
        }

        Map<String, Map<String, Boolean>> linkWires = new HashMap<>();
        List<ObjectNode> linkOutNodes = new ArrayList<>();
        for (ObjectNode n : config) {
            if (!n.get("type").asText().equals("subflow") && !n.get("type").asText().equals("tab") && !n.get("type").asText().equals("group")) {
                Matcher subflowDetails = subflowInstanceRE.matcher(n.get("type").asText());

                if ((subflowDetails.matches() && !flow.subflows.containsKey(subflowDetails.group(1))) ||
                        (!subflowDetails.matches() && !RED.nodes.containsKey(n.get("type").asText()))) {
                    if (!flow.missingTypes.contains(n.get("type").asText())) {
                        flow.missingTypes.add(n.get("type").asText());
                    }
                }
                ObjectNode container = null;
                if (flow.flows.containsKey(n.get("z").asText())) {
                    container= flow.flows.get(n.get("z").asText());
                } else if (flow.subflows.containsKey(n.get("z").asText())) {
                    container = flow.subflows.get(n.get("z").asText());
                }
                if (n.has("x") && n.has("y")) {
                    if (subflowDetails.matches()) {
                        String subflowType = subflowDetails.group(1);
                        n.put("subflow", subflowType);
                        ((ArrayNode) flow.subflows.get(subflowType).get("instances")).add(n);
                    }
                    if (container != null) {
                        ((ObjectNode) container.get("nodes")).put(n.get("id").asText(), n);
                    }
                } else {
                    if (container != null) {
                        ((ObjectNode) container.get("configs")).put(n.get("id").asText(), n);
                    } else {
                        flow.configs.put(n.get("id").asText(), n);
                        flow.configs.get(n.get("id").asText()).putArray("_users");
                    }
                }
                if (n.get("type").asText().equals("link in") && n.has("links")) {
                    // Ensure wires are present in corresponding link out nodes
                    for (JsonNode id : n.get("links")) {
                        linkWires.put(id.asText(), linkWires.getOrDefault(id.asText(), new HashMap<>()));
                        linkWires.get(id.asText()).put(n.get("id").asText(), true);
                    }
                } else if (n.get("type").asText().equals("link out") && n.has("links")) {
                    linkWires.put(n.get("id").asText(), linkWires.getOrDefault(n.get("id").asText(), new HashMap<>()));
                    for (JsonNode id : n.get("links")) {
                        linkWires.get(n.get("id").asText()).put(id.asText(), true);
                    }
                    linkOutNodes.add(n);
                }
            }
        }
        for (ObjectNode n : linkOutNodes) {
            Map<String, Boolean> links = linkWires.get(n.get("id").asText());
            List<String> targets = new ArrayList<>(links.keySet());
            n.put("wires", new ObjectMapper().valueToTree(targets));
        }

        Map<String, JsonNode> addedTabs = new HashMap<>();
        for (ObjectNode n : config) {
            if (!n.get("type").asText().equals("subflow") && !n.get("type").asText().equals("tab") && !n.get("type").asText().equals("group")) {
                /*for (JsonNode prop : n) {
                    if (n.hasOwnProperty(prop) && prop !== 'id' && prop !== 'wires' && prop !== 'type' && prop !== '_users' && flow.configs.hasOwnProperty(n[prop])) {
                        // This property references a global config node
                        flow.configs[n[prop]]._users.push(n.id)
                    }
                }*/
                if (n.has("z") && !flow.subflows.containsKey(n.get("z").asText())) {

                    if (!flow.flows.containsKey(n.get("z").asText())) {
                        ObjectNode tab = new ObjectMapper().createObjectNode();
                        tab.put("type", "tab");
                        tab.put("id", n.get("z").asText());
                        tab.putObject("subflows");
                        tab.putObject("configs");
                        tab.putObject("nodes");
                        flow.flows.put(n.get("z").asText(), tab);
                        addedTabs.put(n.get("z").asText(), tab);
                    }
                    if (addedTabs.containsKey(n.get("z").asText())) {
                        if (n.has("x") && n.has("y")) {
                            ((ObjectNode) addedTabs.get(n.get("z").asText()).get("nodes")).put(n.get("id").asText(), n);
                        } else {
                            ((ObjectNode) addedTabs.get(n.get("z").asText()).get("configs")).put(n.get("id").asText(), n);
                        }
                    }
                }
            }
        }

        return flow;
    }

    public static INode createNode(Flow flow, JsonNode config) {
        String type = config.get("type").asText();
        if (!RED.nodes.registry.containsKey(type)) {
            return null;
        }
        return RED.nodes.registry.get(type).apply(flow, config);
    }
}
