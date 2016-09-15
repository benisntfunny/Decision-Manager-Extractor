package com.allstate.rtd.clm;

import com.allstate.rtd.clm.db.Table;

import java.io.FileWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static Configuration config = new Configuration();
    static private List<Table> tables = new ArrayList();
    static ReadAndParseInsertScript rap = new ReadAndParseInsertScript(config.INSERT_FILE);
    static int maxChoiceSeq = 1000001;
    static int maxProjectSeq = 2;
    static int maxNoteSeq = 101;
    public static void main(String[] args) {
        //Print.out(rap.getTables());
        scanDB();
    }

    private static void makeResults()
    {
        try {
            FileWriter fw = new FileWriter(config.OUT_FILE);
            for (int i = 0; i < tables.size(); i++) {


                Table ct = tables.get(i);
                if (!ct.name.equalsIgnoreCase("RELATIONSHIP_TYPE")) {

                    if (ct.name.equals("CHOICE"))
                    {
                        String max = ct.data.get(ct.data.size()-1).get(0) + 5;
                        maxChoiceSeq = Integer.valueOf(max);
                    }
                    if (ct.name.equals("NOTE"))
                    {
                        String max = ct.data.get(ct.data.size()-1).get(0) + 5;
                        maxNoteSeq = Integer.valueOf(max);
                    }
                    if (ct.name.equals("PROJECT"))
                    {
                        for (int x=ct.data.size()-1;x>=0;x--){
                            try {
                                if (Integer.valueOf(ct.data.get(x).get(1)) > maxProjectSeq)
                                {
                                    maxProjectSeq = Integer.valueOf(ct.data.get(x).get(1)) + 100;
                                }
                            }
                            catch (Exception e){
                            }
                        }
                    }
                    fw.write("/************** " + ct.name + " **************/\n");
                    Print.out("/************** " + ct.name + " **************/\n");
                    fw.write("/* " + ct.columns + " */\n");
                    Print.out("/* " + ct.columns + " */\n");
                    ct.columnCount = ct.columns.size();

                    for (int row = 0; row < ct.data.size(); row++) {
                        fw.write("INSERT INTO " + ct.name + " (" + ct.columns.toString().replaceAll(",", ",").replaceAll("[\\[.\\].\\s+]", "") +
                                ") VALUES (");
                        Print.outf("INSERT INTO " + ct.name + " (" + ct.columns.toString().replaceAll(",", ",").replaceAll("[\\[.\\].\\s+]", "") +
                                ") VALUES (");
                        for (int column = 0; column < ct.columnCount; column++) {

                            if (column != ct.columnCount - 1) {
                                if (ct.data.get(row).get(column).equals("null")) {
                                    fw.write("null,");
                                    Print.outf("null,");
                                } else {
                                    fw.write("'" + ct.data.get(row).get(column).replaceAll("'", "''") + "',");
                                    Print.outf("'" + ct.data.get(row).get(column).replaceAll("'", "''") + "',");
                                }
                            } else {
                                if (ct.data.get(row).get(column).equals("null")) {
                                    fw.write("null");
                                    Print.outf("null");
                                } else {
                                    fw.write("'" + ct.data.get(row).get(column).replaceAll("'", "''") + "'");
                                    Print.outf("'" + ct.data.get(row).get(column).replaceAll("'", "''") + "'");
                                }
                            }

                        }
                        fw.write(");\n");
                        Print.outf(");\n");
                    }
                    fw.write("\n");
                    Print.outf("\n");

                }
            }
            fw.write("DROP SEQUENCE NOTE_SEQ;\n" +
                    "DROP SEQUENCE CHOICE_SEQ;\n" +
                    "DROP SEQUENCE PROJ_DEPLOY_SEQ;\n\n");
            Print.out("DROP SEQUENCE NOTE_SEQ;\n" +
                    "DROP SEQUENCE CHOICE_SEQ;\n" +
                    "DROP SEQUENCE PROJ_DEPLOY_SEQ;\n");
            fw.write("CREATE SEQUENCE NOTE_SEQ INCREMENT BY 1 START WITH 100 CACHE 10;\n");
            fw.write("CREATE SEQUENCE PROJ_DEPLOY_SEQ INCREMENT BY 1 START WITH 1;\n");
            fw.write("CREATE SEQUENCE CHOICE_SEQ INCREMENT BY 1 START WITH 1000000 CACHE 10;\n");
            fw.write("COMMIT;");
            Print.out("CREATE SEQUENCE NOTE_SEQ INCREMENT BY 1 START WITH " + String.valueOf(maxNoteSeq) +" CACHE 10;");
            Print.out("CREATE SEQUENCE PROJ_DEPLOY_SEQ INCREMENT BY 1 START WITH "+ String.valueOf(maxProjectSeq) +";");
            Print.out("CREATE SEQUENCE CHOICE_SEQ INCREMENT BY 1 START WITH "+ String.valueOf(maxChoiceSeq) +" CACHE 10;\n");
            Print.out("COMMIT;");

            fw.close();
        }
        catch (Exception e){
        Print.out("\n" + e);
        }

    }

    private static void scanDB()
    {

        try {
            Connection connection = null;
            connection = DriverManager.getConnection(config.DB_URL, config.DB_USER, config.DB_PASS);
            String query = "select DISTINCT TABLE_NAME\n" +
                    "from all_tab_cols\n" +
                    "where owner = '"+ config.TABLE_OWNER+"'\n" +
                    " AND TABLE_NAME NOT LIKE 'V_%' AND TABLE_NAME != 'DMCONFIG'" +
                    " AND TABLE_NAME != 'SCHEMA_VERSION' " +
                    " AND TABLE_NAME != 'PS_TXN' AND \n (TABLE_NAME LIKE 'ILS_%' OR TABLE_NAME = 'RELATIONSHIP' "+
                    " OR TABLE_NAME = 'CHOICE_GROUP' \n OR TABLE_NAME = 'NOTE' " +
                    " OR TABLE_NAME = 'RELATIONSHIP_TYPE' OR TABLE_NAME = 'CHOICE' OR TABLE_NAME = 'PROJECT')";
            Table queryTables = new Table();
            queryTables.name = "queryTables";
            queryTables = selectTable(connection,query,queryTables);

            for (int i=0;i<queryTables.data.size();i++){
                query = "select * from " + queryTables.data.get(i).get(0);
                Table currentTable = new Table();
                currentTable.name = queryTables.data.get(i).get(0);
                currentTable = selectTable(connection,query,currentTable);
                tables.add(currentTable);
            }
            connection.close();

            CompareOps comp = new CompareOps(rap,tables);
            tables = comp.getResult();
            makeResults();


        } catch (SQLException sqle) {
            sqle.printStackTrace();

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    private static Table selectTable(Connection connection,String query,Table table)
    {
        try {
            Statement stmt = connection.createStatement();

            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

            ResultSet rs = stmt.executeQuery(query);
            //List<String> columnHeader = new ArrayList();
            ResultSetMetaData meta = rs.getMetaData();

            int columnCount = meta.getColumnCount();

            for (int i=1;i<=columnCount;i++)
            {
                table.columns.add(meta.getColumnName(i));
                table.columnType.add(meta.getColumnTypeName(i));
                table.columnPrecision.add(meta.getPrecision(i));
            }
            //table.add(columnHeader);
            while (rs.next()) {
                List<String> row = new ArrayList();
                for (int i=1;i<=columnCount;i++)
                {
                    try {
                        String result = "null";
                        if (meta.getColumnTypeName(i).equals("CLOB")) {
                            try {
                                Clob clob = rs.getClob(meta.getColumnName(i));
                                result = clob.getSubString(1, (int) clob.length()).trim();
                            }
                            catch (Exception e)
                            {
                                result = rs.getObject(meta.getColumnName(i)).toString().trim();
                            }
                        }
                        else if (meta.getColumnTypeName(i).equals("BLOB")) {
                            try {
                                Blob blob = (Blob) rs.getBlob(meta.getColumnName(i));;
                                //result = new String(blob.getBytes(1, (int) blob.length())).trim();
                            }
                            catch (Exception e)
                            {
                                Print.out(e);
                                result = rs.getObject(meta.getColumnName(i)).toString().trim();
                            }

                        }
                        else {
                            result = rs.getObject(meta.getColumnName(i)).toString().trim();
                        }
                        row.add(result);
                    }
                    catch (Exception e){
                        //Print.out(e);
                        row.add("null");
                    }
                }
                if (((table.name.equals("PROJECT") || table.name.equals("ILS_PROJECT") || table.name.equals("CHOICE")) && Long.valueOf(row.get(0)) < 1000001)
                        || table.name.equals("CHOICE_GROUP") && row.get(0).equals("Project")) {}
                else {
                    table.data.add(row);
                }
            }
        }
        catch (SQLException e)
        {

        }
        return table;

    }
}
