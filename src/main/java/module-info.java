module org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject to javafx.fxml;
    exports org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject;
}