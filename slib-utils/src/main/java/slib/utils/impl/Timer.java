/* 
 *  Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 *  
 *  This software is a computer program whose purpose is to provide 
 *  several functionalities for the processing of semantic data 
 *  sources such as ontologies or text corpora.
 *  
 *  This software is governed by the CeCILL  license under French law and
 *  abiding by the rules of distribution of free software.  You can  use, 
 *  modify and/ or redistribute the software under the terms of the CeCILL
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info". 
 * 
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability. 

 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or 
 *  data to be ensured and,  more generally, to use and operate it in the 
 *  same conditions as regards security. 
 * 
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL license and that you accept its terms.
 */
package slib.utils.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
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

        String status = "Running";
        long estop = System.currentTimeMillis();
        if (stop != 0) {
            status = "Stopped";
            estop = stop;
        }
        
        long diff = estop - start;

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        logger.info("Timer "+status);
        logger.info("Start Time  : " + dateFormat.format(new Date(start)));
        logger.info("Current Time: " + dateFormat.format(new Date(estop)));
        logger.info("End Time    : " + dateFormat.format(new Date(stop)));
        logger.info("Total Time  : " + dateFormat.format(new Date(diff)));
    }
}
