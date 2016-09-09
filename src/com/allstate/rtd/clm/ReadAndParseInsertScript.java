package com.allstate.rtd.clm;

import com.allstate.rtd.clm.db.Table;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by beub2 on 9/9/16.
 */
public class ReadAndParseInsertScript {

    private String scriptLocation;
    private List<String> insertScript = new ArrayList();
    private List<String> tableNames = new ArrayList();
    private List<Table> tables = new ArrayList();

    public List<Table> getTables() {
        return tables;
    }

    public ReadAndParseInsertScript (String script){
        scriptLocation = script;

        Print.out(scriptLocation);
        try {
            BufferedReader bufferedreader = new BufferedReader(new FileReader(scriptLocation));

            String line;
            while((line = bufferedreader.readLine()) != null)
            {
                insertScript.add(line);
            }
            bufferedreader.close();
        }
        catch (Exception e)
        {
            Print.out(e);
        }

        for (int i=0;i<insertScript.size();i++)
        {
            String line = insertScript.get(i).trim();
            if (line.contains("CREATE TABLE") && !line.contains("CREATE TABLE ILS_PROJECT"))
            {
                tableNames.add(line.replace("CREATE TABLE ",""));
            }
        }
        for (int t=0;t<tableNames.size();t++)
        {
            Table table = new Table();
            boolean inTable = false;
            boolean finishedReading = false;
            table.name = tableNames.get(t);

            for (int i=0;i<insertScript.size();i++)
            {
                if (!finishedReading) {
                    String line = insertScript.get(i).trim();

                    if (line.equals("CREATE TABLE " + table.name)) {
                        inTable = true;
                    }

                    else if (inTable && !line.contains("CONSTRAINT "))
                    {
                        if (!line.equals("(")) {
                            if (!line.equals(");")) {
                                Pattern p = Pattern.compile("([^\\s]+)");
                                Matcher m = p.matcher(line);
                                if (m.find())
                                {
                                    //Print.out(m.group(0));
                                    table.columns.add(m.group(0));
                                };
                                p = Pattern.compile("([A-z]+\\s)([A-z]+[0-9]*)(^\\()?");
                                m = p.matcher(line);
                                if (m.find()) {
                                  table.columnType.add(m.group(2));
                                  //Print.out(m.group(2));
                                }
                                p = Pattern.compile("[A-z]+\\s[A-z]+[0-9]*[\\(]?([0-9]+)?");
                                m = p.matcher(line);
                                if (m.find()) {
                                    //table.columnType.add(m.group(2));
                                    String match = m.group(1);
                                    int percision = 0;
                                    if (StringUtils.isNumeric(match))
                                    {
                                        percision = Integer.valueOf(m.group(1));
                                    }
                                    table.columnPrecision.add(percision);
                                }
                                //Print.out(line);
                            } else {
                                finishedReading = true;
                            }
                        }
                    }
                }
            }
            tables.add(table);
        }
    }

}
