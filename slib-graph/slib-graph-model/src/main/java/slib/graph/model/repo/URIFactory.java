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
package slib.graph.model.repo;

import java.util.Map;
import org.openrdf.model.URI;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Interface defining a Factory which must be used to create the URIs or namespace.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public interface URIFactory{

   
    /**
     *
     * @param prefix
     * @param reference
     * @return true if the prefix is loaded
     * @throws SLIB_Ex_Critic
     */
    public boolean loadNamespacePrefix(String prefix, String reference) throws SLIB_Ex_Critic;


    /**
     * @param ns_prefix
     * @return the namespace associated to the prefix
     */
    public String getNamespace(String ns_prefix);
    
    public URI getURI(String sURI);
    
    /**
     * Loaded prefixes will be used, use {@link URIFactory#getURI(java.lang.String) } if loaded prefixes are not used.
     * An URI with a prefix is for instance GO:xxxxx.
     * Considering that GO is defined as PREFIX for http://go/ the URI will be http://go/xxxxx.
     * 
     * @param sURI
     * @param useLoadedPrefix set to true loaded prefixes will be used.
     * @throws IllegalArgumentException if the URI is not well formed no error if the prefix do not exists
     * @return the URI
     */
    public URI getURI(String sURI,boolean useLoadedPrefix);
    
    public URI getURI(String snamespace, String localName);
    
    public void clear();
    
    /**
     * Create a string representing the URI using the loaded prefixes if any can be used
     * @param uri the URI
     * @return  the URI as a string
     */
    public String shortURIasString(URI uri);
    
    public Map<String,String> getURIPrefixes();

}
