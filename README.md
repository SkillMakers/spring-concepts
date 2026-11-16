# Spring Boot : bean, injection de dépendances, @Transactional, DTO

Le code de la vidéo ItSkillsMaker. Une petite boutique (articles, clients, commandes, une carte bancaire qui refuse
au-delà de 500 euros) qui montre, par de vraies exécutions :

- ce qu'est un **bean** : 155 dans le conteneur, dont 16 à nous ; singleton par défaut, `@Scope("prototype")` sinon ;
- l'**injection de dépendances** : par constructeur (`CalculPrix`, testable avec `new`) et par champ
  (`CalculPrixChamp`, `NullPointerException` hors de Spring) ;
- les deux erreurs de démarrage : **aucun bean** (`--paiement.actif=false`) et **deux beans** (`--paiement.double=true`),
  et `@Qualifier` (`Remboursement`, test `qualifier`) ;
- le **proxy** : `CommandeService$$SpringCGLIB$$0` ;
- **@Transactional** : sans transaction (commande fantôme), avec (rollback), exception vérifiée (pas de rollback),
  `rollbackFor` ;
- le **piège** : `this.passer()` depuis la même classe ignore `@Transactional` ; la `Caisse`, un autre bean, corrige ;
- l'**entité** renvoyée telle quelle (hash du mot de passe, JSON qui tourne en rond) contre un **DTO** (`ClientDto`).

Spring Boot 4.1.2, Java 25, base H2 en mémoire.

## Lancer

Sans rien installer, avec Docker :

```
docker compose run --rm demo
```

Le conteneur compile, lance les 15 tests, joue chaque scénario et écrit les sorties dans `preuves/` :

| Fichier | Contenu |
|---|---|
| `tests.txt` | les 15 tests, un par ligne |
| `demo.txt` | les 8 scénarios (beans, proxy, transactions, le piège) |
| `demarrage-aucun-bean.txt`, `demarrage-deux-beans.txt` | le rapport « APPLICATION FAILED TO START » de Spring Boot |
| `dto.txt`, `api-journal.txt` | l'entité et le DTO interrogés avec curl (et un client inconnu : 404), et le journal du serveur |
| `dto-sans-open-in-view.txt` | la même entité avec `spring.jpa.open-in-view=false` : erreur 500, collection non chargeable |

Avec Java 25 et Maven installés : `mvn verify`, puis `java -jar target/spring-concepts-1.0.jar --demo=true
--spring.main.web-application-type=none`.
