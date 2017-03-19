package org.thoughtcrime.securesms.additions;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by  on 13.03.2017.
 */

// Steffi:

/**
 * Repräsentiert die Kontaktinformation eines Kindes
 */
public class VCard extends Contact {
    private Date expirationDate;
    private ArrayList<ParentsContact> parents;

    public VCard() {
    }

    public VCard(VCard vCard) {
        this.setFirstName(vCard.getFirstName());
        this.setLastName(vCard.getLastName());
        this.setMobileNumber(vCard.getMobileNumber());
        this.setParents(vCard.getParents());
        this.setExpirationDate(vCard.getExpirationDate());
    }


    public VCard(String firstName, String lastName, String mobileNumber) {
        super(firstName, lastName, mobileNumber);
        this.parents = new ArrayList<ParentsContact>();
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Prüf-Method, ob VCard abgelaufen ist
     *
     * @param dateToCheck Datum, gegen das geprüft werden soll
     * @return TRUE, wenn das Ablaufdatum der VCard überschritten wurde, ansonsten FALSE.
     */
    public boolean isExpired(Date dateToCheck) {
        return this.expirationDate.before(dateToCheck);
    }

    public ArrayList<ParentsContact> getParents() {
        return parents;
    }

    public void setParents(ArrayList<ParentsContact> parents) {
        this.parents = parents;
    }

    public void addParent(ParentsContact parent) {
        parents.add(parents.size(), parent);
    }
}
