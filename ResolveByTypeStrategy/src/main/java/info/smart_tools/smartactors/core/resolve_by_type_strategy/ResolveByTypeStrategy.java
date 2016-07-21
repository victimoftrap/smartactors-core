package info.smart_tools.smartactors.core.resolve_by_type_strategy;

import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Strategy for resolving by type
 */
public class ResolveByTypeStrategy implements IResolveDependencyStrategy {
    /**
     * Specific strategies for resolve
     */
    private ConcurrentMap<IKey, IResolveDependencyStrategy> resolveStrategies;

    /**
     * Default constructor
     */
    public ResolveByTypeStrategy() {
        this.resolveStrategies = new ConcurrentHashMap<>();
    }

    /**
     * Put strategy for specific output type
     * @param key the key for output type
     * @param strategy the strategy for specific output type
     */
    public void register(final IKey key, final IResolveDependencyStrategy strategy) {
        resolveStrategies.put(key, strategy);
    }

    @Override
    public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {
        try {
            IResolveDependencyStrategy strategy = resolveStrategies.get(Keys.getOrAdd(args[0].getClass().getCanonicalName()));
            Object result = strategy.resolve(args[0]);
            return (T) result;
        } catch (Exception e) {
            throw new ResolveDependencyStrategyException("Object resolution failed.", e);
        }
    }
}
