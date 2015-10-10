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
package slib.tools.smltoolkit.sm.cli.profile.mesh;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import slib.tools.module.ToolCmdHandlerCst;
import slib.tools.smltoolkit.SmlToolKitCliCst;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class SmProfileMeSHCst extends ToolCmdHandlerCst {

    /**
     *
     */
    private static final String _moduleName = SmlToolKitCliCst.ToolName_SM;
    /**
     *
     */
    private static final String _appCmdName = SmlToolKitCliCst._appCmdName + " -t " + _moduleName + " -profile MESH";
    /**
     *
     */
    private final static boolean _debugMode = false;
    static String ANNOTSFORMAT_DEFAULT = "TSV";
    static String MTYPE_PAIRWISE = "p";
    static String MTYPE_GROUPWISE = "g";
    static String MTYPE_DEFAULT = "p";
    static String NOTFOUND_DEFAULT = "exclude";
    static String NOANNOTS_DEFAULT = "set=-1";
    static String THREADS_DEFAULT = "1";
    /*
     * Error messages  
     */

    /*
     * Setting Options 
     */
    @SuppressWarnings("static-access")
    private static final Option _mesh = OptionBuilder.withArgName("file path")
            .hasArg()
            .withDescription("\nThe path to the MeSH XML format - from version 2014, e.g. desc2014.xml. This file is required. The DTD, e.g. desc2014.dtd, must also be located in the same directory\n")
            .create("mesh");
    @SuppressWarnings("static-access")
    private static final Option _annots = OptionBuilder.withArgName("file path")
            .hasArg()
            .withDescription("\nThe path to the annotation file. This file defines a set of entities characterised by a set of MeSH descriptors (DescriptorUIs), e.g., documents annotated by MesH descriptors. The file must contains an entity description per line. The entry must be of the form entity id[tabulation]list of mesh descriptors (DescriptorUIs) separated by ';'. This file is required for groupwise measures (-mtype g see above) "
                    + "or any measure relying on a extrinsic metric (e.g. Resnik's Information Content)")
            .create("annots");
    @SuppressWarnings("static-access")
    private static final Option _queries = OptionBuilder.withArgName("file path")
            .hasArg()
            .withDescription("\nThe path to the file which contains the queries (one per line), i.e. the pairs of MeSH descriptors or the pairs of entity ids. In all cases, the entity or MeSH descriptor ids must be separated by a tabulation, i.e. each line must be of the form id_1[tabulation]id_2. This file is always required. An example is provided above.")
            .create("queries");
    @SuppressWarnings("static-access")
    private static final Option _output = OptionBuilder.withArgName("file path")
            .hasArg()
            .withDescription("\nOutput file to store the results (required)")
            .create("output");
    @SuppressWarnings("static-access")
    private static final Option _mtype = OptionBuilder.withArgName("type")
            .hasArg()
            .withDescription("\nThe type of semantic measures you want to use:\n"
                    + "- 'p' (pairwise) to compute semantic measures between MeSH descriptors.\n"
                    + "- 'g' (groupwise) to compute semantic measures between sets of MeSH descriptors.\n"
                    + "accepted values [p,g], default p\n"
                    + "example -mtype p")
            .create("mtype");
    @SuppressWarnings("static-access")
    private static final Option _notfound = OptionBuilder.withArgName("flag")
            .hasArg()
            .withDescription("\nDefine the behavior if an entry element of the query file cannot be found: (i) in pairwise measures: one of the two MeSH descriptors cannot be found, (ii) in groupwise measures: one of the two gene products cannot be found."
                    + " Accepted values [exclude, stop, set=<value>]:\n"
                    + "- 'exclude' the entry will not be processed (a message will be logged if -quiet is not used)\n"
                    + "- 'stop'    the program will stop\n"
                    + "- 'set=<value>' the entry will not be processed (a message will be logged if -quiet is not used).\n"
                    + "default value = 'exclude'")
            .create("notfound");
    @SuppressWarnings("static-access")
    private static final Option _pm = OptionBuilder.withArgName("flag")
            .hasArg()
            .withDescription("\nPairwise measure [ 'resnik', 'lin', 'schlicker', 'jc' ] (required for pairwise measures or indirect groupwise measures). See the complete list on the website.")
            .create("pm");
    @SuppressWarnings("static-access")
    private static final Option _gm = OptionBuilder.withArgName("flag")
            .hasArg()
            .withDescription("\nDirect groupwise measure [ 'to', 'nto', 'gic', 'ui', ... ] or aggregation method (mixing strategy) [ 'min', 'max', 'bma', 'bmm', ... ] if an indirect groupwise measure is be used (require a pairwise measure to be set). See the complete list on the website.")
            .create("gm");
    @SuppressWarnings("static-access")
    private static final Option _ic = OptionBuilder.withArgName("flag")
            .hasArg()
            .withDescription("\nInformation content method. Extrinsic (require annotation file): [ 'resnik' ], Intrinsic : [ 'sanchez', 'zhou', 'seco' ]. See the complete list on the website.")
            .create("ic");
    @SuppressWarnings("static-access")
    private static final Option _quiet = OptionBuilder.withArgName("quiet")
            .withDescription("\nDo not show warning messages")
            .create("quiet");
    @SuppressWarnings("static-access")
    private static final Option _notr = OptionBuilder.withArgName("notr")
            .withDescription("\nDo not perform a transitive reduction of the MeSH")
            .create("notrmesh");
    @SuppressWarnings("static-access")
    private static final Option _notrannots = OptionBuilder.withArgName("notrannots")
            .withDescription("\nDo not remove annotation redundancy, i.e. if an entity is annoted by two MeSH descriptors {X,Y} and X is subsumed by Y in the MeSH, the MeSH descriptor Y will be removed from the annotations.")
            .create("notrannots");
    @SuppressWarnings("static-access")
    private static final Option _threads = OptionBuilder.withArgName("nb")
            .hasArg()
            .withDescription("\nInteger definying the number of threads to use, i.e. processes allocates to the execution, default 1.\n Setting more threads reduce execution time, suited configuration depends on tour computer, use with care if you don't get the implications in term of computational resources which will be used.")
            .create("threads");
    /*
     * Use this data structure to define order of options in help message
     */
    private final static Map<Option, Integer> _optionsOrder = new LinkedHashMap<Option, Integer>();

    static {
        _optionsOrder.put(_mesh, _optionsOrder.size());
        _optionsOrder.put(_annots, _optionsOrder.size());
        _optionsOrder.put(_queries, _optionsOrder.size());
        _optionsOrder.put(_output, _optionsOrder.size());
        _optionsOrder.put(_mtype, _optionsOrder.size());
        _optionsOrder.put(_notfound, _optionsOrder.size());
        _optionsOrder.put(_pm, _optionsOrder.size());
        _optionsOrder.put(_gm, _optionsOrder.size());
        _optionsOrder.put(_ic, _optionsOrder.size());
        _optionsOrder.put(_quiet, _optionsOrder.size());
        _optionsOrder.put(_notr, _optionsOrder.size());
        _optionsOrder.put(_notrannots, _optionsOrder.size());
        _optionsOrder.put(_threads, _optionsOrder.size());
    }

    /**
     *
     */
    public SmProfileMeSHCst() {
        super(_appCmdName, _debugMode, SmProfileMeSHCst._optionsOrder);
    }
}
