package info.smart_tools.smartactors.message_processing.actor_receiver_creator;

import info.smart_tools.smartactors.message_processing_interfaces.irouter.IRouter;
import info.smart_tools.smartactors.message_processing_interfaces.irouter.exceptions.RouteNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActorInnerRouter implements IRouter {

    Map<Object, IMessageReceiver> map = new HashMap<>();

    @Override
    public IMessageReceiver route(Object targetId) throws RouteNotFoundException {
        return this.map.get(targetId);
    }

    @Override
    public void register(Object targetId, IMessageReceiver receiver) {
        this.map.put(targetId, receiver);
    }

    @Override
    public void deregister(Object targetId) {
        IMessageReceiver receiver = map.remove(targetId);

        if (null != receiver) {
            receiver.dispose();
        }
    }

    @Override
    public List<Object> enumerate() {
        return new ArrayList<>(map.keySet());
    }
}
