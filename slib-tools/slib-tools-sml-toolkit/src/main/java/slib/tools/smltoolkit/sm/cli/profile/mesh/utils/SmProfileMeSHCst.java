///*
//
// Copyright or © or Copr. Ecole des Mines d'Alès (2012) 
//
// This software is a computer program whose purpose is to 
// process semantic graphs.
//
// This software is governed by the CeCILL  license under French law and
// abiding by the rules of distribution of free software.  You can  use, 
// modify and/ or redistribute the software under the terms of the CeCILL
// license as circulated by CEA, CNRS and INRIA at the following URL
// "http://www.cecill.info". 
//
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
//
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
//
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL license and that you accept its terms.
//
// */
//package slib.tools.smltoolkit.sm.cli.profile.mesh.utils;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//import org.apache.commons.cli.Option;
//import org.apache.commons.cli.OptionBuilder;
//import slib.tools.module.ToolCmdHandlerCst;
//import slib.tools.smltoolkit.SmlToolKitCliCst;
//
///**
// *
// * @author Sébastien Harispe
// */
//public class SmProfileMeSHCst extends ToolCmdHandlerCst {
//
//    /**
//     *
//     */
//    private static final String _moduleName = SmlToolKitCliCst.ToolName_SM;
//    /**
//     *
//     */
//    private static final String _appCmdName = SmlToolKitCliCst._appCmdName + " -t " + _moduleName + " -profile MESH";
//    /**
//     *
//     */
//    private static boolean _debugMode = false;
//    static String ANNOTSFORMAT_DEFAULT = "TSV";
//    static String MTYPE_PAIRWISE = "p";
//    static String MTYPE_GROUPWISE = "g";
//    static String MTYPE_DEFAULT = "p";
//    static String NOTFOUND_DEFAULT = "exclude";
//    static String NOANNOTS_DEFAULT = "set=-1";
//    static String THREADS_DEFAULT = "1";
//    /*
//     * Error messages  
//     */
//
//    /*
//     * Setting Options 
//     */
//    @SuppressWarnings("static-access")
//    private static final Option _mesh = OptionBuilder.withArgName("file path")
//            .hasArg()
//            .withDescription("\nThe path to the GO in OBO 1.2 format (required)\n")
//            .create("mesh");
//    @SuppressWarnings("static-access")
//    private static final Option _annots = OptionBuilder.withArgName("file path")
//            .hasArg()
//            .withDescription("\nThe path to the annotation file. Required for groupwise measures (-mtype g see above) "
//            + "or any measure relying on a extrinsic metric (e.g. Resnik's Information Content)")
//            .create("annots");
//    @SuppressWarnings("static-access")
//    private static final Option _queries = OptionBuilder.withArgName("file path")
//            .hasArg()
//            .withDescription("\nThe path to the file containing the queries, i.e. the pairs of GO term or gene product ids separated by tabs (required). An example is provided above.")
//            .create("queries");
//    @SuppressWarnings("static-access")
//    private static final Option _output = OptionBuilder.withArgName("file path")
//            .hasArg()
//            .withDescription("\nOutput file to store the results (required)")
//            .create("output");
//    @SuppressWarnings("static-access")
//    private static final Option _mtype = OptionBuilder.withArgName("type")
//            .hasArg()
//            .withDescription("\nThe type of semantic measures you want to use:\n"
//            + "- 'p' (pairwise) to compute semantic measures between GO terms.\n"
//            + "- 'g' (groupwise) to compute semantic measures between gene products.\n"
//            + "accepted values [p,g], default p\n"
//            + "example -mtype p")
//            .create("mtype");
//    @SuppressWarnings("static-access")
//    private static final Option _aspect = OptionBuilder.withArgName("type")
//            .hasArg()
//            .withDescription("\nSpecify the aspect of the GO to use:\n"
//            + "- 'MF'  Molecular Function\n"
//            + "- 'BP'  Biological Process\n"
//            + "- 'CC'  Cellular Component\n"
//            + "- 'GLOBAL'  the three aspects MF-BP-CC will be used using a virtual root between the three\n"
//            + "- 'custom=<GO term id>' specify a GO term which will be considered as root e.g. custom=GO:XXXXX.\n"
//            + "accepted values [MF,BP,CC,GLOBAL,custom=<GO term id>], default BP. examples (1) -aspect MF (2) -aspect custom=GO:XXXXX")
//            .create("aspect");
//    @SuppressWarnings("static-access")
//    private static final Option _notfound = OptionBuilder.withArgName("flag")
//            .hasArg()
//            .withDescription("\nDefine the behavior if an entry element of the query file cannot be found: (i) in pairwise measures: one of the two GO terms cannot be found, (ii) in groupwise measures: one of the two gene products cannot be found."
//            + " Accepted values [exclude, stop, set=<value>]:\n"
//            + "- 'exclude' the entry will not be processed (a message will be logged if -quiet is not used)\n"
//            + "- 'stop'    the program will stop\n"
//            + "- 'set=<value>' the entry will not be processed (a message will be logged if -quiet is not used).\n"
//            + "default value = 'exclude'")
//            .create("notfound");
//    @SuppressWarnings("static-access")
//    private static final Option _pm = OptionBuilder.withArgName("flag")
//            .hasArg()
//            .withDescription("\nPairwise measure see the list of available measures below (required for pairwise measures or indirect groupwise measures)")
//            .create("pm");
//    @SuppressWarnings("static-access")
//    private static final Option _gm = OptionBuilder.withArgName("flag")
//            .hasArg()
//            .withDescription("\nDirect groupwise measure or aggregation method if an indirect groupwise measure must be used (require a pairwise measure to be set). see the list of available measures below (required for groupwise measures).")
//            .create("gm");
//    @SuppressWarnings("static-access")
//    private static final Option _ic = OptionBuilder.withArgName("flag")
//            .hasArg()
//            .withDescription("\nInformation content method see the list of IC available below")
//            .create("ic");
//    @SuppressWarnings("static-access")
//    private static final Option _quiet = OptionBuilder.withArgName("quiet")
//            .withDescription("\nDo not show warning messages")
//            .create("quiet");
//    @SuppressWarnings("static-access")
//    private static final Option _notr = OptionBuilder.withArgName("notr")
//            .withDescription("\nDo not perform a transitive reduction of the GO")
//            .create("notrgo");
//    @SuppressWarnings("static-access")
//    private static final Option _notrannots = OptionBuilder.withArgName("notrannots")
//            .withDescription("\nDo not remove annotation redundancy i.e. if a gene product is annoted by two GO terms {X,Y} and X is subsumed by Y in the GO, the GO term Y will be removed from the annotations.")
//            .create("notrannots");
//    @SuppressWarnings("static-access")
//    private static final Option _threads = OptionBuilder.withArgName("nb")
//            .hasArg()
//            .withDescription("\nInteger definying the number of threads to use, i.e. processes allocates to the execution, default 1.\n Setting more threads reduce execution time, suited configuration depends on tour computer, use with care if you don't get the implications in term of computational resources which will be used.")
//            .create("threads");
//    /*
//     * Use this data structure to define order of options in help message
//     */
//    private final static Map<Option, Integer> _optionsOrder = new LinkedHashMap<Option, Integer>();
//
//    static {
//        _optionsOrder.put(_mesh, _optionsOrder.size());
//        _optionsOrder.put(_annots, _optionsOrder.size());
//        _optionsOrder.put(_queries, _optionsOrder.size());
//        _optionsOrder.put(_output, _optionsOrder.size());
//        _optionsOrder.put(_mtype, _optionsOrder.size());
//        _optionsOrder.put(_aspect, _optionsOrder.size());
//        _optionsOrder.put(_notfound, _optionsOrder.size());
//        _optionsOrder.put(_pm, _optionsOrder.size());
//        _optionsOrder.put(_gm, _optionsOrder.size());
//        _optionsOrder.put(_ic, _optionsOrder.size());
//        _optionsOrder.put(_quiet, _optionsOrder.size());
//        _optionsOrder.put(_notr, _optionsOrder.size());
//        _optionsOrder.put(_notrannots, _optionsOrder.size());
//        _optionsOrder.put(_threads, _optionsOrder.size());
//    }
//
//    /**
//     *
//     */
//    public SmProfileMeSHCst() {
//        super(_appCmdName, _debugMode, SmProfileMeSHCst._optionsOrder);
//    }
//}
