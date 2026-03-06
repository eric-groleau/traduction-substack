package org.traductionsubstack.modele;

import java.util.List;
import java.util.Objects;

public class ArticleStructure {
    private final ArticleSource source;
    private final List<BlocContenu> blocs;

    public ArticleStructure(ArticleSource source, List<BlocContenu> blocs) {
        this.source = Objects.requireNonNull(source, "source ne doit pas être nulle");
        this.blocs = List.copyOf(Objects.requireNonNull(blocs, "blocs ne doit pas être nulle"));
    }

    public ArticleSource getSource() {
        return source;
    }

    public List<BlocContenu> getBlocs() {
        return blocs;
    }
}
