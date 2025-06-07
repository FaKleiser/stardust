/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.traces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The spectra class holds all nodes and traces belonging to the spectra.
 *
 * You can imagine the information accessible through this class has a matrix layout:
 *
 * <pre>
 *          | Trace1 | Trace2 | Trace3 | ... | TraceN |
 *  --------|--------|--------|--------|-----|--------|
 *  Node1   |   1    |   0    |   0    | ... |   1    |
 *  Node2   |   1    |   0    |   1    | ... |   1    |
 *  Node3   |   0    |   1    |   0    | ... |   1    |
 *  ...     |  ...   |  ...   |  ...   | ... |  ...   |
 *  NodeX   |   1    |   1    |   1    | ... |   0    |
 *  --------|--------|--------|--------|-----|--------|
 *  Result  |   1    |   1    |   0    | ... |   0    |
 * </pre>
 *
 * The nodes are the components of a system that are analyed. For each trace the involvement of the node is stored. A
 * '1' denotes node involvement, a '0' denotes no involvement of the node in the current execution trace. For each
 * execution trace we also know whether the execution was successful or not.
 *
 * Given this information it is possible to use this spectra as input for various fault localization techniques.
 *
 * @param <T>
 *            type used to identify nodes in the system.
 */
public class Spectra<T> implements Cloneable, ISpectra<T> {

    /** Holds all nodes belonging to this spectra */
    private final Map<T, Node<T>> nodes = new HashMap<>();

    /** Holds all traces belonging to this spectra */
    private final List<IMutableTrace<T>> traces = new ArrayList<>();

    /**
     * Creates a new spectra.
     */
    public Spectra() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<INode<T>> getNodes() {
        return new ArrayList<>(this.nodes.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public INode<T> getNode(final T identifier) {
        if (!this.nodes.containsKey(identifier)) {
            this.nodes.put(identifier, new Node<T>(identifier, this));
        }
        return this.nodes.get(identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNode(final T identifier) {
        return this.nodes.containsKey(identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ITrace<T>> getTraces() {
        return new ArrayList<>(this.traces);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ITrace<T>> getFailingTraces() {
        final List<ITrace<T>> failingTraces = new ArrayList<>();
        for (final IMutableTrace<T> trace : this.traces) {
            if (!trace.isSuccessful()) {
                failingTraces.add(trace);
            }
        }
        return failingTraces;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ITrace<T>> getSuccessfulTraces() {
        final List<ITrace<T>> successTraces = new ArrayList<>();
        for (final IMutableTrace<T> trace : this.traces) {
            if (trace.isSuccessful()) {
                successTraces.add(trace);
            }
        }
        return successTraces;
    }

    /**
     * Adds a new trace to this spectra.
     *
     * @param successful
     *            True if the trace execution was successful, false otherwise
     * @return the trace object
     */
    public IMutableTrace<T> addTrace(final boolean successful) {
        final Trace<T> trace = new Trace<>(this, successful);
        this.traces.add(trace);
        return trace;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Spectra<T> clone() throws CloneNotSupportedException {
        return (Spectra<T>) super.clone();
    }
}
