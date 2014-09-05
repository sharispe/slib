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
package slib.indexer.snomed_ct;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.indexer.URIDescriptionBasic;
import slib.indexer.IndexHash;
import slib.graph.model.graph.G;
import slib.graph.model.repo.URIFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class IndexerSNOMEDCT_RF2 {

    private static final int DESCRIPTION_CONCEPT_ID = 4;
    private static final int DESCRIPTION_ACTIVE = 2;
    private static final int DESCRIPTION_TERM = 7;
    private static final int DESCRIPTION_DATE = 1;
    static Logger logger = LoggerFactory.getLogger(IndexerSNOMEDCT_RF2.class);
    static Pattern p_tab = Pattern.compile("\\t");
    static URIFactory repo;

    /**
     * Only load an index for the URI already loaded
     *
     * @param factory
     * @param graph
     * @param description_file
     * @param defaultNamespace
     * @param EXCLUDE_INACTIVE_DESCRIPTIONS
     * @return the index.
     * @throws SLIB_Exception
     */
    public static IndexHash buildIndex(URIFactory factory, G graph, String description_file, String defaultNamespace, boolean EXCLUDE_INACTIVE_DESCRIPTIONS) throws SLIB_Exception {

        repo = factory;
        logger.info("Building Index");
        logger.info("Description file: " + description_file);
        logger.info("EXCLUDE_INACTIVE_DESCRIPTIONS: " + EXCLUDE_INACTIVE_DESCRIPTIONS);

        IndexHash index = new IndexHash();

        FileInputStream fstream;
        try {
            fstream = new FileInputStream(description_file);

            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line;
            String[] split;

            boolean header = true;


            while ((line = br.readLine()) != null) {

                if (header) {
                    header = false;
                    continue;
                }

                split = p_tab.split(line);

                boolean active = split[DESCRIPTION_ACTIVE].trim().equals("1");

                if (active || !EXCLUDE_INACTIVE_DESCRIPTIONS) {

                    URI cURI = repo.getURI(defaultNamespace + split[DESCRIPTION_CONCEPT_ID]);

                    if (graph.containsVertex(cURI)) { // the concept is loaded in the repository

                        if (!index.getMapping().containsKey(cURI)) { // we add the entry to the collection

                            URIDescriptionBasic i = new URIDescriptionBasic(cURI, split[DESCRIPTION_TERM]);
                            index.getMapping().put(cURI, i);
                        } else {
                            index.getMapping().get(cURI).addDescription(split[DESCRIPTION_TERM]);
                        }
                    }
                }
            }
            in.close();

            logger.info("Process Done");

        } catch (IOException ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        }

        return index;
    }

    /**
     * Same as builIndex removing the index not associated to a loaded vertex
     *
     * @param factory
     * @param description_file
     * @param defaultNamespace
     * @param graph
     * @param EXCLUDE_INACTIVE_DESCRIPTIONS
     * @return the index.
     * @throws SLIB_Exception
     */
    public static IndexHash buildIndex(URIFactory factory, String description_file, String defaultNamespace, G graph, boolean EXCLUDE_INACTIVE_DESCRIPTIONS) throws SLIB_Exception {

        logger.info("Building Index");
        IndexHash index = buildIndex(factory, graph, description_file, defaultNamespace, EXCLUDE_INACTIVE_DESCRIPTIONS);

        logger.info("Cleaning Index");
        Set<URI> toRemove = new HashSet<URI>();
        for (URI k : index.getMapping().keySet()) {

            if (!graph.containsVertex(k)) {
                toRemove.add(k);
            }
        }
        for (URI v : toRemove) {
            index.getMapping().remove(v);
        }
        logger.info("Done");
        return index;

    }
}
