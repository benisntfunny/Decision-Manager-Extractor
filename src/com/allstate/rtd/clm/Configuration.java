package com.allstate.rtd.clm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    public final String DB_USER;
    public final String DB_PASS;
    public final String DB_URL;
    public final String OUT_FILE;
    public final String TABLE_OWNER;

    public Configuration()
    {
        Properties prop = new Properties();
        InputStream input;
        try {
            input = new FileInputStream("config.properties");
            prop.load(input);
        }
        catch (IOException ioEx) {
            //@TODO Make a nice error message about missing files
            ioEx.printStackTrace();

        }
        finally {
            //@TODO Check values before loading. For now we'll assume you did things right
            DB_USER = prop.getProperty("databaseUser");
            DB_PASS = prop.getProperty("databaseUserPassword");
            DB_URL = prop.getProperty("databaseUrl");
            OUT_FILE = prop.getProperty("outputfile");
            TABLE_OWNER = prop.getProperty("tableOwner");
        }
    }
    @Override
    public String toString() {
        return "config.properties\n" +
                "\nDB_USER=" + DB_USER + "\n" +
                "DB_PASS=" + DB_PASS +  "\n" +
                "DB_URL=" + DB_URL +   "\n" +
                "OUT_FILE=" + OUT_FILE + "\n";
    }
}



