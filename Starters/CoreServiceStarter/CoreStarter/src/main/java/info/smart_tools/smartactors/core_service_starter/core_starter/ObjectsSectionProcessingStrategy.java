package info.smart_tools.smartactors.core_service_starter.core_starter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.message_processing_interfaces.irouter.IRouter;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.object_creation_interfaces.IReceiverObjectCreator;
import info.smart_tools.smartactors.message_processing_interfaces.object_creation_interfaces.IReceiverObjectListener;
import info.smart_tools.smartactors.message_processing_interfaces.object_creation_interfaces.exeptions.InvalidReceiverPipelineException;
import info.smart_tools.smartactors.message_processing_interfaces.object_creation_interfaces.exeptions.ReceiverObjectCreatorException;
import info.smart_tools.smartactors.message_processing_interfaces.object_creation_interfaces.exeptions.ReceiverObjectListenerException;

import java.util.List;
import java.util.ListIterator;

/**
 * Creates objects using configuration.
 *
 * Expects the following configuration format:
 *
 * <pre>
 *     {
 *         "objects": [
 *             {
 *                 "filters": [
 *                      {
 *                          "dependency": "  . . .  ",
 *                          . . .
 *                      },
 *                      . . .
 *                 ]
 *                 // . . .
 *             },
 *             {
 *                 // . . .
 *             }
 *         ]
 *     }
 * </pre>
 */
public class ObjectsSectionProcessingStrategy implements ISectionStrategy {
    private final IFieldName name;

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public ObjectsSectionProcessingStrategy()
            throws ResolutionException {
        this.name = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "objects");
    }

    @Override
    public void onLoadConfig(final IObject config) throws ConfigurationProcessingException {
        try {
            List<IObject> section = (List<IObject>) config.getValue(name);

            for (IObject objDesc : section) {
                IReceiverObjectListener listener = IOC.resolve(Keys.getOrAdd("global router registration receiver object listener"));
                IReceiverObjectCreator creator = IOC.resolve(Keys.getOrAdd("full receiver object creator"), objDesc);
                IObject context = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"));
                creator.create(listener, objDesc, context);
            }
        } catch (InvalidReceiverPipelineException | ResolutionException | InvalidArgumentException | ReadValueException
                | ReceiverObjectCreatorException | ReceiverObjectListenerException e) {
            throw new ConfigurationProcessingException("Error occurred loading \"objects\" configuration section.", e);
        }
    }

    @Override
    public void onRevertConfig(final IObject config) throws ConfigurationProcessingException {
        try {
            IRouter router = IOC.resolve(Keys.getOrAdd(IRouter.class.getCanonicalName()));
            IFieldName objectNameFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "name");
            List<IObject> section = (List<IObject>) config.getValue(name);
            ListIterator<IObject> sectionIterator = section.listIterator(section.size());
            Object objectName;
            IObject objDesc;

            while (sectionIterator.hasPrevious()) {
                objDesc = sectionIterator.previous();
                objectName = objDesc.getValue(objectNameFieldName);
                router.deregister(objectName);
            }
        } catch ( ResolutionException | InvalidArgumentException | ReadValueException e) {
            throw new ConfigurationProcessingException("Error occurred reverting \"objects\" configuration section.", e);
        }
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
