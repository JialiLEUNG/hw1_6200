package com.example.cs6200_hw1.DataPreProcess;

import java.io.File;
import java.io.IOException;

public class DeleteFile {
    private String[] fileName;

    public DeleteFile(String[] fileName) {
        this.fileName = fileName;
    }

    public void delete() throws IOException {
        for (String f : fileName){
            FilePathGenerator fpg = new FilePathGenerator(f);
            String path = fpg.getPath();

            File myObj = new File(path);
            if (myObj.delete()) {
//                System.out.println("Deleted the file: " + myObj.getName());
            } else {
                System.out.println("Failed to delete the file.");
            }

        }

    }
}
