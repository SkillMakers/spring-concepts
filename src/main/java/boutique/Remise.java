package boutique;

/** Une regle de remise. Interface fonctionnelle : un test peut la remplacer par une lambda. */
@FunctionalInterface
public interface Remise {

    int sur(int montant);
}
