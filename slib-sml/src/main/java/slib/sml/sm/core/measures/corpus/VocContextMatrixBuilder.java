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
package slib.sml.sm.core.measures.corpus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.utils.FileUtils;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class VocContextMatrixBuilder {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    List<String> docFields = new ArrayList<String>();
    Set<String> vocabulary;
    Matrix matrix;
    MatrixType matrixType;

    public VocContextMatrixBuilder(MatrixType matrixType, Set<String> voc) {
        this.matrixType = matrixType;
        vocabulary = voc;
        docFields = new ArrayList<String>();

        switch (matrixType) {

            case WORD_WORD:
                matrix = new Matrix<String, String>();
                break;
            case WORD_DOC:
                matrix = new Matrix<String, Document>();
                break;
        }
        logger.info("Matrix Builder: " + this.matrixType);
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public VocContextMatrixBuilder(MatrixType matrixType, Set<String> voc, List<String> docFields) {
        this(matrixType, voc);
        setDocFields(docFields);
    }

    public List<String> getDocFields() {
        return docFields;
    }

    final public void setDocFields(Collection<String> s) {
        for (String ss : s) {
            addDocFields(ss);
        }
    }

    public void addDocFields(String s) {
        if (!docFields.contains(s)) {
            docFields.add(s);
        }
    }

    public void buildMatrix(Iterable<Document> documents) throws SLIB_Ex_Critic {

        logger.info("Building voc-context matrix");
        logger.info("voc: " + vocabulary);
        logger.info("fields: " + docFields);

        if (docFields.isEmpty()) {
            throw new SLIB_Ex_Critic("Please specify a field to analyse in the given documents");
        }

        for (Document doc : documents) {
            process(doc);
        }
    }

    public void process(Document doc) throws SLIB_Ex_Critic {

        logger.debug("processing doc: " + doc.toString());

        for (String field : docFields) {

            logger.info(field + " : " + doc.get(field));

            if (doc.getField(field) == null) {
                logger.warn("Skip field " + field);
                break;
            }

            switch (matrixType) {

                case WORD_WORD: // Word to Word co-occurence in a document

                    Set<String> words = new HashSet<String>();
                    words.addAll(Arrays.asList(doc.get(field).split("\\s")));
                    String[] wordsDoc = words.toArray(new String[words.size()]);

                    for (int i = 0; i < wordsDoc.length; i++) {
                        for (int j = i + 1; j < wordsDoc.length; j++) {

                            if (vocabulary == null || vocabulary.contains(wordsDoc[i]) && vocabulary.contains(wordsDoc[j])) {
//                                logger.info(wordsDoc[i] + "\t" + wordsDoc[j]);
                                matrix.addValue(wordsDoc[i], wordsDoc[j], 1);
                                matrix.addValue(wordsDoc[j], wordsDoc[i], 1);
                            }
                        }
                    }
                    break;

                case WORD_DOC: // Word to Word co-occurence in a document

                    Set<String> wordsCol = new HashSet<String>();
                    wordsCol.addAll(Arrays.asList(doc.get(field).split("\\s")));
                    String[] wordsArray = wordsCol.toArray(new String[wordsCol.size()]);

                    for (int i = 0; i < wordsArray.length; i++) {
                        matrix.addValue(wordsArray[i], doc, 1);
                    }
                    break;

            }

        }
    }

    public static void main(String[] args) throws SLIB_Ex_Critic, IOException {

        String[] ext = {"txt"};
        List<File> files = FileUtils.listFilesForFolder("/data/tmp/wiki/", Arrays.asList(ext), 100000);

        List<String> docField = new ArrayList<String>();
        docField.add("content");

        String[] vocArray = {"lion", "panthera", "Africa", "lamb","insecticides","animal","Genealogists","rugby","football","Sydney","Australia"};
        Set<String> voc = new HashSet<String>(Arrays.asList(vocArray));

        MatrixType matrixType = MatrixType.WORD_WORD;
        VocContextMatrixBuilder matrixBuilder = new VocContextMatrixBuilder(matrixType, voc, docField);

        for (File f : files) {
            Document doc = new Document();
            String fileAsString = FileUtils.readFile(f.getAbsolutePath(), Charset.defaultCharset());
            doc.add(new StoredField("content", fileAsString));
            matrixBuilder.process(doc);
        }

//        Document docA = new Document();
//        docA.add(new StoredField("title", "Lion article"));
//        docA.add(new StoredField("content", "The lion (Panthera leo) is one of the four big cats in the genus Panthera and a member of the family Felidae. With some males exceeding 250 kg (550 lb) in weight,[4] it is the second-largest living cat after the tiger. Wild lions currently exist in sub-Saharan Africa and in Asia (where an endangered remnant population resides in Gir Forest National Park in India) while other types of lions have disappeared from North Africa and Southwest Asia in historic times. Until the late Pleistocene, about 10,000 years ago, the lion was the most widespread large land mammal after humans"));
//
//        Document docB = new Document();
//        docB.add(new StoredField("title", "Java (programming language)"));
//        docB.add(new StoredField("content", "Java is a computer programming language that is concurrent, class-based, object-oriented, and specifically designed to have as few implementation dependencies as possible. It is intended to let application developers \"write once, run anywhere\" (WORA), meaning that code that runs on one platform does not need to be recompiled to run on another. Java applications are typically compiled to bytecode (class file) that can run on any Java virtual machine (JVM) regardless of computer architecture. Java is, as of 2014, one of the most popular programming languages in use, particularly for client-server web applications, with a reported 9 million developers.[10][11] Java was originally developed by James Gosling at Sun Microsystems (which has since merged into Oracle Corporation) and released in 1995 as a core component of Sun Microsystems' Java platform. The language derives much of its syntax from C and C++, but it has fewer low-level facilities than either of them."));
//
//        Document docC = new Document();
//        docC.add(new StoredField("title", "Java (programming language)"));
//        docC.add(new StoredField("content", "Java is a computer programming language that is concurrent, class-based, object-oriented, and specifically designed to have as few implementation dependencies as possible. It is intended to let application developers \"write once, run anywhere\" (WORA), meaning that code that runs on one platform does not need to be recompiled to run on another. Java applications are typically compiled to bytecode (class file) that can run on any Java virtual machine (JVM) regardless of computer architecture. Java is, as of 2014, one of the most popular programming languages in use, particularly for client-server web applications, with a reported 9 million developers.[10][11] Java was originally developed by James Gosling at Sun Microsystems (which has since merged into Oracle Corporation) and released in 1995 as a core component of Sun Microsystems' Java platform. The language derives much of its syntax from C and C++, but it has fewer low-level facilities than either of them."));
//
//        docs.add(docA);
//        docs.add(docB);
//        docs.add(docC);
//        matrixBuilder.buildMatrix(docs);

        Matrix<String, String> mat = matrixBuilder.getMatrix();

        System.out.println("size: " + mat.getInternalStorage().keySet().size());

        for (String s : mat.getInternalStorage().keySet()) {
            System.out.println(s + "\t(" + mat.getInternalStorage().get(s).size() + ")\t" + mat.getInternalStorage().get(s));
        }
    }

}
