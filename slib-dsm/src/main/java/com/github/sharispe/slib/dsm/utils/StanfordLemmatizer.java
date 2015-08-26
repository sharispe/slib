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

package com.github.sharispe.slib.dsm.utils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;

import static slib.utils.FileUtils.readFile;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class StanfordLemmatizer {
    
    /**
     * Lemmatize a document and save the result in another file
     * @param inputFile the file to lemmatize
     * @param outputFile the result 
     * @throws IOException 
     */     
    public static void lemmatize(String inputFile, String outputFile, String path_to_pos_model) throws IOException {

        // https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
        String[] pennTags = {"NN", "NNS", "NNP", "VB"};
        List<String> acceptedPennTag = Arrays.asList(pennTags);
        String textContent = readFile(inputFile, StandardCharsets.UTF_8);
        String textContentProcess = "";

        // To remove the annoying log
        RedwoodConfiguration.empty().capture(System.err).apply();
        
        Properties props = new Properties();
        props.put("pos.model", path_to_pos_model);
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(textContent);

        // run all Annotators on this text
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        
        String sentenceLem;

        for (CoreMap sentence : sentences) {
            sentenceLem = "";

            boolean f = true;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                if (acceptedPennTag.contains(pos)) {
                    if (!f) {
                        sentenceLem += " ";
                    }
                    sentenceLem += lemma;
                    f = false;
                }
            }
            textContentProcess += sentenceLem + "\n";
        }
        // enable log
        RedwoodConfiguration.current().clear().apply();
        FileUtils.writeStringToFile(new File(outputFile), textContentProcess, false);
    }
    
}
