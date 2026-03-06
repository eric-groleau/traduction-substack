package org.traductionsubstack.modele;

import java.util.Objects;

public class BlocTexte implements BlocContenu {
    private final String id;
    private final String texteOriginal;
    private final String texteTraduit;

    public BlocTexte(String id, String texteOriginal, String texteTraduit) {
        this.id = Objects.requireNonNull(id, "id ne doit pas être nul");
        this.texteOriginal = Objects.requireNonNull(texteOriginal, "texteOriginal ne doit pas être nul");
        this.texteTraduit = texteTraduit;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TypeBloc getType() {
        return TypeBloc.TEXTE;
    }

    public String getTexteOriginal() {
        return texteOriginal;
    }

    public String getTexteTraduit() {
        return texteTraduit;
    }
}
