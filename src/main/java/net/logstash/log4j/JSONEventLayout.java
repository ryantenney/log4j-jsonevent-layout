package net.logstash.log4j;

import net.logstash.log4j.data.HostData;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import net.minidev.json.JSONObject;
import org.apache.commons.lang.*;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.apache.log4j.spi.LocationInfo;

public class JSONEventLayout extends Layout {

    private String[] tags = null;
    private String source = null;
    private String sourceHost = null;
    private String sourcePath = null;

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final ThreadLocal<DateFormat> dateFormatter = new ThreadLocal<DateFormat>() {
        protected DateFormat initialValue() {
            return new SimpleDateFormat(DATE_FORMAT);
        }
    };

    public String dateFormat(long timestamp) {
        Date date = new Date(timestamp);
        String formatted = dateFormatter.get().format(date);

        /* 
       	 * No native support for ISO8601 woo!
         */
        return formatted.substring(0,26) + ":" + formatted.substring(26);
    }

    public String format(LoggingEvent loggingEvent) {
        HashMap<String, Object> fieldData = new NonNullHashMap<String, Object>();

        fieldData.put("mdc", loggingEvent.getProperties());
        fieldData.put("ndc", loggingEvent.getNDC());
        fieldData.put("level", loggingEvent.getLevel().toString());
        fieldData.put("thread_name", loggingEvent.getThreadName());
        fieldData.put("logger_name", loggingEvent.getLoggerName());

        final ThrowableInformation throwableInformation = loggingEvent.getThrowableInformation();
        if (throwableInformation != null) {
            HashMap<String, Object> exceptionInformation = new NonNullHashMap<String, Object>();
            exceptionInformation.put("exception_class", throwableInformation.getThrowable().getClass().getCanonicalName());
            exceptionInformation.put("exception_message", throwableInformation.getThrowable().getMessage());
            final String[] stacktrace = throwableInformation.getThrowableStrRep();
            if (stacktrace != null) {
                exceptionInformation.put("stacktrace", StringUtils.join(stacktrace,"\n"));
            }
            fieldData.put("exception", exceptionInformation);
        }

        if (loggingEvent.locationInformationExists()) {
            final LocationInfo info = loggingEvent.getLocationInformation();
            fieldData.put("file", info.getFileName());
            fieldData.put("line_number", info.getLineNumber());
            fieldData.put("class", info.getClassName());
            fieldData.put("method", info.getMethodName());
        }

        JSONObject logstashEvent = new JSONObject();

        logstashEvent.put("@source", this.source);
        logstashEvent.put("@source_host", this.sourceHost);
        logstashEvent.put("@source_path", this.sourcePath);
        logstashEvent.put("@tags", this.tags);
        logstashEvent.put("@message", loggingEvent.getRenderedMessage());
        logstashEvent.put("@timestamp", dateFormat(loggingEvent.getTimeStamp()));
        logstashEvent.put("@fields", fieldData);

        return logstashEvent.toString() + "\n";
    }

    public boolean ignoresThrowable() {
        return false;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public void setTags(String tags) {
        this.tags = StringUtils.split(tags, ", ");
    }

    public void activateOptions() {
        if (this.sourceHost == null) {
            this.sourceHost = new HostData().getHostName();
        }
    }

}
