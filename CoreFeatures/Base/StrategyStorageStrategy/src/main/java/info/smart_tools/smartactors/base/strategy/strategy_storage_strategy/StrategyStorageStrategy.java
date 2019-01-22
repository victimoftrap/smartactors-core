package info.smart_tools.smartactors.base.strategy.strategy_storage_strategy;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.IRegistrationStrategy;
import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.exception.RegistrationStrategyException;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunctionTwoArgs;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.interfaces.istrategy.exception.StrategyException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Strategy to storage some specific strategies united by a common purpose
 */
public class StrategyStorageStrategy implements IStrategy, IRegistrationStrategy {

    /**
     * Strategy storage
     */
    private ConcurrentMap<Object, IStrategy> strategyStorage;
    private IFunction argToKeyFunction;
    private IFunctionTwoArgs findValueByArgumentFunction;


    /**
     * @param argToKeyFunction the function that transforms argument to a map key
     * @param findValueByArgumentFunction the function that finds value by given argument in the given map
     */
    public StrategyStorageStrategy(final IFunction argToKeyFunction, final IFunctionTwoArgs findValueByArgumentFunction) {
        this.strategyStorage = new ConcurrentHashMap<>();
        this.argToKeyFunction = argToKeyFunction;
        this.findValueByArgumentFunction = findValueByArgumentFunction;
    }

    @Override
    public <T> T resolve(Object... args) throws StrategyException {
        try {
            IStrategy strategy = (IStrategy) this.findValueByArgumentFunction.execute(this.strategyStorage, args[0]);

            return null == strategy ? null : strategy.resolve(args);
        } catch (Exception e) {
            throw new StrategyException("Object resolution failed.", e);
        }
    }

    @Override
    public void register(Object arg, IStrategy value)
            throws RegistrationStrategyException {
        try {
            this.strategyStorage.put(this.argToKeyFunction.execute(arg), value);
        } catch (InvalidArgumentException | FunctionExecutionException e) {
            throw new RegistrationStrategyException(e);
        }
    }

    @Override
    public void unregister(Object arg)
            throws RegistrationStrategyException {
        try {
            this.strategyStorage.remove(this.argToKeyFunction.execute(arg));
        } catch (FunctionExecutionException | InvalidArgumentException e) {
            throw new RegistrationStrategyException(e);
        }
    }
}
