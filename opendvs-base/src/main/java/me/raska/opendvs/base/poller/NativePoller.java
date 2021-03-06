package me.raska.opendvs.base.poller;

import java.util.function.Consumer;

import me.raska.opendvs.base.model.poller.PollerAction;

public interface NativePoller {
    String getId();
    void process(PollerAction action, Consumer<PollerAction> callback);
}
