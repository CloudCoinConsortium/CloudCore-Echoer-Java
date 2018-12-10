package com.cloudcore.EchoServant;

import com.cloudcore.Echoer;
import com.cloudcore.desktop.FolderWatcher;
import com.cloudcore.desktop.core.Config;
import com.cloudcore.desktop.core.FileSystem;
import com.cloudcore.desktop.raida.Node;
import com.cloudcore.desktop.raida.RAIDA;
import com.cloudcore.desktop.raida.Response;
import com.cloudcore.desktop.raida.ServiceResponse;
import com.cloudcore.desktop.utils.SimpleLogger;
import com.cloudcore.desktop.utils.Utils;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.nio.file.Path;
import java.nio.file.Paths;
//import java.nio.file.StandardWatchEventKind;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;
public class EchoServant {
    final static String dir = System.getProperty("user.dir") + File.separator + "Echo";

    final static String BasePath = Utils.GetWorkDirPath();
    final static String CommandPath = BasePath + "Command";
    final static String LogPath = BasePath + "Logs";
    static Echoer echoer = new Echoer();
    public static void main(String[] args) {
        // TODO code application logic here
        FileWriter out = null;
        File directory = new File(dir);
        File dirCommand = new File(CommandPath);
        File dirLog = new File(LogPath);


        // Create Command and Log folders if they dont exist.

        if (! dirCommand.exists()){
            dirCommand.mkdirs();
        }

        if (! dirLog.exists()){
            dirLog.mkdirs();
        }
        FileSystem.createDirectories();
        SimpleLogger.writeLog("ServantEchoerStarted", "");
        System.out.println("Echoer Started");

        echoer.Echo(FileSystem.BasePath);
    }


    private synchronized void waitMethod() {

        while (true) {
            System.out.println("always running program ==> " + Calendar.getInstance().getTime());
            try {
                this.wait(2000);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
    }

}
