package org.traductionsubstack.app;

import org.traductionsubstack.collecte.CollecteurSubstack;
import org.traductionsubstack.collecte.GestionnaireStockageArticle;
import org.traductionsubstack.modele.ArticleSource;

import java.nio.file.Path;

public class ApplicationPrincipale {

    public static void main(String[] args) throws Exception {
        String urlArticle = "https://prussiagate.substack.com/p/urania";

        GestionnaireStockageArticle gestionnaire =
                new GestionnaireStockageArticle(Path.of("donnees/brut"));

        CollecteurSubstack collecteur = new CollecteurSubstack(gestionnaire);

        ArticleSource article = collecteur.collecterDepuisUrl(urlArticle);

        System.out.println("Article collecté :");
        System.out.println("ID : " + article.getId());
        System.out.println("Titre : " + article.getTitre());
        System.out.println("Date : " + article.getDatePublication());
        System.out.println("HTML local : " + article.getCheminHtmlLocal());
    }
}
