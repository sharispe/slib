OntoFocus
----------

OntoFocus can be used to reduce any taxonomy considering a set of nodes of 
interest. It can be useful to extract relevant information from large knowledge 
representations which rely on a graph structure, e.g., lightweight ontologies, 
semantic networks, RDF graphs (taxonomic part). 


Considering a query file which specifies a set of reductions to perform, 
OntoFocus generates a DOT file for each reduction. The DOT file can next be used 
to create a (pdf, png, jpeg, etc.) graphical representation of the reduction 
using the dot tool (refer to GraphViz: http://www.graphviz.org). 


Credits: LGI2P Laboratory Ecole des Mines d'Alès (France).

License: CeCiLL (GPL compatible)

Implementation: Harispe Sébastien <harispe.sebastien@gmail.com>

Please cite:

Ranwez, V., Ranwez, S. & Janaqi, S., (2012).

Sub-ontology extraction using hyponym and hypernym closure on is-a directed 
acyclic graphs.

IEEE Transactions on Knowledge and Data Engineering, 24(12), 2288–2300.

Examples:
Query file (qfile.tsv): 
``
q1	GO:0032436,GO:0017109,GO:0006805,GO:0009410
q2	GO:0032436,GO:0017109
...
``

Execution:

``
java -jar ontoFocus.jar -onto gene_ontology_ext.obo -queries qfile.tsv -prefixes GO=http://go/
``

To Generate a pdf file with a graphical representation of the reduction (dot already install)
`dot -Tpdf q1.dot -o q1.pdf`



usage: java -jar OntoFocus.jar  [arguments]
----------------------------------------------------------------------
 -onto <file>          Input file which defines the knowledge
                       representation. The file format must correspond to
                       the default/specified format (required)
 -format <value>       The format of the knowledge representation
                       (optional, default:OBO, values: [OBO, RDF_XML])
 -queries <file>       Input file which defines the reduction(s) to
                       perform (required). For each reduction a
                       configuration is required, it must be specified in
                       a dedicated line according to the following pattern
                       [query_id][TAB][URI],[URI],...,[URI][newline] with
                       [query_id] the identifier of the reduction 
                       (must be unique), [TAB] a tabulation, [URI] an URI
                       specified in the ontology which maps a concept
                       defined in the taxonomy (at least two URIs must be
                       specified, separator ',')
 -outprefix <String>   The prefix which must be used to generate the
                       output file name (optional). An output file will be
                       generated for each query according to the following
                       pattern [prefix]_[query_id].dot or [query_id].dot
                       if no prefix specified
 -prefixes <value>     URI Prefixes which must be used to resolve prefixed
                       URIs, e.g., GO:XXXXX. Multiple prefixes can be
                       specified using comma as separator (optional,
                       default:none, e.g. : GO=http://go/). Commonly used
                       prefixes such as rdfs, owl are already loaded
 -finclude <value>     Force specific URIs of concepts to be included in
                       the final reduction (optional), these URIs will be
                       considered as query entries but they will not be
                       colored in the DOT file. It can be useful to include 
                        MF, BP, and CC roots of the Gene Ontology (2013)
                       GO:0003674,GO:0005575,GO:0008150. Multiple URIs can
                       be specified using comma as separator and prefixes
                       which have been specified can be used.
 -addR                 post-process: add all directed relationships 
                       (of any type/predicate) which involves two nodes of the 
                       final reduction (optional, default:false)
 -tr                   post-process (applied after addR): a
                       transitive reduction of the taxonomic graph
                       corresponding to the reduction will be performed, i.e. 
                       to remove taxonomic redundancies.
 -incR <URI>           URIs of other relationships to consider during
                       topological sort and graph reduction. Taxonomic
                       relationships are always considered. Useful to
                       include other transitive relationship (over
                       taxonomic relationships), e.g part-of.  Notice
                       that the graph composed of all the relationships of
                       the specified types must be acyclic.  Multiple
                       type of relationships/predicate can be specified using 
                        ',' separator
                       (optional, default: {rdfs:subClassOf})
 -help                 print this message
