package org.thoughtcrime.securesms.additions;

import android.content.Context;

import org.thoughtcrime.securesms.util.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by  on 17.03.2017.
 */

public class BlackList {
    private static final Integer EXPIRATION_TIME = 14;
    // Steffi: (Key)String ist mobileNumber, (Value)Date ist Ablaufdatum
    private HashMap<String, Date> blockedContacts;

    public BlackList(HashMap<String, Date> blockedContacts) {
        this.blockedContacts = blockedContacts;
    }

    public BlackList() {
        if (this.blockedContacts == null) {
            this.blockedContacts = new HashMap<>();
        }
    }

    public static void addNumberToFile(final Context context, String number, Date expirationDate) {
        WhiteList.removeNumberFromFile(context, number);
        try {
            BlackList blackList = getBlackListContent(context);
            blackList.blockedContacts.put(number, expirationDate);
            String jsonString = JsonUtils.toJson(blackList);
            FileHelper.writeDataToFile(context, jsonString, FileHelper.blackListFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Date getExpirationDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, EXPIRATION_TIME);
        return cal.getTime();
    }

    public static void removeNumberFromFile(final Context context, String number) {
        try {
            BlackList blackList = getBlackListContent(context);
            blackList.blockedContacts.remove(number);
            String jsonString = JsonUtils.toJson(blackList);
            FileHelper.writeDataToFile(context, jsonString, FileHelper.blackListFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkExpirationDates(final Context context) {
        String jsonString = FileHelper.readDataFromFile(context, FileHelper.blackListFileName);
        try {
            BlackList blackList = JsonUtils.fromJson(jsonString, BlackList.class);
            ArrayList<String> numbersToRemove = new ArrayList<>();
            Date now = new Date();

            // Steffi: Gehe blockierte Kontakte durch und überprüfe Ablaufdatum mit aktuellem Datum
            for (Map.Entry<String, Date> bContact : blackList.blockedContacts.entrySet()) {
                // Wenn kein Datum angegeben, gehe zum nächsten Eintrag
                if (bContact.getValue() == null) continue;

                // Wenn Ablaufdatum kleiner als aktuelles Datum ist, dann markiere die Nummer zum Entfernen
                if (bContact.getValue().before(now)) {
                    numbersToRemove.add(bContact.getKey());
                }
            }

            // Entferne die markierten Nummern aus der blackList
            for (String ntr : numbersToRemove) {
                blackList.blockedContacts.remove(ntr);
            }

            jsonString = JsonUtils.toJson(blackList);
            FileHelper.writeDataToFile(context, jsonString, FileHelper.blackListFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BlackList getBlackListContent(final Context context) {
        String jsonString = FileHelper.readDataFromFile(context, FileHelper.blackListFileName);
        BlackList blackList = new BlackList();
        try {
            blackList = JsonUtils.fromJson(jsonString, BlackList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return blackList;
    }

    public HashMap<String, Date> getBlockedContacts() {
        return blockedContacts;
    }

    public boolean isInBlackList(String mobileNumber) {
        return this.blockedContacts.containsKey(mobileNumber);
    }
}
