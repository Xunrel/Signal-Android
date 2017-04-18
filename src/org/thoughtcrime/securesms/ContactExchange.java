package org.thoughtcrime.securesms;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import org.thoughtcrime.securesms.additions.VCard;
import org.thoughtcrime.securesms.crypto.IdentityKeyUtil;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientFactory;
import org.thoughtcrime.securesms.recipients.Recipients;
import org.thoughtcrime.securesms.service.KeyCachingService;
import org.thoughtcrime.securesms.util.IdentityUtil;
import org.thoughtcrime.securesms.util.JsonUtils;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.concurrent.ListenableFuture;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.fingerprint.Fingerprint;
import org.whispersystems.libsignal.fingerprint.NumericFingerprintGenerator;
import org.whispersystems.libsignal.util.guava.Optional;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ContactExchange extends AppCompatActivity {

    //Steffi: intent extra data, um fingerprint erhalten zu können
    public static final String FINGERPRINT = "qr_fingerprint";
    private static final String TAG = ContactExchange.class.getSimpleName();

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

        // Holen des möglichen Fingerprints
        String fingerprint = getIntent().getStringExtra(FINGERPRINT);

        setContentView(R.layout.activity_contact_exchange);

        // Steffi: Permissions schon bei der Installation bzw. Registrierung einholen zB checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        final Button button = (Button) findViewById(R.id.button_scan);
        VCard vCard  = VCard.getVCard(getApplicationContext());
        String localNumber = vCard.getMobileNumber().trim();
        GregorianCalendar d = new GregorianCalendar();
        String uniqueId  = UUID.randomUUID().toString();
        String qrCode = String.format("%1$s|%2$s", localNumber, d.getTime().toString());

        // Prüfen, ob Fingerprint vorhanden, wenn ja, dann in QR Code einarbeiten
        if (fingerprint != null && !fingerprint.isEmpty()) {
            qrCode += String.format("|%s", fingerprint);
        }

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
            String[] stringResults = result.split("\\|");
            if (stringResults != null && stringResults.length > 0) {
                // Nummer aus dem ersten Item des Arrays nutzen, um vCard zu versenden
                if (stringResults[0] != null) {
                    String mobileNumber = stringResults[0];
                    Context context = this; // getApplicationContext();

                    // Wenn 3 Werte übermittelt wurden, dann muss FIngerprint vorhanden sein als letzter Eintrag
                    if (stringResults.length == 3 && !stringResults[2].isEmpty()) {
                        String qrFingerprint = stringResults[2];

                        checkFingerprint(mobileNumber, qrFingerprint);
                    } else {
                        sendCheckMessage(mobileNumber);
                    }
                }
            }
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

    private void sendCheckMessage(String mobileNumber) {
        Recipients recipients = RecipientFactory.getRecipientsFromString(this, mobileNumber, true);

        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra(ConversationActivity.RECIPIENTS_EXTRA, recipients.getIds());
        intent.putExtra(ConversationActivity.TEXT_EXTRA, String.format("!@check_%s", "check"));
        intent.putExtra(ConversationActivity.IS_CHECK_EXTRA, true);
        intent.setDataAndType(getIntent().getData(), getIntent().getType());

        long existingThread = DatabaseFactory.getThreadDatabase(this).getThreadIdIfExistsFor(recipients);

        intent.putExtra(ConversationActivity.THREAD_ID_EXTRA, existingThread);
        intent.putExtra(ConversationActivity.DISTRIBUTION_TYPE_EXTRA, ThreadDatabase.DistributionTypes.DEFAULT);
        startActivity(intent);
        finish();
    }

    private void checkFingerprint(String remoteNumber, final String qrFingerprint) {
        final Context context = getApplicationContext();
        // Steffi: remotenumber = Nummer des Empfängers ohne Leerzeichen!
        final String remNumber = remoteNumber.replace(" ", "");
        // Eigene Nummer
        final String localNumber = TextSecurePreferences.getLocalNumber(context);
        // Eigener IdentityKey
        final IdentityKey localIdentity = IdentityKeyUtil.getIdentityKey(context);
        // Empfänger als Recipient
        final Recipient recipient = RecipientFactory.getRecipientsFromString(context, remoteNumber, true).getPrimaryRecipient();
        MasterSecret masterSecret = KeyCachingService.getMasterSecret(getApplicationContext());

        // Utility Methode um IdentityKey des Empfängers zu ermitteln
        IdentityUtil.getRemoteIdentityKey(context, masterSecret, recipient).addListener(new ListenableFuture.Listener<Optional<IdentityKey>>() {
            @Override
            public void onSuccess(Optional<IdentityKey> result) {
                // Sobald IdentityKey des Empfängers ermittelt wurde
                if (result.isPresent()) {
                    // Generiere fingerprint
                    Fingerprint fingerprint = new NumericFingerprintGenerator(5200).createFor(localNumber, localIdentity,
                            remNumber, result.get());

                    if (fingerprint.getDisplayableFingerprint().getDisplayText().equals(qrFingerprint)) {
                        sendVCard(remNumber);
                    } else {
                        // TODO Steffi: evtl. anderen Intent starten?
                        return;
                    }
                }
            }

            @Override
            public void onFailure(ExecutionException e) {
                // TODO Steffi: Was passiert, wenn Fingerprint nicht ermittelt werden konnte?
                Log.w(TAG, e);
            }
        });
    }

    private void sendVCard(String mobileNumber) {

        Context context = getApplicationContext();

        VCard vCard = VCard.getVCard(context);
        if (vCard != null) {
            try {
                String vCardString = JsonUtils.toJson(vCard);
                Recipients recipients = RecipientFactory.getRecipientsFromString(this, mobileNumber, true);

                Intent intent = new Intent(this, ConversationActivity.class);
                intent.putExtra(ConversationActivity.RECIPIENTS_EXTRA, recipients.getIds());
                intent.putExtra(ConversationActivity.TEXT_EXTRA, String.format("!@vcard_%s", vCardString));
                intent.putExtra(ConversationActivity.IS_VCARD_EXTRA, true);
                intent.setDataAndType(getIntent().getData(), getIntent().getType());

                long existingThread = DatabaseFactory.getThreadDatabase(this).getThreadIdIfExistsFor(recipients);

                intent.putExtra(ConversationActivity.THREAD_ID_EXTRA, existingThread);
                intent.putExtra(ConversationActivity.DISTRIBUTION_TYPE_EXTRA, ThreadDatabase.DistributionTypes.DEFAULT);
                startActivity(intent);
                finish();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
