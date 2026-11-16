package boutique;

import org.springframework.stereotype.Service;

/**
 * La correction du piege de l'appel interne : l'appel vient d'un AUTRE bean.
 * commandeService est le proxy injecte par Spring, donc @Transactional s'applique.
 */
@Service
public class Caisse {

    private final CommandeService commandeService;

    public Caisse(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    public void commander(long articleId, long clientId, int quantite) {
        commandeService.passer(articleId, clientId, quantite);
    }
}
