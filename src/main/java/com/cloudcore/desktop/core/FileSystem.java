package com.cloudcore.desktop.core;

import com.cloudcore.desktop.utils.CoinUtils;
import com.cloudcore.desktop.utils.FileUtils;
import com.cloudcore.desktop.utils.SimpleLogger;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

public class FileSystem {


    /* Fields */

    public static final String BasePath = "C:" + File.separator + "CloudCoinServer";
    public static final String RootPath = BasePath + File.separator + "accounts" + File.separator + "DefaultUser" + File.separator;

    public static String DetectedFolder = RootPath + Config.TAG_DETECTED + File.separator;

    public static String BankFolder = RootPath + Config.TAG_BANK + File.separator;
    public static String FrackedFolder = RootPath + Config.TAG_FRACKED + File.separator;
    public static String CounterfeitFolder = RootPath + Config.TAG_COUNTERFEIT + File.separator;
    public static String LostFolder = RootPath + Config.TAG_LOST + File.separator;

    public static String LogsFolder = RootPath + Config.TAG_LOGS + File.separator;
    public static String ReceiptsFolder = RootPath + Config.TAG_RECEIPTS + File.separator;
    public static String CommandFolder = BasePath + File.separator + Config.TAG_COMMAND;
    public static String MainLogsFolder = BasePath +File.separator + Config.TAG_LOGS ;
    public static String EchoerLogsFolder = BasePath +File.separator + Config.TAG_LOGS + File.separator + Config.TAG_ECHOER;

    /* Methods */

    public static boolean createDirectories() {
        try {
            Files.createDirectories(Paths.get(RootPath));
            Files.createDirectories(Paths.get(DetectedFolder));
            Files.createDirectories(Paths.get(BankFolder));
            Files.createDirectories(Paths.get(FrackedFolder));
            Files.createDirectories(Paths.get(CounterfeitFolder));
            Files.createDirectories(Paths.get(LostFolder));
            Files.createDirectories(Paths.get(LogsFolder));
            Files.createDirectories(Paths.get(ReceiptsFolder));
            Files.createDirectories(Paths.get(CommandFolder));
            Files.createDirectories(Paths.get(MainLogsFolder));
            Files.createDirectories(Paths.get(EchoerLogsFolder));


        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static int[] getTotalCoinsBank() {
        return getTotalCoinsBank(BankFolder);
    }
    public static int[] getTotalCoinsBank(String accountFolder) {
        int[] totals = new int[6];

        int[] bankTotals = FileUtils.countCoins(accountFolder);

        totals[5] = bankTotals[0];
        totals[0] = bankTotals[1];
        totals[1] = bankTotals[2];
        totals[2] = bankTotals[3];
        totals[3] = bankTotals[4];
        totals[4] = bankTotals[5];

        return totals;
    }

    /**
     * Loads all CloudCoins from a specific folder.
     *
     * @param folder the folder to search for CloudCoin files.
     * @return an ArrayList of all CloudCoins in the specified folder.
     */
    public static ArrayList<CloudCoin> loadFolderCoins(String folder) {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();

        String[] filenames = FileUtils.selectFileNamesInFolder(folder);
        for (String filename : filenames) {
            int index = filename.lastIndexOf('.');
            if (index == -1) continue;

            String extension = filename.substring(index + 1);

            switch (extension) {
                case "stack":
                    ArrayList<CloudCoin> coins = FileUtils.loadCloudCoinsFromStack(folder, filename);
                    folderCoins.addAll(coins);
                    break;
            }
        }

        return folderCoins;
    }

    public static void moveCoins(ArrayList<CloudCoin> coins, String sourceFolder, String targetFolder) {
        moveCoins(coins, sourceFolder, targetFolder, ".stack");
    }
    public static void moveCoins(ArrayList<CloudCoin> coins, String sourceFolder, String targetFolder, String extension) {
        for (CloudCoin coin : coins) {
            String fileName = FileUtils.ensureFilenameUnique(CoinUtils.generateFilename(coin), extension, targetFolder);

            try {
                Files.move(Paths.get(sourceFolder + coin.currentFilename), Paths.get(targetFolder + fileName),
                        StandardCopyOption.REPLACE_EXISTING);
                String fileSeparator = (File.separatorChar == '\\') ? "\\\\" : File.separator;
                String[] folders = targetFolder.split(fileSeparator);
                SimpleLogger.writeLog(coin.getSn() + "." + folders[folders.length - 1], "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

