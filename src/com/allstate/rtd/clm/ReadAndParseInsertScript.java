package com.allstate.rtd.clm;

import com.allstate.rtd.clm.db.Table;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
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
        Table cgroup = new Table();
        Table rtype = new Table();
        Table choiceAttribute = new Table();
            /* Make choice group table */
        cgroup.name = "CHOICE_GROUP";
        cgroup.columns.add("CHOICE_GROUP_ID");
        cgroup.columns.add("NAME");
        cgroup.columns.add("SEARCH_SORT_ORDER");
        cgroup.columns.add("CREATE_SORT_ORDER");

            /* Make relationship type table */
        rtype.name = "RELATIONSHIP_TYPE";
        String[] rtypeColumns = {"RELATIONSHIP_TYPE_ID", "FROM_CHOICE_GROUP_ID", "TO_CHOICE_GROUP_ID", "FROM_NAME", "FROM_DESCRIPTION",
                "TO_NAME", "TO_DESCRIPTION", "CARDINALITY", "ON_DELETE", "PROPAGATE_RULES",
                "           PROPAGATE_EVENTS"};
        rtype.columns=Arrays.asList(rtypeColumns);

            /* Make choice attribute table */
        choiceAttribute.name = "CHOICE_ATTRIBUTE";
        choiceAttribute.columns.add("CHOICE_GROUP_ID");
        choiceAttribute.columns.add("ATTRIBUTE_NAME");
        choiceAttribute.columns.add("RULE_TYPE");
        for (int i=0;i<insertScript.size();i++)
        {
            String line = insertScript.get(i).trim();
            if (line.contains("INSERT INTO CHOICE_GROUP"))
            {
                i++;
                List<String> row = makeRowFromInsert(insertScript.get(i));
                cgroup.data.add(row);
            }
            if (line.contains("INSERT INTO RELATIONSHIP_TYPE"))
            {
                i++;
                List<String> row = makeRowFromInsert(insertScript.get(i));
                rtype.data.add(row);
            }
            if (line.contains("INSERT INTO CHOICE_ATTRIBUTE"))
            {
                i++;
                List<String> row = makeRowFromInsert(insertScript.get(i));
                choiceAttribute.data.add(row);
            }
        }
        tables.add(rtype);tables.add(cgroup);tables.add(choiceAttribute);
    }

    private List<String> makeRowFromInsert(String text){
        text = text.replaceAll("', '",",").replaceAll("\\('","").replaceAll("'\\);","").replaceAll("\\);","").replaceAll(" , ",",").replaceAll("',",",")
        .replaceAll(",'",",").replace(", ",",").replace(" ,",",").trim();
        List<String> row = Arrays.asList(text.split(","));
        //Print.out(row);
        return row;
    }
}
