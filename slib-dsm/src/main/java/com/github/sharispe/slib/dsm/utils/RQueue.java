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

import java.util.ArrayList;
import java.util.List;

/**
 * Result Queue
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 * @param <V>
 * @param <L>
 */
public class RQueue<L, V extends Number> {

    public final int capacity;
    private List<Double> values;
    private List<L> labels;
    int nbValues;
    double extremeValue;
    boolean maximize = true;

    public RQueue(int capacity) {
        this(capacity, true);
    }

    public RQueue(int capacity, boolean maximize) {
        this.maximize = maximize;
        this.capacity = capacity;
        this.values = new ArrayList(capacity + 1);
        this.labels = new ArrayList(capacity + 1);
        nbValues = 0;
    }
    
    

    /**
     *
     * @param label
     * @param value
     * @return
     */
    public boolean add(L label, V value) {

        double v = value.doubleValue();

//        System.out.println(v+"/"+lowestValue);
        if (nbValues < capacity) {

            int id = getID(v);
            values.add(id, v);
            labels.add(id, label);
            nbValues++;

            extremeValue = values.get(nbValues - 1);

//            System.out.println("id: " + id);
            return true;
        } else if (maximize && v > extremeValue) {

//            System.out.println("pass");
            int id = getID(v);
            values.add(id, v);
            labels.add(id, label);
            values.remove(nbValues);
            labels.remove(nbValues);
            extremeValue = values.get(nbValues - 1);

//            System.out.println("id: " + id);
            return true;

        } else if (!maximize && v < extremeValue) { // minimize

//            System.out.println("pass");
            int id = getID(v);
            values.add(id, v);
            labels.add(id, label);
            values.remove(nbValues);
            labels.remove(nbValues);
            extremeValue = values.get(nbValues - 1);

//            System.out.println("id: " + id);
            return true;
        }

        return false;
    }

    private int getID(double value) {

        for (int i = 0; i < nbValues; i++) {

            if (maximize && value > values.get(i)) {
                return i;
            } else if (!maximize && value < values.get(i)) {
                return i;
            }

        }
        return nbValues;
    }

    @Override
    public String toString() {
        String out = "values: " + nbValues + "/" + capacity + "\n";

        for (int i = 0; i < values.size(); i++) {
            out += i + "\t" + values.get(i) + "\t" + labels.get(i) + "\n";
        }
        return out;
    }

    public static void main(String[] args) {

        RQueue<String, Double> kbestValues = new RQueue(3, false);

        System.out.println(kbestValues.toString());

        kbestValues.add("King", 0.0);
        kbestValues.add("Camel2", 0.7);

        System.out.println(kbestValues.toString());

        kbestValues.add("Queen", 1.0);

        System.out.println(kbestValues.toString());

        kbestValues.add("Monkey", 0.2);

        System.out.println(kbestValues.toString());

        kbestValues.add("Camel", -0.7);

        System.out.println(kbestValues.toString());

        kbestValues.add("-0.5", -0.5);

        System.out.println(kbestValues.toString());
        
        kbestValues.add("0.5", 0.5);

        System.out.println(kbestValues.toString());
        
        kbestValues.add("-100", -100.0);

        System.out.println(kbestValues.toString());

    }

    public List<Double> getValues() {
        return values;
    }

   
    public List<L> getLabels() {
        return labels;
    }

   
}
