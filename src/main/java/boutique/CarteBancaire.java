package boutique;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Refuse tout paiement au-dela de 500 euros. Desactivable avec paiement.actif=false (demo du bean manquant). */
@Service
@ConditionalOnProperty(name = "paiement.actif", havingValue = "true", matchIfMissing = true)
public class CarteBancaire implements Paiement {

    static final int PLAFOND = 500;

    @Override
    public void payer(int montant) throws PaiementRefuse {
        if (montant > PLAFOND) {
            throw new PaiementRefuse("paiement refuse : " + montant + " euros, plafond " + PLAFOND);
        }
    }
}
