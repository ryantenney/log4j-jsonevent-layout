package net.logstash.log4j;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Layout;

public class MockAppender extends AppenderSkeleton {

    private List<String> messages = new ArrayList<String>();

    public MockAppender(Layout layout){
        this.layout = layout;
    }

    @Override
    protected void append(LoggingEvent event){
        messages.add(layout.format(event));
    }

    public void close(){
        messages.clear();
    }

    public boolean requiresLayout(){
        return true;
    }

    public String[] getMessages() {
        return messages.toArray(new String[0]);
    }

    public void clear() {
        messages.clear();
    }
}
