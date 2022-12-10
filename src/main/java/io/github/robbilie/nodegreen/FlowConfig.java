package io.github.robbilie.nodegreen;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowConfig {
    Map<String, ObjectNode> allNodes = new HashMap<>();
    Map<String, ObjectNode> subflows = new HashMap<>();
    Map<String, ObjectNode> configs = new HashMap<>();
    Map<String, ObjectNode> flows = new HashMap<>();
    Map<String, ObjectNode> groups = new HashMap<>();
    List<String> missingTypes = new ArrayList<>();
}
