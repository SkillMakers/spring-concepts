package boutique;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Quand plusieurs beans conviennent, @Qualifier designe le bon par son nom. Celui-ci demarre meme avec Paypal actif. */
@Service
@ConditionalOnProperty(name = "paiement.actif", havingValue = "true", matchIfMissing = true)
public class Remboursement {

    private final Paiement paiement;

    public Remboursement(@Qualifier("carteBancaire") Paiement paiement) {
        this.paiement = paiement;
    }

    public String moyen() {
        return paiement.getClass().getSimpleName();
    }
}
