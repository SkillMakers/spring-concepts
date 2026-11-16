package boutique;

/** Ce que l'API montre d'un client, et rien d'autre : ni le mot de passe, ni la liste des commandes. */
public record ClientDto(Long id, String nom, long nombreCommandes) {
}
