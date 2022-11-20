import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MemAppender extends AppenderSkeleton implements MemAppenderMBean{
    //enforces the singleton pattern.
    private volatile static MemAppender instance;
    //stores the LoggingEvents in a list
    private static List<LoggingEvent> loggerEventList;
    //layout
    private Layout layout;
    // maxSize(default)
    private int maxSize=100;
    //record discarded logs number
    //JMX monitoring
    private long discardedLogCount = 0;
    //JMX monitoring
    private  ArrayList<String> messageList;
    private  long logsize;
    //dependency Injection
    private MemAppender (List<LoggingEvent> events) {
        MemAppender.loggerEventList=events;
        instance=this;
    }
    public static MemAppender getInstance(List<LoggingEvent> events) {
        //First null detection
        if (instance == null) {
            synchronized (MemAppender.class) {
                //Second null detection
                if (instance == null) {
                    //Instantiate the object
                    instance = new MemAppender(events);
                }
            }
        }
        return instance;
    }
    //getcurrentlogs
    public List<LoggingEvent> getCurrentLogs() {
        return Collections.unmodifiableList(loggerEventList);
    }
    //getEventStrings
    public List<String> getEventStrings() {

        List<String> eventstrings = getCurrentLogs().stream().map(loggingEvent -> this.layout.format(loggingEvent)).collect(Collectors.toList());
        List<String> EventStrings = Collections.unmodifiableList(eventstrings);
        return EventStrings;
    }
    //printlogs
    public void printlogs() {
        List<String> eventstrings = getEventStrings();
        for (String event:eventstrings) {
            System.out.println(event);
        }
        // clear the logs
        loggerEventList.clear();
    }

    public static List<LoggingEvent> getLoggerEventList() {
        return loggerEventList;
    }

    public static void setLoggerEventList(List<LoggingEvent> loggerEventList) {
        MemAppender.loggerEventList = loggerEventList;
    }

    public int getMaxsize() {
        return maxSize;
    }

    public void setMaxsize(int maxSize) {
        this.maxSize = maxSize;
    }
    @Override
    public Layout getLayout() {
        return layout;
    }
    //setLayout()
    @Override
    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    //Whether you want to output text in a format
    @Override
    public boolean requiresLayout() {
        return false;
    }
    @Override
    protected void append(LoggingEvent loggingEvent) {
        loggerEventList.add(loggingEvent);
//move the oldest logs
        if(loggerEventList.size()>maxSize){
            loggerEventList.remove(0);
            //The number of discarded logs should be tracked
            discardedLogCount++;
        }
    }

    @Override
    public void close() {
        instance = null;
        discardedLogCount = 0;
        loggerEventList = null;
        layout = null;
    }
    //JMX monitoring
    @Override
    public long getDiscardedLogCount() {
        return discardedLogCount;
    }
    public void setDiscardedLogCount(long discardedLogCount) {
        this.discardedLogCount = discardedLogCount;
    }

    @Override
    public ArrayList getMessageList() {
        messageList=new ArrayList<>();
        List<LoggingEvent> currentLogs = instance.getCurrentLogs();
        for (int i = 0; i < currentLogs.size(); i++) {
            messageList.add(currentLogs.get(i).getMessage().toString());
        }
        return messageList;
    }

    @Override
    public long getLogsize() {
        logsize=0;
        List<String> eventStrings = instance.getEventStrings();
        for(int i=0;i<eventStrings.size();i++){
            logsize+=eventStrings.get(i).length();
        }
        return logsize;
    }
}
