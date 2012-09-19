/*

Copyright or © or Copr. Ecole des Mines d'Alès (2012) 

This software is a computer program whose purpose is to 
process semantic graphs.

This software is governed by the CeCILL  license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.

 */
 
 
package slib.utils.impl;

import slib.utils.ex.SGL_Ex_Critic;

public class Util {

	/**
	 * Only throw a {@link SGL_Ex_Critic} passing the given message
	 * @param message the message used
	 * @throws SGL_Ex_Critic
	 */
	public static void error(String message) throws SGL_Ex_Critic{
		throw new SGL_Ex_Critic(message);
	}
	
	/**
	 * Convert the given {@link String} to a boolean value
	 * return true if the given string equals to "true" or "yes".
	 * Ignore Case + trim the string.
	 * @param value the string to evaluate
	 * @return a boolean value according to the defined conditions.
	 */
	public static boolean stringToBoolean(String value){
		if(value != null && (value.trim().equalsIgnoreCase("true") || value.trim().equalsIgnoreCase("yes")))
			return true;
		return false;
	}

	public static Integer stringToInteger(String val) throws SGL_Ex_Critic{
		Integer a = null;

		try{
			a = Integer.parseInt(val);
		}
		catch (NumberFormatException e) {
			Util.error("Cannot convert "+val+" to an integer ");
		}
		return a;
	}
	
	public static Double stringToDouble(String val) throws SGL_Ex_Critic{
		Double a = null;

		try{
			a = Double.parseDouble(val);
		}
		catch (NumberFormatException e) {
			Util.error("Cannot convert "+val+" to an decimal value (double) ");
		}
		return a;
	}

	public static Long stringToLong(String val) throws SGL_Ex_Critic{
		Long a = null;

		try{
			a = Long.parseLong(val);
		}
		catch (NumberFormatException e) {
			Util.error("Cannot convert "+val+" to an long value ");
		}
		return a;
	}
}
