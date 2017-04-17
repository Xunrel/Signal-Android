package org.thoughtcrime.securesms;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.thoughtcrime.securesms.additions.FileHelper;
import org.thoughtcrime.securesms.additions.VCard;

import java.io.File;
import java.security.Timestamp;
import java.sql.Time;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class ContactExchange extends AppCompatActivity {

    private static final int ACTIVITY_RESULT_QR_DRDROID_ENCODE = 5;
    private static final int ACTIVITY_RESULT_QR_DRDROID_SCAN = 3;
    private int size = 0;

    // TODO Steffi: Code aufräumen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CE","Creating Contact Exchange");
        // Steffi: verhindert, dass ein Screenshot gemacht wird
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_contact_exchange);

        // TODO Steffi: Permissions schon bei der Installation bzw. Registrierung einholen zB checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        final Button button = (Button) findViewById(R.id.button_scan);
        VCard vCard  = VCard.getVCard(getApplicationContext());
        String localNumber = vCard.getMobileNumber().trim();
        GregorianCalendar d = new GregorianCalendar();
        String uniqueId  = UUID.randomUUID().toString();
        String qrCode = String.format("%1$s|%2$s", localNumber, d.getTime().toString());

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        size = height > width ? width : height;
        size = Math.round(0.9F * size);

        // Steffi: QR Droid als Ziel des Intends festlegen
        Intent qrDroid = new Intent("la.droid.qr.encode");
        // Steffi: Text für den QR-Code festlegen
        qrDroid.putExtra("la.droid.qr.code", qrCode);
        qrDroid.putExtra("la.droid.qr.image", true);
        // Steffi: Größe des QR-Codes festlegen
        qrDroid.putExtra("la.droid.qr.size", size );
        // Steffi: Intend abschicken und Ergebnis abwarten
        try {
            startActivityForResult(qrDroid, ACTIVITY_RESULT_QR_DRDROID_ENCODE);
        } catch (ActivityNotFoundException activity) {
        }

        //Set action to button
        button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create a new Intent to send to QR Droid
                Intent qrDroid = new Intent("la.droid.qr.scan"); //Set action "la.droid.qr.scan"

                //Check whether a complete or displayable result is needed
                //Notify we want complete results (default is FALSE)
                qrDroid.putExtra("la.droid.qr.complete", true);

                //Send intent and wait result
                try {
                    startActivityForResult(qrDroid, ACTIVITY_RESULT_QR_DRDROID_SCAN);
                } catch (ActivityNotFoundException activity) {
                    // Services.qrDroidRequired(Scan.this);
                }
            }
        });




    }

    @Override
    /**
     * Verarbeitet die von QR Droid zurückgeschickten Daten
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Bei einem Ergebnis wird unterschieden, für welches Intend das Ergebnis geliefert wird
        if (ACTIVITY_RESULT_QR_DRDROID_SCAN == requestCode && null != data && data.getExtras() != null) {
            // Ergebnis wird ausgelesen
            String result = data.getExtras().getString("la.droid.qr.result");

            //TODO Steffi: Infos aus QR-Code verarbeiten
            Log.d("ContactExchange, Text: ", result);
            //Just set result to EditText to be able to view it
            //EditText resultTxt = (EditText) findViewById(R.id.result);
            //resultTxt.setText(result);
            //resultTxt.setVisibility(View.VISIBLE);
        }

        if (ACTIVITY_RESULT_QR_DRDROID_ENCODE == requestCode && null != data && data.getExtras() != null) {
            //Read result from QR Droid (it's stored in la.droid.qr.result)
            //Result is a string or a bitmap, according what was requested
            ImageView imgResult = (ImageView) findViewById(R.id.img_result);
            String qrCode = data.getExtras().getString("la.droid.qr.result");

            //   String qrLocation = Uri.parse(qrCode);
//Log.d("Main", qrCode);
            //           Log.d("Main", getFilesDir().getAbsolutePath());
            File imgFile = new File(qrCode);
            // Log.d("Main", imgFile.getAbsolutePath());

            Bitmap bMap1 = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            //Bitmap bMap = BitmapFactory.decodeFile(imgFile);
            //  // Uri blah = Uri.parse(qrCode);

            // imgResult.setImageURI( Uri.parse(qrCode) );
            // imgResult.setMinimumHeight(size);
            imgResult.setImageBitmap(bMap1);
            imgResult.setVisibility(View.VISIBLE);

            //TODO Steffi: Entscheiden, was mit dem QR-Code geschieht - permanent speichern oder löschen
        }
    }
}
