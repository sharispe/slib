/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.tools.module;

import java.util.Comparator;
import java.util.Map;
import org.apache.commons.cli.Option;

/**
 *
 * @author Sébastien Harispe default
 */
public class OptionComparator implements Comparator {

    Map<Option, Integer> optOrder;

    public OptionComparator(Map<Option, Integer> optOrder) {
        this.optOrder = optOrder;
    }

    @Override
    public int compare(Object obj1, Object obj2) {
        Option o1 = (Option) obj1;
        Option o2 = (Option) obj2;

        int val = -1;
        if (optOrder.get(o1).intValue() > optOrder.get(o2).intValue()) {
            val = 1;
        }
        return val;
    }
}
