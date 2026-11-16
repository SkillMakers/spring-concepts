package boutique;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Commande {

    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Article article;
    @ManyToOne
    private Client client;
    private int quantite;

    protected Commande() {
    }

    public Commande(Article article, Client client, int quantite) {
        this.article = article;
        this.client = client;
        this.quantite = quantite;
    }

    public Long getId() { return id; }
    public Article getArticle() { return article; }
    public Client getClient() { return client; }
    public int getQuantite() { return quantite; }
}
