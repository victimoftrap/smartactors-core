package info.smart_tools.smartactors.actors.authentication;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class, AuthenticationActorPlugin.class})
public class AuthenticationActorPluginTest {

    private IBootstrap<IBootstrapItem<String>> bootstrap;
    private AuthenticationActorPlugin targetPlugin;

    @Before
    public void before() {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        bootstrap = mock(IBootstrap.class);

        targetPlugin = new AuthenticationActorPlugin(bootstrap);
    }

    @Test
    public void MustCorrectLoad() throws Exception {
        IKey cachedCollectionKey = mock(IKey.class);
        when(Keys.getOrAdd(AuthenticationActor.class.toString())).thenReturn(cachedCollectionKey);

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("AuthenticationActorPlugin").thenReturn(item);

        targetPlugin.load();

        verifyStatic();
        Keys.getOrAdd(AuthenticationActor.class.toString());

        verifyNew(BootstrapItem.class).withArguments("AuthenticationActorPlugin");
        verify(item).process(any());
        verify(bootstrap).add(item);
    }

    @Test
    public void MustInCorrectLoadWhenKeysThrowException() throws ResolutionException {
        when(Keys.getOrAdd(AuthenticationActor.class.toString())).thenThrow(new ResolutionException(""));

        try {
            targetPlugin.load();
        } catch (PluginException e) {

            verifyStatic();
            Keys.getOrAdd(AuthenticationActor.class.toString());
            return;
        }
        assertTrue("Must throw exception, but was not", false);
    }

    @Test
    public void MustInCorrectLoadNewIBootstrapItemThrowException() throws Exception {
        IKey cachedCollectionKey = mock(IKey.class);
        when(Keys.getOrAdd(AuthenticationActor.class.toString())).thenReturn(cachedCollectionKey);

        whenNew(BootstrapItem.class).withArguments("AuthenticationActorPlugin").thenThrow(new InvalidArgumentException(""));

        try {
            targetPlugin.load();
        } catch (PluginException e) {

            verifyStatic();
            Keys.getOrAdd(AuthenticationActor.class.toString());

            verifyNew(BootstrapItem.class).withArguments("AuthenticationActorPlugin");
            return;
        }
        assertTrue("Must throw exception, but was not", false);
    }
}