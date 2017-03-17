package org.thoughtcrime.securesms.additions;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by on 13.03.2017.
 */

public final class FileHelper {

    // TODO Steffi: Entfernen dieser Properties
    public static String contactsFileName = "contacts";
    public static String parentsFileName = "parents";

    public static String vCardFileName = "vCard";
    public static String whiteListFileName = "whiteList";
    public static String pendingListFileName = "pendingList";
    public static String blackListFileName = "blackList";

    private FileHelper() {
    }

    public static void writeDataToFile(final Context context, final String data, final String filename) {
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void checkFileExistence(final Context context, String filename) {
        try {
            File file = context.getFileStreamPath(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static String readDataFromFile(final Context context, String filename) {
        checkFileExistence(context, filename);
        FileInputStream inputStream;
        String result = "";
        try {
            inputStream = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            String fileResult;

            while ((fileResult = br.readLine()) != null) {
                result = fileResult;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return result;
        }
        return result;
    }
}
