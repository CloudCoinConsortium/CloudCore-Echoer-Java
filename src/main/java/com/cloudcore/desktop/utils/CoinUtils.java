package com.cloudcore.desktop.utils;

import com.cloudcore.desktop.core.CloudCoin;
import com.cloudcore.desktop.core.Config;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

public class CoinUtils {


    /* Methods */

    public static void calcExpirationDate(CloudCoin coin) {
        coin.setEd(calcExpirationDate());
    }
    public static String calcExpirationDate() {
        LocalDate expirationDate = LocalDate.now().plusYears(Config.EXPIRATION_YEARS);
        return (expirationDate.getMonthValue() + "-" + expirationDate.getYear());
    }

    /**
     * Returns a denomination describing the currency value of the CloudCoin.
     *
     * @param coin CloudCoin
     * @return 1, 5, 25, 100, 250, or 0 if the CloudCoin's serial number is invalid.
     */
    public static int getDenomination(CloudCoin coin) {
        int sn = coin.getSn();
        int nom;
        if (sn < 1)
            nom = 0;
        else if ((sn < 2097153))
            nom = 1;
        else if ((sn < 4194305))
            nom = 5;
        else if ((sn < 6291457))
            nom = 25;
        else if ((sn < 14680065))
            nom = 100;
        else if ((sn < 16777217))
            nom = 250;
        else
            nom = 0;

        return nom;
    }

    /**
     * Generates a name for the CloudCoin based on the denomination, Network Number, and Serial Number.
     * <br>
     * <br>Example: 25.1.6123456
     *
     * @return String a filename
     */
    public static String generateFilename(CloudCoin coin) {
        return getDenomination(coin) + ".CloudCoin." + coin.getNn() + "." + coin.getSn();
    }

    /**
     * Generates secure random GUIDs for pans. An example:
     * <ul>
     * <li>8d3eb063937164c789474f2a82c146d3</li>
     * </ul>
     * These Strings are hexadecimal and have a length of 32.
     */
    public static void generatePAN(CloudCoin coin) {
        coin.pan = new String[Config.nodeCount];
        for (int i = 0; i < Config.nodeCount; i++) {
            SecureRandom random = new SecureRandom();
            byte[] cryptoRandomBuffer = random.generateSeed(16);

            UUID uuid = UUID.nameUUIDFromBytes(cryptoRandomBuffer);
            coin.pan[i] = uuid.toString().replace("-", "");
        }
    }

    public static String getPastStatus(CloudCoin coin, int raida_id) {
        String returnString = "";
        char[] pownArray = coin.getPown().toCharArray();
        switch (pownArray[raida_id]) {
            case 'e':
                returnString = "error";
                break;
            case 'f':
                returnString = "fail";
                break;
            case 'p':
                returnString = "pass";
                break;
            case 'u':
                returnString = "undetected";
                break;
            case 'n':
                returnString = "noresponse";
                break;
        }
        return returnString;
    }

    public static boolean setPastStatus(CloudCoin coin, String status, int raida_id) {
        char[] pownArray = coin.getPown().toCharArray();
        switch (status) {
            case "error":
                pownArray[raida_id] = 'e';
                break;
            case "fail":
                pownArray[raida_id] = 'f';
                break;
            case "pass":
                pownArray[raida_id] = 'p';
                break;
            case "undetected":
                pownArray[raida_id] = 'u';
                break;
            case "noresponse":
                pownArray[raida_id] = 'n';
                break;
        }
        coin.setPown(new String(pownArray));
        return true;
    }

    public static int getPassCount(CloudCoin coin) {
        return Utils.charCount(coin.getPown(), 'p');
    }
    public static int getFailCount(CloudCoin coin) {
        return Utils.charCount(coin.getPown(), 'f');
    }
    public static String getDetectionResult(CloudCoin coin) {
        return (getPassCount(coin) >= Config.passCount) ? "Pass" : "Fail";
    }

    /**
     * Updates the Authenticity Numbers to the new Proposed Authenticity Numbers.
     */
    public static void setAnsToPans(CloudCoin coin) {
        for (int i = 0; (i < Config.nodeCount); i++) {
            coin.getAn().set(i, coin.pan[i]);
        }
    }

    /**
     * CloudCoin Constructor for importing a CloudCoin from a JPG file.
     *
     * @param header   JPG header string.
     * @param folder   the folder containing the Stack file.
     * @param filename the absolute filepath of the Stack file.
     * @return a CloudCoin object.
     */
    public static CloudCoin cloudCoinFromJpgHeader(String header, String folder, String filename) {
        CloudCoin cc = new CloudCoin(folder, filename);

        int startAn = 40;
        for (int i = 0; i < 25; i++) {
            cc.getAn().add(header.substring(startAn, startAn + 32));
            startAn += 32;
        }

        cc.setAoid(null); //header.substring(808, 840);
        cc.setPown(CoinUtils.pownHexToString(header.substring(840, 872)));
        //cc.hc = header.substring(890, 898);
        cc.setEd(CoinUtils.expirationDateHexToString(header.substring(900, 902)));
        cc.setNn(Integer.valueOf(header.substring(902, 904), 16));
        cc.setSn(Integer.valueOf(header.substring(904, 910), 16));

        return cc;
    }

    /**
     * CloudCoin Constructor for importing a CloudCoin from a CSV file.
     *
     * @param csv      CSV file as a String.
     * @param folder   the folder containing the Stack file.
     * @param filename the absolute filepath of the Stack file.
     * @return a CloudCoin object.
     */
    public static CloudCoin cloudCoinFromCsv(String csv, String folder, String filename) {
        CloudCoin coin = new CloudCoin(folder, filename);

        try {
            String[] values = csv.split(",");

            coin.setSn(Integer.parseInt(values[0]));
            // values[1] is denomination.
            coin.setNn(Integer.parseInt(values[2]));
            ArrayList<String> ans = new ArrayList<>();
            for (int i = 0; i < Config.nodeCount; i++)
                ans.add(values[i + 3]);
            coin.setAn(ans);

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

        return coin;
    }

    /**
     * Returns a String containing a hex representation of the last pown results. The results are encoded as such:
     * <br>
     * <br>0: Unknown
     * <br>1: Pass
     * <br>2: No Response
     * <br>E: Error
     * <br>F: Fail
     *
     * @param coin the CloudCoin containing the pown results.
     * @return a hex representation of the pown results.
     */
    public static String pownStringToHex(CloudCoin coin) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0, j = coin.getPown().length(); i < j; i++) {
            if ('u' == coin.getPown().charAt(i))
                stringBuilder.append('0');
            else if ('p' == coin.getPown().charAt(i))
                stringBuilder.append('1');
            else if ('n' == coin.getPown().charAt(i))
                stringBuilder.append('2');
            else if ('e' == coin.getPown().charAt(i))
                stringBuilder.append('E');
            else if ('f' == coin.getPown().charAt(i))
                stringBuilder.append('F');
        }

        // If length is odd, append another zero for a clean hex value.
        if (stringBuilder.length() % 2 == 1)
            stringBuilder.append('0');

        return stringBuilder.toString();
    }

    /**
     * Converts a hexadecimal pown value to String.
     *
     * @param hexString the hexadecimal pown String.
     * @return the pown String.
     */
    public static String pownHexToString(String hexString) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0, j = hexString.length(); i < j; i++) {
            if ('0' == hexString.charAt(i))
                stringBuilder.append('p');
            else if ('1' == hexString.charAt(i))
                stringBuilder.append('9');
            else if ('2' == hexString.charAt(i))
                stringBuilder.append('n');
            else if ('E' == hexString.charAt(i))
                stringBuilder.append('e');
            else if ('F' == hexString.charAt(i))
                stringBuilder.append('f');
        }

        return stringBuilder.toString();
    }

    /**
     * Returns a String containing a hex representation of a new expiration date, measured in months since August 2016.
     *
     * @return a hex representation of the expiration date.
     */
    public static String expirationDateStringToHex() {
        LocalDate zeroDate = LocalDate.of(2016, 8, 13);
        LocalDate expirationDate = LocalDate.now().plusYears(Config.EXPIRATION_YEARS);
        int monthsAfterZero = (int) (DAYS.between(zeroDate, expirationDate) / (365.25 / 12));
        return Integer.toHexString(monthsAfterZero);
    }

    /**
     * Converts a hexadecimal expiration date to String.
     *
     * @param edHex the hexadecimal expiration date.
     * @return the expiration date String.
     */
    public static String expirationDateHexToString(String edHex) {
        long monthsAfterZero = Long.valueOf(edHex, 16);
        LocalDate zeroDate = LocalDate.of(2016, 8, 13);
        LocalDate ed = zeroDate.plusMonths(monthsAfterZero);
        return ed.getMonthValue() + "-" + ed.getYear();
    }

    public static char[] consoleReport(CloudCoin cc) {
        // Used only for console apps
        //  System.out.println("Finished detecting coin index " + j);
        // PRINT OUT ALL COIN'S RAIDA STATUS AND SET AN TO NEW PAN
        char[] pownArray = cc.getPown().toCharArray();
        String report = "   Authenticity Report SN #" + String.format("{0,8}", cc.getSn()) + ", Denomination: " + String.format("{0,3}", getDenomination(cc)) + "  ";

        return pownArray;
    }
}
