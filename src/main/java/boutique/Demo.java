package boutique;

import java.util.Arrays;

import org.springframework.aop.support.AopUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Joue chaque scenario de la video et affiche le resultat reel. Lance seulement avec demo=true
 * (voir executer.sh). Avant chaque scenario, la base revient a 3 claviers, 3 ecrans et aucune commande.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "demo", havingValue = "true")
public class Demo implements ApplicationRunner {

    private final ApplicationContext contexte;
    private final CommandeService commandeService;
    private final Caisse caisse;
    private final CalculPrix calculPrix;
    private final Donnees donnees;

    public Demo(ApplicationContext contexte, CommandeService commandeService, Caisse caisse,
                CalculPrix calculPrix, Donnees donnees) {
        this.contexte = contexte;
        this.commandeService = commandeService;
        this.caisse = caisse;
        this.calculPrix = calculPrix;
        this.donnees = donnees;
    }

    @Override
    public void run(ApplicationArguments args) {
        titre("1. Les beans");
        System.out.println("beans dans le conteneur : " + contexte.getBeanDefinitionCount());
        String[] nos = Arrays.stream(contexte.getBeanDefinitionNames())
                .filter(nom -> {
                    Class<?> type = contexte.getType(nom);
                    return type != null && type.getPackageName().equals("boutique");
                })
                .sorted().toArray(String[]::new);
        System.out.println("dont les notres (" + nos.length + ") : " + String.join(", ", nos));
        System.out.println("le meme objet a chaque demande ? "
                + (contexte.getBean(CommandeService.class) == commandeService));
        System.out.println("panier : le meme objet a chaque demande ? "
                + (contexte.getBean(Panier.class) == contexte.getBean(Panier.class)));
        System.out.println("calculPrix.total(200) = " + calculPrix.total(200));

        titre("2. Ce que Spring injecte vraiment");
        System.out.println("commandeService.getClass() : " + commandeService.getClass().getName());
        System.out.println("c'est un proxy ? " + AopUtils.isAopProxy(commandeService));
        System.out.println("calculPrix.getClass()      : " + calculPrix.getClass().getName());
        System.out.println("c'est un proxy ? " + AopUtils.isAopProxy(calculPrix));

        scenario("3. Sans @Transactional : 5 claviers demandes, 3 en stock", true,
                ids -> commandeService.passerSansTransaction(ids.clavier(), ids.lea(), 5));
        scenario("4. Avec @Transactional : la meme commande", true,
                ids -> commandeService.passer(ids.clavier(), ids.lea(), 5));
        scenario("5. @Transactional et une exception verifiee : 2 ecrans a 300 euros, plafond 500", false,
                ids -> commandeService.passerEtPayer(ids.ecran(), ids.lea(), 2));
        scenario("6. @Transactional(rollbackFor = PaiementRefuse.class) : la meme commande", false,
                ids -> commandeService.passerEtPayerCorrige(ids.ecran(), ids.lea(), 2));
        scenario("7. Le piege : commander() appelle this.passer(), qui est @Transactional", true,
                ids -> commandeService.commander(ids.clavier(), ids.lea(), 5));
        scenario("8. La correction : la Caisse, un autre bean, appelle commandeService.passer()", true,
                ids -> caisse.commander(ids.clavier(), ids.lea(), 5));
    }

    @FunctionalInterface
    private interface Action {
        void lancer(Donnees.Ids ids) throws Exception;
    }

    /** Remet la base a zero, joue l'action, puis affiche le stock de l'article et le nombre de commandes. */
    private void scenario(String texte, boolean clavier, Action action) {
        Donnees.Ids ids = donnees.reinitialiser();
        titre(texte);
        try {
            action.lancer(ids);
            System.out.println("    aucune exception");
        } catch (Exception e) {
            System.out.println("    exception : " + e.getClass().getSimpleName() + " (" + e.getMessage() + ")");
        }
        long article = clavier ? ids.clavier() : ids.ecran();
        System.out.println("apres : stock = " + donnees.stock(article) + ", commandes en base = " + donnees.nombreCommandes());
    }

    private static void titre(String texte) {
        System.out.println();
        System.out.println("== " + texte);
    }
}
