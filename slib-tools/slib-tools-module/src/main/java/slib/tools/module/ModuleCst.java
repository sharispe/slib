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
package slib.tools.module;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Abstract class defining the constant expected to be specified for each module
 * configuration.
 *
 * @author Sebastien Harispe
 *
 */
public abstract class ModuleCst {

    public String appName = null;
    public String version = null;
    public String reference = null;
    public String description = null;
    public String contact = null;

//    /**
//     * Build the Module Constant object
//     *
//     * @param appName	the name of the application
//     * @param version	the version of the application as String
//     * @param reference	the reference of the module
//     * @param description a description of the module
//     * @param contact the email to contact if a bug is encountered.
//     */
//    public ModuleCst(String appName, String version, String reference, String description, String contact) {
//
//        this.appName = appName;
//        this.version = version;
//        this.reference = reference;
//        this.description = description;
//        this.contact = contact;
//    }

    public ModuleCst(String propertiesFile, String prefix) throws SLIB_Ex_Critic {

        Configuration config;
        
        
        try {
            config = new PropertiesConfiguration(propertiesFile);
        } catch (ConfigurationException ex) {
            throw new SLIB_Ex_Critic("Unable to load configuration file (properties) "+ex.getMessage());
        }

        
        
        this.appName     = config.getString(prefix+".name");
        this.version     = config.getString(prefix+".version");
        this.reference   = config.getString(prefix+".reference");
        this.description = config.getString(prefix+".description");
        this.contact     = config.getString(prefix+".contact");

    }
    
    

    public String getVersion() {
        return version;
    }

    public String getAppName() {
        return appName;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public String getContact() {
        return contact;
    }
}
