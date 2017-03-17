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

public class PendingList {
    private static final Integer EXPIRATION_TIME = 14;
    private HashMap<Integer, VCard> pendingVCards;

    public PendingList(HashMap<Integer, VCard> pendingVCards) {
        this.pendingVCards = pendingVCards;
    }

    public PendingList() {
        if (this.pendingVCards == null) {
            this.pendingVCards = new HashMap<>();
        }
    }

    public static void addNewVCard(final Context context, VCard vCard) {
        try {
            PendingList pendingList = getPendingListContent(context);
            pendingList.addVCard(context, vCard);
            String jsonString = JsonUtils.toJson(pendingList);
            FileHelper.writeDataToFile(context, jsonString, FileHelper.pendingListFileName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static VCard removeVCardById(final Context context, int id) {
        VCard vCard = null;

        try {
            PendingList pendingList = getPendingListContent(context);
            vCard = pendingList.pendingVCards.remove(id);
            String jsonString = JsonUtils.toJson(pendingList);
            FileHelper.writeDataToFile(context, jsonString, FileHelper.pendingListFileName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return vCard;
    }

    public static void checkExpirationDates(final Context context) {
        String jsonString = FileHelper.readDataFromFile(context, FileHelper.pendingListFileName);
        try {
            PendingList pendingList = JsonUtils.fromJson(jsonString, PendingList.class);
            ArrayList<Integer> vCardsToRemove = new ArrayList<>();
            Date now = new Date();

            // Steffi: Gehe Pending Liste durch und überprüfe Ablaufdatum mit aktuellem Datum
            for (Map.Entry<Integer, VCard> vCardEntry : pendingList.pendingVCards.entrySet()) {
                // Wenn kein Datum angegeben, gehe zum nächsten Eintrag
                if (vCardEntry.getValue().getExpirationDate() == null) continue;

                // Wenn Ablaufdatum kleiner als aktuelles Datum ist, dann markiere die vCard zum Entfernen
                if (vCardEntry.getValue().getExpirationDate().before(now)) {
                    vCardsToRemove.add(vCardEntry.getKey());
                }
            }

            // Entferne die markierten vCards aus der Pending Liste
            for (Integer vtr : vCardsToRemove) {
                pendingList.pendingVCards.remove(vtr);
            }

            jsonString = JsonUtils.toJson(pendingList);
            FileHelper.writeDataToFile(context, jsonString, FileHelper.pendingListFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PendingList getPendingListContent(final Context context) {
        PendingList pendingList = new PendingList();
        String jsonString = FileHelper.readDataFromFile(context, FileHelper.pendingListFileName);

        try {
            pendingList = JsonUtils.fromJson(jsonString, PendingList.class);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return pendingList;
    }

    public HashMap<Integer, VCard> getPendingVCards() {
        return pendingVCards;
    }

    public void setPendingVCards(HashMap<Integer, VCard> pendingVCards) {
        this.pendingVCards = pendingVCards;
    }

    private void addVCard(final Context context, VCard vCard) {
        if (existsVCard(vCard)) return;
        if (BlackList.getBlackListContent(context).isInBlackList(vCard.getMobileNumber())) return;
        if (WhiteList.getWhiteListContent(context).isInWhiteList(vCard.getMobileNumber())) return;

        addExpirationDate(vCard);
        Integer newId = getNewId();
        this.pendingVCards.put(newId, vCard);
    }

    private void addExpirationDate(VCard vCard) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, EXPIRATION_TIME);
        vCard.setExpirationDate(cal.getTime());
    }

    private Integer getNewId() {
        Integer newId = 1;

        if (!this.pendingVCards.isEmpty()) {
            Integer lastExistingId = 0;
            for (Integer existingId : this.pendingVCards.keySet()) {
                if (existingId != null && existingId > lastExistingId) {
                    lastExistingId = existingId;
                }
            }
            newId = lastExistingId + 1;
        }

        return newId;
    }

    private boolean existsVCard(VCard vCard) {
        for (Map.Entry<Integer, VCard> pendingVCard : this.pendingVCards.entrySet()) {
            if (pendingVCard.equals(vCard)) return true;
        }
        return false;
    }
}
