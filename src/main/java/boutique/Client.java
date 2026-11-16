package boutique;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Client {

    @Id
    @GeneratedValue
    private Long id;
    private String nom;
    private String email;
    private String motDePasseHash;

    @OneToMany(mappedBy = "client")
    private List<Commande> commandes = new ArrayList<>();

    protected Client() {
    }

    public Client(String nom, String email, String motDePasseHash) {
        this.nom = nom;
        this.email = email;
        this.motDePasseHash = motDePasseHash;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public String getMotDePasseHash() { return motDePasseHash; }
    public List<Commande> getCommandes() { return commandes; }
}
