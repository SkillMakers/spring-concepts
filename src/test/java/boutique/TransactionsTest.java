package boutique;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Chaque comportement de @Transactional montre dans la video. Depart : 3 claviers a 80 euros, 3 ecrans a 300 euros. */
@SpringBootTest
class TransactionsTest {

    @Autowired
    CommandeService commandeService;
    @Autowired
    Caisse caisse;
    @Autowired
    CalculPrix calculPrix;
    @Autowired
    ClientService clientService;
    @Autowired
    Donnees donnees;

    Donnees.Ids ids;

    @BeforeEach
    void depart() {
        ids = donnees.reinitialiser();
    }

    @Test
    @DisplayName("Le service transactionnel est un proxy, pas le service sans annotation")
    void proxy() {
        // GIVEN les deux beans injectes par Spring

        // WHEN
        String classeService = commandeService.getClass().getName();

        // THEN
        assertThat(classeService).contains("$$SpringCGLIB$$");
        assertThat(AopUtils.isAopProxy(commandeService)).isTrue();
        assertThat(AopUtils.isAopProxy(calculPrix)).isFalse();
    }

    @Test
    @DisplayName("Sans transaction : la commande reste en base alors que le stock manquait")
    void sansTransaction() {
        // GIVEN 3 claviers en stock, aucune commande

        // WHEN 5 claviers sont commandes
        Throwable erreur = catchThrowable(() -> commandeService.passerSansTransaction(ids.clavier(), ids.lea(), 5));

        // THEN
        assertThat(erreur).isInstanceOf(IllegalStateException.class);
        assertThat(donnees.nombreCommandes()).isEqualTo(1);
        assertThat(donnees.stock(ids.clavier())).isEqualTo(3);
    }

    @Test
    @DisplayName("Avec @Transactional : une RuntimeException annule la commande")
    void rollbackRuntime() {
        // GIVEN 3 claviers en stock, aucune commande

        // WHEN 5 claviers sont commandes
        Throwable erreur = catchThrowable(() -> commandeService.passer(ids.clavier(), ids.lea(), 5));

        // THEN
        assertThat(erreur).isInstanceOf(IllegalStateException.class);
        assertThat(donnees.nombreCommandes()).isZero();
    }

    @Test
    @DisplayName("Exception verifiee : @Transactional valide quand meme la commande et le stock")
    void exceptionVerifieeValidee() {
        // GIVEN 3 ecrans a 300 euros, plafond de la carte a 500 euros

        // WHEN 2 ecrans sont commandes (600 euros)
        Throwable erreur = catchThrowable(() -> commandeService.passerEtPayer(ids.ecran(), ids.lea(), 2));

        // THEN
        assertThat(erreur).isInstanceOf(PaiementRefuse.class);
        assertThat(donnees.nombreCommandes()).isEqualTo(1);
        assertThat(donnees.stock(ids.ecran())).isEqualTo(1);
    }

    @Test
    @DisplayName("rollbackFor : l'exception verifiee annule tout")
    void rollbackFor() {
        // GIVEN 3 ecrans a 300 euros, plafond de la carte a 500 euros

        // WHEN 2 ecrans sont commandes (600 euros)
        Throwable erreur = catchThrowable(() -> commandeService.passerEtPayerCorrige(ids.ecran(), ids.lea(), 2));

        // THEN
        assertThat(erreur).isInstanceOf(PaiementRefuse.class);
        assertThat(donnees.nombreCommandes()).isZero();
        assertThat(donnees.stock(ids.ecran())).isEqualTo(3);
    }

    @Test
    @DisplayName("Appel interne this.passer() : @Transactional est ignore")
    void appelInterne() {
        // GIVEN 3 claviers en stock, aucune commande

        // WHEN commander() appelle this.passer() pour 5 claviers
        Throwable erreur = catchThrowable(() -> commandeService.commander(ids.clavier(), ids.lea(), 5));

        // THEN la commande fantome est restee
        assertThat(erreur).isInstanceOf(IllegalStateException.class);
        assertThat(donnees.nombreCommandes()).isEqualTo(1);
    }

    @Test
    @DisplayName("Appel depuis un autre bean : @Transactional s'applique")
    void autreBean() {
        // GIVEN 3 claviers en stock, aucune commande

        // WHEN la Caisse appelle commandeService.passer() pour 5 claviers
        Throwable erreur = catchThrowable(() -> caisse.commander(ids.clavier(), ids.lea(), 5));

        // THEN
        assertThat(erreur).isInstanceOf(IllegalStateException.class);
        assertThat(donnees.nombreCommandes()).isZero();
    }

    @Test
    @DisplayName("Une commande valide passe, et le stock baisse")
    void commandeValide() {
        // GIVEN 3 claviers en stock

        // WHEN
        commandeService.passer(ids.clavier(), ids.lea(), 2);

        // THEN
        assertThat(donnees.nombreCommandes()).isEqualTo(1);
        assertThat(donnees.stock(ids.clavier())).isEqualTo(1);
    }

    @Test
    @DisplayName("Une quantite negative est refusee et ne touche pas au stock")
    void quantiteNegative() {
        // GIVEN 3 claviers en stock

        // WHEN
        Throwable erreur = catchThrowable(() -> commandeService.passer(ids.clavier(), ids.lea(), -5));

        // THEN
        assertThat(erreur).isInstanceOf(IllegalArgumentException.class);
        assertThat(donnees.stock(ids.clavier())).isEqualTo(3);
        assertThat(donnees.nombreCommandes()).isZero();
    }

    @Test
    @DisplayName("Le DTO ne contient que l'id, le nom et le nombre de commandes")
    void dto() {
        // GIVEN Lea passe une commande
        commandeService.passer(ids.clavier(), ids.lea(), 1);

        // WHEN
        ClientDto fiche = clientService.fiche(ids.lea());

        // THEN
        assertThat(fiche).isEqualTo(new ClientDto(ids.lea(), "Lea", 1));
    }
}
