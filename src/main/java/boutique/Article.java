package boutique;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Article {

    @Id
    @GeneratedValue
    private Long id;
    private String nom;
    private int prix;
    private int stock;

    protected Article() {
    }

    public Article(String nom, int prix, int stock) {
        this.nom = nom;
        this.prix = prix;
        this.stock = stock;
    }

    /** Retire du stock, ou refuse si la quantite demandee n'y est pas. */
    public void retirer(int quantite) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("quantite invalide : " + quantite);
        }
        if (quantite > stock) {
            throw new IllegalStateException("stock insuffisant : " + stock + " en stock, " + quantite + " demandes");
        }
        stock -= quantite;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public int getPrix() { return prix; }
    public int getStock() { return stock; }
}
