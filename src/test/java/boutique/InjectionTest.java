package boutique;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Injection de dependances : sans Spring pour le constructeur, puis le cablage des beans Paiement.
 * Les tests de cablage ne demarrent que les classes concernees (pas la base), d'ou quelques millisecondes.
 */
class InjectionTest {

    /** CommandeService et ses dependances ; les repositories sont des doublures, la base n'est pas demarree. */
    private final ApplicationContextRunner cablage = new ApplicationContextRunner()
            .withBean(ArticleRepository.class, () -> mock(ArticleRepository.class))
            .withBean(ClientRepository.class, () -> mock(ClientRepository.class))
            .withBean(CommandeRepository.class, () -> mock(CommandeRepository.class))
            .withUserConfiguration(CarteBancaire.class, Paypal.class, CommandeService.class);

    @Test
    @DisplayName("Injection par constructeur : on teste la classe avec new, sans Spring")
    void constructeurSansSpring() {
        // GIVEN une remise factice de 50 euros, passee au constructeur
        CalculPrix calcul = new CalculPrix(montant -> 50);

        // WHEN
        int total = calcul.total(200);

        // THEN
        assertThat(total).isEqualTo(150);
    }

    @Test
    @DisplayName("Injection par champ : hors de Spring, le champ reste null")
    void champSansSpring() {
        // GIVEN la meme classe, injectee par un champ @Autowired
        CalculPrixChamp calcul = new CalculPrixChamp();

        // WHEN
        Throwable erreur = catchThrowable(() -> calcul.total(200));

        // THEN
        assertThat(erreur).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Deux beans Paiement : le contexte refuse de demarrer")
    void deuxCandidats() {
        // GIVEN Paypal actif en plus de CarteBancaire
        cablage.withPropertyValues("paiement.double=true")
                // WHEN le contexte demarre
                .run(contexte -> {
                    // THEN
                    assertThat(contexte).hasFailed();
                    assertThat(contexte.getStartupFailure())
                            .hasRootCauseExactlyInstanceOf(NoUniqueBeanDefinitionException.class);
                });
    }

    @Test
    @DisplayName("Aucun bean Paiement : le contexte refuse de demarrer")
    void aucunCandidat() {
        // GIVEN CarteBancaire desactivee, Paypal inactif
        cablage.withPropertyValues("paiement.actif=false")
                // WHEN le contexte demarre
                .run(contexte -> {
                    // THEN : exactement l'erreur « aucun bean », pas sa sous-classe « deux beans »
                    assertThat(contexte).hasFailed();
                    assertThat(contexte.getStartupFailure())
                            .hasRootCauseExactlyInstanceOf(NoSuchBeanDefinitionException.class);
                });
    }

    @Test
    @DisplayName("@Qualifier : avec deux beans Paiement, Remboursement recoit la carte bancaire")
    void qualifier() {
        // GIVEN deux beans Paiement et le service de remboursement
        new ApplicationContextRunner()
                .withPropertyValues("paiement.double=true")
                .withUserConfiguration(CarteBancaire.class, Paypal.class, Remboursement.class)
                // WHEN le contexte demarre
                .run(contexte -> {
                    // THEN
                    assertThat(contexte).hasNotFailed();
                    assertThat(contexte.getBean(Remboursement.class).moyen()).isEqualTo("CarteBancaire");
                });
    }
}
