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
package slib.tools.smltoolkit.sm.cli.profile.go;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import slib.graph.io.util.GFormat;
import slib.tools.module.ToolCmdHandlerCst;
import slib.tools.smltoolkit.SmlToolKitCliCst;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class SmProfileGOCst extends ToolCmdHandlerCst {

    /**
     *
     */
    private static final String _moduleName = SmlToolKitCliCst.ToolName_SM;
    /**
     *
     */
    private static final String _appCmdName = SmlToolKitCliCst._appCmdName + " -t " + _moduleName + " -profile GO";
    /**
     *
     */
    private static boolean _debugMode = false;
    static String ANNOTSFORMAT_DEFAULT = "GAF2";
    static String GOFORMAT_DEFAULT = "OBO";
    static String GOFORMAT_OWL = "OWL";
    static String[] GOFORMAT_VALID = {GFormat.OBO.name(),GOFORMAT_OWL, GFormat.RDF_XML.name()};
    static String MTYPE_PAIRWISE = "p";
    static String MTYPE_GROUPWISE = "g";
    static String MTYPE_DEFAULT = "p";
    static String ASPECT_DEFAULT = "BP";
    static String NOTFOUND_DEFAULT = "exclude";
    static String NOANNOTS_DEFAULT = "set=-1";
    static String THREADS_DEFAULT = "1";
    /*
     * Error messages  
     */
    /**
     *
     */
    public static final String errorMissingXMLconfOrProfile = "[ERROR] Please specify a profile or an Xml configuration file";
    /*
     * Setting Options 
     */
    @SuppressWarnings("static-access")
    private static final Option _go = OptionBuilder.withArgName("file path")
            .hasArg()
            .withDescription("\nThe path to the GO (required)\n")
            .create("go");
    @SuppressWarnings("static-access")
    private static final Option _go_format = OptionBuilder.withArgName("format")
            .hasArg()
            .withDescription("\nThe ontology file format "+Arrays.toString(GOFORMAT_VALID)+", default "+GOFORMAT_DEFAULT+"\n")
            .create("goformat");
    @SuppressWarnings("static-access")
    private static final Option _annots = OptionBuilder.withArgName("file path")
            .hasArg()
            .withDescription("\nThe path to the annotation file. Required for groupwise measures (-mtype g see above) "
            + "or any measure relying on a extrinsic metric (e.g. Resnik's Information Content)")
            .create("annots");
    @SuppressWarnings("static-access")
    private static final Option _annotsFormat = OptionBuilder.withArgName("format")
            .hasArg()
            .withDescription("\nThe format of the annotation file, accepted values [GAF2,TSV], default GAF2")
            .create("annotsFormat");
    @SuppressWarnings("static-access")
    private static final Option _queries = OptionBuilder.withArgName("file path")
            .hasArg()
            .withDescription("\nThe path to the file containing the queries, i.e. the pairs of GO term or gene product ids separated by tabs (required). An example is provided above.")
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
            + "- 'p' (pairwise) to compute semantic measures between GO terms.\n"
            + "- 'g' (groupwise) to compute semantic measures between gene products.\n"
            + "accepted values [p,g], default p\n"
            + "example -mtype p")
            .create("mtype");
    @SuppressWarnings("static-access")
    private static final Option _aspect = OptionBuilder.withArgName("type")
            .hasArg()
            .withDescription("\nSpecify the aspect of the GO to use:\n"
            + "- 'MF'  Molecular Function\n"
            + "- 'BP'  Biological Process\n"
            + "- 'CC'  Cellular Component\n"
            + "- 'GLOBAL'  the three aspects MF-BP-CC will be used using a virtual root between the three\n"
            + "- 'custom=<GO term id>' specify a GO term which will be considered as root e.g. custom=GO:XXXXX.\n"
            + "accepted values [MF,BP,CC,GLOBAL,custom=<GO term id>], default BP. examples (1) -aspect MF (2) -aspect custom=GO:XXXXX")
            .create("aspect");
    @SuppressWarnings("static-access")
    private static final Option _notfound = OptionBuilder.withArgName("flag")
            .hasArg()
            .withDescription("\nDefine the behavior if an entry element of the query file cannot be found: (i) in pairwise measures: one of the two GO terms cannot be found, (ii) in groupwise measures: one of the two gene products cannot be found."
            + " Accepted values [exclude, stop, set=<value>]:\n"
            + "- 'exclude' the entry will not be processed (a message will be logged if -quiet is not used)\n"
            + "- 'stop'    the program will stop\n"
            + "- 'set=<value>' the entry will not be processed (a message will be logged if -quiet is not used).\n"
            + "default value = 'exclude'")
            .create("notfound");
    @SuppressWarnings("static-access")
    private static final Option _noannots = OptionBuilder.withArgName("flag")
            .hasArg()
            .withDescription("\nDefine the behavior if a gene product of the query file doesn't have annotation (GO terms): Accepted values [exclude,stop, set=<value>]:"
            + " Accepted values [exclude, stop, set=<value>]:\n"
            + "- 'exclude' the entry will not be processed (a message will be logged if -quiet is not used)\n"
            + "- 'stop'    the program will stop\n"
            + "- 'set=<value>' the entry will not be processed (a message will be logged if -quiet is not used).\n"
            + "default value 'set=0' the score is set to 0")
            .create("noannots");
    @SuppressWarnings("static-access")
    private static final Option _filter = OptionBuilder.withArgName("params")
            .hasArg()
            .withDescription("\nThis parameter can be used to filter the GO terms associated to a gene product when the provided annotation file is in GAF2 format.\n"
            + "- EC=<evidence_codes> evidence codes separated by commas e.g. EC=IEA only IEA annotations will be considered.\n"
            + "- Taxon=<taxon_ids> taxon ids separated by commas e.g. Taxon=9696 to only consider annotations associated to Taxon 9696.\n"
            + "Exemple of value -filter EC=IEA,XXX:Taxon=9696."
            + "Default value no filter")
            .create("filter");
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
    private static final Option _notrgo = OptionBuilder.withArgName("notrgo")
            .withDescription("\nDo not perform a transitive reduction of the GO")
            .create("notrgo");
    @SuppressWarnings("static-access")
    private static final Option _notrannots = OptionBuilder.withArgName("notrannots")
            .withDescription("\nDo not remove annotation redundancy i.e. if a gene product is annoted by two GO terms {X,Y} and X is subsumed by Y in the GO, the GO term Y will be removed from the annotations.")
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
        _optionsOrder.put(_go, _optionsOrder.size());
        _optionsOrder.put(_go_format, _optionsOrder.size());
        _optionsOrder.put(_annots, _optionsOrder.size());
        _optionsOrder.put(_annotsFormat, _optionsOrder.size());
        _optionsOrder.put(_queries, _optionsOrder.size());
        _optionsOrder.put(_output, _optionsOrder.size());
        _optionsOrder.put(_mtype, _optionsOrder.size());
        _optionsOrder.put(_aspect, _optionsOrder.size());
        _optionsOrder.put(_notfound, _optionsOrder.size());
        _optionsOrder.put(_noannots, _optionsOrder.size());
        _optionsOrder.put(_filter, _optionsOrder.size());
        _optionsOrder.put(_pm, _optionsOrder.size());
        _optionsOrder.put(_gm, _optionsOrder.size());
        _optionsOrder.put(_ic, _optionsOrder.size());
        _optionsOrder.put(_quiet, _optionsOrder.size());
        _optionsOrder.put(_notrgo, _optionsOrder.size());
        _optionsOrder.put(_notrannots, _optionsOrder.size());
        _optionsOrder.put(_threads, _optionsOrder.size());
    }

    /**
     *
     */
    public SmProfileGOCst() {
        super(_appCmdName, _debugMode, SmProfileGOCst._optionsOrder);
    }
}
