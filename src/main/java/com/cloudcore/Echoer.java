package com.cloudcore;

import com.cloudcore.desktop.core.Config;
import com.cloudcore.desktop.core.FileSystem;
import com.cloudcore.desktop.raida.Node;
import com.cloudcore.desktop.raida.RAIDA;
import com.cloudcore.desktop.raida.Response;
import com.cloudcore.desktop.raida.ServiceResponse;
import com.cloudcore.desktop.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class Echoer {

    public void Echo(String BasePath) {
        String CommandFolder = BasePath + File.separator + Config.TAG_COMMAND;
        String EchoerLogsFolder = BasePath +File.separator + Config.TAG_LOGS + File.separator + Config.TAG_ECHOER;

        try{
            WatchService watchService
                    = FileSystems.getDefault().newWatchService();

            Path path = Paths.get(CommandFolder);
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
                        String NewFileName = event.context().toString();
                        if(NewFileName.contains("echo.txt")) {
                            System.out.println("Echo Command Recieved");
                            EchoRaida(EchoerLogsFolder);

                            System.out.println(FileSystem.CommandFolder+ File.separator+ event.context().toString());

                            File fDel = new File(FileSystem.CommandFolder+ File.separator+ event.context().toString());
                            fDel.delete();
                            System.out.println("Deleted");
                        }


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

    public static String EchoRaida(String EchoerLogsFolder) {
        System.out.println("Starting Echo to RAIDA Network 1");
        System.out.println("----------------------------------");
        RAIDA raida = RAIDA.getInstance();

        ArrayList<CompletableFuture<Response>> tasks = raida.getEchoTasks();


        try{
            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).get();

            System.out.println("Ready Count - " + raida.getReadyCount());
            System.out.println("Not Ready Count - " + raida.getNotReadyCount());
            System.out.println(" ---------------------------------------------------------------------------------------------\n");
            System.out.println(" | Server   | Status | Message                               | Version | Time                |\n");

            Arrays.stream(new File(EchoerLogsFolder).listFiles()).forEach(File::delete);

            for (int i = 0; i < raida.nodes.length; i++) {

                System.out.println(EchoerLogsFolder + File.separator + GetLogFileName(i));
                PrintWriter writer = new PrintWriter(EchoerLogsFolder + File.separator + GetLogFileName(i), "UTF-8");
                writer.println("{\n" +
                        "    \"url\":\""+ raida.nodes[i].fullUrl +"\"\n" +
                        "}");
                //writer.println(RAIDA.getInstance().nodes[i].fullUrl);
                writer.close();
            }
            System.out.println(" ---------------------------------------------------------------------------------------------");

            int readyCount = raida.getReadyCount();

            JSONArray nodeArray = new JSONArray();
            for(int i=0;i<raida.nodes.length;i++) {
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("timestamp",raida.nodes[i].echoResponse.timestamp);
                jsonObject.put("request",raida.nodes[i].fullUrl);
                jsonObject.put("response",raida.nodes[i].echoResponse.fullResponse);
                nodeArray.put(jsonObject);

            }

            String pattern = "yyyy.MM.dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            String date = simpleDateFormat.format(new Date());

            PrintWriter writerLog = new PrintWriter(EchoerLogsFolder + File.separator + date + ".txt");
            writerLog.println(nodeArray);
            writerLog.close();


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

    private static String GetLogFileName(int num) {
        Node node = RAIDA.getInstance().nodes[num];
        return String.valueOf(num) + "_"+ node.RAIDANodeStatus.toString().toLowerCase() + "_"+
                node.responseTime +"_" + node.internalExecutionTime +".txt";
    }

}
