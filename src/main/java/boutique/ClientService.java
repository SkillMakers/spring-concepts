package boutique;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** La conversion entite -> DTO se fait ici, dans une transaction en lecture seule. */
@Service
public class ClientService {

    private final ClientRepository clients;
    private final CommandeRepository commandes;

    public ClientService(ClientRepository clients, CommandeRepository commandes) {
        this.clients = clients;
        this.commandes = commandes;
    }

    @Transactional(readOnly = true)
    public ClientDto fiche(long id) {
        Client client = trouver(id);
        // une requete de comptage : on ne charge pas toutes les commandes pour un seul nombre
        return new ClientDto(client.getId(), client.getNom(), commandes.countByClientId(id));
    }

    @Transactional(readOnly = true)
    public Client entite(long id) {
        return trouver(id);
    }

    private Client trouver(long id) {
        return clients.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "client inconnu : " + id));
    }
}
