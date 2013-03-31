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