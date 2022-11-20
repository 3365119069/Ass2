import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.*;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VelocityLayoutTest {
    //format
    //getPattern()
    //setPattern(String pattern)
    VelocityLayout velocityLayout;
    private Logger logger;
    private long timeStamp;
    private Throwable throwable;
    private LoggingEvent loggingEventDebug, loggingEventInfo, loggingEventError;
    private String pattern;
    MemAppender memAppender;
    ArrayList<LoggingEvent>  arrayList;
    private MBeanServer server;
    //setPattern
    @BeforeEach
    public void setUp(){
        logger = Logger.getLogger("VelocityLayoutTestLogger");
        pattern="[$p] $c $d: $m$n";
        timeStamp = System.currentTimeMillis();
        throwable = new Throwable();
        arrayList=new ArrayList<>();
        velocityLayout=new VelocityLayout();
        server= ManagementFactory.getPlatformMBeanServer();
        loggingEventDebug = new LoggingEvent("org.apache.log4j.Logger", logger, timeStamp, Level.DEBUG, "This is a DEBUG log", throwable);
        loggingEventInfo = new LoggingEvent("org.apache.log4j.Logger", logger, timeStamp, Level.INFO, "This is a INFO log", throwable);
        loggingEventError = new LoggingEvent("org.apache.log4j.Logger", logger, timeStamp, Level.ERROR, "This is a ERROR log", throwable);
    }
    @AfterEach
    public void tearDown(){
        arrayList=null;
        velocityLayout=null;
    }
    //test setPattern
    @Test
    public void testSetPattern() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        arrayList.add(loggingEventDebug);
        memAppender = MemAppender.getInstance(arrayList);
        velocityLayout.setPattern(pattern);
        memAppender.setLayout(velocityLayout);
        String expected="["+loggingEventDebug.getLevel()+"] "+loggingEventDebug.getLoggerName()+" "+new Date(loggingEventDebug.getTimeStamp()).toString()+": "+
                loggingEventDebug.getMessage()+"\n";
        String actual=velocityLayout.format(loggingEventDebug);
        ObjectName objectName=new ObjectName("com.exampleSetPattern:type=MemAppender");
        server.registerMBean(memAppender,objectName);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(2000);
        assertEquals(expected, actual);
    }
    //test getPattern
    @Test
    public void testGetPattern() {
        String expected="[$p] $d $m$n";
        String actual=velocityLayout.getPattern();
        assertEquals(expected, actual);
    }
    //Test format with Debug Level(default pattern)
    @Test
    public void testFormatWithDebugLevel() {
        String expected = "[DEBUG] "+new Date(timeStamp)+" This is a DEBUG log\n";
        String actual = velocityLayout.format(loggingEventDebug);
        assertEquals(expected, actual);
    }
    //Test format with Info Level(default pattern)
    @Test
    public void testFormatWithInfoLevel() {
        String expected = "[INFO] "+new Date(timeStamp)+" This is a INFO log\n";
        String actual = velocityLayout.format(loggingEventInfo);
        assertEquals(expected, actual);
    }
    //Test format with Error Level(default pattern)
    @Test
    public void testFormatWithErrorLevel() {
        String expected = "[ERROR] "+new Date(timeStamp)+" This is a ERROR log\n";
        String actual = velocityLayout.format(loggingEventError);
        assertEquals(expected, actual);
    }
}
