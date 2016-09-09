package com.allstate.rtd.clm;

import com.allstate.rtd.clm.db.Table;
import oracle.jdbc.OracleResultSet;

import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.*;

public class Main {
    static Configuration config = new Configuration();
    static private List<Table> tables = new ArrayList();
    public static void main(String[] args) {
        ReadAndParseInsertScript rap = new ReadAndParseInsertScript(config.INSERT_FILE);
        Print.out(rap.getTables());
        scanDB();
    }

    private static void makeResults()
    {
        try {
            FileWriter fw = new FileWriter(config.OUT_FILE);
            for (int i = 0; i < tables.size(); i++) {
            /*
            System.out.println("Press \"ENTER\" to continue...");
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
            */

                Table ct = tables.get(i);
                fw.write("/************** " + ct.name + " **************/\n");
                fw.write("/* " + ct.columns + " */\n");
                ct.columnCount = ct.columns.size();

                for (int row = 0; row < ct.data.size(); row++) {
                    fw.write("INSERT INTO " + ct.name + " (" + ct.columns.toString().replaceAll(",", ",").replaceAll("[\\[.\\].\\s+]", "") +
                            ") VALUES (");
                    for (int column = 0; column < ct.columnCount; column++) {

                        if (column != ct.columnCount - 1)
                            fw.write("'" + ct.data.get(row).get(column).replaceAll("'","''") + "',");
                        else
                            fw.write("'" + ct.data.get(row).get(column).replaceAll("'","''") + "'");

                    }
                    fw.write(");\n");
                }
                fw.write("\n");
            }
            fw.close();
        }
        catch (Exception e){

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
                    " AND TABLE_NAME != 'SCHEMA_VERSION' AND TABLE_NAME != 'PROJECT'" +
                    " AND TABLE_NAME != 'ILS_PROJECT' AND TABLE_NAME != 'PS_TXN'" +
                    " AND TABLE_NAME != 'NOTE' AND TABLE_NAME != 'CHOICE_GROUP' AND TABLE_NAME != 'CHOICE_ATTRIBUTE'" +
                    " AND TABLE_NAME != 'RELATIONSHIP_TYPE' ";
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
            makeResults();
            connection.close();

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
