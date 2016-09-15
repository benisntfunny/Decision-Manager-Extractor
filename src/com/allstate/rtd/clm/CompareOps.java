package com.allstate.rtd.clm;

import com.allstate.rtd.clm.db.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Created by beub2 on 9/13/16.
 */
public class CompareOps {

    private ReadAndParseInsertScript newt;
    static private List<Table> oldt;
    private HashMap<String,Table> newMap = new HashMap<String,Table>();
    private HashMap<String,Table> oldMap = new HashMap<String,Table>();

    public CompareOps(ReadAndParseInsertScript newTables, List<Table> currentTables)
    {
        newt = newTables;
        oldt = currentTables;
        for (Table newTable : newt.getTables()){
            newMap.put(newTable.name,newTable);
        }
        for (Table oldTable : oldt){
            oldMap.put(oldTable.name,oldTable);
        }

        findAndRemoveData();
        checkColumns();
        lookForRenamedRelationshipTypes();
    };

    private void findAndRemoveData()
    {
        List<Integer> tablesToRemove = new ArrayList();
        for (int i=0;i<oldt.size();i++)
        {
            String tableName = oldt.get(i).name;
            if (tableName.contains("ILS_") && newt.tableNames.indexOf(tableName) == -1) {
            tablesToRemove.add(i);
            }
        }
        for (int j = tablesToRemove.size()-1; j >= 0; j--)
        {
            int removeInt = tablesToRemove.get(j);
            String removeGroup = oldt.get(removeInt).name.replace("ILS_","");
            List<String> removeChoiceRows = new ArrayList();
            List<String> removeChoiceGroupRows = new ArrayList();
            List<String> removeRelationshipTypeRows = new ArrayList();
            List<String> removeRelationshipTypeIds = new ArrayList();
            List<String> removeRelationshipRows = new ArrayList();
            List<String> removeNoteRows = new ArrayList();

            for (Table ct : oldt)
            {
                if (ct.name.equals("CHOICE"))
                {
                    for (int i=0;i<ct.data.size();i++)
                    {
                        List<String> row = ct.data.get(i);
                        String rowGroup = row.get(7).toUpperCase().trim();
                        if (rowGroup.equals(removeGroup)) {
                            //Print.out("Setting row [" + i + "] with group " + row.get(7) + " for removal:" + row);
                            removeChoiceRows.add(row.get(0));
                        }
                    }
                }
                else if (ct.name.equals("CHOICE_GROUP"))
                {
                    for (int i=0;i<ct.data.size();i++)
                    {
                        List<String> row = ct.data.get(i);
                        if (row.get(0).toUpperCase().equals(removeGroup)) removeChoiceGroupRows.add(row.get(0));
                    }
                }
                else if (ct.name.equals("RELATIONSHIP_TYPE"))
                {
                    for (int i=0;i<ct.data.size();i++)
                    {
                        List<String> row = ct.data.get(i);
                        if (row.get(1).toUpperCase().equals(removeGroup) || row.get(2).toUpperCase().equals(removeGroup)) {
                            removeRelationshipTypeIds.add(row.get(0));
                        }
                    }
                }
                else if (ct.name.equals("NOTE"))
                {
                    for (int i=0;i<ct.data.size();i++)
                    {
                        List<String> row = ct.data.get(i);
                        String rowGroup = row.get(2).toUpperCase().trim();
                        //Print.out(rowGroup + " is the same as " + removeGroup + " " + rowGroup.equals(removeGroup));
                        if (rowGroup.equals(removeGroup)) removeNoteRows.add(row.get(0));
                    }
                }
            }
            //do check again now that we know RELATIONSHIP_TYPEs
            for (Table ct: oldt)
            {
                if (ct.name.equals("RELATIONSHIP"))
                {
                    for (String id :removeRelationshipTypeIds)
                    {
                        for (int i=0;i<ct.data.size();i++){
                            List<String> row = ct.data.get(i);
                            if (row.get(3).equals(id)) removeRelationshipRows.add(ct.data.get(i).get(0));
                        }
                    }
                }
            }
            //boot old rows out of tables
            for (Table ct: oldt)
            {
                if (ct.name.equals("CHOICE")) {
                        for (int x = ct.data.size()-1;x>=0;x--) {
                            List<String> row = ct.data.get(x);
                            if (removeChoiceRows.indexOf(row.get(0)) > -1) {
                                ct.data.remove(x);
                            }
                        }
                }
                else if (ct.name.equals("NOTE")) {
                    for (int x = ct.data.size() -1;x>=0;x--) {
                        List<String> row = ct.data.get(x);
                        if (removeNoteRows.indexOf(row.get(0)) > -1) {
                            //Print.out("Removing row [" + x + "] with group " + row.get(2) + " from NOTE: " + ct.data.get(x));
                            ct.data.remove(x);
                        }
                    }
                }
                else if (ct.name.equals("CHOICE_GROUP")) {
                    for (int x = ct.data.size() -1;x>=0;x--) {
                        List<String> row = ct.data.get(x);
                        if (removeChoiceGroupRows.indexOf(row.get(0)) > -1) {
                            //Print.out("Removing row [" + x + "] with group " + row.get(2) + " from NOTE: " + ct.data.get(x));
                            ct.data.remove(x);
                        }
                    }
                }
                else if (ct.name.equals("RELATIONSHIP")) {
                    for (int x = ct.data.size() -1;x>=0;x--) {
                        List<String> row = ct.data.get(x);
                        if (removeRelationshipRows.indexOf(row.get(0)) > -1) {
                            //Print.out("Removing row [" + x + "] with group " + row.get(2) + " from RELATIONSHIP: " + ct.data.get(x));
                            ct.data.remove(x);
                        }
                    }
                }
                else if (ct.name.equals("RELATIONSHIP_TYPE")) {
                    for (int x = ct.data.size() -1;x>=0;x--) {
                        List<String> row = ct.data.get(x);
                        if (removeRelationshipTypeIds.indexOf(row.get(0)) > -1) {
                            //Print.out("Removing row [" + x + "] with group " + row.get(2) + " from RELATIONSHIP_TYPE: " + ct.data.get(x));
                            ct.data.remove(x);
                        }
                    }
                }
            }
            Print.out(oldt.get(removeInt).name + " no longer exists and is being removed");
            oldt.remove(removeInt);
        }
    }

    private void checkColumns()
    {
        for (Table ct : oldt)
        {
            if (ct.name.contains("ILS_")) {
                Table nt = newMap.get(ct.name);
                List<Integer> removeColumns = new ArrayList();
                for (int c = 0; c < ct.columns.size(); c++) {
                    String oldcolumn = ct.columns.get(c);

                    int newindex = nt.columns.indexOf(oldcolumn);

                    if (newindex == -1)
                    {
                        removeColumns.add(c);
                    }
                }
                ct = dropColumns(ct,removeColumns);
            }
        }
        for (Table ct : newt.getTables())
        {
            if (ct.name.contains("ILS_")) {
                for (Table ot : oldt)
                {
                    if (ot.name.equals(ct.name))
                    {
                        for (int c=0;c < ct.columns.size();c++)
                        {
                            String curColumn = ct.columns.get(c);
                            if (ot.columns.indexOf(curColumn) == -1)
                            {
                                ot.columns.add(curColumn);
                                ot.columnPrecision.add(ct.columnPrecision.get(c));
                                ot.columnType.add(ct.columnType.get(c));
                                if (!ct.columnNullable.get(c)) {

                                    Boolean stillWorking = true;

                                    while (stillWorking) {
                                        System.out.println("New column " + curColumn + " " + ct.columnType.get(c) + " requires a value\n"
                                                + "Please enter one now and press RETURN.\n");
                                        Scanner scanner = new Scanner(System.in);
                                        String value = scanner.nextLine();
                                        if (value.length() > 0)
                                        {
                                            System.out.println("New value will be filled for " + curColumn + " : " + value + "\n" +
                                            "[Y/N]");

                                            String value2 = scanner.nextLine();

                                            if (value2.toUpperCase().equals("Y"))
                                            {
                                                for (List<String> row : ot.data)
                                                {
                                                    //Print.out(row);
                                                    row.add(value);
                                                }
                                                stillWorking=false;
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    Boolean stillWorking = true;

                                    while (stillWorking) {
                                        System.out.println("New column " + curColumn + " " + ct.columnType.get(c) + " does not require a value\n"
                                                + "Please enter one now if desired and press RETURN.\n");
                                        Scanner scanner = new Scanner(System.in);
                                        String value = scanner.nextLine();
                                        if (value.length() > 0)
                                        {
                                            System.out.println("New value will be filled for " + curColumn + " : " + value + "\n" +
                                                    "[Y/N] \n");

                                            String value2 = scanner.nextLine();

                                            if (value2.toUpperCase().equals("Y"))
                                            {
                                                for (List<String> row : ot.data)
                                                {
                                                    row.add(value);
                                                }
                                                stillWorking=false;
                                            }
                                        }
                                        else
                                        {
                                            System.out.println("New value will be filled for " + curColumn + " : null \n" +
                                                    "[Y/N] \n");

                                            String value2 = scanner.nextLine();

                                            if (value2.toUpperCase().equals("Y"))
                                            {
                                                for (List<String> row : ot.data)
                                                {
                                                    row.add("null");
                                                }
                                                stillWorking=false;
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private Table dropColumns(Table input, List<Integer> index)
    {

        for (int j = index.size()-1; j >= 0; j--) {
            Print.out("Dropping column " + input.columns.get(index.get(j)));

            int removeIndex = index.get(j);

            input.columns.remove(removeIndex);
            input.columnPrecision.remove(removeIndex);
            input.columnType.remove(removeIndex);

            for (List<String> row : input.data)
            {
                row.remove(removeIndex);
            }
        }
        return input;
    }

    public List<Table> getResult()
    {
        return oldt;
    }

    private void lookForRenamedRelationshipTypes()
    {
        Table newT = newMap.get("RELATIONSHIP_TYPE");
        Table oldT = oldMap.get("RELATIONSHIP_TYPE");
        for (List<String> row : newT.data)
        {
            String group1 = row.get(1);
            String group2 = row.get(2);
            String name = row.get(0);

            for (int i = 0;i<oldT.data.size();i++)
            {
                List<String> old_row = oldT.data.get(i);
                if (old_row.get(1).equals(group1) && old_row.get(2).equals(group2) && (!name.equals(old_row.get(0))))
                {
                    Print.out("Row name mismatch old row called " + old_row.get(0) + " new row named " + name);
                    renameRelationshipRows(old_row.get(0),name);
                }
            }

        }
    }
    private void renameRelationshipRows(String oldName, String newName)
    {
        for (Table table : oldt)
        {
            if (table.name.equals("RELATIONSHIP"))
            {
                for (List<String> row : table.data){
                    if (row.get(3).equals(oldName))
                    {
                        Print.out("Changing " + oldName + " to " + newName + " for: " + row);
                        row.set(3,newName);
                    }
                }
            }
        }
    }

}
