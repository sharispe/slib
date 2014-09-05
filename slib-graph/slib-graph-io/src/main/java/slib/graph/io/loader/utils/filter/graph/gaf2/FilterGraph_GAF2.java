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
package slib.graph.io.loader.utils.filter.graph.gaf2;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import slib.graph.io.loader.utils.filter.graph.FilterGraph;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.i.Conf;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class FilterGraph_GAF2 extends FilterGraph {

    HashSet<String> taxons;
    HashSet<String> excludedEC;

    /**
     *
     * @param id
     */
    public FilterGraph_GAF2(String id) {
        super(id, FilterGraph_GAF2_cst.TYPE);
    }

    /**
     *
     * @param conf
     * @throws SLIB_Ex_Critic
     */
    public FilterGraph_GAF2(Conf conf) throws SLIB_Ex_Critic {
        super(conf);

        String taxids = (String) conf.getParam(FilterGraph_GAF2_cst.TAXONS_IDS);
        String ecids = (String) conf.getParam(FilterGraph_GAF2_cst.REMOVE_EC);

        if (taxids != null) {
            addTaxons(Arrays.asList(taxids.split(",")));
        }

        if (ecids != null) {
            addECtoExclude(Arrays.asList(ecids.split(",")));
        }

    }

    /**
     *
     * @param t
     */
    public void addTaxons(String t) {

        if (taxons == null) {
            taxons = new HashSet<String>();
        }
        taxons.add(t);
    }

    /**
     *
     * @param ec
     */
    public void addECtoExclude(String ec) {

        if (excludedEC == null) {
            excludedEC = new HashSet<String>();
        }
        excludedEC.add(ec);
    }

    /**
     *
     * @param t
     */
    public void addECtoExclude(Collection<String> t) {

        if (excludedEC == null) {
            excludedEC = new HashSet<String>();
        }
        excludedEC.addAll(t);
    }

    /**
     *
     * @param t
     */
    public void addTaxons(Collection<String> t) {

        if (taxons == null) {
            taxons = new HashSet<String>();
        }
        taxons.addAll(t);
    }

    /**
     *
     * @return a set of string associated to the Taxons to filter
     */
    public Set<String> getTaxons() {
        return taxons;
    }

    /**
     *
     * @return a set of string associated to the Evidence Codes to filter
     */
    public Set<String> getExcludedEC() {
        return excludedEC;
    }

    @Override
    public String toString() {

        String out = super.toString();

        String taxonsRestrictions = "None";
        String excludedECs = "None";

        if (taxons != null) {
            taxonsRestrictions = taxons.toString();
        }

        if (excludedEC != null) {
            excludedECs = excludedEC.toString();
        }


        out += "\nTaxons    : " + taxonsRestrictions;
        out += "\nexcludeEC : " + excludedECs;
        out += "\n";
        return out;
    }
}
