package org.traductionsubstack.collecte;

import java.net.URI;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

public final class IdentifiantArticle {

    private IdentifiantArticle() {
    }

    public static String depuisUrl(String url) {
        Objects.requireNonNull(url, "url ne doit pas être nulle");

        URI uri = URI.create(url);
        String chemin = uri.getPath();

        if (chemin == null || chemin.isBlank() || "/".equals(chemin)) {
            return "article-sans-chemin";
        }

        String[] segments = chemin.split("/");
        String dernierSegment = "";

        for (String segment : segments) {
            if (segment != null && !segment.isBlank()) {
                dernierSegment = segment;
            }
        }

        if (dernierSegment.isBlank()) {
            dernierSegment = "article";
        }

        return normaliser(dernierSegment);
    }

    private static String normaliser(String valeur) {
        String sansAccents = Normalizer.normalize(valeur, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String slug = sansAccents
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");

        return slug.isBlank() ? "article" : slug;
    }
}
