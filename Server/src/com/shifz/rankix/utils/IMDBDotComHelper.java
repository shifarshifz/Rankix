package com.shifz.rankix.utils;

/**
 * Created by shifar on 15/12/15.
 */
public class IMDBDotComHelper {

    private static final String MOVIE_NAME_EXP1_REGEX = "<h1 itemprop=\"name\" class=\"\">";
    private static final java.lang.String MOVIE_NAME_EXP2_REGEX = "</h1>";
    private static final String MOVIE_GENDER_EXP1_REGEX = "<\\/time>";
    private static final String MOVIE_GENDER_EXP2_REGEX = "<span class=\"ghost\">\\|<\\/span>";
    private static final String HTML_REMOVE_REGEX = "<[^>]*>";
    private static final String MOVIE_RATING_EXP1_REGEX = "<span itemprop=\"ratingValue\">";
    private static final java.lang.String MOVIE_RATING_EXP2_REGEX = "</span></strong>";
    private static final java.lang.String MOVIE_PLOT_EXP1_REGEX = "<div class=\"summary_text\" itemprop=\"description\">";
    private static final java.lang.String MOVE_PLOT_EXP2_REGEX = "</div><div class=\"credit_summary_item\">";
    private static final java.lang.String MOVIE_POSTER_EXP1_REGEX = "itemprop=\"image\"";
    private static final java.lang.String MOVIE_POSTER_EXP2_REGEX = "src=\"";
    private final String imdbHtml;

    public IMDBDotComHelper(String imdbHtml) {
        this.imdbHtml = imdbHtml;
    }

    public String getMovieName() {
        final String[] exp1 = this.imdbHtml.split(MOVIE_NAME_EXP1_REGEX);
        return exp1[1].split(MOVIE_NAME_EXP2_REGEX)[0];
    }

    public String getRating() {
        final String[] exp1 = this.imdbHtml.split(MOVIE_RATING_EXP1_REGEX);
        return exp1[1].split(MOVIE_RATING_EXP2_REGEX)[0];
    }

    private static void debug(String[] exp1) {
        System.out.println("Length: " + exp1.length);
        for (final String exp : exp1) {
            System.out.println(exp);
            System.out.println();
        }
    }

    public String getPlot() {
        final String[] exp1 = this.imdbHtml.split(MOVIE_PLOT_EXP1_REGEX);
        return exp1[1].split(MOVE_PLOT_EXP2_REGEX)[0];
    }

    public String getPosterUrl(final String movieName) {
        final String[] exp1 = this.imdbHtml.split(String.format("<img alt=\"%s Poster", movieName));
        final String exp2 = exp1[1].split(MOVIE_POSTER_EXP1_REGEX)[0];
        String imageUrl = exp2.split(MOVIE_POSTER_EXP2_REGEX)[1];
        return imageUrl.substring(0, imageUrl.length() - 1);
    }

    public String getGender() {
        final String[] exp1 = this.imdbHtml.split(MOVIE_GENDER_EXP1_REGEX);
        final String[] exp2 = exp1[1].split(MOVIE_GENDER_EXP2_REGEX);
        return exp2[1].replaceAll(HTML_REMOVE_REGEX, "");
    }
}