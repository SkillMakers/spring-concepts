package boutique;

/**
 * Exception VERIFIEE : elle herite d'Exception, pas de RuntimeException.
 * Par defaut, @Transactional ne fait PAS de rollback sur ce type d'exception.
 */
public class PaiementRefuse extends Exception {

    public PaiementRefuse(String message) {
        super(message);
    }
}
