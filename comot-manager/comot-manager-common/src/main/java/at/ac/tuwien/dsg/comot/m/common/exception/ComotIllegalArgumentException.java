package at.ac.tuwien.dsg.comot.m.common.exception;

/**
 * Thrown to indicate that an input for comot was unexpected
 * 
 * @author Juraj
 *
 */
public class ComotIllegalArgumentException extends IllegalArgumentException {

	private static final long serialVersionUID = -4065942560770162499L;

	public ComotIllegalArgumentException(String message) {
		super(message);
	}
}
