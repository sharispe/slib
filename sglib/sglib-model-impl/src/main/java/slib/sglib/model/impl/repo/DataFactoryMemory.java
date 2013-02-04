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
package slib.sglib.model.impl.repo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import slib.sglib.model.graph.G;
import slib.sglib.model.repo.DataFactory;
import slib.sglib.model.repo.PredicateFactory;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * This class defines the singleton used as an in memory repository which manage
 * all used URI and storage element i.e. graphs and knowledge base. The main
 * goal of the DataFactory singleton is to avoid URI and NameSpace object
 * duplicates and must therefore be used to create and load URIs.
 *
 * The repository must be in agreement to graph representation i.e. URI loaded
 * in a graph are linked to the corresponding storage element in the Data
 * repository. All change of the graph must be propagated on the Data Repository
 *
 * @author Sebastien Harispe
 *
 */
public class DataFactoryMemory extends SLIBValueFactory implements DataFactory {

    private static DataFactoryMemory repository;
    private  PredicateFactory eTypes;
    private Set<String> namespaces;
    private Map<String, String> namespacesPrefix;
    private Map<URI, G> graphs;

    /**
     * @return the {@link DataRepository} singleton
     */
    public static DataFactoryMemory getSingleton() {
        
        if (repository == null) {
            repository = new DataFactoryMemory();
        }
        return repository;
    }

    private DataFactoryMemory() {
        
        eTypes           = PredicateURIRepo.getInstance(this);
        namespaces       = new HashSet<String>();
        namespacesPrefix = new HashMap<String, String>();
        graphs           = new HashMap<URI, G>();
    }

    @Override
    public void createNamespace(String nm_s) {
        namespaces.add(nm_s);
    }

    @Override
    public boolean loadNamespacePrefix(String prefix, String reference) throws SLIB_Ex_Critic {

        if (!namespaces.contains(reference)) {
            createNamespace(reference);
        }

        if (!namespacesPrefix.containsKey(prefix)) {
            namespacesPrefix.put(prefix, reference);
            return true;
        } else if (namespacesPrefix.containsKey(prefix) && !namespacesPrefix.get(prefix).equals(reference)) {
            throw new SLIB_Ex_Critic("Cannot include namespace prefix " + prefix + " for namespace " + reference + ""
                    + "\n prefix already linked to " + namespacesPrefix.get(prefix));
        }
        return false;
    }

    @Override
    public G getGraph(URI uri) {
        return graphs.get(uri);
    }

    @Override
    public Map<URI, G> getGraphs() {
        return graphs;
    }

    @Override
    public void addGraph(G g) {
        graphs.put(g.getURI(), g);
    }

    /**
     * Return the namespace associated to the given prefix
     *
     * @param ns_prefix
     * @return the associated namespace or null
     */
    @Override
    public String getNamespace(String ns_prefix) {
        if (ns_prefix == null) {
            return null;
        }
        return namespacesPrefix.get(ns_prefix);
    }
    
    @Override
    public PredicateFactory getPredicateFactory(){
        return eTypes;
    }

    @Override
    public Set<URI> getURIs() {
        return getMemURIs();
    }
    
    @Override
    public void clear() {
        super.clear();
        namespacesPrefix.clear();
        namespaces.clear();
        graphs.clear();
        eTypes.clear();       
    }
    
    

}
