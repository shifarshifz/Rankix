package com.shifz.rankix.servlets;

import com.shifz.rankix.database.tables.Movies;
import com.shifz.rankix.models.Movie;
import com.shifz.rankix.utils.BlowIt;
import com.shifz.rankix.utils.IMDBDotComHelper;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by shifar on 12/12/15.
 */
@WebServlet(urlPatterns = {"/imdbServlet"})
public class IMDBServlet extends BaseServlet {

    private static final String KEY_IMDB_ID = "imdbId";
    private static final String REGEX_IMDBID = "tt\\d{7}";
    private static final String IMDB_URL_FORMAT = "http://imdb.com/title/%s/";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_RATING = "rating";
    private static final String KEY_PLOT = "plot";
    private static final String KEY_POSTER_URL = "poster_url";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json");
        final PrintWriter out = resp.getWriter();

        final String imdbId = req.getParameter(KEY_IMDB_ID);
        if (imdbId != null && imdbId.matches(REGEX_IMDBID)) {

            //Checking db
            final Movies movies = Movies.getInstance();
            Movie movie = movies.getMovie(Movies.COLUMN_IMDB_ID, imdbId);

            if (movie != null && !movie.isRatingExpired()) {
                out.write(getJSONMovieString(movie));

            } else {

                //Download new data
                final URL imdbUrl = new URL(String.format(IMDB_URL_FORMAT, imdbId));
                final HttpURLConnection con = (HttpURLConnection) imdbUrl.openConnection();

                if (con.getResponseCode() == 200) {

                    final BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    final StringBuilder sb = new StringBuilder();

                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line.trim());
                    }

                    br.close();

                    final IMDBDotComHelper imdbHelper = new IMDBDotComHelper(imdbId, sb.toString());

                    if (movie != null) {

                        //Just the rating has expired.
                        movie.setRating(imdbHelper.getRating());

                        //Updating rating column only
                        final boolean isUpdated = movies.update(movie.getId(), Movies.COLUMN_RATING, movie.getRating());

                        if (!isUpdated) {
                            throw new Error("Failed to update the rating");
                        }

                    } else {
                        //New movie
                        movie = imdbHelper.getMovie();
                        final boolean isMovieAdded = movies.add(movie);

                        if (!isMovieAdded) {
                            throw new Error("Failed to add new movie");
                        }
                    }

                    out.write(getJSONMovieString(movie));

                } else {
                    out.write(getJSONError("Movie not found"));
                }
            }


        } else {
            out.write(getJSONError("Invalid imdbId"));
        }

        out.flush();
        out.close();

    }

    private static String getJSONMovieString(Movie movie) {
        final JSONObject jMovie = new JSONObject();
        try {
            jMovie.put(KEY_ERROR, false);
            jMovie.put(KEY_NAME, movie.getName());
            jMovie.put(KEY_GENDER, movie.getGender());
            jMovie.put(KEY_RATING, movie.getRating());
            jMovie.put(KEY_PLOT, movie.getPlot());
            jMovie.put(KEY_POSTER_URL, movie.getPosterUrl());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jMovie.toString();
    }


}
