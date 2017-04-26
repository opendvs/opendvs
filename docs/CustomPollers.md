# Implementing custom Pollers

To implement custom Pollers (e.g. PyPi repository), you need to create implementation of `me.raska.opendvs.base.poller.NativePoller` interface, which is specified by following definition:
```
import java.util.function.Consumer;

public interface NativePoller {
    String getId();
    void process(PollerAction action, Consumer<PollerAction> callback);
}
```


## getId()
Should return unique identifier of the poller.

## process(PollerAction, Consumer<PollerAction>)
This method should do all the necessary tasks for identifying component metadata and trigger consumer with updated PollerAction with recent changes (e.g. don't include already consumed components). If unhandled exception occurs, PollerAction will be redirected into DLX from where can be transfered for reprocessing.

Common usage is to trigger NativePoller only where PollerAction's `filter` starts with `<Poller ID>:` string. There is special usage with `*` character. This should be used in case it's resource-friendlier to fetch all possible metadata (e.g. single database file), otherwise it's not recommended to crawl through the repository to achieve expected behaviour.
