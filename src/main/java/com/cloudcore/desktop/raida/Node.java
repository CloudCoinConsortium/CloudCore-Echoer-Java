package com.cloudcore.desktop.raida;

import com.cloudcore.desktop.utils.Utils;
import com.google.gson.Gson;
import org.asynchttpclient.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static org.asynchttpclient.Dsl.asyncHttpClient;

/*
 * This Class Contains the properties of a RAIDA node.
 */
public class Node {

    public String ms;
    public String internalExecutionTime = "";
    public enum NodeStatus {
        Ready,
        NotReady,
    }


    /* Fields */

    public enum TicketHistory {Untried, Failed, Success}

    private AsyncHttpClient client;
    private Gson gson;

    public int nodeNumber;
    public String fullUrl;
    public MultiDetectResponse multiResponse = new MultiDetectResponse();

    public boolean failsDetect;
    public boolean failsFix;
    public boolean failsEcho;

    public TicketHistory ticketHistory = TicketHistory.Untried;
    public String ticket = "";
    public boolean hasTicket;
    public int NetworkNumber = 1;
    public Response echoResponse;
    public NodeStatus RAIDANodeStatus = NodeStatus.NotReady;

    public long responseTime;

    //Constructor
    public Node(int NodeNumber) {
        this.nodeNumber = NodeNumber;
        fullUrl = GetFullURL();
        //System.out.println(fullUrl);

        client = asyncHttpClient();
        gson = Utils.createGson();
        this.NetworkNumber = 1;
    }

    public Node(int NodeNumber, RAIDANode node) {
        this.nodeNumber = NodeNumber;
        fullUrl = "https://" + node.urls[0].url + "/service/";
        fullUrl = getFullURL();
        client = asyncHttpClient();
        gson = Utils.createGson();
        this.NetworkNumber = 1;
    }


    /* Methods */

    public String getFullURL() {
        return "https://raida" + (nodeNumber - 1) + ".cloudcoin.global/service/";
    }

    public void switchToRealHost() {
        this.fullUrl = "https://raida" + nodeNumber + ".cloudcoin.global/service/";
        this.NetworkNumber = 1;
    }

    /** Sets the RAIDA to the test server. */
    public void switchToFakeHost() {
        this.fullUrl = "https://raida" + nodeNumber + "-net2.cloudcoin.global/service/";
        this.NetworkNumber =2;
    }

    public void resetTicket() {
        hasTicket = false;
        ticketHistory = TicketHistory.Untried;
        ticket = "";
    }

    public void newCoin() {
        hasTicket = false;
        ticketHistory = TicketHistory.Untried;
        ticket = "";
        failsDetect = false;
    }

    public class MultiDetectResponse {
        public Response[] responses;
    }

    public boolean isFailed() {
        return failsEcho || failsDetect;
    }

    public String GetFullURL() {
        return "https://raida" + (nodeNumber - 1) + ".cloudcoin.global/service/";
    }



    public CompletableFuture<Response> echo() {
        return CompletableFuture.supplyAsync(() -> {
            Response echoResponse = new Response();
            echoResponse.fullRequest = this.fullUrl + "echo";
            long before = System.currentTimeMillis();
            failsEcho = true;
            long start=0;

            try {
                //System.out.println("Full Request-" + echoResponse.fullRequest);
                echoResponse.fullResponse = Utils.getHtmlFromURL(echoResponse.fullRequest);

                Gson gson = new Gson();
                Properties data = gson.fromJson(echoResponse.fullResponse, Properties.class);
                System.out.println(echoResponse.fullResponse + data.getProperty("message"));
                String message = data.getProperty("message");
                echoResponse.timestamp = data.getProperty("time");

                this.internalExecutionTime = data.getProperty("message").substring(message.lastIndexOf("=")+1).trim();
                //System.out.println(echoResponse.internalExecutionTime);
                start = System.nanoTime();

                if (echoResponse.fullResponse.contains("ready")) {
                    echoResponse.success = true;
                    echoResponse.outcome = "ready";
                    this.RAIDANodeStatus = NodeStatus.Ready;
                    failsEcho = false;
                } else {
                    this.RAIDANodeStatus = NodeStatus.NotReady;
                    echoResponse.success = false;
                    echoResponse.outcome = "error";
                    failsEcho = true;
                }
            } catch (Exception ex) {
                echoResponse.outcome = "error";
                echoResponse.success = false;
                this.RAIDANodeStatus = NodeStatus.NotReady;
                failsEcho = true;
                if (ex.getMessage() != null)
                    echoResponse.fullResponse = ex.getMessage();
                //System.out.println("Error---" + ex.getMessage());
            }
            long after = System.currentTimeMillis();
            responseTime = after - before;
            echoResponse.milliseconds = (int) responseTime;
            //System.out.println("Echo Complete-Node No.-" + NodeNumber + ".Status-" + RAIDANodeStatus);
            long end = System.nanoTime();
            this.ms = "" + new DecimalFormat("####.###").format((end - start) * 0.000001f);
            this.echoResponse = echoResponse;
            return echoResponse;
        });
    }

    //int[] nn, int[] sn, String[] an, String[] pan, int[] d, int timeout
  

    /**
     * Method FIX
     * Repairs a fracked RAIDA
     *
     * @param triad three ints trusted server RAIDA numbers
     * @param m1 String ticket from the first trusted server
     * @param m2 String ticket from the second trusted server
     * @param m3 String ticket from the third trusted server
     * @param pan String proposed authenticity number (to replace the wrong AN the RAIDA has)
     * @return String status sent back from the server: sucess, fail or error.
     */
    public Response fix(int[] triad, String m1, String m2, String m3, String pan) {
        Response fixResponse = new Response();
        long before = System.currentTimeMillis();
        fixResponse.fullRequest = fullUrl + "fix?fromserver1=" + triad[0] + "&message1=" + m1 + "&fromserver2=" + triad[1] + "&message2=" + m2 + "&fromserver3=" + triad[2] + "&message3=" + m3 + "&pan=" + pan;

        try {
            fixResponse.fullResponse = Utils.getHtmlFromURL(fixResponse.fullRequest);
            long after = System.currentTimeMillis();
            long ts = after - before;
            fixResponse.milliseconds = (int) ts;

            if (fixResponse.fullResponse.contains("success")) {
                fixResponse.outcome = "success";
                fixResponse.success = true;
            } else {
                fixResponse.outcome = "fail";
                fixResponse.success = false;
            }
        } catch (Exception ex) {//quit
            fixResponse.outcome = "error";
            fixResponse.fullResponse = ex.getMessage();
            fixResponse.success = false;
        }
        return fixResponse;
    }

}

