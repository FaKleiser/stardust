/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.traces;


/**
 * A basic execution trace that provides read-only access.
 *
 * @param <T>
 *            type used to identify nodes in the system.
 */
public interface ITrace<T> {

    /**
     * Returns true if the actual execution of the trace was successful and false if an error occured during execution.
     *
     * @return successful
     */
    public abstract boolean isSuccessful();

    /**
     * Returns the spectra this trace belongs to.
     *
     * @return spectra
     */
    public abstract ISpectra<T> getSpectra();

    /**
     * Checks whether the given node is involved in the current trace.
     *
     * @param node
     *            the node to check
     * @return true if it was involved, false otherwise
     */
    public abstract boolean isInvolved(INode<T> node);

}