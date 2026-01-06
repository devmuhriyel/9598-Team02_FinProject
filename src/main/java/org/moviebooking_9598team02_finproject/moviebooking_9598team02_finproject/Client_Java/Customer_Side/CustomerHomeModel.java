package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

public class CustomerHomeModel extends CustomerParentModel {
    // ✅ ADD: Constructor to accept session data
    public CustomerHomeModel() {
        // Default constructor for basic usage
    }

    public CustomerHomeModel(int customerId, String sessionToken) {
        this.customerId = customerId;
        this.sessionToken = sessionToken;
    }

    public void removeSession(int customerID){
        // Implementation if needed
    }
}