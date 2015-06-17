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
package com.github.sharispe.slib.dsm.main;

import com.github.sharispe.slib.dsm.core.engine.Voc;
import com.github.sharispe.slib.dsm.core.engine.VocStatComputer;
import com.github.sharispe.slib.dsm.core.engine.wordIterator.WordIteratorAbstract;
import com.github.sharispe.slib.dsm.core.engine.wordIterator.WordIterator_Allow_Shorter;
import com.github.sharispe.slib.dsm.core.engine.wordIterator.WordIterator_FixedSize;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import slib.utils.impl.UtilDebug;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Test {

    public static void main(String[] args) throws Exception {

        File f = new File("//home/seb/data/OANC/data/written_2/non-fiction/OUP/Castro/chL.txt");
        WordIteratorAbstract w;
//        w = new WordIterator_Allow_Shorter(f, 2);
//        while (w.hasNext()) {
//            System.out.println(w.next());
//        }

        System.out.println("FIXED SIZE");
        w = new WordIterator_FixedSize(f, 2);
        while (w.hasNext()) {
            System.out.println(w.next());
        }

        UtilDebug.exit();

        List<Integer> list = new ArrayList();
        list.add(1);
        list.add(2);
        list.add(3);
        System.out.println(list.toString());
        list.add(1, 5);

        System.out.println(list.toString());

        String[] arguments = {"/data/xp/dsm", "/tmp/voc", "6", "2"};
        MainCLI.CMD_VOC_INDEX(arguments);
        VocStatComputer.computeStat("/tmp/voc", 50);

        System.exit(0);

        Voc index = new Voc("/tmp/voc");
        System.out.println(index.size());

        System.exit(0);

        String input = "  ( king ) + ( man ) ";

        new Test().process(input);

    }

    private void process(String input) throws Exception {

        String[] elements = input.split("\\s");

        Node current = null;

        for (String e : elements) {

            if (e.trim().isEmpty()) {
                continue;
            }

            if (e.equals("+") || e.equals("-")) {
                // do something
                System.out.println("Operation: " + e);
                Node n = new Node(e);
                n.setOperation(e);
                n.setLeftPart(current);
                current = n;
            } else if (e.equals("(")) {

            } else {
                System.out.println("Create Node: " + e);
                Node n = new Node(e);

                if (current == null) {
                    current = n;
                } else if (current.getOperation() == null) {
                    throw new Exception("Maformed Expression");
                } else {
                    current.setRightPart(n);
                }

            }
        }
        System.out.println(current);
    }

    private class Node {

        String label;
        String operation;
        Node leftPart, rightPart;

        public Node(String label) {
            this.label = label;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public Node getLeftPart() {
            return leftPart;
        }

        public void setLeftPart(Node leftPart) {
            this.leftPart = leftPart;
        }

        public Node getRightPart() {
            return rightPart;
        }

        public void setRightPart(Node rightPart) {
            this.rightPart = rightPart;
        }

        @Override
        public String toString() {

            if (this.operation != null) {
                return "(" + this.leftPart.toString() + this.operation + this.rightPart + ")";
            } else {
                return this.label;
            }

        }
    }
}
