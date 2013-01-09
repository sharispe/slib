package slib.utils.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author seb
 */
public class Timer {

    long start = 0;
    long stop  = 0;
    Logger logger = LoggerFactory.getLogger(Timer.class);

    /**
     *
     */
    public void start() {
        logger.info("Start Timer");
        start = System.currentTimeMillis();
        stop = 0;
    }

    /**
     *
     */
    public void stop() {
        logger.info("Stop Timer");
        stop = System.currentTimeMillis();
    }

    /**
     *
     */
    public void elapsedTime() {

        if (stop == 0) {
            stop();
        }
        long diff = stop - start;

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        logger.info("Start Time: " + dateFormat.format(new Date(start)));
        logger.info("End Time  : " + dateFormat.format(new Date(stop)));
        logger.info("Total Time: " + dateFormat.format(new Date(diff)));
    }
}
