/*
 * 
 * Copyright or © or Copr. Ecole des Mines d'Alès (2012) 
 * LGI2P research center
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 * 
 */
package slib.tools.smltoolkit.sm.cli.utils;

/**
 *
 * @author Sébastien Harispe
 */
public class SML_SM_module_XML_block_conf {

    public String graphURI;
    public String threads;
    public String icShortFlag;
    public String pmShortFlag;
    public String gmShortFlag;
    public String mtype;
    public String queries;
    public String output;
    public String quiet;
    public String noAnnots;
    public String notFound;

    public SML_SM_module_XML_block_conf setGraphURI(String graphURI) {
        this.graphURI = graphURI;
        return this;
    }

    public SML_SM_module_XML_block_conf setThreads(String threads) {
        this.threads = threads;
        return this;
    }

    public SML_SM_module_XML_block_conf setIcShortFlag(String ic) {
        this.icShortFlag = ic;
        return this;
    }

    public SML_SM_module_XML_block_conf setPmShortFlag(String pm) {
        this.pmShortFlag = pm;
        return this;
    }

    public SML_SM_module_XML_block_conf setGmShortFlag(String gm) {
        this.gmShortFlag = gm;
        return this;
    }

    public SML_SM_module_XML_block_conf setMtype(String mtype) {
        this.mtype = mtype;
        return this;
    }

    public SML_SM_module_XML_block_conf setQueries(String queries) {
        this.queries = queries;
        return this;
    }

    public SML_SM_module_XML_block_conf setOutput(String output) {
        this.output = output;
        return this;
    }

    public SML_SM_module_XML_block_conf setNoAnnots(String noannots) {
        this.noAnnots = noannots;
        return this;
    }
    
    public SML_SM_module_XML_block_conf setNotFound(String e) {
        this.notFound = e;
        return this;
    }

    public SML_SM_module_XML_block_conf setQuiet(String e) {
        this.quiet = e;
        return this;
    }
}
