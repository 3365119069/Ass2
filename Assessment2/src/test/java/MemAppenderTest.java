import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MemAppenderTest {
    private VelocityLayout velocityLayout;
    private static final String pattern = "[$p] $c $d: $m$n";
    private MemAppender memAppender;
    MBeanServer server;
    @BeforeEach
    public void setUp(){
        server= ManagementFactory.getPlatformMBeanServer();
        velocityLayout = new VelocityLayout(pattern);
    }
    //Check if it is in the singleton pattern
    @Test
    public void testIfsingleton() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        MemAppender memAppender1 = MemAppender.getInstance(new ArrayList<>(1));
        memAppender1.setLayout(velocityLayout);
        MemAppender memAppender2 = MemAppender.getInstance(new ArrayList<>(2));
        memAppender2.setLayout(velocityLayout);
        assertEquals(memAppender1,memAppender2);
        ObjectName objectName=new ObjectName("com.exampleTestSingleTon:type=MemAppender");
        ObjectName objectName1=new ObjectName("com.exampleTestSingleTon1:type=MemAppender");
        server.registerMBean(memAppender1,objectName);
        server.registerMBean(memAppender2,objectName1);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(1000);
        memAppender1.close();
        memAppender2.close();
    }
    //Check the append method
    @Test
    public void testAppendLoggingEvent() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        Logger logger  = Logger.getLogger("appendLogger");
        memAppender = MemAppender.getInstance(new ArrayList<>());
        memAppender.setLayout(velocityLayout);
        logger.addAppender(memAppender);
        logger.info("info append log");
        assertEquals(memAppender.getCurrentLogs().size(),1);
        assertEquals("info append log",memAppender.getCurrentLogs().get(0).getMessage());
        ObjectName objectName2=new ObjectName("com.exampleAppendLoggingEvent:type=MemAppender");
        server.registerMBean(memAppender,objectName2);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(1000);
        memAppender.close();
    }
    //Check the append method
    @Test
    public void testMaxSizeWithloggerSize() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        Logger maxSizelogger = Logger.getLogger("maxSizelogger");
        memAppender = MemAppender.getInstance(new ArrayList<>());
        memAppender.setLayout(velocityLayout);
        maxSizelogger.addAppender(memAppender);
        memAppender.setMaxsize(10);
        for (int i = 0; i < 100; i++) {
            maxSizelogger.debug("debug log");
        }
        assertEquals(10,memAppender.getCurrentLogs().size());
        ObjectName objectName3=new ObjectName("com.exampleMaxSizeWithloggerSize:type=MemAppender");
        server.registerMBean(memAppender,objectName3);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(1000);
        memAppender.close();
    }
    //Check the printLogs function（use Velocitylayout）
    @Test
    public void testPrintLogs() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        Logger printLogslogger  = Logger.getLogger("printLogsLogger");
        memAppender = MemAppender.getInstance(new ArrayList<>());
        memAppender.setLayout(velocityLayout);
        printLogslogger.addAppender(memAppender);
        printLogslogger.warn("warning log");
//        velocityLayout = new VelocityLayout(pattern);
//        memAppender.setLayout(velocityLayout);
        memAppender.setLayout(velocityLayout);
        memAppender.printlogs();
        assertEquals(memAppender.getLayout(),velocityLayout);
        //test if it clear the logs from its memory.
        assertEquals(0,memAppender.getCurrentLogs().size());
        ObjectName objectName4=new ObjectName("com.examplePrintLogs:type=MemAppender");
        server.registerMBean(memAppender,objectName4);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(1000);
        memAppender.close();
    }
    //Check the setLevel function
    @Test
    public void testLoggerLevel() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        Logger levellogger  = Logger.getLogger("LoggerLevelLogger");
        memAppender = MemAppender.getInstance(new ArrayList<>());
        memAppender.setLayout(velocityLayout);
        levellogger.addAppender(memAppender);
        levellogger.setLevel(Level.WARN);

        levellogger.fatal("Fatal Message");
        levellogger.error("Error Message");
        levellogger.warn("Warn Message");
        levellogger.info("Info Message");
        levellogger.debug("Debug Message");
        levellogger.trace("trace Message");

        assertEquals(3,memAppender.getCurrentLogs().size());
        ObjectName objectName5=new ObjectName("com.exampleLoggerLevel:type=MemAppender");
        server.registerMBean(memAppender,objectName5);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(1000);
        memAppender.close();
    }
    //getDiscardedLogCount()
    @Test
    public void testDiscardedLogCount() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        Logger discardedLogCountlogger  = Logger.getLogger("DiscardedLogCountlogger");
        memAppender = MemAppender.getInstance(new ArrayList<>());
        memAppender.setLayout(velocityLayout);
        discardedLogCountlogger.addAppender(memAppender);
        memAppender.setMaxsize(2500);
        for (int i = 0; i < 2600; i++) {
            discardedLogCountlogger.error("error log");
        }
        assertEquals(100,memAppender.getDiscardedLogCount());
        ObjectName objectName6=new ObjectName("com.exampleDiscardedLogCount:type=MemAppender");
        server.registerMBean(memAppender,objectName6);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(1000);
        memAppender.close();
    }
    //getEventStrings()
    @Test
    public void testGetEventStrings() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        Logger eventStringslogger  = Logger.getLogger("EventStringslogger");
        memAppender = MemAppender.getInstance(new ArrayList<>());
        memAppender.setLayout(velocityLayout);
        eventStringslogger.addAppender(memAppender);
        eventStringslogger.warn("warning log");
        memAppender.setLayout(new SimpleLayout());
        assertEquals("WARN - warning log",memAppender.getEventStrings().get(0).trim());
        ObjectName objectName7=new ObjectName("com.exampleGetEventStrings:type=MemAppender");
        server.registerMBean(memAppender,objectName7);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(1000);
        memAppender.close();
    }
    //getCurrentLogs()
    @Test
    public void testGetCurrentLogs() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        Logger currentLogslogger  = Logger.getLogger("CurrentLogslogger");
        memAppender = MemAppender.getInstance(new ArrayList<>());
        memAppender.setLayout(velocityLayout);
        currentLogslogger.addAppender(memAppender);
        currentLogslogger.warn("warning log");
        currentLogslogger.warn("warning log1");
        assertEquals(2,memAppender.getCurrentLogs().size());
        ObjectName objectName8=new ObjectName("com.exampleGetCurrentLogs:type=MemAppender");
        server.registerMBean(memAppender,objectName8);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(1000);
        memAppender.close();
    }
    @Test
    public void testGetMessageList() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        Logger MessageListlogger=Logger.getLogger("Messagelistlogger");
        memAppender = MemAppender.getInstance(new ArrayList<>());
        memAppender.setLayout(velocityLayout);
        MessageListlogger.addAppender(memAppender);
        MessageListlogger.error("error log");
        assertEquals("error log",memAppender.getMessageList().get(0).toString());
        ObjectName objectName9=new ObjectName("com.exampleGetMessageList:type=MemAppender");
        server.registerMBean(memAppender,objectName9);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(1000);
        memAppender.close();
    }
    @Test
    public void testLogSize() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        Logger logSizelogger=Logger.getLogger("logSizelogger");
        memAppender = MemAppender.getInstance(new ArrayList<>());
        memAppender.setLayout(velocityLayout);
        logSizelogger.addAppender(memAppender);
        logSizelogger.error("error");
        assertEquals(memAppender.getEventStrings().get(0).length(),memAppender.getLogsize());
        ObjectName objectName10=new ObjectName("com.exampleLogSize:type=MemAppender");
        server.registerMBean(memAppender,objectName10);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(1000);
        memAppender.close();
    }
}
