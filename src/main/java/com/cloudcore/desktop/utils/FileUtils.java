package com.cloudcore.desktop.utils;

import com.cloudcore.desktop.core.CloudCoin;
import com.cloudcore.desktop.core.Stack;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class FileUtils {


    /* Fields */

    private static Random random = new Random();
    private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";


    /* Methods */

    /**
     * Method countCoins counts how many coins of a given extension are in a given directory
     *
     * @param folderPath A folder path.
     * @return An array of the different denominations of coins. The first index is the total amount of all coins added together.
     */
    public static int[] countCoins(String folderPath) {
        int[] returnCounts = new int[6]; // 0. Total, 1.1s, 2,5s, 3.25s 4.100s, 5.250s

        String[] fileNames = FileUtils.selectFileNamesInFolder(folderPath);
        for (int i = 0; i < fileNames.length; i++) {
            String[] nameParts = fileNames[i].split("\\.");
            String denomination = nameParts[0];
            switch (denomination) {
                case "1": returnCounts[0] += 1; returnCounts[1]++; break;
                case "5": returnCounts[0] += 5; returnCounts[2]++; break;
                case "25": returnCounts[0] += 25; returnCounts[3]++; break;
                case "100": returnCounts[0] += 100; returnCounts[4]++; break;
                case "250": returnCounts[0] += 250; returnCounts[5]++; break;
            }
        }
        return returnCounts;
    }

    public static int countCoins(ArrayList<CloudCoin> coins) {
        return countCoins(coins.toArray(new CloudCoin[0]));
    }
    public static int countCoins(CloudCoin[] coins) {
        int total = 0;
        for (CloudCoin coin : coins) {
            total += CoinUtils.getDenomination(coin);
        }
        return total;
    }

    /**
     * Appends a filename with an increasing index if a filename is in use. Loops until a free filename is found.
     * TODO: Potential endless loop if every filename is taken.
     *
     * @param filename
     * @return an unused filename
     */
    public static String ensureFilenameUnique(String filename, String extension, String folder) {
        if (!Files.exists(Paths.get(folder + filename + extension)))
            return filename + extension;

        filename = filename + '.';
        String newFilename;
        int loopCount = 0;
        do {
            newFilename = filename + Integer.toString(++loopCount);
        }
        while (Files.exists(Paths.get(folder + newFilename + extension)));
        return newFilename + extension;
    }

    /**
     * Loads an array of CloudCoins from a Stack file.
     *
     * @param folder   the folder containing the Stack file.
     * @param filename the absolute filepath of the Stack file.
     * @return ArrayList of CloudCoins.
     */
    public static ArrayList<CloudCoin> loadCloudCoinsFromStack(String folder, String filename) {
        try {
            String file = new String(Files.readAllBytes(Paths.get(folder + filename)));
            Stack stack = Utils.createGson().fromJson(file, Stack.class);
            for (CloudCoin coin : stack.cc) {
                coin.folder = folder;
                coin.currentFilename = filename;
            }
            return new ArrayList<>(Arrays.asList(stack.cc));
        } catch (IOException | JsonSyntaxException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Loads an array of CloudCoins from a Stack file.
     *
     * @param fullFilePath the absolute filepath of the Stack file.
     * @return ArrayList of CloudCoins.
     */
    public static ArrayList<CloudCoin> loadCloudCoinsFromStack(String fullFilePath) {
        try {
            String file = new String(Files.readAllBytes(Paths.get(fullFilePath)));
            Stack stack = Utils.createGson().fromJson(file, Stack.class);
            for (CloudCoin coin : stack.cc) {
                coin.setFullFilePath(fullFilePath);
            }
            return new ArrayList<>(Arrays.asList(stack.cc));
        } catch (IOException | JsonSyntaxException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Loads an array of CloudCoins from a Stack JSON String.
     *
     * @param json the JSON string of the Stack file.
     * @return ArrayList of CloudCoins.
     */
    public static ArrayList<CloudCoin> loadCloudCoinsFromStackJson(String json) {
        try {
            Stack stack = Utils.createGson().fromJson(json, Stack.class);
            return new ArrayList<>(Arrays.asList(stack.cc));
        } catch (JsonSyntaxException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static String randomString(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }

    /**
     * Returns an array containing all filenames in a directory.
     *
     * @param folderPath the directory to check for files
     * @return String[]
     */
    public static String[] selectFileNamesInFolder(String folderPath) {
        File folder = new File(folderPath);
        Collection<String> files = new ArrayList<>();
        if (folder.isDirectory()) {
            File[] filenames = folder.listFiles();

            if (null != filenames) {
                for (File file : filenames) {
                    if (file.isFile()) {
                        files.add(file.getName());
                    }
                }
            }
        }
        return files.toArray(new String[]{});
    }
}
