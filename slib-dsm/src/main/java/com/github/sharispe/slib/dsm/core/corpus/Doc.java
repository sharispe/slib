/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sharispe.slib.dsm.core.corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sharispe
 */
public class Doc implements Document {

    final String id;
    final File file;
    final String content;

    public Doc(String id, String content) {
        this.id = id;
        this.content = content;
        file = null;
    }

    public Doc(File file) throws FileNotFoundException {
       
        this.file = file;
        this.id = file.getPath();
        content = null;
    }

    @Override
    public String getContent() {
        if(file != null) try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Doc.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Doc.class.getName()).log(Level.SEVERE, null, ex);
        }
        return content;
    }
    
    @Override
    public String getId() {
        return id;
    }

}
