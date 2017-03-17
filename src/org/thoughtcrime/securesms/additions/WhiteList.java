package org.thoughtcrime.securesms.additions;

import android.content.Context;

import org.thoughtcrime.securesms.util.JsonUtils;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by  on 17.03.2017.
 */

public class WhiteList {
    // Steffi: (Key)String ist mobileNumber, (Value)String ist displayName -> displayName später für Signal displayName notwendig
    private HashMap<String, String> contactList;

    public WhiteList(HashMap<String, String> contactList) {
        this.contactList = contactList;
    }

    public WhiteList() {
        if (this.contactList == null) {
            this.contactList = new HashMap<>();
        }
    }

    public static void addNumberToFile(final Context context, String number, String displayName) {
        BlackList.removeNumberFromFile(context, number);
        try {
            WhiteList whiteList = getWhiteListContent(context);
            whiteList.contactList.put(number, displayName);
            String jsonString = JsonUtils.toJson(whiteList);
            FileHelper.writeDataToFile(context, jsonString, FileHelper.whiteListFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeNumberFromFile(final Context context, String number) {
        try {
            WhiteList whiteList = getWhiteListContent(context);
            whiteList.contactList.remove(number);
            String jsonString = JsonUtils.toJson(whiteList);
            FileHelper.writeDataToFile(context, jsonString, FileHelper.whiteListFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static WhiteList getWhiteListContent(final Context context) {
        WhiteList whiteList = new WhiteList();
        String jsonString = FileHelper.readDataFromFile(context, FileHelper.whiteListFileName);
        try {
            whiteList = JsonUtils.fromJson(jsonString, WhiteList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return whiteList;
    }

    public boolean isInWhiteList(String mobileNumber) {
        return this.contactList.containsKey(mobileNumber);
    }

    public HashMap<String, String> getContactList() {
        return contactList;
    }
}