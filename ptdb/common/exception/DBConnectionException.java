package ptdb.common.exception;

/**
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (abijwe)
 *  @Pt.AcceptedRating Red (abijwe)
 * This is an exception class for all the exceptions raised during XML database connection related operations
 * @author abijwe
 *
 */
public class DBConnectionException extends Exception {

    /**
     * Constructor to create a new DBConnectionException with the given message 
     * @param errorMessage - exception message
     */
    public DBConnectionException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructor to wrap other exceptions 
     * @param errorMessage - exception message
     * @param cause
     */
    public DBConnectionException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this._cause = cause;
    }

    /**
     * Returns the underlying cause for the exception
     */
    public Throwable getCause() {
        return this._cause;
    }

    private Throwable _cause;
}
