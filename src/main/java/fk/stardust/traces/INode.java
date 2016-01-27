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
 * Represents a node in the system.
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public interface INode<T> {

    /**
     * Returns the identifier for this node
     *
     * @return the identifier
     */
    public abstract T getIdentifier();

    /**
     * Returns the spectra this node belongs to
     *
     * @return spectra
     */
    public abstract ISpectra<T> getSpectra();

    /**
     * Returns the amount of traces this node was not involved in, but passed.
     *
     * @return amount of traces in spectra
     */
    public abstract int getNS();

    /**
     * Returns the amount of traces this node was not involved in and failed.
     *
     * @return amount of traces in spectra
     */
    public abstract int getNF();

    /**
     * Returns the amount of traces where this node was executed and which passed.
     *
     * @return amount of traces in spectra
     */
    public abstract int getIS();

    /**
     * Returns the amount of traces where this node was executed and which failed.
     *
     * @return amount of traces in spectra
     */
    public abstract int getIF();

    /**
     * Display node identifier as string
     *
     * @return identifying string for this node
     */
    @Override
    public abstract String toString();

}