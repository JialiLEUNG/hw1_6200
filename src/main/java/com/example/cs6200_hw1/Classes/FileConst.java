package com.example.cs6200_hw1.Classes;

public class FileConst {
    public static int kb_length = 1024;
    public static int query_doc_id = 9999999;
//    public static int topK_relevant_doc = 20;

    private int topK_relevant_doc;

    public FileConst(int topK_relevant_doc) {
        this.topK_relevant_doc = topK_relevant_doc;
    }

}
