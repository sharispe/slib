# RDSM - Reusable Distributional Semantic Models

Distributional Semantic Models can be used to compute semantic relatedness between words or to represent vector space models; they are used in numerous algorithms in Information Retrieval, Natural Language Processing and more generally in Artificial Intelligence. This technical documentation presents Reusable Distributional Semantic Models (RDSM), simple API & Software independent approaches for storing distributional models into disks.  

The main objective of RDSM is to provide an easy way to share distributional models accross applications without them being locked due to specific programming language specificities. The models stored in RDSM format can therefore be generated and consumed by a diversity of software tools whatever the programming language in which they have been developped and/or their purposes. Examples of software and/or API which can generate and/or consume these models are presented below. 

> WARNING
>
> This project is in its early version which makes tested model formats subject to change in the near future.


The projects which can:
 - Generate models in RDSM Format: SML-dist
 - Consume models in RDSM Format: SML-sm

## Distributional Models supported

This section presents the different models which are supported by the library.
- 2D Distributional models
    - 2D- Term Distributional models (type=TWO_D_TERM_MODEL)
    - 2D- Doc Distributional models (type=2D_DOC_TERM_MODEL): model which is similar to Vector space models

> Proposal for new models are warmly welcomed

----------------------------------------------------------------------------------------------------------------------------------------------------

### 2D Distributional models

2D-distributional models are represented by two dimensional matrices. They are commonly used to represent words or documents  -- acording to the distributional hypothesis or the vector space model. 

As an example, 2D-distributional models are used to analyse and to represent words through word cooccurrence analysis. In the context of word analysis, such a model generally refers to a data structure which can be used (i) to store the relationship between words, e.g. in term of co-occurrences, and (ii) to store vector-based representations of words. 

More generally a 2D-distributional model can refer to a <TERM,TERM> model, a <TERM,DOCUMENT> or even more generally a <TERM,LEX_UNIT> with LEX_UNIT a specific lexical unit, e.g. sentence, paragraph, document... In some other cases, e.g. in Vector Space Model, a <DOCUMENT, TERM> model will be considered.

Here we present the approach which is adopted to store in persistance and to access a 2D-Distributional model. We will not discuss the various approaches which have been proposed to build/compute such models, we here focus on the technical detail related to the storage strategy -- how the distributional model is stored, the storage format and so on. 

As we said a 2D-distributional model is represented by a N x M matrix -- in some cases N=M, e.g. in a <TERM,TERM> matrix. The model are represented by (java) double values. Considering that the matrix is used to represent the elements of A with regard to a collection of B (e.g. A=TERM, B=DOCUMENT), this approach is "optimized" to access the representation of any element of A with regard to B, the representation being a vector of size |B|. Note however, the approach is not optimized to access the representation of any element of B w.r.t A.  

We present the two models used to represent Terms and documents.

#### Term Distributional model

This model is used to represent a Term in a 2D space. A common approach is to represent words according to their cooccurences and therefore to consider a <TERM,TERM> model which is simply a coocurence matrix. However, more generally, this model can be used to represent a word using a <TERM,CONTEXT> space with CONTEXT a specified definition of a context, e.g. a context can be a document, a sentence, a window of words.

##### Persistent storage (version 0.1)

This section presents how a 2D-Term distributional model is stored in persistence for fast processing. 

A model is saved into a dedicated directory which contains several files: 
 - `model.properties` various information regarding the model
 - `model_index_table.tsv` information about the index structure
 - `model.bat` the model saved into a binary format.
 - `entity_index.tsv` the file which indexes the entities, i.e. the file used to map an entity to a matrix row. In this case, since the matrix rows refer to the terms, this index is similar to the vocabulary index generally provided to compute this model
 - `dimension_index.tsv (optional)`  index which is used to associate a label to a dimension, i.e. column id. In some cases this file will not be specified, e.g. if we don't want to store information regarding the dimensions used to characterize the entities.

Note that the model will be considered as corrupted and will not be usable if one of these files is missing (except for dimension_index.tsv which is optional).

We present the structure of each file.

##### model.properties

This file stores various information regarding the model. It uses the structure of a [.properties](http://en.wikipedia.org/wiki/.properties) file in Java. The keys used are: 
- `type` the type of model, here set to TWO_D_TERM_MODEL
- `name` the name of the model, i.e. a string used to name the model
- `entities_size` the size of the collection of entities for which a vector representation is stored, i.e. the number of row od the matrix which correspond to the size of the vocabulary.
- `vec_size` the size of the vector stored, i.e. number of dimensions
- `version` the version of the storage format
- `nb_files` the number of files used to build the model

Example:
```
type=TWO_D_TERM_MODEL
name=data/xp/model
entities_size=203330
vec_size=203330
version=0.0
nb_files=43249
```

##### model_index_table.tsv

This file is a TSV (Tab-separated values) file; it is used to store technical information about the data location in the model. These informations are required to read the model, i.e. the file `model.bat`. 

The structure of the file is: ID_ENT, START_POS, LENGTH_DOUBLE_NON_NULL (separator tab).
The first line of the file is dedicated to the header, next, each line is dedicated to a specific entity.
- `ID_ENT` refers to the unique id associated to the entity, e.g. the id of a term in a vocabulary (refer to `entity_index.tsv`).
- `START_POS` refers to the start location (in byte) of the chunk of data dedicated to the entity into `model.bat`.
- `LENGTH_DOUBLE_NON_NULL` refers to the numbers of non null values which compose the vector representation of this entity.
- `NB_FILES_WITH_WORD` refers to the numbers of files used to build the model which contains the words.

Example:
```
ID_ENT	START_POS	LENGTH_DOUBLE_NON_NULL	NB_FILES_WITH_WORD
0	0	8	1
1	129	166	9
...
203328	715521392	32	1
203329	715521905	2	1
```
##### model.bat

This file is a binary file which stores a simple compressed representation of the vector representation of each entity. The vector is compressed in order to reduce the size of sparse vectors -- the compression is only effective for sparse vectors... The data defined into `model.properties` and `model_index_table.tsv` are required to decode this file.

First let's discussed the compression strategy used to represent a vector v = [0,0.1,0.4,0,0,0,0,0.3,0,0.2] of size 10. All null values are skipped and non null values are stored according to pattern p_i = (id_i,val_i) without separation between id_i and val_i and between pairs of values. Therefore v will be represented by the vector [(1,0.1),(7,0.3),(2,0.4),(9,0.2)] which is stored into an array of double v_c = [1,0.1,2,0.4,7,0.3.,9,0.2]. This is the conversion of v_c into a byte array which is stored into the file. 

Thus considering that the data corresponding to an entityt starts ar START_POS, the chunk of DATA corresponding to this entity is located between START_POS and START_POS +  LENGTH_DOUBLE_NON_NULL x 2 x BYTE_PER_DOUBLE.
For convenience, the different vectors stored into the file are separated by a one byte separator, this is to separate vectors when they are only composed of null values.

##### entity_index.tsv

A TSV File of the form ID, LABEL which specifies the label to associate to each entity characterized by the model, i.e. row of the matrix. In this case, this file refers to the vocabulary index. This file does not contain header.

Example:
```
0	Actrius
1	Catalan
2	actress
3	film
4	Ventura
...
```
##### dimension_index.tsv

A TSV File of the form ID, LABEL which specifies the label to associate to each dimension used to represent the entities in the model, i.e. column of the matrix. When the model corresponds to a co-occurence matrix this file refers to the vocabulary index. This file does not contain a header. In addition this file is cosidered optional and must only be requiered to propose advanced capabbilities to interact with the model.

Example when the dimensions used to characterized the entities are documents:
```
0	doc_A
1	doc_B
...
```

----------------------------------------------------------------------------------------------------------------------------------------------------

#### Document Distributional model

This model is used to represent a Document in a 2D space and refers to the well-known Vector Space model commonly used in information retrieval. A common approach is to represent documents according to the words they contain <DOC,TERM>. However, more generally, this model can be complexified by considering the frequency of words and weighting words w.r.t this aspect (e.g. using the simple TF-IDF metric).

##### Persistent storage

This section presents how a Document distributional model is stored in persistence for fast processing. 

A model is saved into a dedicated directory which contains several files: 
 - `model.properties` various information regarding the model
 - `model_index_table.tsv` information about the index structure
 - `model.bat` the model saved into a binary format.
 - `entity_index.tsv` the file which indexes the entities/documents, i.e. the file used to map a document to a matrix row. In this case, since the matrix rows refer to the documents, this index is built by considering the name of the files which are represented.
 - `dimension_index.tsv (optional)`  index which is used to associate a label to a dimension, i.e. column id. In most models, which are SVM, this file refers to the vocabulary which is used to characterize the documents.

Note that the model will be considered as corrupted and will not be usable if one of these files is missing (except for dimension_index.tsv which is optional).

We present the structure of each file.

##### model.properties

This file stores various information regarding the model. It uses the structure of a [.properties](http://en.wikipedia.org/wiki/.properties) file in Java. The keys used are: 
- `type` the type of model, here set to TWO_D_DOC_MODEL
- `name` the name of the model, i.e. a string used to name the model
- `entities_size` the size of the collection of entities for which a vector representation is stored, i.e. the number of row of the matrix which correspond to the size of the document collection.
- `vec_size` the size of the vector stored, i.e. number of dimensions
- `version` the version of the storage format
- `nb_files` the number of files used to build the model, this is a duplicate of `entities_size`. 

Example:
```
type=TWO_D_DOC_MODEL
name=data/xp/model_doc
entities_size=10000
vec_size=203330
version=0.0
nb_files=10000
```

##### model_index_table.tsv

This file is a TSV (Tab-separated values) file; it is used to store technical information about the data location in the model. These informations are required to read the model, i.e. the file `model.bat`. 

The structure of the file is: ID_ENT, START_POS, LENGTH_DOUBLE_NON_NULL (separator tab).
The first line of the file is dedicated to the header, next, each line is dedicated to a specific entity.
- `ID_ENT` refers to the unique id associated to the entity, e.g. the id of a document (refer to `entity_index.tsv`).
- `START_POS` refers to the start location (in byte) of the chunk of data dedicated to the entity into `model.bat`.
- `LENGTH_DOUBLE_NON_NULL` refers to the numbers of non null values which compose the vector representation of this entity.

Example:
```
ID_ENT	START_POS	LENGTH_DOUBLE_NON_NULL
0	0	8
1	129	166
...
9999	5521392	32
10000	5521905	2
```
##### model.bat

This file is a binary file which stores a simple compressed representation of the vector representation of each entity. The vector is compressed in order to reduce the size of sparse vectors -- the compression is only effective for sparse vectors... The data defined into `model.properties` and `model_index_table.tsv` are required to decode this file.

Refer to the technical documentation related to the `model.bat` file used to store distributional model for terms.

##### entity_index.tsv

A TSV File of the form ID, LABEL which specifies the label to associate to each entity characterized by the model, i.e. row of the matrix. In this case, this file stores the identifiers which have been associated to the files. This file does not contain header.

Example:
```
0	/tmp/collections/doc_A
1	/tmp/collections/doc_B
2	/tmp/collections/doc_C
...
```
##### dimension_index.tsv

A TSV File of the form ID, LABEL which specifies the label to associate to each dimension used to represent the document in the model, i.e. column of the matrix. This file does not contain a header. It is considered optional and must only be required to propose advanced interaction capabbilities with the model.

Example when the dimensions used to characterized the entities are documents:
```
0	car
1	storm
...
```

## Credits

RDSM as first been proposed by Sébastien Harispe (during a Postdoctoral research position at Ecole des mines d'Alès).