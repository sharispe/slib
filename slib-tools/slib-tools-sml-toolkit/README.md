# Semantic Measures Library - ToolKit

The Semantic Measures Library Toolkit (SML-Toolkit) is a Java Toolkit dedicated to semantic measures computation and analysis.
Please visit the dedicated website for downloads or extra documentation http://www.semantic-measures-library.org

The SML-Toolkit is composed of various tools related to Semantic Similarity/Relatedness computation and analysis.
Those tools are provided through a common command-line interface.

The source code is written using Java 1.6 and the SML-Toolkit can therefore be used on any platform (Linux, Windows, Mac) in which Java is installed.

The project currently focuses on Semantic measures related to semantic graphs, e.g. between terms or concepts structured in a graph (ontology), between groups of concepts...
A package dedicated to String similarity measures is also provided but this is not currently the main concern of the project.
Contact me if you want to contribute. 

The excerpt of the documentation presented below is related to the tool dedicated to semantic similarity or relatedness computation.

## Semantic measures computation

Semantic measures computations are made through the SM tool (Semantic Measures tool).
The SM tool is a fast (multi-threads compatible) software solution which can be used to compute semantic measures.
For instance, this tool is used in our laboratory to compute billions of semantic measures scores (yes billions ;) in few hours. 


This tool can be used from the SML-Toolkit typing:

`java -jar sml-toolkit-<version> -t sm`

Note that the last build of the toolkit can be downloaded at http://www.semantic-measures-library.org

The toolkit provides two interfaces to specify a configuration:
* **Profile configurations**: context specific interfaces which can be used to compute semantic measures for specific use cases, e.g. involving particular ontologies (the Gene Ontology, MesH) ...
* **Generic configuration**: Generic configuration through an XML file, this is a configuration mode for advanced user. It gives a low level access to the SML-Toolkit but can be not suited for practitionners which know few about semantic measures.

We briefly present the *profile configurations* available.

---------------------------------------
### Profile Configurations
---------------------------------------

*Profiles are supported since the 0.0.6 version of the SML-Toolkit.*

Profiles provide easy-to-use command-line interfaces for specific contexts of use, e.g. to compute semantic similarity between genes (products) annotated by Gene Ontology terms.
Indeed, the generic configuration which can be made through the XML interface requires complex configuration files to be specified and is therefore not suited for most practionners.

To ease the use of the SML-Toolkit, specific profiles (or command-line interfaces) have been developped to wrap the generic configuration mode into domain specific layers.
Those profile aim at generating XML configuration files from command-line parameters.
Such a configuration mode is therefore particularly adapted for users which are not experts of semantic measures and/or those only interested on a easy-to-use command line tool (not a geeky and complex tool ;).

Various profiles are supported by the SML-Toolkit.
Below, the list of contexts of use for which a profile is provided:
* Graph-Based Semantic Similarity or Relatedness
	* **GOFast**, the profile dedicated to the Gene Ontology. This profile can be used to compute semantic measure scores between GO terms or gene products. See dedicated section above.
	* **MeSH**, the profile dedicated to the MeSH. Planned for the next version of the toolkit
	* **SnomedCT**, the profile dedicated to the SnomedCt. Planned for the next version of the toolkit
	* **WordNet**, the profile dedicated to the WordNet. Planned for the next version of the toolkit
	* **Disease Ontology**, the profile dedicated to the Disease Ontology. Planned for the next version of the toolkit
	* **Yago**, the profile dedicated to the YaGO Ontology. Planned for the next version of the toolkit
	* **OBO**, the general profile dedicated to the OBO Ontologies. Planned for the next version of the toolkit
	* **RDF**, the general profile dedicated to the ontologies expressed in RDF. Planned for the next version of the toolkit




####  GOFast : Semantic Measures and Gene Ontology
---------------------------------------

This profile is dedicated to the Gene Ontology (GO).
It can be used to compute semantic measures (SMs) between GO terms or gene products annotated by GO terms.
The Gene Ontology must be in OBO format, the gene products annotations must be in GAF2 format or TSV (Tabulated Separated Values).
The GO and associated annotations can be downloaded at http://www.geneontology.org/

Please post a message to http://semantic-measures-library.org if you encounter any troubles.
More information can also be found on the website. 

Below the parameters which can be used, command-line examples are also provided:


##### Parameters

* `-go <file path>` the path to the GO in OBO 1.2 format, other format are not supported (required).
This file can be downloaded at http://www.geneontology.org/

* `-annots <file path>` the path to the annotation file. 
Required for groupwise measures (`-mtype g` see above) or any measure relying on a extrinsic metric (e.g. Resnik's Information Content)
This file can be downloaded at http://www.geneontology.org/

* `-annotsFormat <format>` the format of the annotation file, accepted values [GAF2,TSV], more information about the TSV format above, default GAF2

* `-queries <file path>` the path to the file containing the queries.
This file must contain the pairs of GO term or gene product ids separated by tabs (required). 
When similarities between gene products are computed, the ids are the values specified in the second column of the GAF2 file specifying the annotations of the gene products.
An example is provided above.

* `-output <file path>` output file in which the results will be flushed (required).

* `-mtype <type>` the type of semantic measures you want to use: 
	* `p` (pairwise) to compute semantic measures between GO terms.
	* `g` (groupwise) to compute semantic measures between gene products.
	Accepted values [p,g], default `p`. example `-mtype p`.

		
* `-aspect <flag>` specifies the aspect of the GO to use:
	* `MF` - Molecular Function
	* `BP` - Biological Process
	* `CC` - Cellular Component
	* `GLOBAL` - the three aspects MF-BP-CC will be used using a virtual root between the three		
	When a groupwise measure is used, all gene products' annotations not related to the aspect selected will not be considered. 
	Accepted values [MF,BP,CC,GLOBAL], default `BP`. example  `-aspect MF`.

		
* `-notfound <flag>` defines the behavior of the program if an entry element of the query file cannot be found, (i) in pairwise measures: one of the two GO terms cannot be found, (ii) in groupwise measures: one of the two gene products cannot be found. Accepted values [exclude, stop, set=<value>]:
	* `exclude` the entry will not be processed (a message will be logged if `-quiet` is not used)
	* `stop`    the program will stop
	* `set=<numerical value>` the entry will not be processed and the given value will be set as score (a message will be logged if `-quiet` is not used)
	default value = 'exclude'.

			
* `-noannots <flag>` defines the behavior if a gene product of the query file doesn't have any annotation (GO terms). Accepted values [exclude,stop, set=<value>]:
	* `exclude` the entry will not be processed (a message will be logged if -quiet is not used)
	* `stop`    the program will stop
	* `set=<numerical value>` the entry will not be processed and the given value will be set as score (a message will be logged if -quiet is not used)
	default value = 'set=-1' the score is set to -1.

			
* `-filter <params>` this parameter can be used to filter the GO terms associated to a gene product when the provided annotation file is in GAF2 format.
	* `noEC=<evidence_codes>` evidence codes to exclude separated by commas e.g. `EC=IEA` IEA annotations will not be considered
	* `Taxon=<taxon_ids>` taxon ids separated by commas e.g. `Taxon=9696` to only consider annotations associated to Taxon 9696
	Use `:` as separator between `noEC` and `Taxon` if any is required.
	Example of value `-filter noEC=IEA:Taxon=9696,5454`.
	Default value no filter.


* `-pm <flag>` a String value defining the pairwise measure to use. See the list of available measures below (required for pairwise measures or indirect groupwise measures).


* `-gm <flag>` a String value defining the direct groupwise measure or aggregation method (if an indirect groupwise measure must be used). See the list of available measures below (required for groupwise measures).


* `-ic <flag>` a String value defining the Information Content method to use (required by some measures). See the list of IC available below. 


* `-quiet` do not show warning messages

* `notrgo` do not perform a transitive reduction of the GO


* `notrannots` do not remove annotation redundancies i.e. if a gene product is annotated by two GO terms {X,Y} and X is subsumed by Y in the GO, the GO term Y will be removed from the annotations.



####  MeSH: Semantic Measures
---------------------------------------

Coming soon (Already available through the generic XML interface)

####  SnomedCT: Semantic Measures
---------------------------------------

Coming soon (Already available through the generic XML interface)

####  Disease Ontology: Semantic Measures
---------------------------------------

Coming soon (Already available through the generic XML interface)

####  WordNet: Semantic Measures
---------------------------------------

Coming soon (Already available through the generic XML interface)

####  Yago2: Semantic Measures
---------------------------------------

Coming soon (Already available through the generic XML interface)

####  OBO: Semantic Measures
---------------------------------------

Coming soon (Already available through the generic XML interface)

####  RDF: Semantic Measures
---------------------------------------

Coming soon (Already available through the generic XML interface)


---------------------------------------
####  Supported Semantic Similarity and Relatedness measures and Parameters
---------------------------------------

Please refer to http://www.semantic-measures-library.org/sml/index.php?q=sml-semantic-measures to consult the updated list of semantic measures available.

---------------------------------------
### Generic configuration
---------------------------------------

This configuration mode can be used to specify complex configurations using an XML configuration file.
This configuration mode can be considered as a low level command-line interface compared to profile mode.
Note that profiles only generate the configuration file which can be manually specified by advanced users.
More about the syntax of the configuration file at http://www.semantic-measures-library.org


## Questions
Do you have a question about the usage of this toolkit in your project? 
Please use the dedicated mailing list at http://www.semantic-measures-library.org

## Bugs and requests
If you have found a bug or a request for additional functionality, please use the issue tracker on GitHub.

https://github.com/sharispe/slib/issues using [SML-Toolkit] as message prefix.

##About

Sébastien Harispe (PhD c.) is the project leader, more about this project at http://www.semantic-measures-library.org
