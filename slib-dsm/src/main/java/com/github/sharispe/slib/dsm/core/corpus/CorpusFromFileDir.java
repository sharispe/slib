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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Corpus in which all the documents are the files located into a specific
 * directory
 *
 * @author sharispe
 */
public class CorpusFromFileDir implements Corpus {

    String corpusDir;
    Long nbFiles;

    public CorpusFromFileDir(String location) {
        corpusDir = location;
    }

    @Override
    public long getSize() {
        if (nbFiles == null) {
            try {
                nbFiles = Utils.countNbFiles(corpusDir);
            } catch (IOException ex) {
                Logger.getLogger(CorpusFromFileDir.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return nbFiles;
    }

    @Override
    public Iterable<Document> getDocuments() {
        DocIterable di = null;
        try {
            di = new DocIterable(Files.newDirectoryStream(FileSystems.getDefault().getPath(corpusDir)));
        } catch (IOException ex) {
            Logger.getLogger(CorpusFromFileDir.class.getName()).log(Level.SEVERE, null, ex);
        }
        return di;
    }

    private class DocIterable implements Iterable<Document> {

        @SuppressWarnings("unused")
		private DirectoryStream<Path> newDirectoryStream;
        private Iterator<Path> pathIterator;
        private List<File> subdirectories;

        private DocIterable(DirectoryStream<Path> newDirectoryStream) {
            this.newDirectoryStream = newDirectoryStream;
            pathIterator = newDirectoryStream.iterator();
            subdirectories = new ArrayList<File>();
        }

        @Override
        public Iterator<Document> iterator() {

            return new Iterator<Document>() {

                boolean isInit = false;
                Document next = null;

                @Override
                public boolean hasNext() {

                    if (!isInit) {
                        loadNext();
                        isInit = true;
                    }
                    return next != null;
                }

                private void loadNext() {

                    next = null;

                    try {

                        while (pathIterator.hasNext()) {

                            Path p = pathIterator.next();

                            File f = new File(p.toString());
                            if (!f.isDirectory()) {
                                next = new Doc(f);
                            } else {
                                subdirectories.add(f);
                            }

                            if (next != null) {
                                break;
                            }
                        }

                        if (next == null && !subdirectories.isEmpty()) {
                            File subdir = subdirectories.get(0);
                            subdirectories.remove(0);
                            pathIterator = Files.newDirectoryStream(subdir.toPath()).iterator();
                            loadNext();
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(CorpusFromFileDir.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                @Override
                public Document next() {

                    if (!isInit) {
                        loadNext();
                        isInit = true;
                    }
                    Document n = next;
                    loadNext();
                    return n;
                }
            };
        }

    }

    public static void main(String[] args) {

        Corpus c = new CorpusFromFileDir("/tmp/corpus");
        System.out.println(c.getSize());

        for (Document d : c.getDocuments()) {
            System.out.println(d.getId());
            System.out.println(d.getContent());
        }

    }
}
