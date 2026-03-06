package org.traductionsubstack.modele;

import java.util.Objects;

public class BlocImage implements BlocContenu {
    private final String id;
    private final ImageSource imageSource;

    public BlocImage(String id, ImageSource imageSource) {
        this.id = Objects.requireNonNull(id, "id ne doit pas être nul");
        this.imageSource = Objects.requireNonNull(imageSource, "imageSource ne doit pas être nulle");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TypeBloc getType() {
        return TypeBloc.IMAGE;
    }

    public ImageSource getImageSource() {
        return imageSource;
    }
}
