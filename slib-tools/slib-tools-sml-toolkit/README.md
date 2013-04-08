# Semantic Measures Library - ToolKit

The Semantic Measures Library Toolkit (SML-Toolkit) is a Java Toolkit dedicated to semantic measures computation and analysis.
Please visit the dedicated website for **downloads** or extra documentation http://www.semantic-measures-library.org

The SML-Toolkit is composed of various tools related to Semantic Measures computation and anaylysis.
The excerpt of the documentation presented below is related to the tool dedicated to semantic measures computation.

## Semantic measures computation

Semantic measures computations are made through the SM (semantic measures) tool:

`java -jar sml-toolkit-<version> -t sm`

The toolkit provides two interfaces to specify a configuration:
* **Profile configuration**: context specific interfaces which can be used to compute semantic measures for specific use cases, e.g. involving particular ontologies (the Gene Ontology, MesH) ...
* **Generic configuration**: Generic configuration through an XML file

---------------------------------------
### Profile Configurations
---------------------------------------

Profile provides easy-to-use command-line interfaces for specific contexts of use, e.g. compute semantic measures between genes annotated by Gene Ontology terms.
Indeed, the generic configuration which can be made through the XML interface is in some cases not suited as it requires complex configuration files to be specified.
To ease the use of the SML-Toolkit, specific profiles (interfaces) are specified to wrap the generic configuration mode into domain specific layers.
Such a configuration mode is therefore particularly adapted for users which are not experts of semantic measures and/or those only interested on a easy-to-use command line tool (not a geeky and complex tool ;).

Below, the list of contexts of use for which a profile is provided:
* **GOFast**, the profile dedicated to the Gene Ontology. This profile can be used to compute semantic measure scores between GO terms or gene products. See dedicated section above.
* **MeSH**, the profile dedicated to the MeSH. Planned for the next version of the toolkit
* **SnomedCT**, the profile dedicated to the SnomedCt. Planned for the next version of the toolkit
* **WordNet**, the profile dedicated to the WordNet. Planned for the next version of the toolkit
* **Disease Ontology**, the profile dedicated to the Disease Ontology. Planned for the next version of the toolkit
* **Yago**, the profile dedicated to the YaGO Ontology. Planned for the next version of the toolkit
* **OBO**, the general profile dedicated to the OBO Ontologies. Planned for the next version of the toolkit
* **RDF**, the general profile dedicated to the ontologies expressed in RDF. Planned for the next version of the toolkit

Profile are supported since the 0.0.6 version of the SML-Toolkit.


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

`-go <file path>` the path to the GO in OBO 1.2 format (required)

`-annots <file path>` the path to the annotation file. 
Required for groupwise measures (-mtype g see above) or any measure relying on a extrinsic metric (e.g. Resnik's Information Content)

`-annotsFormat <format>` the format of the annotation file, accepted values [GAF_2,TSV], default GAF_2

`-queries <file path>` the path to the file containing the queries, i.e. the pairs of GO term or gene product ids separated by tabs (required). 
An example is provided above.

`-output <file path>` output file to store the results (required).

`-mtype <type>` the type of semantic measures you want to use: 
* 'p' (pairwise) to compute semantic measures between GO terms.
* 'g' (groupwise) to compute semantic measures between gene products.
Accepted values [p,g], default p. example `-mtype p`
		
`-aspect <flag>` specify the aspect of the GO to use:
* MF - Molecular Function
* BP - Biological Process
* CC - Cellular Component
* GLOBAL - the three aspects MF-BP-CC will be used using a virtual root between the three
* custom=<GO term id> specify a GO term which will be considered as root e.g. custom=GO:XXXXX		
Accepted values [MF,BP,CC,GLOBAL,custom=<GO term id>], default BP. examples (1) -aspect MF (2) -aspect custom=GO:XXXXX
		
`-notfound <flag>` define the behavior if an entry element of the query file cannot be found:
			- in pairwise measures: one of the two GO terms cannot be found
			- in groupwise measures: one of the two gene products cannot be found
			Accepted values [exclude, stop, set=<value>]:
				- 'exclude' the entry will not be processed (a message will be logged if -quiet is not used)
				- 'stop'    the program will stop
				- 'set=<value>' the entry will not be processed (a message will be logged if -quiet is not used)
			default value = 'exclude'
			
`-noannots <flag>` define the behavior if a gene product of the query file doesn't have annotation (GO terms):
			Accepted values [exclude,stop, set=<value>]:
				- 'exclude' the entry will not be processed (a message will be logged if -quiet is not used)
				- 'stop'    the program will stop
				- 'set=<value>' the entry will not be processed (a message will be logged if -quiet is not used)
			default value = 'set=0' the score is set to 0
			
`-filter <params>` this parameter can be used to filter the GO terms associated to a gene product when the provided annotation file is in GAF2 format.
				- noEC=<evidence_codes> evidence codes to exclude separated by commas e.g. EC=IEA IEA annotations will not be considered
				- Taxon=<taxon_ids> taxon ids separated by commas e.g. Taxon=9696 to only consider annotations associated to Taxon 9696
				Exemple of value -filter noEC=IEA:Taxon=9696,5454
				Default value no filter


`-pm <flag>` pairwise measure see the list of available measures below (required for pairwise measures or indirect groupwise measures)

`-gm <flag>` direct groupwise measure or aggregation method if an indirect groupwise measure must be used (require a pairwise measure to be set). see the list of available measures below (required for groupwise measures).

`-ic <flag>` information content method see the list of IC available below 

`-quiet` do not show warning messages


####  MeSH: Semantic Measures
---------------------------------------

####  SnomedCT: Semantic Measures
---------------------------------------

####  Disease Ontology: Semantic Measures
---------------------------------------

####  WordNet: Semantic Measures
---------------------------------------

####  Yago2: Semantic Measures
---------------------------------------

####  OBO: Semantic Measures
---------------------------------------

####  RDF: Semantic Measures
---------------------------------------



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
