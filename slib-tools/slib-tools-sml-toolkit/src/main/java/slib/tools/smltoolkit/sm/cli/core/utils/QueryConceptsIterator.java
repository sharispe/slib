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
package slib.tools.smltoolkit.sm.cli.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openrdf.model.URI;
import slib.graph.model.graph.G;
import slib.utils.impl.QueryEntry;
import slib.utils.impl.QueryIterator;

/**
 * Utility used to iterate along the pairs of concepts which can be build
 * according to the set of concepts (CLASS) composing the graph. Considered as
 * Symmetric
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class QueryConceptsIterator implements QueryIterator {

    G g;
    List<URI> classes;
    int i = 0;
    int j = 1;

    public QueryConceptsIterator(Set<URI> classes) {
        this.classes = new ArrayList<URI>(classes);
    }

    @Override
    public QueryEntry next() {

        QueryEntry q = new QueryEntry(classes.get(i).stringValue(), classes.get(j).stringValue());
        
        j++;
        if (j == classes.size()) {
            i++;
            j = i+1;
        }
        return q;

    }

    @Override
    public List<QueryEntry> nextValids(int nbValues) throws Exception {

        List<QueryEntry> s = new ArrayList<QueryEntry>();
        for (int k = 0; k < nbValues && hasNext(); k++) {
            s.add(next());
        }
        return s;
    }

    @Override
    public boolean hasNext() {
        
//        System.out.println(i+"  "+j);
        if (i < classes.size() && j < classes.size()) {
            return true;
        }
        return false;
    }

    @Override
    public long getNumberQueries() throws Exception {
        return (classes.size() * (classes.size() - 1)) / 2;
    }

    @Override
    public void close() throws Exception {
        // do nothing
    }
}
