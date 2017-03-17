package org.thoughtcrime.securesms.additions;

/**
 * Created by  on 17.03.2017.
 */

public final class MessageHelper {
    // Steffi: Konvention ->
    // "!@ COMMAND PARAMS"
    // "!@ ok ID"
    // "!@ block ID (perm)"
    // "!@ new NUMBER DISPLAYNAME"
    // "!@ list LISTNAME"
    // "!@ help"
    private static final String SPECIAL_CODE_PREFIX = "!@ ";

    public static boolean startsWithSpecialCode(String message) {
        return message.startsWith(SPECIAL_CODE_PREFIX);
    }

    public static String getNumberFromMessage(String message) {
        String[] messageParts = getMessageParts(message);
        String number = "";
        if (messageParts.length == 3) {
            number = messageParts[1];
        }
        return number;
    }

    public static String getDisplayNameFromMessage(String message) {
        String[] messageParts = getMessageParts(message);
        String number = "";
        if (messageParts.length == 3) {
            number = messageParts[2];
        }
        return number;
    }

    private static String[] getMessageParts(String message) {
        String newMessage = message.replaceAll(SPECIAL_CODE_PREFIX, "").trim();
        return newMessage.split(" ");
    }

    public static Integer getIdFromMessage(String message) {
        Integer id = 0;

        String[] messageParts = getMessageParts(message);

        if (messageParts.length > 1) {
            try {
                id = Integer.valueOf(messageParts[messageParts.length]);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }

        return id;
    }

    public static String getCommandFromMessage(String message) {
        String specialCommand = "";

        String[] messageParts = getMessageParts(message);

        if (messageParts.length > 0) {
            specialCommand = messageParts[0];
        }

        return specialCommand.toLowerCase();
    }

    public static String getListNameFromMessage(String message) {
        String listName = "";
        String[] messageParts = getMessageParts(message);
        if (messageParts.length == 2) {
            listName = messageParts[1].toLowerCase();
        }
        return listName;
    }

    public static boolean isPermanentBlock(String message) {
        String[] messageParts = getMessageParts(message);

        return messageParts.length == 3 && message.endsWith(" perm");
    }
}
