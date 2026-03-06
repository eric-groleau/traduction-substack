package org.traductionsubstack.modele;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public class ArticleSource {
    private final String id;
    private final String url;
    private final String titre;
    private final OffsetDateTime datePublication;
    private final String langueSource;
    private final Path cheminHtmlLocal;
    private final List<ImageSource> images;

    public ArticleSource(
            String id,
            String url,
            String titre,
            OffsetDateTime datePublication,
            String langueSource,
            Path cheminHtmlLocal,
            List<ImageSource> images
    ) {
        this.id = Objects.requireNonNull(id, "id ne doit pas être nul");
        this.url = Objects.requireNonNull(url, "url ne doit pas être nulle");
        this.titre = Objects.requireNonNull(titre, "titre ne doit pas être nul");
        this.datePublication = datePublication;
        this.langueSource = Objects.requireNonNull(langueSource, "langueSource ne doit pas être nulle");
        this.cheminHtmlLocal = Objects.requireNonNull(cheminHtmlLocal, "cheminHtmlLocal ne doit pas être nul");
        this.images = List.copyOf(Objects.requireNonNull(images, "images ne doit pas être nulle"));
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitre() {
        return titre;
    }

    public OffsetDateTime getDatePublication() {
        return datePublication;
    }

    public String getLangueSource() {
        return langueSource;
    }

    public Path getCheminHtmlLocal() {
        return cheminHtmlLocal;
    }

    public List<ImageSource> getImages() {
        return images;
    }

    @Override
    public String toString() {
        return "ArticleSource{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", titre='" + titre + '\'' +
                ", datePublication=" + datePublication +
                ", langueSource='" + langueSource + '\'' +
                ", cheminHtmlLocal=" + cheminHtmlLocal +
                ", images=" + images.size() +
                '}';
    }
}
