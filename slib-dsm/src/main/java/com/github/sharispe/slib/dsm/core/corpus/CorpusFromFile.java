/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sharispe.slib.dsm.core.corpus;

import com.github.sharispe.slib.dsm.utils.Utils;
import java.io.File;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 * Corpus in which all the documents are specified into a single file. Each line
 * is considered to be a document.
 *
 * @author sharispe
 */
public class CorpusFromFile implements Corpus {

    String filepath;
    Long nbDoc;
    long size = -1;

    public CorpusFromFile(String location) {
        filepath = location;
    }

    @Override
    public long getSize() {

        if (size == -1) {
            try {
                size = Utils.countLines(filepath);
            } catch (IOException ex) {
                Logger.getLogger(CorpusFromFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return size;
    }

    @Override
    public Iterable<Document> getDocuments() {
        DocIterableLine di = null;
        try {
            di = new DocIterableLine(filepath);
        } catch (IOException ex) {
            Logger.getLogger(CorpusFromFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return di;
    }

    private class DocIterableLine implements Iterable<Document> {

        DirectoryStream<Path> newDirectoryStream;
        Iterator<Path> pathIterator;
        List<File> subdirectories;
        String filepath;
        LineIterator it;

        private DocIterableLine(String filepath) throws IOException {
            this.filepath = filepath;
            it = FileUtils.lineIterator(new File(filepath), "UTF-8");
        }

        @Override
        public Iterator<Document> iterator() {

            return new Iterator<Document>() {

                @Override
                public boolean hasNext() {
                    boolean hasNext = it.hasNext();
                    if (!hasNext) {
                        it.close();
                    }
                    return hasNext;
                }

                @Override
                public Document next() {

                    if (!hasNext()) {
                        return null;
                    } else {
                        String line = it.nextLine();
                        String[] data = line.split("\t", 2);
                        if(data.length != 2) return next();
                        return new Doc(data[0], data[1]);
                    }
                }
            };
        }

    }
}
