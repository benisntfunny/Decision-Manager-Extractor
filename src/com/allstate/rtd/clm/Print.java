package com.allstate.rtd.clm;

public class Print {
    static final boolean DEBUG = true;

    public static void out(Object print){
        if (DEBUG) System.out.println(print);
    }
    public static void out(Object[] print){
        if (DEBUG) System.out.println(print);
    }
    public static void out(){
        if (DEBUG) System.out.println();
    }
    public static void outf(String print)
    {
        if (DEBUG)
        {
            print = print.replaceAll("%","%%");
            System.out.printf(print);
        }
    }
}