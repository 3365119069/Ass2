import java.util.ArrayList;
public interface MemAppenderMBean{
    public ArrayList getMessageList();
    public long getLogsize();
    public long getDiscardedLogCount();
}
