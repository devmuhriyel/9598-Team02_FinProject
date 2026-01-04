package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

public class AdminHomeModel extends AdminParentModel {
    // ✅ ADD: Constructor to accept session data
    public AdminHomeModel() {
        // Default constructor for basic usage
    }

    public AdminHomeModel(int adminId, String sessionToken) {
        this.adminId = adminId;
        this.sessionToken = sessionToken;
    }

    public void removeSession(int adminID){
        // Implementation if needed
    }
}