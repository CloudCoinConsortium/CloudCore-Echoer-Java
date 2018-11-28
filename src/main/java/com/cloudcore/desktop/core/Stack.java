package com.cloudcore.desktop.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Stack {


    /* Fields */

    @Expose
    @SerializedName("cloudcoin")
    public CloudCoin[] cc;


    /* Methods */

    public Stack(CloudCoin coin) {
        cc = new CloudCoin[1];
        cc[0] = coin;
    }

    public Stack(CloudCoin[] coins) {
        cc = coins;
    }

    public Stack(ArrayList<CloudCoin> coins) {
        cc = coins.toArray(new CloudCoin[0]);
    }
}
