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
 
 
package slib.tools.smltoolkit.smutils.cli.utils;

import slib.tools.module.ModuleCst;
import slib.tools.smltoolkit.SmlToolKitCst;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author seb
 */
public class SmUtilsToolKitCst extends ModuleCst {



	
	/**
     *
     */
    public static final String   ndescription = 
        "\n"+ "Note that the module can also be used to process all files which respect the following restrictions " +

        "\n"+ "\t - tab separated format"+
        "\n"+ "\t - containing a header (first non-empty line which do not start with ! ). Use ! to prefix comment lines"+
        "\n"+ "\t - the first two columns are dedicated to the name of the entity (gene, document ...) compared"+
        "\n"+ "\t - the other columns are considered as double (decimal) results."+
        "\n"+ "Input file example"+
        "\n"+ "e_A\te_B\tmesure_1\tmesure_2\t..."+
        "\n"+ "g1\tg2\t0.5\t0.6\t..."+
        "\n"+ "g2\tg1\t0.5\t0.6\t..."+
        "\n"+ "g2\tg3\t0.5\t0.6\t..."+


        "\n\n"+ "The main utility of the module is to merge results files. " +
        "\n"+ "Two processes can be used depending on the size of the files to process." +

        "\n\n** "+ SmUtilsCmdHandlerCst.process_resultMerger+" is dedicated to in memory processing of files." +
        "\n"+ "Note that an error can occur if the files are too larges. See below for alternatives." +
        "\n"+ "A deprecated function also exists to process large files splitting them into subfiles " +
        "\n"+ "but depending on the file size the process can be time consumming" +

        "\n\n** "+ SmUtilsCmdHandlerCst.process_sqlLiteUtils+" is dedicated to the process of large results files through SQLlite databases." +
        "\n"+ "Note that SQLlite do not require any Database Managment System to be installed." +
        "\n"+ SmUtilsCmdHandlerCst.process_sqlLiteUtils+" can be used to: " +
        "\n"+ "\t - build an SQLlite database table from a result file" +
        "\n"+ "\t - merge two tables" +
        "\n"+ "\t - copy a table (from a database to another)" +
        "\n"+ "\t - delete/rename a table" +
        "\n"+ "\t - delete specific columns of a table" +
        "\n"+ "\t - get information from a database" +
        "\n"+ "\t - flush a database table into a tabular file" +
        "\n"+ "Thus to merge two files you only need to build database tables from the file you want to merge. " +
        "\n"+ "Merge the tables, then export the table into a tabular format." +


        "\n";
	
	
        /**
     *
     */
    public static final String   properties_prefix   = "sml-toolkit-utils";
	
	
	/**
     *
     * @throws SLIB_Ex_Critic
     */
    public SmUtilsToolKitCst() throws SLIB_Ex_Critic {
		super(SmlToolKitCst.properties_file_name,properties_prefix);
                this.description = SmUtilsToolKitCst.ndescription;
	}
}
