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
 
 
package slib.sml.sm.core.metrics.ic.utils;

import slib.sml.sm.core.utils.SMConstants;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.i.Conf;


public abstract class ICconf extends Conf{

	public String  id;				// ID used to represent the IC 
	public String  label;			// label used to plot IC values
	public String  flag;			// SSPConstants flag corresponding to the IC 
	public String  className;
	public boolean isCorpusBased;

	
	public ICconf(String id) throws SGL_Ex_Critic{
		this.id 	= id;
		this.label  = id;
		this.flag   = id;

		validate();
	}
	
	/**
	 * 
	 * @param id 	ID used to represent the IC 
	 * @param label	label used to plot IC values
	 * @param flag	SSPConstants flag corresponding to the IC 
	 * @throws SGL_Ex_Critic 
	 */
	public ICconf(String id,String label,String flag) throws SGL_Ex_Critic{
		this.id 	= id;
		this.label  = label;
		this.flag   = flag;

		validate();

	}

	private void validate() throws SGL_Ex_Critic {
		
		if(SMConstants.SIM_PAIRWISE_DAG_NODE_IC_ANNOT.containsKey(flag)){

			className = SMConstants.SIM_PAIRWISE_DAG_NODE_IC_ANNOT.get(flag);
			this.isCorpusBased  = true;
		}
		else if(SMConstants.SIM_PAIRWISE_DAG_NODE_IC_INTRINSIC.containsKey(flag)){

			className = SMConstants.SIM_PAIRWISE_DAG_NODE_IC_INTRINSIC.get(flag);
			this.isCorpusBased  = false;
		}
		else 
			throw new SGL_Ex_Critic("Unknown IC Flag "+flag);
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return flag;
	}

	public String getFlag() {
		return flag;
	}
	public String getClassName() {
		return className;
	}

	public boolean isCorpusBased() {
		return isCorpusBased;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ICconf other = (ICconf) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	
	public String toString(){
		String out = "id : "+id+"\n";
		out += "label : "+id+"\n";
		out += "corpusBased : "+isCorpusBased+"\n";
		out += "flag : "+flag+"\n";
		out += super.toString();
		
		return out;
	}




}
