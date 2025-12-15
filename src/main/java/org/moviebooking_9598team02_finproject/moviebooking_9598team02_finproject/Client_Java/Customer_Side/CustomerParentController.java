package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Customer_Side;

public class CustomerParentController {
    private CustomerParentModel model;
    private CustomerParentView view;
    private Runnable onSuccessCallback;

    public CustomerParentController(CustomerParentModel model, CustomerParentView view, Runnable onSuccessCallback){
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
                CustomerParentModel.initializeCORBAConnection(new String[]{});

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