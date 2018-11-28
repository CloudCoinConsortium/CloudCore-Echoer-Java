package com.cloudcore.desktop.utils;

import com.cloudcore.desktop.core.FileSystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimpleLogger {


    /* Fields */

    private static String fullFilePath = FileSystem.LogsFolder;


    public static void writeLog(String filenameDetails, String logFileDetails) {
        String filepath = fullFilePath + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")).toLowerCase() + '-' + filenameDetails;
        String finalFilepath = filepath + ".log";
        int counter = 0;

        for (int i = 0; i < 10; i++) {
            try {
                Path path = Paths.get(finalFilepath);
                if (!Files.exists(path)) {
                    Files.createDirectories(path.getParent());
                    Files.createFile(path);
                }
                Files.write(path, logFileDetails.getBytes(StandardCharsets.UTF_8));
                break;
            } catch (IOException e) {
                finalFilepath = filepath + '.' + counter + ".log";
            }
            counter++;
        }
    }
}
