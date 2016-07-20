package info.smart_tools.smartactors.core.actor_receiver_creator;

import info.smart_tools.smartactors.core.actor_receiver.ActorReceiver;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.handler_routing_receiver.HandlerRoutingReceiver;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ireceiver_generator.IReceiverGenerator;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iroutable_object_creator.IRoutedObjectCreator;
import info.smart_tools.smartactors.core.iroutable_object_creator.exceptions.ObjectCreationException;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.core.iwrapper_generator.IWrapperGenerator;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link IRoutedObjectCreator}.
 * Generates {@code HandlerReceivers} and puts its to the new instance of {@link HandlerRoutingReceiver}.
 * After that puts instance of {@link HandlerRoutingReceiver} to the new instance of {@link ActorReceiver}.
 */
public class ActorReceiverCreator implements IRoutedObjectCreator {

    private FieldName name;
    private FieldName dependency;
    private FieldName wrapper;

    /**
     * Default constructor.
     * Create instance of {@link ActorReceiverCreator} and initialize private fields.
     * @throws ObjectCreationException if {@link IOC} resolution failed.
     */
    public ActorReceiverCreator()
            throws ObjectCreationException {
        try {
            this.name = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
            this.dependency = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "dependency");
            this.wrapper = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "wrapper");
        } catch (Throwable e) {
            throw new ObjectCreationException("Could not create instance of ActorReceiverCreator.");
        }
    }

    @Override
    public void createObject(final IRouter router, final IObject description)
            throws ObjectCreationException, InvalidArgumentException {
        try {
            Map<Object, IMessageReceiver> handlerReceiversMap = new HashMap<>();
            IWrapperGenerator wg = IOC.resolve(Keys.getOrAdd(IWrapperGenerator.class.getCanonicalName()));
            IReceiverGenerator rg = IOC.resolve(Keys.getOrAdd(IReceiverGenerator.class.getCanonicalName()));
            Object object = IOC.resolve(
                    Keys.getOrAdd((String) description.getValue(this.dependency)),
                    (IObject) description.getValue(this.wrapper)
            );
            for (Method m : object.getClass().getDeclaredMethods()) {
                Class wrapperInterface = m.getParameterTypes()[0];
                Object wrapper = wg.generate(wrapperInterface);
                IResolveDependencyStrategy strategy = new SingletonStrategy(wrapper);
                IMessageReceiver handlerReceiver = rg.generate(object, strategy, m.getName());
                handlerReceiversMap.put(m.getName(), handlerReceiver);
            }
            IMessageReceiver handlerRoutingReceiver = new HandlerRoutingReceiver(handlerReceiversMap);
            IMessageReceiver actorReceiver = new ActorReceiver(handlerRoutingReceiver);
            router.register((String) description.getValue(this.name) , actorReceiver);
        } catch (Throwable e) {
            throw new ObjectCreationException("Could not create receiver chain.", e);
        }
    }

}
