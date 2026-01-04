package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.*;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.AdminParentModel;

/**
 * GenreMgmtModel - Business Logic for Genre Management
 */
public class GenreMgmtModel extends AdminParentModel {
    private int adminId;

    public GenreMgmtModel(int adminId) {
        this.adminId = adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public Genre[] loadAllGenres() throws AuthenticationException {
        return adminServices.getAllGenres(adminId, sessionToken);
    }

    public Genre getGenreById(int genreId) throws AuthenticationException {
        return adminServices.getGenreById(adminId, sessionToken, genreId);
    }

    public int addGenre(String genreName, String genreDescription) throws AuthenticationException, InvalidInputException {
        return adminServices.addGenre(adminId, sessionToken, genreName, genreDescription);
    }

    public void updateGenre(int genreId, String genreName, String genreDescription) throws AuthenticationException {
        adminServices.updateGenre(adminId, sessionToken, genreId, genreName, genreDescription);
    }

    public void deleteGenre(int genreId) throws AuthenticationException {
        adminServices.deleteGenre(adminId, sessionToken, genreId);
    }
}
