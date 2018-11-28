package com.cloudcore.EchoServant;

import com.cloudcore.desktop.raida.RAIDA;
import com.cloudcore.desktop.raida.Response;
import com.cloudcore.desktop.raida.ServiceResponse;
import com.cloudcore.desktop.utils.SimpleLogger;
import com.cloudcore.desktop.utils.Utils;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
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

public class EchoServant {
    final static String dir = System.getProperty("user.dir") + File.separator + "Echo";
    public static void main(String[] args) {
        // TODO code application logic here
        FileWriter out = null;
        //EchoService object = new EchoService();
        //object.waitMethod();
        Path myDir = Paths.get(dir);
        try{
            out = new FileWriter(System.getProperty("user.dir") + File.separator + "echologs.txt",true);
        }
        catch(Exception e){

        }
        File directory = new File(dir);
        if (! directory.exists()){
            directory.mkdirs();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        System.out.println("current dir = " + dir);

        try{
            WatchService watchService
                    = FileSystems.getDefault().newWatchService();

            Path path = Paths.get(dir);
            path.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    // System.out.println(
                    // "Event kind:" + event.kind()
                    // + ". File affected: " + event.context() + ".");
                    if(event.kind().name().equalsIgnoreCase("ENTRY_CREATE")) {
                        System.out.println("Caught File Create. File Name : " + event.context());
                        EchoRaida(event.context().toString(),out);
                        out.write("Caught File Create. File Name : " + event.context());
                        out.flush();
                        File fDel = new File(dir+ File.separator+ event.context());
                        fDel.delete();
                        //out.close();
                    }
                }
                key.reset();
            }

        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }


    }


    public static String EchoRaida(String fileName,FileWriter out) {
        System.out.println("Starting Echo to RAIDA Network 1");
        System.out.println("----------------------------------");
        RAIDA raida = RAIDA.getInstance();
        ArrayList<CompletableFuture<Response>> tasks = raida.getEchoTasks();

        try{
            out.write("Starting Echo to RAIDA Network 1\n");
            out.write("----------------------------------\n");
            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).get();

            System.out.println("Ready Count - " + raida.getReadyCount());
            System.out.println("Not Ready Count - " + raida.getNotReadyCount());
            System.out.println(" ---------------------------------------------------------------------------------------------\n");
            System.out.println(" | Server   | Status | Message                               | Version | Time                |\n");

            out.write("Ready Count - " + raida.getReadyCount());
            out.write("Not Ready Count - " + raida.getNotReadyCount());
            out.write(" ---------------------------------------------------------------------------------------------\n");
            out.write(" | Server   | Status | Message                               | Version | Time                |\n");

            for (int i = 0; i < raida.nodes.length; i++) {
                String idWhitespace = ((i+1) > 9) ? " " : "  ";
                String time = Long.toString(raida.nodes[i].responseTime);
                time = (time.length() < 4) ? "0." + time : time.substring(0, time.length() - 3) + '.' + time.substring(time.length() - 3);

                System.out.println(" ---------------------------------------------------------------------------------------------\n");
                out.write(" ---------------------------------------------------------------------------------------------\n");
                out.write(" | RAIDA " + (i+1) + idWhitespace +
                        "| " + raida.nodes[i].RAIDANodeStatus +
                        " | " + "Execution Time (milliseconds) = " + time +
                        " |  " + ServiceResponse.version +
                        " | " + Utils.getSimpleDate() + " |\n");

                System.out.println(" | RAIDA " + (i+1) + idWhitespace +
                        "| " + raida.nodes[i].RAIDANodeStatus +
                        " | " + "Execution Time (milliseconds) = " + time +
                        " |  " + ServiceResponse.version +
                        " | " + Utils.getSimpleDate() + " |");
            }
            System.out.println(" ---------------------------------------------------------------------------------------------");
            out.write(" ---------------------------------------------------------------------------------------------\n");

            int readyCount = raida.getReadyCount();

            ServiceResponse response = new ServiceResponse();
            response.bankServer = "localhost";
            response.time = Utils.getDate();
            response.readyCount = Integer.toString(readyCount);
            response.notReadyCount = Integer.toString(raida.getNotReadyCount());
            if (readyCount > 20) {
                response.status = "ready";
                response.message = "The RAIDA is ready for counterfeit detection.";
            } else {
                response.status = "fail";
                response.message = "Not enough RAIDA servers can be contacted to import new coins.";
            }

            new SimpleLogger().LogGoodCall(Utils.createGson().toJson(response));
            return Utils.createGson().toJson(response);

        }
        catch(Exception e) {

        }

        try {
        } catch (Exception e) {
            System.out.println("RAIDA#PNC:" + e.getLocalizedMessage());
        }

        return "";
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
