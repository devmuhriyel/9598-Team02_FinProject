package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Genre;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.InvalidInputException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;

import java.util.ArrayList;
import java.util.List;

public class GenreMgmtModel extends AdminParentModel {
    private int adminId;

    public GenreMgmtModel(int adminId) {
        this.adminId = adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    // ===== LOGIC METHODS =====

    public GenreOperationResult addGenre(String genreName, String genreDescription) {
        // Validate input
        if (genreName == null || genreName.trim().isEmpty()) {
            return new GenreOperationResult(false, "Error: Genre name is required", null);
        }

        try {
            int newGenreId = adminServices.addGenre(adminId, genreName, genreDescription);
            return new GenreOperationResult(true, "Success: Genre added with ID " + newGenreId, null);
        } catch (InvalidInputException e) {
            return new GenreOperationResult(false, "Error: " + e.message, null);
        } catch (AuthenticationException e) {
            return new GenreOperationResult(false, authErrorMessage(e), null);
        } catch (Exception e) {
            return new GenreOperationResult(false, "Error: " + e.getMessage(), null);
        }
    }

    public GenreOperationResult updateGenre(int genreId, String genreName, String genreDescription) {
        // Validate input
        if (genreName == null || genreName.trim().isEmpty()) {
            return new GenreOperationResult(false, "Error: Genre name is required", null);
        }

        try {
            adminServices.updateGenre(adminId, genreId, genreName, genreDescription);
            return new GenreOperationResult(true, "Success: Genre updated successfully", null);
        } catch (AuthenticationException e) {
            return new GenreOperationResult(false, authErrorMessage(e), null);
        } catch (Exception e) {
            return new GenreOperationResult(false, "Error: " + e.getMessage(), null);
        }
    }

    public GenreOperationResult deleteGenre(int genreId) {
        try {
            adminServices.deleteGenre(adminId, genreId);
            return new GenreOperationResult(true, "Success: Genre deleted successfully", null);
        } catch (AuthenticationException e) {
            return new GenreOperationResult(false, authErrorMessage(e), null);
        } catch (Exception e) {
            return new GenreOperationResult(false, "Error: " + e.getMessage(), null);
        }
    }

    public GenreLoadResult loadGenres() {
        try {
            Genre[] genres = adminServices.getAllGenres(adminId);
            List<GenreTableItem> genreList = new ArrayList<>();

            for (Genre genre : genres) {
                genreList.add(new GenreTableItem(
                        genre.genreId,
                        genre.genreName,
                        genre.genreDescription
                ));
            }

            return new GenreLoadResult(true, null, genreList);
        } catch (AuthenticationException e) {
            return new GenreLoadResult(false, authErrorMessage(e), null);
        } catch (Exception e) {
            return new GenreLoadResult(false, "Error: " + e.getMessage(), null);
        }
    }

    public Genre getGenreById(int genreId) throws AuthenticationException {
        return adminServices.getGenreById(adminId, genreId);
    }

    // Provide clearer hint when authentication fails so user knows to relog/restart.
    private String authErrorMessage(AuthenticationException e) {
        return "Auth error (adminId=" + adminId + "): " + e.getMessage() + ". Please log in again and ensure the server was restarted with the latest build.";
    }

    // ===== RESULT CLASSES =====

    public static class GenreOperationResult {
        private boolean success;
        private String message;
        private Genre genre;

        public GenreOperationResult(boolean success, String message, Genre genre) {
            this.success = success;
            this.message = message;
            this.genre = genre;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Genre getGenre() {
            return genre;
        }
    }

    public static class GenreLoadResult {
        private boolean success;
        private String errorMessage;
        private List<GenreTableItem> genres;

        public GenreLoadResult(boolean success, String errorMessage, List<GenreTableItem> genres) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.genres = genres;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public List<GenreTableItem> getGenres() {
            return genres;
        }
    }

    public static class GenreTableItem {
        private int genreId;
        private String genreName;
        private String genreDescription;

        public GenreTableItem(int genreId, String genreName, String genreDescription) {
            this.genreId = genreId;
            this.genreName = genreName;
            this.genreDescription = genreDescription;
        }

        public int getGenreId() {
            return genreId;
        }

        public String getGenreName() {
            return genreName;
        }

        public String getGenreDescription() {
            return genreDescription;
        }
    }
}

