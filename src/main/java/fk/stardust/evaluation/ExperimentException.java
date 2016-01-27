/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.evaluation;

/**
 * To be used by STARDUST experiments in case of failure.
 */
public class ExperimentException extends Exception {

    /** serial version UID */
    private static final long serialVersionUID = 1L;

    /**
     * Create exception
     * 
     * @param msg
     *            exception msg
     */
    public ExperimentException(final String msg) {
        super(msg);
    }

    /**
     * Create exception
     * 
     * @param msg
     *            exception msg
     * @param e
     *            pass existing exceptions
     */
    public ExperimentException(final String msg, final Throwable e) {
        super(msg, e);
    }

    /**
     * Create exception
     * 
     * @param e
     *            pass existing exceptions
     */
    public ExperimentException(final Throwable e) {
        super(e);
    }
}
