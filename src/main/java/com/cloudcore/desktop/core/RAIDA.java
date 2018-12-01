package com.cloudcore.desktop.core;

import com.cloudcore.desktop.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.time.*;

public class RAIDA {

    public int RAIDANumber;
    public String fullUrl;

    public long msServer; // milliseconds for server
    public String ms = ""; //total milliseconds

    public String lastJsonRaFromServer = null;
    public String lastHtml = "empty";//ticket, fail, error
    public String status = "unknown"; //Unknown, slow or ready
    public String msg = "";//message from server


    // Constructors

    /** Constructor for objects of class RAIDA. */
    public RAIDA(int RAIDANumber) {
        this.RAIDANumber = RAIDANumber;
        switchToRealHost();
    }


    // Methods

    /** Sets the RAIDA to the production server. */
    public void switchToRealHost() {
        this.fullUrl = "https://raida" + RAIDANumber + ".cloudcoin.global/service/";
    }

    /** Sets the RAIDA to the test server. */
    public void switchToFakeHost() {
        this.fullUrl = "https://raida" + RAIDANumber + "-net2.cloudcoin.global/service/";
    }

    /** Pings a RAIDA server, and records the result and ping times. */
    public String echo() {
        String html = "";
        String url = this.fullUrl + "echo";
        long start;

        try {
            html = Utils.getHtmlFromURL(url);//getHtml(url);
            this.lastJsonRaFromServer = html;

            start = System.nanoTime();

            JSONObject json = new JSONObject(html);
            this.msg = json.getString("message");
            this.msServer = (long) (1000f * Float.valueOf(msg.substring(msg.lastIndexOf('=') + 2)));
        } catch (JSONException | NumberFormatException e) {
            this.status = "error";
            start = System.nanoTime();
            System.out.println("Error: html response for " + fullUrl + " does not contain a valid message response: " + html);
            e.printStackTrace();
        }

        boolean isReady = html.contains("ready");
        this.status = (isReady) ? "ready" : "error";

        long end = System.nanoTime();
        this.ms = "" + new DecimalFormat("####.###").format((end - start) * 0.000001f);

        return this.status;
    }
}
