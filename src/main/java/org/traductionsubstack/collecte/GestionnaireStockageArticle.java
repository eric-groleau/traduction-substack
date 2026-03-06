package org.traductionsubstack.collecte;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class GestionnaireStockageArticle {

    private final Path racineDonneesBrutes;

    public GestionnaireStockageArticle(Path racineDonneesBrutes) {
        this.racineDonneesBrutes = Objects.requireNonNull(
                racineDonneesBrutes,
                "racineDonneesBrutes ne doit pas être nulle"
        );
    }

    public Path cheminDossierArticle(String idArticle) {
        return racineDonneesBrutes.resolve(idArticle);
    }

    public Path cheminHtml(String idArticle) {
        return cheminDossierArticle(idArticle).resolve("article.html");
    }

    public boolean articleExiste(String idArticle) {
        return Files.exists(cheminHtml(idArticle));
    }

    public void créerDossierArticle(String idArticle) throws IOException {
        Files.createDirectories(cheminDossierArticle(idArticle));
    }

    public void sauvegarderHtml(Path cheminHtml, String contenuHtml) throws IOException {
        Files.createDirectories(cheminHtml.getParent());
        Files.writeString(cheminHtml, contenuHtml, StandardCharsets.UTF_8);
    }

    public String lireHtml(Path cheminHtml) throws IOException {
        return Files.readString(cheminHtml, StandardCharsets.UTF_8);
    }
}
