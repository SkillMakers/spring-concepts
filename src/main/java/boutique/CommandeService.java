package boutique;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Passer une commande : enregistrer la commande, puis retirer le stock, puis (selon la methode) encaisser.
 * Chaque variante montre un comportement de @Transactional. Les dependances arrivent par le constructeur.
 */
@Service
public class CommandeService {

    private final ArticleRepository articles;
    private final ClientRepository clients;
    private final CommandeRepository commandes;
    private final Paiement paiement;

    public CommandeService(ArticleRepository articles, ClientRepository clients,
                           CommandeRepository commandes, Paiement paiement) {
        this.articles = articles;
        this.clients = clients;
        this.commandes = commandes;
        this.paiement = paiement;
    }

    /** Sans transaction : chaque save() est valide tout de suite, meme si la suite echoue. */
    public void passerSansTransaction(long articleId, long clientId, int quantite) {
        enregistrer(articleId, clientId, quantite);
    }

    /** Tout ou rien : une RuntimeException annule tout ce qui a ete fait dans la methode. */
    @Transactional
    public void passer(long articleId, long clientId, int quantite) {
        enregistrer(articleId, clientId, quantite);
    }

    /** Piege : PaiementRefuse est une exception verifiee, et par defaut elle ne declenche PAS de rollback. */
    @Transactional
    public void passerEtPayer(long articleId, long clientId, int quantite) throws PaiementRefuse {
        Article article = enregistrer(articleId, clientId, quantite);
        paiement.payer(article.getPrix() * quantite);
    }

    /** La correction : rollbackFor ajoute l'exception verifiee aux cas d'annulation. */
    @Transactional(rollbackFor = PaiementRefuse.class)
    public void passerEtPayerCorrige(long articleId, long clientId, int quantite) throws PaiementRefuse {
        Article article = enregistrer(articleId, clientId, quantite);
        paiement.payer(article.getPrix() * quantite);
    }

    /**
     * Le piege principal : this.passer() est un appel interne. Il ne passe pas par le proxy de Spring,
     * donc l'annotation @Transactional de passer() est ignoree.
     */
    public void commander(long articleId, long clientId, int quantite) {
        this.passer(articleId, clientId, quantite);
    }

    private Article enregistrer(long articleId, long clientId, int quantite) {
        System.out.println("    transaction active : " + TransactionSynchronizationManager.isActualTransactionActive());
        Article article = articles.findById(articleId).orElseThrow();
        Client client = clients.findById(clientId).orElseThrow();
        commandes.save(new Commande(article, client, quantite));
        article.retirer(quantite);
        articles.save(article);
        return article;
    }
}
