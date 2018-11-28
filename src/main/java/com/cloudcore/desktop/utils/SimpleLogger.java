package com.cloudcore.desktop.utils;

import com.cloudcore.desktop.core.FileSystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.cloudcore.desktop.utils.SimpleLogger.LogLevel.*;

public class SimpleLogger {


    /* Fields */

    private static DateTimeFormatter logTimestampFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private StringBuilder logsRecord;

    private static String fullFilePath;


    /* Constructor */

    /**
     * Initialize a new instance of SimpleLogger class.
     */
    public SimpleLogger() {
        initialize(FileSystem.LogsFolder);
    }


    /* Methods */

    /**
     * Initializes a new master log and its filepath.
     *
     * @param fullFilePath the absolute filepath of the log.
     */
    private void initialize(String fullFilePath) {
        this.fullFilePath = fullFilePath;
        logsRecord = new StringBuilder();
    }

    public void Log(String text) {
        writeFormattedLog(LogLevel.INFO, text);
    }

    public void LogBadLogin(String text) {
        writeFormattedLog(LogLevel.BADLOGIN, text);
    }

    public void LogBadCall(String text) {
        writeFormattedLog(LogLevel.BADCALL, text);
    }

    public void LogGoodCall(String text) {
        writeFormattedLog(LogLevel.GOODCALL, text);
    }

    public void LogFatal(String text) {
        writeFormattedLog(LogLevel.FATAL, text);
    }

    /**
     * Log a message to the master log.
     *
     * @param text the text to append to the log.
     */
    public void appendLog(String text) {
        logsRecord.append(text).append(System.lineSeparator());
    }

    /**
     * Log an error message to the master log.
     *
     * @param text               the text to append to the log.
     * @param stackTraceElements the stack trace of an exception.
     */
    public void appendLog(String text, StackTraceElement[] stackTraceElements) {
        logsRecord.append(text).append(System.lineSeparator());
        for (StackTraceElement stack : stackTraceElements)
            logsRecord.append("    at ").append(stack.toString()).append(System.lineSeparator());
    }

    /**
     * Log an info message.
     *
     * @param text the text to append to the log.
     */
    public static void Info(String text) {
        writeFormattedLog(INFO, text);
    }

    /**
     * Writes all log messages to a file.
     */
    public void writeLogToFile() {
        writeFormattedLog(INFO, logsRecord.toString());
    }

    /// <summary>
    /// Format a log message based on log level
    /// </summary>
    /// <param name="level">Log level</param>
    /// <param name="text">Log message</param>
    private static void writeFormattedLog(LogLevel level, String text) {
        String pretext;
        switch (level) {
            case TRACE:
                pretext = LocalDateTime.now().format(logTimestampFormat) + " [TRACE]   ";
                break;
            case INFO:
                pretext = LocalDateTime.now().format(logTimestampFormat) + " [INFO]    ";
                break;
            case DEBUG:
                pretext = LocalDateTime.now().format(logTimestampFormat) + " [DEBUG]   ";
                break;
            case WARNING:
                pretext = LocalDateTime.now().format(logTimestampFormat) + " [WARNING] ";
                break;
            case ERROR:
                pretext = LocalDateTime.now().format(logTimestampFormat) + " [ERROR]   ";
                break;
            case FATAL:
                pretext = LocalDateTime.now().format(logTimestampFormat) + " [FATAL]   ";
                break;
            default:
                pretext = "";
                break;
        }

        writeLine(pretext + text);
    }

    /**
     * Write a formatted log message into a log file.
     *
     * @param text formatted log message.
     */
    private static void writeLine(String text) {
        writeLine(text, false);
    }

    /**
     * Write a formatted log message into a log file.
     *
     * @param text   formatted log message.
     * @param append true to append to an existing file, false to overwrite the file.
     */
    private static void writeLine(String text, boolean append) {
        try {
            String filepath = fullFilePath + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")).toLowerCase();
            String finalFilepath = filepath;
            int counter = 1;
            while (Files.exists(Paths.get(finalFilepath + ".log"))) {
                finalFilepath = filepath + '.' + counter++;
            }
            finalFilepath += ".log";

            StandardOpenOption option = (append) ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;

            Path path = Paths.get(finalFilepath);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            Files.write(path, text.getBytes(StandardCharsets.UTF_8), option);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Supported log levels.
     */
    enum LogLevel {
        BADLOGIN,
        BADCALL,
        GOODCALL,
        TRACE,
        INFO,
        DEBUG,
        WARNING,
        ERROR,
        FATAL
    }
}
