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
package slib.tools.smltoolkit.sm.cli.profile.go;

import java.util.HashMap;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import slib.tools.module.ToolCmdHandlerCst;
import slib.tools.smltoolkit.SmlToolKitCliCst;

/**
 *
 * @author Sébastien Harispe
 */
public class SmProfileGOCst extends ToolCmdHandlerCst {

    /**
     *
     */
    private static final String _moduleName = SmlToolKitCliCst.ToolName_SM;
    /**
     *
     */
    private static final String _appCmdName = SmlToolKitCliCst._appCmdName + " -t " + _moduleName + "-profile GO";
    /**
     *
     */
    private static boolean _debugMode = false;
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
            .withDescription("the path to the GO in OBO 1.2 format (required)")
            .create("go");
    @SuppressWarnings("static-access")
    private static final Option _annots = OptionBuilder.withArgName("file path")
            .hasArg()
            .withDescription("the path to the annotation file. Required for groupwise measures (-mtype g see above) or any measure relying on a extrinsic metric (e.g. Resnik's Information Content)")
            .create("annots");
    @SuppressWarnings("static-access")
    private static final Option _annotsFormat = OptionBuilder.withArgName("format")
            .hasArg()
            .withDescription("the format of the annotation file, accepted values [GAF2,TSV], default GAF2")
            .create("annotsFormat");
    @SuppressWarnings("static-access")
    private static final Option _queries = OptionBuilder.withArgName("file path")
            .hasArg()
            .withDescription("the path to the file containing the queries, i.e. the pairs of GO term or gene product ids separated by tabs (required). An example is provided above.")
            .create("queries");
    @SuppressWarnings("static-access")
    private static final Option _output = OptionBuilder.withArgName("file path")
            .hasArg()
            .withDescription("output file to store the results (required)")
            .create("output");
    @SuppressWarnings("static-access")
    private static final Option _mtype = OptionBuilder.withArgName("type")
            .hasArg()
            .withDescription("the type of semantic measures you want to use: (i) 'p' (pairwise) to compute semantic measures between GO terms. (ii) 'g' (groupwise) to compute semantic measures between gene products. accepted values [p,g], default p. example -mtype p")
            .create("mtype");
    @SuppressWarnings("static-access")
    private static final Option _aspect = OptionBuilder.withArgName("type")
            .hasArg()
            .withDescription("specify the aspect of the GO to use: (i) MF - Molecular Function (ii) BP - Biological Process (iii) CC - Cellular Component (iii) GLOBAL - the three aspects MF-BP-CC will be used using a virtual root between the three (iv) custom=<GO term id> specify a GO term which will be considered as root e.g. custom=GO:XXXXX. accepted values [MF,BP,CC,GLOBAL,custom=<GO term id>], default BP. examples (1) -aspect MF (2) -aspect custom=GO:XXXXX")
            .create("aspect");
    @SuppressWarnings("static-access")
    private static final Option _notfound = OptionBuilder.withArgName("flag")
            .hasArg()
            .withDescription("define the behavior if an entry element of the query file cannot be found: (i) in pairwise measures: one of the two GO terms cannot be found, (ii) in groupwise measures: one of the two gene products cannot be found."
            + " Accepted values [exclude, stop, set=<value>]: "
            + "(i) 'exclude' the entry will not be processed (a message will be logged if -quiet is not used)"
            + "(ii) 'stop'    the program will stop"
            + "(ii) 'set=<value>' the entry will not be processed (a message will be logged if -quiet is not used)."
            + "default value = 'exclude'")
            .create("notfound");
    @SuppressWarnings("static-access")
    private static final Option _noannots = OptionBuilder.withArgName("flag")
            .hasArg()
            .withDescription("define the behavior if a gene product of the query file doesn't have annotation (GO terms): Accepted values [exclude,stop, set=<value>]:"
            + " Accepted values [exclude, stop, set=<value>]: "
            + "(i) 'exclude' the entry will not be processed (a message will be logged if -quiet is not used)"
            + "(ii) 'stop'    the program will stop"
            + "(ii) 'set=<value>' the entry will not be processed (a message will be logged if -quiet is not used)."
            + "default value 'set=0' the score is set to 0")
            .create("noannots");
    @SuppressWarnings("static-access")
    private static final Option _filter = OptionBuilder.withArgName("params")
            .hasArg()
            .withDescription("this parameter can be used to filter the GO terms associated to a gene product when the provided annotation file is in GAF2 format."
            + "(i) EC=<evidence_codes> evidence codes separated by commas e.g. EC=IEA only IEA annotations will be considered."
            + "(ii) Taxon=<taxon_ids> taxon ids separated by commas e.g. Taxon=9696 to only consider annotations associated to Taxon 9696."
            + "Exemple of value -filter EC=IEA,XXX:Taxon=9696."
            + "Default value no filter")
            .create("filter");
    @SuppressWarnings("static-access")
    private static final Option _pm = OptionBuilder.withArgName("flag")
            .hasArg()
            .withDescription("pairwise measure see the list of available measures below (required for pairwise measures or indirect groupwise measures)")
            .create("pm");
    @SuppressWarnings("static-access")
    private static final Option _gm = OptionBuilder.withArgName("flag")
            .hasArg()
            .withDescription("direct groupwise measure or aggregation method if an indirect groupwise measure must be used (require a pairwise measure to be set). see the list of available measures below (required for groupwise measures).")
            .create("gm");
    @SuppressWarnings("static-access")
    private static final Option _ic = OptionBuilder.withArgName("flag")
            .hasArg()
            .withDescription("information content method see the list of IC available below")
            .create("ic");
    @SuppressWarnings("static-access")
    private static final Option _quiet = OptionBuilder.withArgName("quiet")
            .withDescription("do not show warning messages")
            .create("quiet");
    @SuppressWarnings("static-access")
    private static final Option _notrgo = OptionBuilder.withArgName("trgo")
            .withDescription("Do not perform a transitive reduction of the GO")
            .create("trgo");
    @SuppressWarnings("static-access")
    private static final Option _notrannots = OptionBuilder.withArgName("trannots")
            .withDescription("Do not remove annotation redundancy i.e. if a gene product is annoted by two GO terms {X,Y} a X is subsumed by Y in the GO, the GO term Y will be removed from the annotations.")
            .create("trannots");
    /*
     * Use this data structure to define order of options in help message
     */
    private final static HashMap<Option, Integer> _optionsOrder = new HashMap<Option, Integer>();

    static {
        _optionsOrder.put(_go, _optionsOrder.size());
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
    }

    /**
     *
     */
    public SmProfileGOCst() {
        super(_appCmdName, _debugMode, _optionsOrder);
    }
}
