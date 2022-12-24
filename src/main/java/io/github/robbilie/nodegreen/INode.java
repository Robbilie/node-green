package io.github.robbilie.nodegreen;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.function.Consumer;

public interface INode {
    void send(ObjectNode msg);
    void send(List<ObjectNode> msgs);
    void receive(ObjectNode msg);
    void onInput(Consumer<ObjectNode> callback);
    void onClose(Consumer<INode> callback);
    Context getContext();
    void close();
    void close(Boolean removed);
}
