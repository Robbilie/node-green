package io.github.robbilie.nodegreen;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class SendEvent {
    public ObjectNode getMsg() {
        return msg;
    }

    public void setMsg(ObjectNode msg) {
        this.msg = msg;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public INode getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(INode sourceNode) {
        this.sourceNode = sourceNode;
    }

    public Integer getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(Integer sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public INode getDestinationNode() {
        return destinationNode;
    }

    public void setDestinationNode(INode destinationNode) {
        this.destinationNode = destinationNode;
    }

    public boolean shouldCloneMessage() {
        return cloneMessage;
    }

    public void setCloneMessage(boolean cloneMessage) {
        this.cloneMessage = cloneMessage;
    }

    ObjectNode msg;
    String sourceId;
    INode sourceNode;
    Integer sourcePort;
    String destinationId;
    INode destinationNode;
    boolean cloneMessage;

    public SendEvent(ObjectNode msg, String sourceId, INode sourceNode, Integer sourcePort, String destinationId, INode destinationNode, boolean cloneMessage) {
        this.msg = msg;
        this.sourceId = sourceId;
        this.sourceNode = sourceNode;
        this.sourcePort = sourcePort;
        this.destinationId = destinationId;
        this.destinationNode = destinationNode;
        this.cloneMessage = cloneMessage;
    }
}
