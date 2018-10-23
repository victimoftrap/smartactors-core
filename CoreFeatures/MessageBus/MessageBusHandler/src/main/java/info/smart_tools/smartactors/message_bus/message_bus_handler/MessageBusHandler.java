package info.smart_tools.smartactors.message_bus.message_bus_handler;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_handler.IMessageBusHandler;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_handler.exception.MessageBusHandlerException;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.IResponseStrategy;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageProcessorProcessException;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MessageBusHandler implements IMessageBusHandler {

    private final IFieldName replyToFieldName;
    private final IFieldName finalActionsFieldName;
    private final IFieldName responseStrategyFieldName;

    private final IQueue<ITask> taskQueue;
    private final int stackDepth;
    private final String defaultChainName;
    private final IAction<IObject> replyAction;

    private final IResponseStrategy messageBusResponseStrategy;
    private final IResponseStrategy nullResponseStrategy;
    private final IKey keyIMessageProcessingSequence;
    private final IKey keyIMessageProcessor;

    /**
     * @param taskQueue Queue of the tasks
     * @param stackDepth Stack depth of the {@link IMessageProcessor}
     * @param receiverChainName the chain for processing incoming message
     * @param finalAction the final action for
     * @throws InvalidArgumentException if there is invalid arguments
     */
    public MessageBusHandler(final IQueue<ITask> taskQueue, final int stackDepth, final String receiverChainName, final IAction<IObject> finalAction)
            throws InvalidArgumentException, ResolutionException {
        if (null == taskQueue) {
            throw new InvalidArgumentException("Task queue should not be null.");
        }
        if (stackDepth < 0) {
            throw new InvalidArgumentException("Stack depth should be positive number.");
        }
        if (null == receiverChainName) {
            throw new InvalidArgumentException("Receiver chain name should not be null.");
        }
        if (null == finalAction)  {
            throw new InvalidArgumentException("FinalAction should not be null.");
        }
        this.stackDepth = stackDepth;
        this.taskQueue = taskQueue;
        this.defaultChainName = receiverChainName;
        this.replyAction = finalAction;

        this.keyIMessageProcessingSequence = Keys.getOrAdd("info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence");
        this.keyIMessageProcessor = Keys.getOrAdd("info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor");

        this.finalActionsFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "finalActions");
        this.replyToFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "messageBusReplyTo");
        this.responseStrategyFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "responseStrategy");

        this.messageBusResponseStrategy = IOC.resolve(Keys.getOrAdd("message bus response strategy"));
        this.nullResponseStrategy = IOC.resolve(Keys.getOrAdd("null response strategy"));
    }

    @Override
    public void handle(final IObject message)
            throws MessageBusHandlerException {
        handle0(message, defaultChainName);
    }

    @Override
    public void handle(final IObject message, final Object chainName)
            throws MessageBusHandlerException {
        handle0(message, chainName);
    }

    @Override
    public void handleForReply(final IObject message, final Object replyToChainName)
            throws MessageBusHandlerException {
        handleForReply0(message, defaultChainName, replyToChainName);
    }

    @Override
    public void handleForReply(final IObject message, final Object chainName, final Object replyToChainName)
            throws MessageBusHandlerException {
        handleForReply0(message, chainName, replyToChainName);
    }

    private void handle0(final IObject message, final Object dstChainName)
            throws MessageBusHandlerException {
        try {
            resolveMessageProcessor(dstChainName, message)
                    .process(message, resolveDefaultContext());
        } catch (ResolutionException | InvalidArgumentException | ChangeValueException | MessageProcessorProcessException e) {
            throw new MessageBusHandlerException("Failed to handle message to MessageBus.", e);
        }
    }

    private void handleForReply0(final IObject message, final Object dstChainName, final Object replyToChainName)
            throws MessageBusHandlerException {
        try {
            resolveMessageProcessor(dstChainName, message)
                    .process(message, resolveReplyContext(replyToChainName));
        } catch (ResolutionException | ChangeValueException | InvalidArgumentException | MessageProcessorProcessException e) {
            throw new MessageBusHandlerException("Failed to handle message to MessageBus.", e);
        }
    }

    private IMessageProcessor resolveMessageProcessor(final Object mpChainName, IObject message) throws ResolutionException {
        IMessageProcessingSequence processingSequence = IOC.resolve(
                this.keyIMessageProcessingSequence,
                this.stackDepth,
                mpChainName,
                message
        );
        return IOC.resolve(
                this.keyIMessageProcessor,
                this.taskQueue,
                processingSequence
        );
    }

    private IObject resolveDefaultContext()
            throws InvalidArgumentException, ResolutionException, ChangeValueException {
        IObject context = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"));
        context.setValue(responseStrategyFieldName, nullResponseStrategy);
        return context;
    }

    private IObject resolveReplyContext(final Object replyToChainName)
            throws InvalidArgumentException, ResolutionException, ChangeValueException {
        IObject context = resolveDefaultContext();

        context.setValue(responseStrategyFieldName, messageBusResponseStrategy);

        List<IAction> actionList = new ArrayList<>();
        actionList.add(this.replyAction);
        context.setValue(this.finalActionsFieldName, actionList);

        context.setValue(this.replyToFieldName, replyToChainName);

        return context;
    }
}
