/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.traces;

import java.util.List;


/**
 * Provides the interface that can represent a whole spectra.
 *
 * @param <T>
 *            type used to identify nodes in the system.
 */
public interface ISpectra<T> {

    /**
     * {@inheritDoc}
     */
    public abstract List<INode<T>> getNodes();

    /**
     * Returns the node for the given identifier.
     *
     * If the node is not present in the current spectra, the node will be created.
     *
     * @param identifier
     *            identifier
     * @return the spectra node object for the identifier
     */
    public abstract INode<T> getNode(T identifier);

    /**
     * Checks whether the node with the given identifier is present in the current spectra.
     *
     * @param identifier
     *            of the node
     * @return true if it is present, false otherwise
     */
    public abstract boolean hasNode(T identifier);

    /**
     * Returns a list of all traces available in the spectra.
     *
     * @return traces
     */
    public abstract List<ITrace<T>> getTraces();

    /**
     * Returns all failing traces in this spectra.
     *
     * @return failingTraces
     */
    public abstract List<ITrace<T>> getFailingTraces();

    /**
     * Returns all successful traces in this spectra.
     *
     * @return successfulTraces
     */
    public abstract List<ITrace<T>> getSuccessfulTraces();

}