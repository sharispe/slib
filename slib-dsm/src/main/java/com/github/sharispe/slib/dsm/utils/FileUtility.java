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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class FileUtility {

    public static boolean createDir(String dir) throws SLIB_Exception {

        File theDir = new File(dir);
        boolean result = false;
        
        if (!theDir.exists()) {
            System.out.println("creating directory: " + dir);
        
            try {
                theDir.mkdir();
                result = true;
            } catch (SecurityException se) {
                throw new SLIB_Exception("Cannot create directory "+dir);
            }
        }
        return result;
    }

    public static List<File> listFilesForFolder(String folder) {
        System.out.println("List Folder from: " + folder);
        return listFilesForFolderInner(new File(folder), (List<String>) null, new ArrayList<File>());
    }

    /**
     * Return the list of files which have been found in the specified directory
     * and which respect the given constraint.
     *
     * @param folder the folder in which the files are located (recursively
     * process the directories)
     * @param admittedExtensions the extensions which are admitted (no
     * restriction if set to null).
     * @return a list of File
     */
    public static List<File> listFilesFromFolder(String folder, List<String> admittedExtensions) {
        System.out.println("List Folder from: " + folder);
        return listFilesForFolderInner(new File(folder), admittedExtensions, new ArrayList<File>());
    }

    private static List<File> listFilesForFolderInner(final File folder, List<String> admittedExtensions, List<File> files) {
        
        if(!folder.exists()) return files;
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolderInner(fileEntry, admittedExtensions, files);
            } else {
                if (admittedExtensions == null || admittedExtensions.contains(FilenameUtils.getExtension(fileEntry.getPath()))) {
                    files.add(fileEntry);
                }
            }
        }
        return files;
    }

}
