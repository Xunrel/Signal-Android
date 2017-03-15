package org.thoughtcrime.securesms.additions;

import java.util.ArrayList;

/**
 * Created by  on 13.03.2017.
 */

public class ChildContact extends Contact {
    private ArrayList<ParentsContact> parents;

    public ChildContact() {
    }

    public ChildContact(ChildContact childContact) {
        this.setFirstName(childContact.getFirstName());
        this.setLastName(childContact.getLastName());
        this.setMobileNumber(childContact.getMobileNumber());
        this.setParents(childContact.getParents());
    }


    public ChildContact(String firstName, String lastName, String mobileNumber) {
        super(firstName, lastName, mobileNumber);
        this.parents = new ArrayList<ParentsContact>();
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
