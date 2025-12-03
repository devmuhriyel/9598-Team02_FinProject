package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordManager {
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}
