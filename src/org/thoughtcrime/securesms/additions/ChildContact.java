package org.thoughtcrime.securesms.additions;

import java.util.ArrayList;

/**
 * Created by  on 13.03.2017.
 */

public class ChildContact extends Contact {
    private ArrayList<ParentsContact> parents;

    public ChildContact(String firstName, String lastName, String mobileNumber) {
        super(firstName, lastName, mobileNumber);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ParentsContact{" + "firstName='").append(super.getFirstName()).append('\'')
                .append(", lastName='").append(super.getLastName()).append('\'')
                .append(", mobileNumber='").append(super.getMobileNumber());

        for (ParentsContact parent :
                parents) {
            sb.append(", parent='" + parent.toString() + '\'');
        }
        sb.append("}");

        return sb.toString();
    }
}
