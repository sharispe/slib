Slib - Semantic Library
========================

Slib is a JAVA library dedicated to semantic data analysis.
The project currently mainly focuses on semantic graphs (networks) and on the development of the Semantic Measures Library (SML). 
The library is splited in various sub-libraries (i.e. maven modules):
* *sglib* semantic graph engine, a simple in-memory graph engine relying on the sesame library (enabling reasoning, RDF/OWL data loading...). The graph engine provide an easy way to process a semantic graph (e.g. RDF graph) as a graph in which traversal can easily be performed. Numerous algorithms commonly used to process semantic graph (e.g. for semantic measure design) are implemented.  
* *slib-sml* The Semantic Measures Library, a library dedicated to semantic measures (similarity/relatedness) computation, evaluation and analysis. The dedicated web site of this specific project is http://semantic-measures-library.com
* *slib-tools* various tools performing processes on semantic graph/data (e.g. SML-Toolkit which enabling semantic similarity/relatedness computation)
* *slib-indexer* a dummy set of in-memory indexer implementation (will be deprecated, please use Lucene)




