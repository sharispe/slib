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
package slib.graph.algo.accessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class InstanceAccessorGen implements InstanceAccessor {

    Set<URI> instances;
    Map<URI, Set<URI>> mapInstancesClass;

    public InstanceAccessorGen(Set<URI> instances, Map<URI, Set<URI>> mapClassInstances) {
        this.instances = instances;
        this.mapInstancesClass = new HashMap();
        for (Map.Entry<URI, Set<URI>> e : mapClassInstances.entrySet()) {

            for (URI i : e.getValue()) {
                if (!instances.contains(i)) {
                    throw new IllegalArgumentException("Error loading Instance Accessor "
                            + "URI " + i + " defined as an instance of class "+e.getKey()+" "
                            + "is not defined as an instance...");
                }
                if (!mapInstancesClass.containsKey(i)) {
                    mapInstancesClass.put(i, new HashSet<URI>());
                }
                mapInstancesClass.get(i).add(e.getKey());
            }

        }
    }

    @Override
    public Iterator<URI> getInstancesIt() {
        return getInstances().iterator();
    }

    @Override
    public Set<URI> getInstances() {
        return instances;
    }

    @Override
    public int getInstanceNb() {
        return getInstances().size();
    }

    @Override
    public Iterator<URI> getClassesOfInstanceIt(URI instance) {
        return getClassesOfInstance(instance).iterator();
    }

    @Override
    public Set<URI> getClassesOfInstance(URI instance) {
        errorIfNotInstance(instance);
        return mapInstancesClass.get(instance);
    }

    private void errorIfNotInstance(URI instance) {
        if (!instances.contains(instance)) {
            throw new IllegalArgumentException("URI " + instance + " does not refer to an instance in the given instance Accessor");
        }
    }

}
