package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java.Admin_Side.GenreMgmtMVCs;

import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.Genre;

public class GenreMgmtController {
    private GenreMgmtModel model;

    public GenreMgmtController(GenreMgmtModel genreModel) {
        this.model = genreModel;
    }

    // ===== MIDDLEMAN METHODS - JUST PASS CALLS TO MODEL =====

    public GenreMgmtModel.GenreOperationResult addGenre(String genreName, String genreDescription) {
        return model.addGenre(genreName, genreDescription);
    }

    public GenreMgmtModel.GenreOperationResult updateGenre(int genreId, String genreName, String genreDescription) {
        return model.updateGenre(genreId, genreName, genreDescription);
    }

    public GenreMgmtModel.GenreOperationResult deleteGenre(int genreId) {
        return model.deleteGenre(genreId);
    }

    public GenreMgmtModel.GenreLoadResult loadGenres() {
        return model.loadGenres();
    }

    public Genre getGenreById(int genreId) throws AuthenticationException {
        return model.getGenreById(genreId);
    }

    public void setAdminId(int adminId) {
        model.setAdminId(adminId);
    }
}

