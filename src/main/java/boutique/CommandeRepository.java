package boutique;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    /** Spring Data deduit la requete du nom : SELECT COUNT(*) ... WHERE client.id = ? */
    long countByClientId(Long clientId);
}
