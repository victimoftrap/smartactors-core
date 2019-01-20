package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_self;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

/**
 * Strategy for converting from class to itself
 */
public class ClassToClassResolutionStrategy implements IResolutionStrategy {

    @Override
    public <T> T resolve(final Object... args) throws ResolutionStrategyException {
            return (T) args[0];
    }
}
