Slib - Semantic Library
========================

Slib is a JAVA library dedicated to semantic data analysis.
The project currently mainly focuses on semantic data represented as semantic graphs (networks).
The library is splited in various sub-libraries (i.e. maven modules):
* *sglib* semantic graph engine, a simple in-memory graph engine relying on the sesame library (enabling reasoning, RDF/OWL data loading...). The graph engine provide an easy way to process a semantic graph (e.g. RDF graph) as a graph in which traversal can easily be performed. Numerous algorithms commonly used to process semantic graphs (e.g. for semantic measure design) are implemented.  
* *slib-sml* The Semantic Measures Library (SML), a library dedicated to semantic measures (similarity/relatedness) computation, evaluation and analysis. See dedicated web site: http://www.semantic-measures-library.com
* *slib-tools* various command-line tools performing processes on semantic graph/data (e.g. SML-Toolkit, a command-line tool dedicated to semantic similarity/relatedness computation).
* *slib-indexer* a dummy set of in-memory indexers implementation (changes are planned to take advantage of Lucene).




        