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
package slib.utils.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class ThreadManager extends PoolLocker {

    private static ThreadManager instance;
    Logger logger = LoggerFactory.getLogger(ThreadManager.class);
    private static final int MAX_THREAD_DEFAULT = 1;

    /**
     *
     * @return the manager
     */
    public static ThreadManager getSingleton() {
        if (instance == null) {
            instance = new ThreadManager();
        }
        return instance;
    }

    private ThreadManager() {
        super(MAX_THREAD_DEFAULT);
    }

    /**
     *
     * @return a pool worker of maximal capacity considering the current
     * configuration.
     * @throws SLIB_Ex_Critic
     */
    public PoolWorker getMaxLoadPoolWorker() throws SLIB_Ex_Critic {

        synchronized (lock) {

            int allowedSize = capacity - running;

            running += allowedSize;
            PoolWorker poolWorker = new PoolWorker(allowedSize);

            logger.info("Create a pool worker of size " + allowedSize);
            return poolWorker;
        }
    }

    /**
     *
     * @param size
     * @return a pool worker of the given size.
     * @throws SLIB_Ex_Critic
     */
    public PoolWorker getPoolWorker(int size) throws SLIB_Ex_Critic {

        synchronized (lock) {

            if (size > capacity) {
                throw new SLIB_Ex_Critic("Error during pool worker request maximum Number of thread " + capacity + " requested " + size);
            } else if (running + size > capacity) {
                throw new SLIB_Ex_Critic("Unable to allocate the requested number of threads, available " + (capacity - running) + " requested " + size);
            }

            running += size;

            logger.info("Create a pool worker of size " + size);
            PoolWorker poolWorker = new PoolWorker(size);
            return poolWorker;
        }
    }

    /**
     *
     * @param maxThread
     */
    public void setMaxThread(int maxThread) {
        capacity = maxThread;
        logger.info("Setting maximal number of threads to " + capacity);
    }

    /**
     *
     * @return the maximal number of threads allocated.
     */
    public int getMaxThread() {
        return capacity;
    }

    /**
     *
     * @param nbThread
     */
    public void freeResource(int nbThread) {
        this.running -= nbThread;
    }
}