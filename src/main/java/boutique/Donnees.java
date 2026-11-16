package boutique;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Le jeu de donnees de la demo : 3 claviers a 80 euros, 3 ecrans a 300 euros, Lea (une commande) et Hugo (aucune). */
@Component
@Order(1)
public class Donnees implements ApplicationRunner {

    public record Ids(long clavier, long ecran, long lea, long hugo) {
    }

    private final ArticleRepository articles;
    private final ClientRepository clients;
    private final CommandeRepository commandes;

    public Donnees(ArticleRepository articles, ClientRepository clients, CommandeRepository commandes) {
        this.articles = articles;
        this.clients = clients;
        this.commandes = commandes;
    }

    @Override
    public void run(ApplicationArguments args) {
        Ids ids = reinitialiser();
        Article souris = articles.save(new Article("souris", 20, 10));
        commandes.save(new Commande(souris, clients.findById(ids.lea()).orElseThrow(), 1));
    }

    /** Remet la base dans l'etat de depart, sans aucune commande. */
    public Ids reinitialiser() {
        commandes.deleteAll();
        articles.deleteAll();
        clients.deleteAll();
        Article clavier = articles.save(new Article("clavier", 80, 3));
        Article ecran = articles.save(new Article("ecran", 300, 3));
        Client lea = clients.save(new Client("Lea", "lea@exemple.fr", "$2a$10$Qm4p1Xv8bN0eYdTq7rLwUe9Jt3sK2fH6gC5aZ1oP4iR8uV0wY2xMa"));
        Client hugo = clients.save(new Client("Hugo", "hugo@exemple.fr", "$2a$10$Lk7s2Dq9vB3nXcEr5tYwUi1Op8aS4dF6gH0jK2lZ3xC5vB7nM9qWe"));
        return new Ids(clavier.getId(), ecran.getId(), lea.getId(), hugo.getId());
    }

    public int stock(long articleId) {
        return articles.findById(articleId).orElseThrow().getStock();
    }

    public long nombreCommandes() {
        return commandes.count();
    }
}
