package ccf.domain.standard;

public class TaxonomyException extends RuntimeException {
    public TaxonomyException(String id,String message) {
        super("Taxonomy id " + id + ":" + message);
    }

    public TaxonomyException(String id,String message, Throwable cause) {
        super("Taxonomy id " + id + ":" + message, cause);
    }   
}
