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
package slib.sml.smutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * Class used to process large SME results files through SQLlite databases
 *
 * Processed file must respect the following restrictions: - tab separated
 * format - containing a header (first non-empty line which do not start with !
 * ). Use ! to prefix comment lines - the first two columns are dedicated to the
 * name of the entity compared (the name of the corresponding columns will be
 * set to "entity_A" and "entity_B") - the other columns are considered as
 * double (decimal) results.
 *
 * The class can be used to : - create an SQLite database from an input file
 * containing SME result a table will be create for each input file - merge two
 * tables into a new database table - delete a specific table - copy a table
 * from an SQLlite database to another - get information from a specific
 * database or table
 *
 * Limits Note that SQLite implementation to not allow SELECT queries on
 * databases containing tables with more than 500 columns. If such big table are
 * processed a SQL_Exception will be throw.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class SQLiteUtils {

    Logger logger = LoggerFactory.getLogger(SQLiteUtils.class);
    /**
     *
     */
    public static int BATCH_LIMIT = 25000;
    /**
     *
     */
    public static int BATCH_LIMIT_MATRIX_LINE = 1000;
    private String e_A_flag = "entity_A";
    private String e_B_flag = "entity_B";

    /**
     * Extract the header of the specified tabular file as an array of String
     * containing its elements (columns). The header is the first non empty line
     * which do not start with !
     *
     * @param filepath The file where to extract the header
     * @return an array of String containing the elements composing the header
     * or null if not header is found
     *
     * @throws SGL_Ex_Critic
     */
    private String[] getHeader(String filepath) throws SLIB_Ex_Critic {

        String[] header = null;

        try {
            FileInputStream fstream = new FileInputStream(filepath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (!line.startsWith("!") && !line.isEmpty()) {
                    header = line.split("\t");
                    break;
                }
            }
            in.close();
        } catch (IOException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
        return header;
    }

    /**
     * Build an String from an array of String considering tab as glue
     *
     * @param array, array of String
     * @return a String containing the array elements separated by a tab
     */
    private String implodeArray(String[] array) {

        String output = "";

        StringBuilder sb = new StringBuilder();
        sb.append(array[0]);

        for (int i = 1; i < array.length; i++) {
            sb.append("\t");
            sb.append(array[i]);
        }

        output = sb.toString();
        return output;
    }

    /**
     * Create a SQLlite database from a tabular file. The input file must
     * respect the following restrictions: - tab separated format - the first
     * two columns are dedicated to the name of the entity compared (the name of
     * the corresponding columns will be set to "entity_A" and "entity_B") - the
     * other columns are considered as double results.
     *
     * Warning: note that the table will be dropped of the database if it
     * already exists
     *
     * The insertion are made through a PreparedStatement object. Batch
     * execution is governed by globla BATCH_LIMIT
     *
     * @param filepath the tabular file containing the results
     * @param db the name of the database to consider
     * @param tableName the name of the table where to flush the results
     * @throws SLIB_Ex_Critic
     *
     */
    public void createTableDB(String filepath, String db, String tableName) throws SLIB_Ex_Critic {


        logger.info("Create SQLlite DB from " + filepath);
        logger.info("Batch limit : " + BATCH_LIMIT);

        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + db);


            Statement stat = conn.createStatement();
            stat.executeUpdate("drop table if exists " + tableName + ";");


            String[] header = getHeader(filepath);

            if (header == null) {
                throw new SLIB_Ex_Critic("Cannot locate header of file " + filepath);
            } else if (header.length < 2) {
                throw new SLIB_Ex_Critic("Corrupted file " + filepath + ", header must contains at least two fields dtected header :" + implodeArray(header));
            }

            logger.info("Populating SQLlite DB ... be patient");

            String methodNames = "";
            String methodNames_q = "";
            for (int i = 2; i < header.length; i++) {
                methodNames += ", \"" + header[i] + "\"";
                methodNames_q += ",?";
            }

            String header_db = "\"" + e_A_flag + "\", \"" + e_B_flag + "\"" + methodNames;

            logger.info("Header: " + header_db);

            String query = "create table " + tableName + " (" + header_db + ")";

            logger.debug("query: " + query);
            stat.executeUpdate(query);

            // id A - id -B - header values
            PreparedStatement prep = conn.prepareStatement("insert into " + tableName + " values (?, ?" + methodNames_q + ");");

            conn.setAutoCommit(false);


            // read the file and load the DB
            try {
                FileInputStream fstream = new FileInputStream(filepath);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line;
                long countLine = 0;

                int batchCount = 0;

                long processed = 0;

                boolean headerb = false;
                while ((line = br.readLine()) != null) {

                    line = line.trim();
                    countLine++;

                    if (!line.startsWith("!") && !line.isEmpty()) {

                        String[] data = line.split("\t");

                        if (headerb == false) {
                            headerb = true;
                        } else {
                            if (data.length != header.length) {
                                throw new SLIB_Ex_Critic("Corrupted file " + filepath + ", result line " + countLine + " contains abnormal number of values considering header");
                            } else {


                                // TODO check if duplicate entry are detected
                                for (int j = 0; j < data.length; j++) {

                                    if (j < 2) {
                                        prep.setString(j + 1, data[j]);
                                    } else {
                                        prep.setDouble(j + 1, Double.parseDouble(data[j]));
                                    }
                                }

                                prep.addBatch();
                                batchCount++;

                                if (batchCount == BATCH_LIMIT) {
                                    prep.executeBatch();
                                    batchCount = 0;
                                }
                                processed++;
                                if (processed % BATCH_LIMIT == 0) {
                                    logger.info("processed " + processed);
                                }

                            }
                        }
                    }
                }
                in.close();
            } catch (IOException e) {//Catch exception if any
                throw new SLIB_Ex_Critic(e.getMessage());
            }
            prep.executeBatch();
            conn.commit();
            conn.close();

            logger.info("Table " + tableName + " created in database " + db);

        } catch (SQLException e) {
            throw new SLIB_Ex_Critic(e);
        } catch (ClassNotFoundException e) {
            throw new SLIB_Ex_Critic(e);
        }
    }

    /**
     * Merge two tables of a same database into a new table located in another
     * database.
     *
     * Only tuples of the two tables who share entity_A and entity_B values will
     * be conserved in the merged table. Note that the first table is considered
     * as reference which implied that duplicate columns between the two tables
     * will always take the value specified in the reference table.
     *
     * @param db_tAB database of the tables to merge
     * @param table_A first table name
     * @param table_B second table name
     * @param db_tmerge database in which the merge table will be created
     * (different from db_tAB)
     * @param table_merge the name of the new table
     *
     * @throws SLIB_Exception
     */
    public void mergeTables(String db_tAB, String table_A, String table_B, String db_tmerge, String table_merge) throws SLIB_Exception {

        mergeTables(db_tAB, table_A, db_tAB, table_B, db_tmerge, table_merge);
    }

    /**
     * Merge two tables into a new table.
     *
     * Only tuples of the two tables who share entity_A and entity_B values will
     * be conserved in the merged table. Note that the first table is considered
     * as reference which implied that duplicate columns between the two tables
     * will always take the value specified in the reference table.
     *
     * @param db_tA database of the first table to merge
     * @param table_A first table name
     * @param db_tB database of the second table to merge
     * @param table_B second table name
     * @param db_tmerge database in which the merge table will be created
     * @param table_merge the name of the new table
     *
     * @throws SLIB_Ex_Critic
     */
    public void mergeTables(String db_tA, String table_A, String db_tB, String table_B, String db_tmerge, String table_merge) throws SLIB_Ex_Critic {

        logger.info("Loading SQLlite DB : " + table_A + " from " + db_tA + "  " + table_B + " from " + db_tB);
        logger.info("Batch limit : " + BATCH_LIMIT);

        try {

            Class.forName("org.sqlite.JDBC");

            Connection conn_A, conn_B, conn_M;
            Statement stat_A, stat_B, stat_M;

            conn_A = DriverManager.getConnection("jdbc:sqlite:" + db_tA);

            if (db_tA.equals(db_tB)) {
                conn_B = conn_A;
            } else {
                conn_B = DriverManager.getConnection("jdbc:sqlite:" + db_tB);
            }

            //			if(db_tA.equals(db_tmerge) || db_tB.equals(db_tmerge))
            //				throw new SGL_Exception_Critical("Due to concurrent access restriction on SQLlite engine you cannot use an already specified database for merge table "+table_merge+" (do not use specified databases "+db_tA+"/"+db_tB+")");

            if (db_tA.equals(db_tmerge)) {
                conn_M = conn_A;
            } else if (db_tB.equals(db_tmerge)) {
                conn_M = conn_B;
            } else {
                conn_M = DriverManager.getConnection("jdbc:sqlite:" + db_tmerge);
            }

            stat_A = conn_A.createStatement();
            stat_B = conn_B.createStatement();
            stat_M = conn_M.createStatement();



            stat_M.executeUpdate("drop table if exists " + table_merge + ";");


            logger.info("Merging SQLlite DB  to " + db_tmerge + " table " + table_merge + "... be patient");

            // Build Header merging table headers

            DatabaseMetaData meta_A = conn_A.getMetaData();
            DatabaseMetaData meta_B = conn_B.getMetaData();

            if (!tablesExists(meta_A, table_A)) {
                throw new SLIB_Ex_Critic("Cannot find table " + table_A + " in database " + db_tA);
            }

            if (!tablesExists(meta_B, table_B)) {
                throw new SLIB_Ex_Critic("Cannot find table " + table_B + " in database " + db_tB);
            }


            ArrayList<String> colNames_tA = new ArrayList<String>();
            ArrayList<String> colNames_tB = new ArrayList<String>();

            ResultSet rsColumns = meta_A.getColumns(null, null, table_A, null);
            while (rsColumns.next()) {
                String columnName = rsColumns.getString("COLUMN_NAME");
                colNames_tA.add(columnName);
            }
            rsColumns.close();

            rsColumns = meta_B.getColumns(null, null, table_B, null);
            while (rsColumns.next()) {
                String columnName = rsColumns.getString("COLUMN_NAME");
                colNames_tB.add(columnName);
            }
            rsColumns.close();

            // Delete duplicates found in table 2
            ArrayList<String> columnsNames_tableToRemove = new ArrayList<String>();
            columnsNames_tableToRemove.add(e_A_flag);
            columnsNames_tableToRemove.add(e_B_flag);

            for (int i = 2; i < colNames_tB.size(); i++) {

                if (colNames_tA.contains(colNames_tB.get(i))) {
                    logger.info("skipping column results " + colNames_tB.get(i) + " specified in table " + table_B);
                    columnsNames_tableToRemove.add(colNames_tB.get(i));
                }
            }

            int[] colNames_tB_id = new int[colNames_tB.size() - columnsNames_tableToRemove.size()];

            int id = 0;

            for (int i = 0; i < colNames_tB.size(); i++) {
                if (!columnsNames_tableToRemove.contains(colNames_tB.get(i))) {
                    colNames_tB_id[id] = i + 1;
                    id++;
                }
            }

            colNames_tB.removeAll(columnsNames_tableToRemove);

            @SuppressWarnings("unchecked") // this is safe
            ArrayList<String> headerMerge = (ArrayList<String>) colNames_tA.clone();
            headerMerge.addAll(colNames_tB);

            logger.info("Final header : " + headerMerge);

            if (colNames_tB.size() == 0) {
                throw new SLIB_Exception("Empty table " + table_A + ", no values to merge");
            }
            if (colNames_tB.size() == 0) {
                throw new SLIB_Exception("Empty table " + table_B + ", no values to merge");
            }

            String header_m = "\"" + e_A_flag + "\",\"" + e_B_flag + "\"";
            String header_q = "?,?";

            for (int i = 2; i < headerMerge.size(); i++) {
                header_m += ", \"" + headerMerge.get(i) + "\"";
                header_q += ", ?";
            }

            int batchCount = 0;
            int skipped = 0;
            int processed = 0;

            ResultSet rs_tA = stat_A.executeQuery("select * from " + table_A + ";");
            ResultSet rs_tB = stat_B.executeQuery("select * from " + table_B + ";");
            ResultSet rez = null;


            stat_M.executeUpdate("create table " + table_merge + " (" + header_m + ");");



            // id A - id -B - header values
            PreparedStatement prep = conn_M.prepareStatement("insert into " + table_merge + " values (" + header_q + ");");

            conn_M.setAutoCommit(false);

            processed = 0;

            while (rs_tA.next()) {

                String e_A = rs_tA.getString(e_A_flag);
                String e_B = rs_tA.getString(e_B_flag);

                rez = getResultSet(rs_tB, stat_B, table_B, e_A, e_B);



                if (rez != null) {

                    processed++;

                    // Insert to the new Database
                    prep.setString(1, e_A);
                    prep.setString(2, e_B);

                    int c = 3;

                    for (int i = 2; i < colNames_tA.size(); i++) {
                        prep.setDouble(c, rs_tA.getDouble(i + 1));
                        c++;
                    }

                    for (int i = 0; i < colNames_tB_id.length; i++) {
                        prep.setDouble(i + c, rez.getDouble(colNames_tB_id[i]));
                    }

                    prep.addBatch();

                    batchCount++;

                    if (batchCount == BATCH_LIMIT) {
                        prep.executeBatch();
                        batchCount = 0;
                    }
                    if (processed % BATCH_LIMIT == 0) {
                        logger.info("processed " + processed);
                    }
                } else {
                    skipped++;
                    logger.info("skipped = " + e_A + "   " + e_B);
                }
            }


            prep.executeBatch();
            prep.close();

            if (rez != null) {
                rez.close();
            }

            rs_tA.close();
            rs_tB.close();

            stat_A.close();
            stat_B.close();
            stat_M.close();


            conn_M.commit();

            conn_A.close();

            if (!conn_B.isClosed()) {
                conn_B.close();
            }


            if (!conn_M.isClosed()) {
                conn_M.close();
            }



            logger.info("Merging performed.");
            logger.info("merged  : " + processed);
            logger.info("skipped : " + skipped);

        } catch (Exception e) {
            e.printStackTrace();
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

    /**
     * Check if the specified table exists considering DatabaseMetaData
     * information
     *
     * @param dbMetadata database meta data as DatabaseMetaData object
     * @param table name of the table existence have to be checked
     * @return boolean, true if the table exists
     *
     * Note that SQLlite implementation to not allow SELECT queries on databases
     * containing tables with more than 500 columns. If such big table are
     * processed a SQL_Exception will be throw.
     *
     * @throws SGL_Exception SQL_Exception are encompassed in a SGL_Exception
     */
    private boolean tablesExists(DatabaseMetaData dbMetadata, String table) throws SLIB_Exception {

        try {
            ResultSet res = dbMetadata.getTables(null, null, null,
                    new String[]{"TABLE"});

            while (res.next()) {
                if (table.equalsIgnoreCase(res.getString("TABLE_NAME"))) {
                    res.close();
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
        return false;
    }

    /**
     * Return a resultSet at a state corresponding to the specified entries
     * (entity_A and entity_B) The resultSet will be iterated such as the
     * corresponding tuple is found. If the queried tuple is founded the
     * resulSet is returned such as the cursor is placed on the specified tuple
     * If the query cannot be satisfied the returned ResultSet is set to null.
     *
     *
     * @param rs resultSet on which the iteration has to be made
     * @param stat the statement enabling to set the resultSet cursor position
     * to beforeFirst. SQLlite only admit forward cursor...
     * @param table the table on which the query have to be made to relocate the
     * cursor if required
     * @param e_A entity_A the query must satisfied
     * @param e_B entity_B the query must satisfied
     * @return a resulSet such as the cursor is placed on the tuple
     * characterized by the specified query. Null if the query cannot be
     * satisfied
     * @throws SQLException
     */
    private ResultSet getResultSet(ResultSet rs, Statement stat, String table, String e_A, String e_B) throws SQLException {

        String firstTested_eA = null;
        String firstTested_eB = null;

        ResultSet rez = null;

        while (rs.next()) {


            String table_2_e1_tmp = rs.getString(e_A_flag);
            String table_2_e2_tmp = rs.getString(e_B_flag);

            if (firstTested_eA == null) {
                firstTested_eA = table_2_e1_tmp;
                firstTested_eB = table_2_e2_tmp;
            }

            if (e_A.equals(table_2_e1_tmp) && e_B.equals(table_2_e2_tmp)) {
                rez = rs;
                break;
            }
        }

        if (rez == null) {

            rs.close();
            rs = stat.executeQuery("select * from " + table + ";");

            while (rs.next()) {

                String e__b_A = rs.getString(e_A_flag);
                String e__b_B = rs.getString(e_B_flag);

                if (firstTested_eA == null) {
                    firstTested_eA = e__b_A;
                    firstTested_eB = e__b_B;
                }

                if (e_A.equals(e__b_A) && e_B.equals(e__b_B)) {
                    rez = rs;
                    break;
                }

                if (firstTested_eA.equals(e__b_A) && firstTested_eB.equals(e__b_B)) {
                    break;
                }
            }
        }
        return rez;
    }

    /**
     * Flush a table in a tabular file The first two columns are considered as
     * literal, the others as Double values
     *
     * The number of tuples load into memory before flushing are governed by
     * BATCH_LIMIT_MATRIX_LINE
     *
     * @param sqlLiteDB the database containing the table to consider
     * @param table the name of the table
     * @param outfile the file in which the table will be flush
     * @throws SLIB_Ex_Critic
     *
     *
     */
    public void flushTableInFile(String sqlLiteDB, String table, String outfile) throws SLIB_Ex_Critic {


        logger.info("flushing SQLlite table " + table + " of database " + sqlLiteDB + " in " + outfile);
        logger.info("Batch limit (matrix line) : " + BATCH_LIMIT_MATRIX_LINE);
        logger.info("Please wait...");

        try {

            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + sqlLiteDB);
            Statement stat = conn.createStatement();

            DatabaseMetaData meta = conn.getMetaData();

            ArrayList<String> columnsNames = new ArrayList<String>();

            int e_A_id = -1;
            int e_B_id = -1;

            ResultSet rsColumns = meta.getColumns(null, null, table, null);
            int c = 0;
            while (rsColumns.next()) {
                String columnName = rsColumns.getString("COLUMN_NAME");
                columnsNames.add(columnName);

                if (columnName.equals(e_A_flag)) {
                    e_A_id = c;
                } else if (columnName.equals(e_B_flag)) {
                    e_B_id = c;
                }
                c++;

            }



            FileWriter fstream = new FileWriter(outfile);
            BufferedWriter outbuff = new BufferedWriter(fstream);

            StringBuilder tmp_s = new StringBuilder();
            for (int i = 0; i < columnsNames.size(); i++) {
                if (i != 0) {
                    tmp_s.append("\t");
                }
                tmp_s.append(columnsNames.get(i));
            }
            outbuff.write(tmp_s.toString());


            logger.info("Column number : " + columnsNames.size());

            ResultSet rs = stat.executeQuery("select * from " + table + ";");

            long line = 0;
            long lc = 0;

            tmp_s = new StringBuilder();

            while (rs.next()) {

                line++;
                lc++;

                tmp_s.append("\n");

                for (int i = 0; i < columnsNames.size(); i++) {

                    if (i != 0) {
                        tmp_s.append("\t");
                    }

                    if (i == e_A_id || i == e_B_id) {
                        tmp_s.append(rs.getString(i + 1));
                    } else {
                        tmp_s.append(rs.getDouble(i + 1));
                    }
                }

                if (lc == BATCH_LIMIT_MATRIX_LINE) {
                    lc = 0;
                    outbuff.write(tmp_s + "\n");
                    tmp_s = new StringBuilder();
                }

                if (line % BATCH_LIMIT_MATRIX_LINE == 0) {
                    logger.info("processed " + line);
                }
            }

            if (lc != 0) {
                outbuff.write(tmp_s + "\n");
            }

            outbuff.close();

            rs.close();
            conn.close();

            logger.info(line + " lines flushed");
            logger.info("db flushed, consult " + outfile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

    /**
     * Copy a table. If the destination database do not exits it will be created
     * if the already exists in the new database it will be dropped
     *
     * @param db the database containing the table to copy
     * @param table the name of the table
     * @param newDB the name of the database where the new table will be created
     * @param tableNew
     * @throws SLIB_Ex_Critic
     */
    public void copyTable(String db, String table, String newDB, String tableNew) throws SLIB_Ex_Critic {

        logger.info("Copy SQLlite DB : " + table + " from " + db + " to " + newDB + " table " + tableNew);
        logger.info("Batch limit : " + BATCH_LIMIT);

        boolean newDBexists = (new File(newDB)).exists();

        if (!newDBexists) {
            logger.info("Creating new database.");
        }

        try {

            Class.forName("org.sqlite.JDBC");

            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + db);
            Connection connNewDB;

            //			if(db.equals(newDB))
            //				throw new SGL_Exception_Critical("Cannot copy a table to the same database");

            if (db.equals(newDB)) {
                connNewDB = conn;
            } else {
                connNewDB = DriverManager.getConnection("jdbc:sqlite:" + newDB);
            }

            DatabaseMetaData meta = conn.getMetaData();
            DatabaseMetaData metaNew = connNewDB.getMetaData();

            if (!tablesExists(meta, table)) {
                throw new SLIB_Ex_Critic("Cannot locate table " + table + " in database " + db);
            }

            if (tablesExists(metaNew, tableNew)) {
                logger.info("dropt table " + tableNew + " (already exists in database " + newDB + ")");
            }

            Statement stat = conn.createStatement();
            Statement statNew = connNewDB.createStatement();

            statNew.executeUpdate("drop table if exists " + tableNew + ";");

            // retrieve columns
            String colString = "";
            String colStringQ = "";
            ResultSet rsColumns = meta.getColumns(null, null, table, null);
            int colCount = 0;
            while (rsColumns.next()) {
                String columnName = rsColumns.getString("COLUMN_NAME");

                if (colCount != 0) {
                    colString += ",";
                    colStringQ += ",";
                }

                colString += "\"" + columnName + "\"";
                colStringQ += "?";
                colCount++;
            }
            rsColumns.close();

            String query = "create table " + tableNew + " (" + colString + ")";
            logger.debug(query);
            statNew.executeUpdate(query);


            // Copy values 

            ResultSet rs = stat.executeQuery("select * from " + table + ";");
            PreparedStatement prep = connNewDB.prepareStatement("insert into " + tableNew + " values (" + colStringQ + ");");

            connNewDB.setAutoCommit(false);

            long processed = 0;
            int batchCount = 0;

            while (rs.next()) {

                processed++;

                String e_A = rs.getString(e_A_flag);
                String e_B = rs.getString(e_B_flag);

                // Insert to the new Database
                prep.setString(1, e_A);
                prep.setString(2, e_B);

                for (int i = 2; i < colCount; i++) {
                    prep.setDouble(i + 1, rs.getDouble(i + 1));
                }

                prep.addBatch();

                batchCount++;

                if (batchCount == BATCH_LIMIT) {
                    prep.executeBatch();
                    batchCount = 0;
                }
                if (processed % BATCH_LIMIT == 0) {
                    logger.info("processed " + processed);
                }
            }


            prep.executeBatch();
            prep.close();

            statNew.close();
            connNewDB.commit();
            connNewDB.close();


            rs.close();
            stat.close();
            conn.close();

            logger.info("Copy performed");

        } catch (Exception e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

    /**
     * Drop specified columns from the table
     *
     * @param db the database containing the table to process
     * @param table the table to process
     * @param columnsToDrop a list of strings corresponding to the columns to
     * delete
     *
     * SQLlite do not support ALTER column, a temporal copy of the table is
     * performed during the process
     * @throws SLIB_Ex_Critic
     */
    public void dropColumns(String db, String table, Set<String> columnsToDrop) throws SLIB_Ex_Critic {

        logger.info("Drop Columns " + Arrays.toString(columnsToDrop.toArray()) + " from " + table + " of database " + db);
        logger.info("Batch limit : " + BATCH_LIMIT);

        try {

            Class.forName("org.sqlite.JDBC");

            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + db);

            DatabaseMetaData meta = conn.getMetaData();

            if (!tablesExists(meta, table)) {
                throw new SLIB_Ex_Critic("Cannot locate table " + table + " in database " + db);
            }

            Statement stat = conn.createStatement();
            Statement statDrop = conn.createStatement();



            // retrieve columns
            String colString = "";
            String colStringQ = "";
            ResultSet rsColumns = meta.getColumns(null, null, table, null);
            int colCount = 1;

            ArrayList<Integer> columnIds = new ArrayList<Integer>();

            while (rsColumns.next()) {
                String columnName = rsColumns.getString("COLUMN_NAME");

                if (!columnsToDrop.contains(columnName)) {

                    if (columnIds.size() != 0) {
                        colString += ",";
                        colStringQ += ",";
                    }

                    colString += "\"" + columnName + "\"";
                    colStringQ += "?";
                    columnIds.add(colCount);
                }
                colCount++;
            }
            rsColumns.close();

            // generate a tmp table
            Random generator = new Random();
            String tmp_table = "tmp_table" + generator.nextInt(100000);

            while (tablesExists(meta, tmp_table)) {
                tmp_table = "tmp_table_" + generator.nextInt(100000);
            }

            String query = "create table " + tmp_table + " (" + colString + ")";
            logger.debug(query);
            statDrop.executeUpdate(query);


            ResultSet rs = stat.executeQuery("select " + colString + " from " + table + ";");
            PreparedStatement prep = conn.prepareStatement("insert into " + tmp_table + " values (" + colStringQ + ");");

            conn.setAutoCommit(false);


            int id = 0;
            long batchCount = 0;
            long processed = 0;
            while (rs.next()) {


                for (int i = 0; i < columnIds.size(); i++) {
                    id = columnIds.get(i);

                    if (id < 3) // id 1 && 2 correspond to literal (entity id)
                    {
                        prep.setString(i + 1, rs.getString(i + 1));
                    } else {
                        prep.setDouble(i + 1, rs.getDouble(i + 1));
                    }
                }

                prep.addBatch();
                batchCount++;
                processed++;

                if (batchCount == BATCH_LIMIT) {
                    prep.executeBatch();
                    batchCount = 0;
                }
                if (processed % BATCH_LIMIT == 0) {
                    logger.info("processed " + processed);
                }
            }

            prep.executeBatch();
            prep.close();
            conn.setAutoCommit(true);

            statDrop.close();

            rs.close();


            stat.executeUpdate("drop table if exists " + table + ";");
            stat.executeUpdate("ALTER TABLE " + tmp_table + " RENAME TO " + table + ";");

            stat.close();
            conn.close();

            logger.info("Drop column performed");

        } catch (Exception e) {
            e.printStackTrace();
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

    /**
     * Drop a table from a database
     *
     * @param db the database
     * @param table the name of table to drop
     * @throws SLIB_Ex_Critic
     */
    public void dropTable(String db, String table) throws SLIB_Ex_Critic {

        logger.info("Drop table : " + table + " from " + db);

        try {

            Class.forName("org.sqlite.JDBC");

            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + db);

            Statement stat = conn.createStatement();
            stat.executeUpdate("drop table if exists " + table + ";");
            stat.close();
            conn.close();

            logger.info("Table dropped");

        } catch (Exception e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

    /**
     * Return info a specified database
     *
     * @param db the database
     * @throws SLIB_Ex_Critic
     */
    public void getInfo(String db) throws SLIB_Ex_Critic {

        getInfo(db, null);
    }

    /**
     * Return info a specified table
     *
     * @param db the database
     * @param table the table
     * @throws SLIB_Ex_Critic
     */
    public void getInfo(String db, String table) throws SLIB_Ex_Critic {


        try {

            Class.forName("org.sqlite.JDBC");

            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + db);
            Statement stat = conn.createStatement();

            DatabaseMetaData meta = conn.getMetaData();

            String[] types = {"TABLE"};
            ResultSet resultSet = meta.getTables(null, null, "%", types);

            String dbInfo = "Database " + db + " ";
            String tableInfo = "";
            int tableCount = 0;
            // Get the table names
            while (resultSet.next()) {

                tableCount++;

                // Get the table name
                String tableName = resultSet.getString(3);

                if (table == null || (table != null && table.equalsIgnoreCase(tableName))) {

                    String columInfo = "";
                    int columnCount = 0;

                    ResultSet rs = stat.executeQuery("select COUNT(*) from " + tableName + ";");
                    long rowNB = rs.getLong(1);

                    ResultSet rsColumns = meta.getColumns(null, null, tableName, null);
                    while (rsColumns.next()) {
                        String columnName = rsColumns.getString("COLUMN_NAME");
                        columInfo += "\t" + columnName + "\n";
                        columnCount++;
                    }
                    rsColumns.close();

                    tableInfo += tableName + " (" + columnCount + " columns, " + rowNB + " tuples)\n";
                    tableInfo += columInfo;
                }
            }



            resultSet.close();

            stat.close();
            conn.close();

            dbInfo += "(" + tableCount + " tables) \n";
            dbInfo += tableInfo;

            System.out.println(dbInfo);
        } catch (Exception e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

    /**
     * Rename a table in a database. Throw an exception if the table name
     * already exists
     *
     * @param db
     * @param table
     * @param newTableName
     * @throws SLIB_Ex_Critic
     */
    public void renameTable(String db, String table, String newTableName) throws SLIB_Ex_Critic {

        logger.info("Rename table : " + table + " from " + db + " to " + newTableName);

        try {

            Class.forName("org.sqlite.JDBC");

            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + db);

            Statement stat = conn.createStatement();
            stat.executeUpdate("ALTER TABLE " + table + " RENAME TO " + newTableName + ";");
            stat.close();
            conn.close();

            logger.info("Table renamed");

        } catch (Exception e) {
            e.printStackTrace();
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

    /**
     *
     * @return the value associated to the batch limit, the number of value
     * stored in memory.
     */
    public int getBATCH_LIMIT() {
        return BATCH_LIMIT;
    }

    /**
     *
     * Setter of the number of value stored in memory.
     *
     * @param BATCH_LIMIT
     */
    public void setBATCH_LIMIT(int BATCH_LIMIT) {
        this.BATCH_LIMIT = BATCH_LIMIT;
    }

    /**
     * @return the number of values stored by line.
     */
    public int getBATCH_LIMIT_MATRIX_LINE() {
        return BATCH_LIMIT_MATRIX_LINE;
    }

    /**
     * Mutator of the number of values stored by line.
     *
     * @param bATCH_LIMIT_MATRIX_LINE
     */
    public void setBATCH_LIMIT_MATRIX_LINE(int bATCH_LIMIT_MATRIX_LINE) {
        BATCH_LIMIT_MATRIX_LINE = bATCH_LIMIT_MATRIX_LINE;
    }
}
