package boutique;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Un second moyen de paiement, actif seulement avec paiement.double=true.
 * Il y a alors deux beans Paiement : Spring ne sait plus lequel injecter dans CommandeService.
 */
@Service
@ConditionalOnProperty(name = "paiement.double", havingValue = "true")
public class Paypal implements Paiement {

    @Override
    public void payer(int montant) {
    }
}
