package boutique;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** La meme classe en injection par champ : hors de Spring, rien ne remplit le champ, il reste null. */
@Service
public class CalculPrixChamp {

    @Autowired
    private Remise remise;

    public int total(int montant) {
        return montant - remise.sur(montant);
    }
}
