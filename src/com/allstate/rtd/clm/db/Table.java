package com.allstate.rtd.clm.db;

import java.util.ArrayList;
import java.util.List;

public class Table {

    public String name;
    public List<String> columns = new ArrayList();
    public List<String> removedColumns = new ArrayList();
    public List<String> addedColumns = new ArrayList();
    public List<String> columnType = new ArrayList();
    public List<Integer> columnPrecision = new ArrayList();
    public List<List<String>> data = new ArrayList();
    public long columnCount = columns.size();
    public long rowCount = data.size();

    @Override
    public String toString() {
        return "Table{" +  "\n" +
                "name='" + name + '\'' + "\n" +
                ", columns=" + columns +  "\n" +
                ", columnType=" + columnType +  "\n" +
                ", columnPrecision=" + columnPrecision +  "\n" +
                ", data=" + data +
                '}';
    }
}
