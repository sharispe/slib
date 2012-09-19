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
 
 
package slib.sml.smbb.core.bioinfo.bmark.seqsim.core;

import slib.utils.ex.SGL_Ex_Critic;

/**
 * Implementation of Sequence related metrics proposed by 
 * ﻿Pesquita C, Faria D, Bastos H, et al.: 
 * Metrics for GO based protein semantic similarity: a systematic evaluation. 
 * BMC Bioinformatics 2008, 9 Suppl 5:S4.
 * 
 * @author Sebastien Harispe
 *
 */
public class SeqSimMetricComputer {
	
	IblastScoreAcessor blastScoreAcessor;
	
	public SeqSimMetricComputer(IblastScoreAcessor blastScoreAcessor){
		this.blastScoreAcessor = blastScoreAcessor;
	}

	public double LRBS(String protA, String protB) throws SGL_Ex_Critic{
		
		double blastScore_AvsB = blastScoreAcessor.getBlastScore(protA, protB);
		double blastScore_BvsA = blastScoreAcessor.getBlastScore(protB, protA);
		
		return Math.log10((blastScore_AvsB + blastScore_BvsA)/2.);
	}
	
	public double RRBS(String protA, String protB) throws SGL_Ex_Critic{
		
		double blastScore_AvsB = blastScoreAcessor.getBlastScore(protA, protB);
		double blastScore_BvsA = blastScoreAcessor.getBlastScore(protB, protA);
		double blastScore_AvsA = blastScoreAcessor.getBlastScore(protA, protA);
		double blastScore_BvsB = blastScoreAcessor.getBlastScore(protB, protB);
		
		System.out.println(protB+" "+protA+" "+blastScore_BvsA);
		return (blastScore_AvsB + blastScore_BvsA)/(blastScore_AvsA + blastScore_BvsB);
	}
	
	public static void main(String[] args) {
		
		String pref = System.getProperty("user.dir")+"/modules/smf/smbb/data/bioinfo/seqsim/";
		String bscores = pref+"blastScores.csv";
		
		try {
			
			IblastScoreAcessor bscoreAccessor = new BlastScoreAccesorInmemory(bscores);
			SeqSimMetricComputer bscoreComputer = new SeqSimMetricComputer(bscoreAccessor);
			
			String protA = "P05091";
			String protB = "P13804";
			
			double lrbs = bscoreComputer.LRBS(protA, protB);
			double rrbs = bscoreComputer.RRBS(protA, protB);
			
			System.out.println("lrbs "+lrbs);
			System.out.println("rrbs "+rrbs);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
