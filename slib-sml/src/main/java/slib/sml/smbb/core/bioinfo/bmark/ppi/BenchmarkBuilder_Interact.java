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


package slib.sml.smbb.core.bioinfo.bmark.ppi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.extraction.rvf.instances.InstancesAccessor;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.repo.impl.DataFactoryMemory;
import slib.sml.smbb.core.bioinfo.bmark.ppi.utils.Interaction;
import slib.sml.smbb.core.bioinfo.bmark.ppi.utils.InteractionSet;
import slib.sml.smbb.core.bioinfo.i_o.loader.mitab.MITAB25_reader;
import slib.sml.smbb.core.conf.xml.utils.SmbbConf_GO_PPI;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * Class used to generate interaction benchmarks
 * Considering a particular configuration defined in a {@link SmbbConf_GO_PPI} object.
 * 
 * @author Sebastien Harispe
 *
 */
public class BenchmarkBuilder_Interact {

	/**
	 * Generate an interaction considering the given configuration specified in 
	 * a {@link SmbbConf_GO_PPI} object.
	 * @param g the graph containing the annotations involved
	 * @param kb the knowledge base containing the entities involved in the interaction files.
	 * @param conf the configuration to take into account during benchmark creation
	 * @throws SGL_Exception 
	 * @see {@link SmbbConf_GO_PPI} for the configuration of the process.
	 */
	public static void generateBenchmark(
			G g, 
			InstancesAccessor instancesAccessor,
			SmbbConf_GO_PPI conf) throws SLIB_Exception{


		Integer setSize = conf.getSetSize();

		Logger logger = LoggerFactory.getLogger(BenchmarkBuilder_Interact.class);

		String taxon = conf.getTaxon();

		Integer minAnnot = conf.getMin_annot();

		logger.info("Starting  benchmark generation.");
		logger.info(conf.toString());

		DataFactoryMemory data = DataFactoryMemory.getSingleton();

		boolean useOneInteractionSrc = false;

		if(conf.getKnownRel().equals(conf.getKnownRelBase()))
			useOneInteractionSrc = true;

		// Load Interaction information (PPI) for specified taxon
		MITAB25_reader positiveIreader  = new MITAB25_reader();
		InteractionSet iSet 		  	= positiveIreader.load(conf.getKnownRel(),g.getURI().toString(), taxon,taxon);

		if(!useOneInteractionSrc) // clean index if reader is not use anymore
			positiveIreader.clean();


		/*
		 *	Selection of PPI composed of proteins present in the GO and GOA
		 *	All proteins involved in a PPI are selected 
		 *
		 *	Enable the generation of :
		 *		- PPI positive set (PPI+)
		 *		- All the annotations of the PPI+ proteins
		 */
		logger.debug("Merging Interaction & Knowledge Base");


		logger.info("Generating positive set to :"+conf.getOutputPositiveRel());
		HashSet<Interaction> 	  positivePPIset  	= new HashSet<Interaction>();
		HashSet<V> positiveInstances 				= new HashSet<V>();

		FileWriter fstream;
		BufferedWriter outfile;


		// Load positive class i.e. known interaction
		for(Interaction i: iSet.interactions){

			URI ia = data.createURI(iSet.interactors.get(i.a));
			URI ib = data.createURI(iSet.interactors.get(i.b));

			V a = g.getV(ia);
			V b = g.getV(ib);

			if( a != null && b !=null){

				Set<V> aClasses = instancesAccessor.getDirectClass(a);
				Set<V> bClasses = instancesAccessor.getDirectClass(b);
				
				if( aClasses.size() >= minAnnot && 
					bClasses.size() >= minAnnot){

					logger.debug(a+" ("+aClasses.size()+")\t"+b+" ("+bClasses.size()+")");

					positiveInstances.add(a);
					positiveInstances.add(b);

					positivePPIset.add(i);
				}
			}

			logger.debug("Skipped\t"+ia+"\t"+a+"\t"+ib+"\t"+b);

		}




		logger.info("Set of potential interaction contains "+positivePPIset.size());
		logger.info("Number of proteins  "+positiveInstances.size());


		Random random = new Random();

		List<Interaction> positivePPIlist = new ArrayList<Interaction>(positivePPIset);

		if(conf.getSetSize() != null){


			logger.info("Randomly build the positive class composed of "+conf.getSetSize()+" couples of entities ");

			if(conf.getSetSize() > positivePPIset.size())
				throw new SLIB_Exception("Cannot build benchmarks... class size exceed potential interactions loaded respectively "+conf.getSetSize()+" "+positivePPIset.size());

			HashSet<Interaction> positivePPIsetTest = new HashSet<Interaction>();

			for (int i = 0; i < conf.getSetSize(); i++) {

				int id = random.nextInt(positivePPIlist.size());
				positivePPIsetTest.add(positivePPIlist.get(id));
				positivePPIlist.remove(id);
			}
			positivePPIset = positivePPIsetTest;
			logger.info("Positive class successfully build "+conf.getSetSize()+" couples of entities ");
		}
		else
			logger.info("Full positive class considered");


		try {
			fstream = new FileWriter(conf.getOutputPositiveRel());

			outfile = new BufferedWriter(fstream);

			for(Interaction i: positivePPIset){

				URI ia = data.createURI(iSet.interactors.get(i.a));
				URI ib = data.createURI(iSet.interactors.get(i.b));

				V a = g.getV(ia);
				V b = g.getV(ib);

				Set<V> aClasses = instancesAccessor.getDirectClass(a);
				Set<V> bClasses = instancesAccessor.getDirectClass(b);

				logger.debug(a+" ("+aClasses.size()+")\t"+b+" ("+bClasses.size()+")");

				outfile.write(ia.getLocalName()+"\t"+ib.getLocalName()+"\n");
			}

			outfile.close();


			if(positivePPIset.size() == 0)
				throw new SLIB_Ex_Critic("Cannnot generate a correct PPI benchmark no positive interaction loaded... please check parameters");

			// Load PPI information to generate the negative set

			InteractionSet ppiSetControl 				   = null;
			HashMap<String, Integer> fastPPIcontrolIndex   = null;

			if(useOneInteractionSrc){
				ppiSetControl = iSet;
				fastPPIcontrolIndex = positiveIreader.interactorsID;
			}
			else{
				MITAB25_reader 	 r  = new MITAB25_reader();
				ppiSetControl 		= r.load(conf.getKnownRelBase(),g.getURI().toString(),taxon,taxon);
				fastPPIcontrolIndex = r.interactorsID;
			}



			// Test if a negative set can be generated

			int nbPositivePPI = positivePPIset.size();
			int nbProteins    = instancesAccessor.getInstances().size();

			if(nbProteins*nbProteins - nbPositivePPI < nbPositivePPI)
				throw new SLIB_Ex_Critic("Due to the number of positive PPI set, the program cannot generate a negative PPI set");


			V[] fullBase = new V[nbProteins];
			fullBase = instancesAccessor.getInstances().toArray(fullBase);

			random = new Random();
			int negativePPIsetSize = 0;

			logger.debug("Building index");
			HashMap<Integer, HashSet<Integer>> fastIndex = ppiSetControl.buildFastIndex();

			logger.info("Generating negative set to :"+conf.getOutputNegativeRel());

			fstream = new FileWriter(conf.getOutputNegativeRel());
			outfile = new BufferedWriter(fstream);

			HashSet<V> negativeInstances = new HashSet<V>();

			long max_nbIteration = (fullBase.length*fullBase.length)/2;
			long nbIteration = 0;

			HashMap<Integer, HashSet<Integer>> interactionGenerated = new HashMap<Integer, HashSet<Integer>>();

			while(negativePPIsetSize != positivePPIset.size() && nbIteration < max_nbIteration ){

				// generate invalid PPI
				V a = fullBase[random.nextInt(fullBase.length)];
				V b = fullBase[random.nextInt(fullBase.length)];

				Integer a_id = fastPPIcontrolIndex.get(a.getValue().stringValue());
				Integer b_id = fastPPIcontrolIndex.get(b.getValue().stringValue());


				// test if random invalid PPI generated is not a valid one
				if(a_id != null && b_id != null){ 

					if(
							!(fastIndex.get(a_id).contains(b_id)  || fastIndex.get(b_id).contains(a_id)) && 
							(instancesAccessor.getDirectClass(a).size() >= minAnnot && instancesAccessor.getDirectClass(b).size() >= minAnnot) &&
							!( interactionGenerated.containsKey(a_id) && interactionGenerated.get(a_id).contains(b_id)) &&
							!( interactionGenerated.containsKey(b_id) && interactionGenerated.get(b_id).contains(a_id)) 
							){
						
						
						URI aURI = (URI) a.getValue();
						URI bURI = (URI) b.getValue();
						
						logger.debug(aURI.getLocalName()+
									 "("+instancesAccessor.getDirectClass(a).size()+")"+
									 "\t"+bURI.getLocalName()+"("+
									 instancesAccessor.getDirectClass(b).size()+")");

						outfile.write(aURI.getLocalName()+"\t"+bURI.getLocalName()+"\n");
						negativePPIsetSize++;

						negativeInstances.add(a);
						negativeInstances.add(b);

						if(!interactionGenerated.containsKey(b_id))
							interactionGenerated.put(b_id,new HashSet<Integer>());
						if(!interactionGenerated.containsKey(a_id))
							interactionGenerated.put(a_id,new HashSet<Integer>());

						interactionGenerated.get(a_id).add(b_id);
						interactionGenerated.get(b_id).add(a_id);


						if(setSize != null && negativePPIsetSize == setSize)
							break;
					}
				}
				nbIteration++;
			}

			outfile.close();

			if(nbIteration == max_nbIteration){
				throw new SLIB_Ex_Critic("Cannot generate benchmark, please reconsider applied restrictions ...");
			}

			logger.info("Set of incorrect interaction contains "+negativePPIsetSize);
			logger.info("Number of proteins  "+negativeInstances.size());

			if(setSize != null && (positivePPIset.size() != setSize || negativePPIsetSize != setSize)){
				logger.info("Impossible to create a benchmark containing "+setSize+" interactions");
				logger.info("positive set : "+positivePPIset.size());
				logger.info("negative set : "+negativePPIsetSize);
			}

		} catch (IOException e) {
			throw new SLIB_Ex_Critic(e);
		}
	}
}
