package com.example.cs6200_hw1.DataPreProcess;

import java.io.File;
import java.io.IOException;

public class FilePathGenerator {

    private String fileName;

    public FilePathGenerator(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() throws IOException {
        String configFilePath = new File(System.getProperty("user.dir")).getParent();
        File dataFolder = createDirectory(configFilePath + File.separator + "tempDataFiles");
        String path = dataFolder.getPath() + File.separator + fileName;
        return path;
    }

    private static File createDirectory(String directoryPath) throws IOException {
        File dir = new File(directoryPath);

        if (dir.exists()) {
//            System.out.println("File Directory already exists");
            return dir;
        }
        if (dir.mkdirs()) {
//            System.out.println("File Directory created to be used for storing files");
            return dir;
        }
        throw new IOException("Failed to create directory '" + dir.getAbsolutePath() + "' for an unknown reason.");
    }
}
