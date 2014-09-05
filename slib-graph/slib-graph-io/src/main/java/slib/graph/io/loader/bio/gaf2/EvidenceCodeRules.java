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
package slib.graph.io.loader.bio.gaf2;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Evidence codes are not statements of the quality of the annotation. Within
 * each evidence code classification, some methods produce annotations of higher
 * confidence or greater specificity than other methods, in addition the way in
 * which a technique has been applied or interpreted in a paper will also affect
 * the quality of the resulting annotation. Thus evidence codes cannot be used
 * as a measure of the quality of the annotation.
 *
 * The EXP code is the parent code for the IDA, IPI, IMP, IGI and IEP
 * experimental codes. Experimental Evidence Codes
 *
 * EXP: Inferred from Experiment -- IDA: Inferred from Direct Assay -- IPI:
 * Inferred from Physical Interaction -- IMP: Inferred from Mutant Phenotype --
 * IGI: Inferred from Genetic Interaction -- IEP: Inferred from Expression
 * Pattern
 *
 * Computational Analysis Evidence Codes ISS: Inferred from Sequence or
 * Structural Similarity -- ISO: Inferred from Sequence Orthology -- ISA:
 * Inferred from Sequence Alignment -- ISM: Inferred from Sequence Model
 *
 * IGC: Inferred from Genomic Context IBA: Inferred from Biological aspect of
 * Ancestor IBD: Inferred from Biological aspect of Descendant IKR: Inferred
 * from Key Residues IRD: Inferred from Rapid Divergence RCA: inferred from
 * Reviewed Computational Analysis
 *
 * IEA: Inferred from Electronic Annotation
 *
 * Author Statement Evidence Codes TAS: Traceable Author Statement NAS:
 * Non-traceable Author Statement
 *
 * Curator Statement Evidence Codes IC: Inferred by Curator ND: No biological
 * Data available
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class EvidenceCodeRules {

    /**
     *
     */
    public final static Set<String> EXPchildren = new HashSet<String>();

    static {
        EXPchildren.add("IDA");
        EXPchildren.add("IPI");
        EXPchildren.add("IMP");
        EXPchildren.add("IGI");
        EXPchildren.add("IEP");
    }

    /**
     *
     */
    public final static Set<String> ISSchildren = new HashSet<String>();

    static {
        ISSchildren.add("ISO");
        ISSchildren.add("ISA");
        ISSchildren.add("ISM");
    }

    /**
     * Evaluate if a particular EC respect restrictions contains in a Collection
     * of String defining invalid EC EC hierarchy is considered during the
     * evaluation
     *
     * @param ecCodesRestriction a Collection of String defining EC restrictions
     * @param ec the EC evaluated
     * @return boolean true if the evaluated EC is valid considered given
     * restrictions
     */
    public static boolean areValid(Collection<String> ecCodesRestriction, String ec) {

        for (String ecR : ecCodesRestriction) {

            if (ecR.equals(ec)) {
                return false;
            } else if (ecR.equals("EXP") && EXPchildren.contains(ec)) {
                return false;
            } else if (ecR.equals("ISS") && ISSchildren.contains(ec)) {
                return false;
            }
        }
        return true;
    }

}
