package boutique;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    /** A ne pas faire : l'entite JPA renvoyee telle quelle. */
    @GetMapping("/{id}/entite")
    public Client entite(@PathVariable long id) {
        return clientService.entite(id);
    }

    /** Le DTO : on choisit ce qui sort. */
    @GetMapping("/{id}")
    public ClientDto fiche(@PathVariable long id) {
        return clientService.fiche(id);
    }
}
