package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.AdminMgmt.AdminMgmtController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.AdminMgmt.AdminMgmtModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.AdminMgmt.AdminMgmtView;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.CustomerMgmt.CustomerMgmtController;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.CustomerMgmt.CustomerMgmtModel;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.CustomerMgmtMVCs.CustomerMgmt.CustomerMgmtView;

import java.io.IOException;

/**
 * AccountNavigationManager - Unified manager for both Admin and Customer account management
 */
public class AccountNavigationManager {

    // Admin Management
    private Pane adminPane;
    private AdminMgmtView adminView;
    private AdminMgmtModel adminModel;
    private AdminMgmtController adminController;

    // Customer Management
    private Pane customerPane;
    private CustomerMgmtView customerView;
    private CustomerMgmtModel customerModel;
    private CustomerMgmtController customerController;

    private int adminId;

    // Constructor for Admin Management
    public AccountNavigationManager(int adminId, boolean isAdminManagement) {
        this.adminId = adminId;
        if (isAdminManagement) {
            initializeAdminMVC();
        } else {
            initializeCustomerMVC();
        }
    }

    // Convenience constructor (defaults to Admin Management for backward compatibility)
    public AccountNavigationManager(int adminId) {
        this(adminId, true);
    }

    // ===== ADMIN MANAGEMENT INITIALIZATION =====
    private void initializeAdminMVC() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Admin/AccountAdminMgmt.fxml")
            );
            adminPane = loader.load();
            adminView = loader.getController();

            adminModel = new AdminMgmtModel(adminId);
            adminController = new AdminMgmtController(adminModel, adminView);

            System.out.println("✅ Admin Management MVC initialized");

        } catch (IOException | AuthenticationException e) {
            e.printStackTrace();
            System.err.println("❌ Error initializing Admin Management: " + e.getMessage());
        }
    }

    // ===== CUSTOMER MANAGEMENT INITIALIZATION =====
    private void initializeCustomerMVC() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/moviebooking_9598team02_finproject/moviebooking_9598team02_finproject/Admin/AccountCustomerMgmt.fxml")
            );
            customerPane = loader.load();
            customerView = loader.getController();

            customerModel = new CustomerMgmtModel(adminId);
            customerController = new CustomerMgmtController(customerModel, customerView);

            System.out.println("✅ Customer Management MVC initialized");

        } catch (IOException | AuthenticationException e) {
            e.printStackTrace();
            System.err.println("❌ Error initializing Customer Management: " + e.getMessage());
        }
    }

    // ===== GETTERS - Admin Management =====
    public Pane getAdminPane() {
        return adminPane;
    }

    public AdminMgmtView getAdminView() {
        return adminView;
    }

    public AdminMgmtModel getAdminModel() {
        return adminModel;
    }

    public AdminMgmtController getAdminController() {
        return adminController;
    }

    // ===== GETTERS - Customer Management =====
    public Pane getCustomerPane() {
        return customerPane;
    }

    public CustomerMgmtView getCustomerView() {
        return customerView;
    }

    public CustomerMgmtModel getCustomerModel() {
        return customerModel;
    }

    public CustomerMgmtController getCustomerController() {
        return customerController;
    }

    // ===== UTILITY =====
    public int getAdminId() {
        return adminId;
    }
}