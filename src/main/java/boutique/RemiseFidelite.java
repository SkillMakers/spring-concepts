package boutique;

import org.springframework.stereotype.Component;

/** 10 % de remise au-dela de 100 euros. */
@Component
public class RemiseFidelite implements Remise {

    @Override
    public int sur(int montant) {
        return montant > 100 ? montant / 10 : 0;
    }
}
