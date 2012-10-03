/*

Copyright or © or Copr. Ecole des Mines d'Alès (2012) 

This software is a computer program whose purpose is to 
process semantic graphs.

This software is governed by the CeCILL  license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.

 */


package slib.tools.smltoolkit.sm.cli.utils;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.extraction.rvf.instances.InstancesAccessor;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.repo.impl.DataRepository;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.tools.smltoolkit.sm.cli.SmCli;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.impl.QueryEntry;
import slib.utils.threads.PoolWorker;



public class EntityToEntity_Thread implements Callable<ThreadResultsQueryLoader>{

	Logger logger = LoggerFactory.getLogger(this.getClass());

	PoolWorker poolWorker;

	int skipped  = 0;
	int setValue = 0;

	Collection<QueryEntry> queriesBench;
	InstancesAccessor iAccessor;

	SmCli sspM;
	G g;


	private int nbMeasures;

	public EntityToEntity_Thread(PoolWorker poolWorker, Collection<QueryEntry> queriesBench,SmCli sspM, int nbMeasures){

		this.poolWorker   = poolWorker;
		this.queriesBench = queriesBench;
		this.sspM 		  = sspM;
		this.nbMeasures   = nbMeasures;
		this.g = sspM.getG();
		this.iAccessor = sspM.getiAccessor();
	}

	public ThreadResultsQueryLoader call() throws Exception{

		ThreadResultsQueryLoader results = null;
		try{

			results = new ThreadResultsQueryLoader(queriesBench.size());


			DataRepository   df   = DataRepository.getSingleton();

			String uriE1s,uriE2s;
			StringBuilder tmp_buffer = new StringBuilder();

			URI uriE1,uriE2;
			V e1, e2;
			Set<V> setE1, setE2;
			double sim;

			for (QueryEntry q : queriesBench) {

				uriE1s = q.getKey();
				uriE2s = q.getValue();

				try {


					uriE1 = df.createURI(uriE1s);
					uriE2 = df.createURI(uriE2s);

				}
				catch (IllegalArgumentException e) {

					throw new SGL_Ex_Critic("Query file contains an invalid URI: "+e.getMessage());
				}
				
				e1 = g.getV(uriE1);
				e2 = g.getV(uriE2);

				if(e1 == null || e2 == null){
					if(e1 == null)
						throw new SGL_Ex_Critic("Cannot locate "+uriE1+" in "+g.getURI());
					if(e2 == null)
						throw new SGL_Ex_Critic("Cannot locate "+uriE2+" in "+g.getURI());
				}

				// clear tmp_buffer
				tmp_buffer.delete(0, tmp_buffer.length());

				tmp_buffer.append(uriE1);
				tmp_buffer.append("\t");
				tmp_buffer.append(uriE2);


				setE1 = iAccessor.getDirectClass(e1);
				setE2 = iAccessor.getDirectClass(e2);

				if(setE1.size() == 0 || setE2.size() == 0){

					if(!sspM.SKIP_EMPTY_ANNOTATION){

						setValue++;

						for (int i = 0; i < nbMeasures; i++) 
							tmp_buffer.append("\t"+sspM.EMPTY_ANNOTATION_SCORE);

						tmp_buffer.append("\n");

						results.buffer.append(tmp_buffer);
					}
					else{
						logger.debug("Skip "+e1+" (annot size = "+setE1.size()+") / "+e2+" (annot size = "+setE2.size()+")");
						skipped++;
					}
					continue;
				}

				for(SMconf m : sspM.conf.gConfGroupwise){

					if(SMConstants.SIM_GROUPWISE_ADD_ON.containsKey(m.flag)){

						String pm_id   = m.pairwise_measure_id;
						SMconf pm_conf = null;

						for(SMconf p : sspM.conf.gConfPairwise){
							if(pm_id.equals(p.id)){
								pm_conf = p;
								break;
							}
						}

						if(pm_conf == null)
							throw new SGL_Ex_Critic("Cannot locate configuration associated to pairwise measure "+pm_id);

						sim = sspM.simManager.computeGroupwiseAddOnSim(m, pm_conf, setE1,setE2);

						tmp_buffer.append("\t"+sim);

						if(Double.isNaN(sim) || Double.isInfinite(sim))
							SMutils.throwArithmeticCriticalException(m,pm_conf, e1,e2,sim);

					}
					else if(SMConstants.SIM_FRAMEWORK.containsKey(m.flag))
						throw new UnsupportedOperationException("Sim Framework are currently not supported as groupwise measures... sorry");

					else{

						sim = sspM.simManager.computeGroupwiseStandaloneSim(m, setE1,setE2);

						if(Double.isNaN(sim) || Double.isInfinite(sim))
							SMutils.throwArithmeticCriticalException(m, e1,e2,sim);

						tmp_buffer.append("\t");
						tmp_buffer.append(sim);
					}
				}
//				logger.debug(tmp_buffer.toString());
				tmp_buffer.append("\n");

				results.buffer.append(tmp_buffer);
			}
			results.setSetValue(setValue);
			results.setSkipped(skipped);
		}
		catch(Exception e){
			if(logger.isDebugEnabled()) e.printStackTrace();
			throw new Exception(e);
		}
		finally{
			poolWorker.taskComplete();
		}
		return results;
	}

}