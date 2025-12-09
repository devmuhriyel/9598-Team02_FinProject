package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side;

public class AdminParentController {
    private AdminParentModel model;
    private AdminParentView view;
    private Runnable onSuccessCallback;

    public AdminParentController(AdminParentModel model, AdminParentView view, Runnable onSuccessCallback){
        this.model = model;
        this.view = view;
        this.onSuccessCallback = onSuccessCallback;
        initializeButton();
    }

    private void initializeButton(){
        view.getConnectButton().setOnAction(event -> {
            String ip = view.getIPField();
            String portStr = view.getPortField();

            try {
                int port = Integer.parseInt(portStr);
                model.setHostIP(ip);
                model.setHostPort(port);

                // Reinitialize CORBA with new settings
                AdminParentModel.initializeCORBAConnection(new String[]{});

                if(model.isConnected()){
                    view.clearError();
                    if(onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                } else {
                    view.showError("Failed to connect to server");
                }
            } catch (NumberFormatException e) {
                view.showError("Invalid port number");
            } catch (Exception e) {
                view.showError("Connection error: " + e.getMessage());
            }
        });
    }
}