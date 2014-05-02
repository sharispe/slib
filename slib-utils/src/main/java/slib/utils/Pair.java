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
package slib.utils;


/**
 *
 * @author
 * http://stackoverflow.com/questions/779414/java-generics-pairstring-string-stored-in-hashmap-not-retrieving-key-value-p
 * @param <TYPEA>
 * @param <TYPEB>
 */
public class Pair<TYPEA, TYPEB> implements Comparable< Pair<TYPEA, TYPEB>> {

    protected final TYPEA Key_;
    protected final TYPEB Value_;

    public Pair(TYPEA key, TYPEB value) {
        Key_ = key;
        Value_ = value;
    }

    public TYPEA getKey() {
        return Key_;
    }

    public TYPEB getValue() {
        return Value_;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("Key: ");
        buff.append(Key_);
        buff.append("\tValue: ");
        buff.append(Value_);
        return (buff.toString());
    }

    @Override
    public int compareTo(Pair<TYPEA, TYPEB> p1) {

        if (null != p1) {
            if (p1.equals(this)) {
                return 0;
            } else if (p1.hashCode() > this.hashCode()) {
                return 1;
            } else if (p1.hashCode() < this.hashCode()) {
                return -1;
            }
        }
        return (-1);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.Key_ != null ? this.Key_.hashCode() : 0);
        hash = 97 * hash + (this.Value_ != null ? this.Value_.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<TYPEA, TYPEB> other = (Pair<TYPEA, TYPEB>) obj;
        if (this.Key_ != other.Key_ && (this.Key_ == null || !this.Key_.equals(other.Key_))) {
            return false;
        }
        if (this.Value_ != other.Value_ && (this.Value_ == null || !this.Value_.equals(other.Value_))) {
            return false;
        }
        return true;
    }
}