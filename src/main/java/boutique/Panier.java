package boutique;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Portee prototype : un nouvel objet a chaque demande, au lieu du singleton par defaut. */
@Component
@Scope("prototype")
public class Panier {

    private final List<String> lignes = new ArrayList<>();

    public List<String> lignes() {
        return lignes;
    }
}
