package boutique;

/** Ce dont la boutique a besoin pour encaisser. Les implementations sont des beans interchangeables. */
public interface Paiement {

    void payer(int montant) throws PaiementRefuse;
}
