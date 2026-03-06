package org.traductionsubstack.collecte;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.traductionsubstack.modele.ArticleSource;
import org.traductionsubstack.modele.ImageSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CollecteurSubstack {

    private static final Duration TIMEOUT_REQUETE = Duration.ofSeconds(30);
    private static final long PAUSE_ENTRE_IMAGES_MS = 750L;

    private final HttpClient clientHttp;
    private final Path repertoireBrut;

    public CollecteurSubstack(Path repertoireBrut) {
        this.repertoireBrut = Objects.requireNonNull(repertoireBrut, "repertoireBrut ne doit pas être nul");
        this.clientHttp = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(TIMEOUT_REQUETE)
                .build();
    }

    /**
     * Collecte un article Substack à partir de son URL.
     *
     * Politique :
     * - si le HTML existe déjà localement, il est réutilisé ;
     * - les images déjà présentes localement ne sont pas retéléchargées ;
     * - toute collecte réussie est sauvegardée immédiatement sur disque.
     */
    public ArticleSource collecterArticle(String urlArticle) throws IOException, InterruptedException {
        Objects.requireNonNull(urlArticle, "urlArticle ne doit pas être nul");

        String idArticle = calculerIdStable(urlArticle);
        Path dossierArticle = repertoireBrut.resolve(idArticle);
        Path fichierHtml = dossierArticle.resolve("article.html");
        Path dossierImages = dossierArticle.resolve("images");

        Files.createDirectories(dossierArticle);
        Files.createDirectories(dossierImages);

        String html;
        if (Files.exists(fichierHtml)) {
            html = Files.readString(fichierHtml);
        } else {
            html = telechargerTexte(urlArticle);
            Files.writeString(fichierHtml, html);
        }

        Document document = Jsoup.parse(html, urlArticle);

        String titre = extraireTitre(document).orElse("Sans titre");
        OffsetDateTime datePublication = extraireDatePublication(document).orElse(null);
        String langueSource = extraireLangue(document).orElse("inconnue");

        List<ImageSource> images = extraireEtTelechargerImages(document, dossierImages, idArticle);

        return new ArticleSource(
                idArticle,
                urlArticle,
                titre,
                datePublication,
                langueSource,
                fichierHtml,
                images
        );
    }

    private List<ImageSource> extraireEtTelechargerImages(
            Document document,
            Path dossierImages,
            String idArticle
    ) throws IOException, InterruptedException {

        List<ImageSource> resultat = new ArrayList<>();
        Elements elementsImage = document.select("img");

        int position = 0;
        for (Element img : elementsImage) {
            String urlImage = img.absUrl("src");
            if (urlImage == null || urlImage.isBlank()) {
                continue;
            }

            position++;
            String alt = nettoyerTexte(img.attr("alt"));
            String idImage = idArticle + "-img-" + position;

            String extension = determinerExtension(urlImage)
                    .orElseGet(() -> determinerExtensionDepuisTypeMime(urlImage).orElse("bin"));

            Path cheminLocal = dossierImages.resolve(idImage + "." + extension);

            if (!Files.exists(cheminLocal)) {
                telechargerBinaire(urlImage, cheminLocal);
                Thread.sleep(PAUSE_ENTRE_IMAGES_MS);
            }

            resultat.add(new ImageSource(
                    idImage,
                    urlImage,
                    cheminLocal,
                    position,
                    alt
            ));
        }

        return resultat;
    }

    private String telechargerTexte(String url) throws IOException, InterruptedException {
        HttpRequest requete = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT_REQUETE)
                .header("User-Agent", "traduction-substack/0.1 (+collecte locale prudente)")
                .GET()
                .build();

        HttpResponse<String> reponse = clientHttp.send(requete, HttpResponse.BodyHandlers.ofString());

        if (reponse.statusCode() < 200 || reponse.statusCode() >= 300) {
            throw new IOException("Échec du téléchargement HTML. Code HTTP : " + reponse.statusCode());
        }

        return reponse.body();
    }

    private void telechargerBinaire(String url, Path destination) throws IOException, InterruptedException {
        HttpRequest requete = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT_REQUETE)
                .header("User-Agent", "traduction-substack/0.1 (+collecte locale prudente)")
                .GET()
                .build();

        HttpResponse<InputStream> reponse = clientHttp.send(requete, HttpResponse.BodyHandlers.ofInputStream());

        if (reponse.statusCode() < 200 || reponse.statusCode() >= 300) {
            throw new IOException("Échec du téléchargement image : " + url + " (HTTP " + reponse.statusCode() + ")");
        }

        try (InputStream corps = reponse.body()) {
            try {
                Files.copy(corps, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (FileAlreadyExistsException e) {
                // Rien à faire : l’image existe déjà.
            }
        }
    }

    private Optional<String> extraireTitre(Document document) {
        Element metaOgTitle = document.selectFirst("meta[property=og:title]");
        if (metaOgTitle != null) {
            String contenu = nettoyerTexte(metaOgTitle.attr("content"));
            if (!contenu.isBlank()) {
                return Optional.of(contenu);
            }
        }

        Element title = document.selectFirst("title");
        if (title != null) {
            String contenu = nettoyerTexte(title.text());
            if (!contenu.isBlank()) {
                return Optional.of(contenu);
            }
        }

        Element h1 = document.selectFirst("h1");
        if (h1 != null) {
            String contenu = nettoyerTexte(h1.text());
            if (!contenu.isBlank()) {
                return Optional.of(contenu);
            }
        }

        return Optional.empty();
    }

    private Optional<OffsetDateTime> extraireDatePublication(Document document) {
        List<String> selecteurs = List.of(
                "meta[property=article:published_time]",
                "meta[name=pubdate]",
                "meta[name=date]",
                "meta[property=og:article:published_time]"
        );

        for (String selecteur : selecteurs) {
            Element meta = document.selectFirst(selecteur);
            if (meta == null) {
                continue;
            }

            String contenu = nettoyerTexte(meta.attr("content"));
            if (contenu.isBlank()) {
                continue;
            }

            try {
                return Optional.of(OffsetDateTime.parse(contenu));
            } catch (DateTimeParseException ignored) {
                // On essaie d'autres formats ou sélecteurs.
            }
        }

        return Optional.empty();
    }

    private Optional<String> extraireLangue(Document document) {
        Element racine = document.selectFirst("html[lang]");
        if (racine != null) {
            String langue = nettoyerTexte(racine.attr("lang"));
            if (!langue.isBlank()) {
                return Optional.of(langue);
            }
        }

        Element metaOgLocale = document.selectFirst("meta[property=og:locale]");
        if (metaOgLocale != null) {
            String locale = nettoyerTexte(metaOgLocale.attr("content"));
            if (!locale.isBlank()) {
                return Optional.of(locale);
            }
        }

        return Optional.empty();
    }

    private Optional<String> determinerExtension(String url) {
        try {
            URI uri = URI.create(url);
            String chemin = uri.getPath();
            if (chemin == null || chemin.isBlank()) {
                return Optional.empty();
            }

            int dernierPoint = chemin.lastIndexOf('.');
            if (dernierPoint < 0 || dernierPoint == chemin.length() - 1) {
                return Optional.empty();
            }

            String extension = chemin.substring(dernierPoint + 1).toLowerCase();
            if (extension.length() > 5) {
                return Optional.empty();
            }

            return Optional.of(extension);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<String> determinerExtensionDepuisTypeMime(String url) {
        try {
            HttpRequest requete = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(TIMEOUT_REQUETE)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .header("User-Agent", "traduction-substack/0.1 (+collecte locale prudente)")
                    .build();

            HttpResponse<Void> reponse = clientHttp.send(requete, HttpResponse.BodyHandlers.discarding());
            String typeMime = reponse.headers().firstValue("Content-Type").orElse("");

            if (typeMime.isBlank()) {
                return Optional.empty();
            }

            String ext = switch (typeMime.toLowerCase()) {
                case "image/jpeg" -> "jpg";
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                case "image/gif" -> "gif";
                case "image/svg+xml" -> "svg";
                default -> null;
            };

            return Optional.ofNullable(ext);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String calculerIdStable(String valeur) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valeur.getBytes());
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            // Solution de repli très improbable.
            return String.valueOf(Math.abs(valeur.hashCode()));
        }
    }

    private String nettoyerTexte(String texte) {
        if (texte == null) {
            return "";
        }
        return texte.replace('\u00A0', ' ').trim().replaceAll("\\s+", " ");
    }
}
