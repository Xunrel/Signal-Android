package org.thoughtcrime.securesms.additions;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * Created by on 13.03.2017.
 */

public class FileHelper {

    public String contactsFileName = "contacts";
    public String parentsFileName = "parents";
    public String vCardFileName = "vCard";

    public void writeNumberToFile(final Context context, final String data, final String filename) {
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String readDataFromFile(final Context context, String filename) {
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
