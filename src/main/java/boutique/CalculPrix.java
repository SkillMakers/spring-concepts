package boutique;

import org.springframework.stereotype.Service;

/** Injection par le constructeur : la dependance est final, et un test peut faire new CalculPrix(...) sans Spring. */
@Service
public class CalculPrix {

    private final Remise remise;

    public CalculPrix(Remise remise) {
        this.remise = remise;
    }

    public int total(int montant) {
        return montant - remise.sur(montant);
    }
}
