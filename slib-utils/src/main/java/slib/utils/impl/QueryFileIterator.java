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
package slib.utils.impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Query Iterator implementing the {@link QueryIterator} interface used to
 * iterate through the queries defined in a tabular delimited file i.e. e1\te2
 *
 * @author Sebastien Harispe
 *
 */
public class QueryFileIterator implements QueryIterator {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    BufferedReader br;
    DataInputStream in;
    String filepath;
    String line;
    final String uriPrefix;

    /**
     * Constructor of a {@link QueryFileIterator}
     *
     * @param filepath the file to load as repository
     * @throws IOException if an IO exception appends
     */
    public QueryFileIterator(String filepath) throws IOException {
        this(filepath, null);
    }

    /**
     * Constructor of a {@link QueryFileIterator}
     *
     * @param filepath the file to load as repository
     * @param uriPrefix
     * @throws IOException if an IO exception appends
     */
    public QueryFileIterator(String filepath, String uriPrefix) throws IOException {

        logger.info("Loading Query file iterator: "+filepath);
        logger.info("prefix (query file iterator): "+uriPrefix);
        this.filepath = filepath;
        this.uriPrefix = uriPrefix;


        FileInputStream fstream = new FileInputStream(filepath);
        in = new DataInputStream(fstream);
        br = new BufferedReader(new InputStreamReader(in));
        line = br.readLine();



    }

    @Override
    public boolean hasNext() {
        if (line == null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new IO_RuntimeException("Error reading file " + filepath + " original " + e.getMessage());
            }
        }

        return line != null;
    }

    /**
     * Note the Query Entry returned can be equal to null if the line is not in
     * agreement with the expectations i.e. e1 \t e2
     */
    @Override
    public QueryEntry next() {

        QueryEntry entry = null;

        try {
            String[] csvRow = line.split("\t");


            if (csvRow.length == 2) {

                if (uriPrefix != null) {
                    entry = new QueryEntry(uriPrefix + csvRow[0], uriPrefix + csvRow[1]);
                } else {
                    entry = new QueryEntry(csvRow[0], csvRow[1]);
                }
            }

            line = br.readLine();

        } catch (IOException e) {
            throw new IO_RuntimeException("Error reading file " + filepath + " original " + e.getMessage());
        }


        return entry;
    }

    @Override
    public void close() throws IOException {

        if (br != null) {
            br.close();
        }
    }

    @Override
    public List<QueryEntry> nextValids(int nbValues) {

        List<QueryEntry> bench = new ArrayList<QueryEntry>();
        int c = 0;

        while (c < nbValues && hasNext()) {
            QueryEntry e = next();

            if (e != null && e.isValid()) {
                bench.add(e);
                c++;
            }
        }
        return bench;
    }

    @Override
    public long getNumberQueries() throws Exception {

        LineNumberReader lnr;
        lnr = new LineNumberReader(new FileReader(new File(filepath)));
        lnr.skip(Long.MAX_VALUE);

        return lnr.getLineNumber()+1; // line numbering starts at 0
    }
}
