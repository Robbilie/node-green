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

    public static ObjectNode parseConfig(List<JsonNode> input) throws JsonProcessingException {
        ObjectNode flow = new ObjectMapper().createObjectNode();
        flow.putObject("allNodes");
        flow.putObject("subflows");
        flow.putObject("configs");
        flow.putObject("flows");
        flow.putObject("groups");
        //flow.putArray("missingTypes");

        List<ObjectNode> config = new ArrayList<>();
        for (JsonNode n : input) {
            config.add(n.deepCopy());
        }

        for (ObjectNode n : config) {
            ((ObjectNode)flow.get("allNodes")).put(n.get("id").asText(), n.deepCopy());
            if (n.get("type").asText().equals("tab")) {
                ((ObjectNode)flow.get("flows")).put(n.get("id").asText(), n);
                ((ObjectNode)flow.get("flows").get(n.get("id").asText())).putObject("subflows");
                ((ObjectNode)flow.get("flows").get(n.get("id").asText())).putObject("configs");
                ((ObjectNode)flow.get("flows").get(n.get("id").asText())).putObject("nodes");
            }
            if (n.get("type").asText().equals("group")) {
                ((ObjectNode)flow.get("groups")).put(n.get("id").asText(), n);
            }
        }

        for (ObjectNode n : config) {
            if (n.get("type").asText().equals("subflow")) {
                ((ObjectNode)flow.get("subflows")).put(n.get("id").asText(), n);
                ((ObjectNode)flow.get("subflows").get(n.get("id").asText())).putObject("configs");
                ((ObjectNode)flow.get("subflows").get(n.get("id").asText())).putObject("nodes");
                ((ObjectNode)flow.get("subflows").get(n.get("id").asText())).putArray("instances");
            }
        }

        Map<String, Map<String, Boolean>> linkWires = new HashMap<>();
        List<ObjectNode> linkOutNodes = new ArrayList<>();
        for (ObjectNode n : config) {
            if (!n.get("type").asText().equals("subflow") && !n.get("type").asText().equals("tab") && !n.get("type").asText().equals("group")) {
                Matcher subflowDetails = subflowInstanceRE.matcher(n.get("type").asText());

                if ((subflowDetails.matches() && !flow.get("subflows").has(subflowDetails.group(1))) ||
                        (!subflowDetails.matches() && !RED.nodes.containsKey(n.get("type").asText()))) {
                    //if (!((ArrayNode)flow.get("missingTypes")).to.contains(n.get("type").asText())) {
                    //    flow.missingTypes.add(n.get("type").asText());
                    //}
                }
                ObjectNode container = null;
                if (n.has("z")) {
                    if (flow.get("flows").has(n.get("z").asText())) {
                        container = (ObjectNode)flow.get("flows").get(n.get("z").asText());
                    } else if (flow.get("subflows").has(n.get("z").asText())) {
                        container = (ObjectNode)flow.get("subflows").get(n.get("z").asText());
                    }
                }
                if (n.has("x") && n.has("y")) {
                    if (subflowDetails.matches()) {
                        String subflowType = subflowDetails.group(1);
                        n.put("subflow", subflowType);
                        ((ArrayNode) flow.get("subflows").get(subflowType).get("instances")).add(n);
                    }
                    if (container != null) {
                        ((ObjectNode) container.get("nodes")).set(n.get("id").asText(), n);
                    }
                } else {
                    if (container != null) {
                        ((ObjectNode) container.get("configs")).set(n.get("id").asText(), n);
                    } else {
                        ((ObjectNode)flow.get("configs")).put(n.get("id").asText(), n);
                        ((ObjectNode)(flow.get("configs")).get(n.get("id").asText())).putArray("_users");
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
            n.set("wires", new ObjectMapper().valueToTree(targets));
        }

        Map<String, JsonNode> addedTabs = new HashMap<>();
        for (ObjectNode n : config) {
            if (!n.get("type").asText().equals("subflow") && !n.get("type").asText().equals("tab") && !n.get("type").asText().equals("group")) {
                /*for (JsonNode prop : n) {
                    if (n.hasOwnProperty(prop) && prop !== 'id' && prop !== 'wires' && prop !== 'type' && prop !== '_users' && ((ObjectNode)flow.get("configs")).hasOwnProperty(n[prop])) {
                        // This property references a global config node
                        ((ObjectNode)flow.get("configs"))[n[prop]]._users.push(n.id)
                    }
                }*/
                if (n.has("z") && !((ObjectNode)flow.get("subflows")).has(n.get("z").asText())) {

                    if (!((ObjectNode)flow.get("flows")).has(n.get("z").asText())) {
                        ObjectNode tab = new ObjectMapper().createObjectNode();
                        tab.put("type", "tab");
                        tab.put("id", n.get("z").asText());
                        tab.putObject("subflows");
                        tab.putObject("configs");
                        tab.putObject("nodes");
                        ((ObjectNode)flow.get("flows")).put(n.get("z").asText(), tab);
                        addedTabs.put(n.get("z").asText(), tab);
                    }
                    if (addedTabs.containsKey(n.get("z").asText())) {
                        if (n.has("x") && n.has("y")) {
                            ((ObjectNode) addedTabs.get(n.get("z").asText()).get("nodes")).set(n.get("id").asText(), n);
                        } else {
                            ((ObjectNode) addedTabs.get(n.get("z").asText()).get("configs")).set(n.get("id").asText(), n);
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
