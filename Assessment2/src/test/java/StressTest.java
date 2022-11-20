import org.apache.log4j.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.LinkedList;

public class StressTest {
    private Logger logger;
    private VelocityLayout velocityLayout;
    private static MemAppender memappender;
    private MBeanServer server;
    public void testPerformance(Appender memAppender, int testNumber, String testObject) {
        //Use testObject that makes comparisons between the MemAppender and other appenders sensible
        logger = Logger.getLogger(testObject);
        long startTime =System.currentTimeMillis();
        logger.addAppender(memAppender);
        addlogs(testNumber,logger);
        long endTime = System.currentTimeMillis();
        long time = endTime - startTime;
        System.out.println("The total time spent by " + testObject +" is "+time);
    }
    public void addlogs(int testNumber,Logger logger) {
        logger.setLevel(Level.ALL);
        for (int i = 0; i < testNumber; i++) {
            logger.error("Error Message");
            logger.warn("Warn Message");
            logger.info("Info Message");
            logger.debug("Debug Message");
        }
    }
    @BeforeEach
    public void setUp() throws InterruptedException {
        server= ManagementFactory.getPlatformMBeanServer();
        //Leave time to open JConsole.
        Thread.sleep(2000);
    }
    @Test
    public void testLinkedlist() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        logger = Logger.getLogger("Linkedlist");
        memappender=MemAppender.getInstance(new LinkedList<>());
        memappender.setLayout(new SimpleLayout());
        logger.addAppender(memappender);
        testPerformance(memappender,10000000,"Linkedlist");
        ObjectName objectName=new ObjectName("com.exampleLinkedlist:type=MemAppender");
        server.registerMBean(memappender,objectName);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(3000);
        memappender.close();
    }
    @Test
    public void testArraylist() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
//        //Sleep for a period of time is used to turn on JConsole
//        Thread.sleep(10000);
        logger = Logger.getLogger("Arraylist");
        memappender=MemAppender.getInstance(new ArrayList<>());
        memappender.setLayout(new SimpleLayout());
        logger.addAppender(memappender);
        testPerformance(memappender,10000000,"Arraylist");
        ObjectName objectName1=new ObjectName("com.exampleArraylist:type=MemAppender");
        server.registerMBean(memappender,objectName1);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(3000);
        memappender.close();
    }
    @Test
    public void testConsoleAppender(){
//        //Sleep for a period of time is used to turn on JConsole
//        Thread.sleep(10000);
        String pattern = "[$t] $p $d $c: $m$n";
        velocityLayout=new VelocityLayout();
        velocityLayout.setPattern(pattern);
        ConsoleAppender appender=new ConsoleAppender(velocityLayout);
        testPerformance(appender,2000,"ConsoleAppender");
        appender.close();
    }
    @Test
    public void testFileAppender() throws IOException{
//        //Sleep for a period of time is used to turn on JConsole
//        Thread.sleep(10000);
        String pattern = "[$t] $p $d $c: $m$n";
        velocityLayout=new VelocityLayout();
        velocityLayout.setPattern(pattern);
        FileAppender appender=new FileAppender(velocityLayout,"log.txt");
        testPerformance(appender,2000,"FileAppender");
        appender.close();
    }
    @Test
    public void testPatternLayout(){
//        //Sleep for a period of time is used to turn on JConsole
//        Thread.sleep(10000);
        PatternLayout patternLayout=new PatternLayout("[%p] %c %d: %m%n");
        ConsoleAppender consoleAppender=new ConsoleAppender(patternLayout);
        testPerformance(consoleAppender,100000,"PatternLayout");
        consoleAppender.close();
    }
    @Test
    public void testVelocityLayout(){
//        //Sleep for a period of time is used to turn on JConsole
//        Thread.sleep(10000);
        velocityLayout=new VelocityLayout("[$p] $c $d: $m$n");
        ConsoleAppender consoleVelocityAppender=new ConsoleAppender(velocityLayout);
        testPerformance(consoleVelocityAppender,100000,"VelocityLayout");
        consoleVelocityAppender.close();
    }
    //test performance before and after maxSize
    @ParameterizedTest
    @ValueSource(ints = {30000,50000})
    void  testBeforeAndAfterMaxSize(int num) throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
//        //Sleep for a period of time is used to turn on JConsole
//        Thread.sleep(10000);
        memappender = MemAppender.getInstance(new ArrayList<>());
        memappender.setLayout(new SimpleLayout());
        memappender.setMaxsize(130000);
        //If we set testNumber to 1000 that the logger will have 4000 logs.(Because output four logs at a time  in addlogs())
        testPerformance(memappender,num,"ifReachMaxsize");
        ObjectName objectName2;
        if(num==30000) {
            objectName2 = new ObjectName("com.exampleBeforeMaxSize:type=MemAppender");
        }else {
            objectName2 = new ObjectName("com.exampleAfterMaxSize:type=MemAppender");
        }
        server.registerMBean(memappender,objectName2);
        //Leave time to open JConsole (MBean).You can set it to longer to prevent that it break when we see MBean in JConsole.
        Thread.sleep(3000);
        memappender.close();
    }
}
