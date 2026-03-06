package org.traductionsubstack.modele;

import java.nio.file.Path;
import java.util.Objects;

public class ImageSource {
    private final String id;
    private final String urlOrigine;
    private final Path cheminLocal;
    private final int positionDansArticle;
    private final String texteAlternatif;

    public ImageSource(
            String id,
            String urlOrigine,
            Path cheminLocal,
            int positionDansArticle,
            String texteAlternatif
    ) {
        this.id = Objects.requireNonNull(id, "id ne doit pas être nul");
        this.urlOrigine = Objects.requireNonNull(urlOrigine, "urlOrigine ne doit pas être nulle");
        this.cheminLocal = Objects.requireNonNull(cheminLocal, "cheminLocal ne doit pas être nul");
        this.positionDansArticle = positionDansArticle;
        this.texteAlternatif = texteAlternatif;
    }

    public String getId() {
        return id;
    }

    public String getUrlOrigine() {
        return urlOrigine;
    }

    public Path getCheminLocal() {
        return cheminLocal;
    }

    public int getPositionDansArticle() {
        return positionDansArticle;
    }

    public String getTexteAlternatif() {
        return texteAlternatif;
    }

    @Override
    public String toString() {
        return "ImageSource{" +
                "id='" + id + '\'' +
                ", urlOrigine='" + urlOrigine + '\'' +
                ", cheminLocal=" + cheminLocal +
                ", positionDansArticle=" + positionDansArticle +
                '}';
    }
}
