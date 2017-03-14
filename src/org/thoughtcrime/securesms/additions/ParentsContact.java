package org.thoughtcrime.securesms.additions;

/**
 * Created by  on 13.03.2017.
 */

public class ParentsContact extends Contact {
    public ParentsContact(String firstName, String lastName, String mobileNumber) {
        super(firstName, lastName, mobileNumber);
    }

    @Override
    public String toString() {
        return "ParentsContact{" +
                "firstName='" + super.getFirstName() + '\'' +
                ", lastName='" + super.getLastName() + '\'' +
                ", mobileNumber='" + super.getMobileNumber() +
                "}";
    }
}
