import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import java.io.StringWriter;
import java.util.Date;

public class VelocityLayout extends Layout {
    private String pattern="[$p] $d $m$n";//default
    VelocityContext context = new VelocityContext();
    VelocityEngine engine = new VelocityEngine();
    //set its pattern in the constructor
    public VelocityLayout(String pattern) {
        this.pattern = pattern;
    }
    public VelocityLayout(){}
    //set its pattern via a setter
    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String format(LoggingEvent loggingEvent) {
        engine.init();
        context.put("c", loggingEvent.getLoggerName());
        context.put("d", (new Date(loggingEvent.getTimeStamp())).toString());
        context.put("m", loggingEvent.getMessage());
        context.put("p", loggingEvent.getLevel());
        context.put("t", loggingEvent.getThreadName());
        context.put("n", "\n");
        StringWriter sw=new StringWriter();
        engine.evaluate(context,sw,"",pattern);
        /*engine.evaluate(context,)*/
        return sw.toString();
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {

    }
}
