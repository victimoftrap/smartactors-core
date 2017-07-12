package info.smart_tools.smartactors.notifier.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import org.junit.Before;

/**
 * Tests for ExceptionFormatter
 */
public class NotifierLayoutTest {

    private Layout<ILoggingEvent> layout;

    @Before
    public void setUp() {
        layout = new NotifierLayout();
    }

// TODO
//    @Test
//    public void testNullMessage() {
//        ILoggingEvent event = new LoggingEvent();
//        assertThat(layout.doLayout(event), matchesPattern())
//        assertEquals("[null message]\n", formatter.getMessage());
//    }
//
//    @Test
//    public void testNullGenerator() {
//        IMessageGenerator formatter = new ExceptionFormatter(null, null);
//        assertEquals("[null generator]\n", formatter.getMessage());
//    }
//
//    @Test
//    public void testOnlyMessage() {
//        IMessageGenerator formatter = new ExceptionFormatter(() -> "a message", null);
//        assertEquals("a message\n", formatter.getMessage());
//    }
//
//    @Test
//    public void testSimpleException() {
//        IMessageGenerator formatter = new ExceptionFormatter(() -> "a message",
//                new Exception("an exception"));
//        assertEquals("a message\n" +
//                "\tjava.lang.Exception: an exception\n",
//                formatter.getMessage());
//    }
//
//    @Test
//    public void testNullMessageWithException() {
//        IMessageGenerator formatter = new ExceptionFormatter(() -> null,
//                new Exception("an exception"));
//        assertEquals("[null message]\n" +
//                        "\tjava.lang.Exception: an exception\n",
//                formatter.getMessage());
//    }
//
//    @Test
//    public void testExceptionWithCauses() {
//        IMessageGenerator formatter = new ExceptionFormatter(() -> "a message",
//                new Exception("an exception", new RuntimeException("a cause", new IOException("another cause"))));
//        assertEquals("a message\n" +
//                        "\tjava.lang.Exception: an exception\n" +
//                        "\tjava.lang.RuntimeException: a cause\n" +
//                        "\tjava.io.IOException: another cause\n",
//                formatter.getMessage());
//    }
//
//    @Test
//    public void testExceptionWithCircularReference() {
//        Throwable thirdException = new Exception("third");
//        Throwable secondException = new Exception("second", thirdException);
//        Throwable firstException = new Exception("first", secondException);
//        thirdException.initCause(firstException);
//
//        IMessageGenerator formatter = new ExceptionFormatter(() -> "a message", firstException);
//        assertEquals("a message\n" +
//                        "\tjava.lang.Exception: first\n" +
//                        "\tjava.lang.Exception: second\n" +
//                        "\tjava.lang.Exception: third\n" +
//                        "\t[circular reference]: java.lang.Exception: first\n",
//                formatter.getMessage());
//    }

}
