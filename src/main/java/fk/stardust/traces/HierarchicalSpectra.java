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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *
 * @param <P>
 *            parent node identifier type
 * @param <C>
 *            child node identifier type
 */
public class HierarchicalSpectra<P, C> extends Spectra<P> {

    /** Holds the child spectra information */
    private final ISpectra<C> childSpectra;

    /** Holds the parent->child node relation */
    Map<INode<P>, Set<INode<C>>> relation = new HashMap<>();

    /** Holds a map of all child traces that are mapped to hierarchical traces of this spectra. */
    Map<ITrace<C>, HierarchicalTrace> traceMap = new HashMap<>();

    /**
     * Creates a new parent spectra object.
     *
     * @param childSpectra
     *            the child spectra to fetch involvement information from
     */
    public HierarchicalSpectra(final ISpectra<C> childSpectra) {
        super();
        this.childSpectra = childSpectra;
    }

    /**
     * Adds childNode as child of parentNode to this hierarchical spectra.
     *
     * @param parentIdentifier
     *            the parent node
     * @param childIdentifier
     *            the child node to be added under the parent node
     */
    public void setParent(final P parentIdentifier, final C childIdentifier) {
        this.setParent(this.getNode(parentIdentifier), this.childSpectra.getNode(childIdentifier));
    }

    /**
     * Adds childNode as child of parentNode to this hierarchical spectra.
     *
     * @param parentNode
     *            the parent node
     * @param childNode
     *            the child node to be added under the parent node
     */
    public void setParent(final INode<P> parentNode, final INode<C> childNode) {
        this.childrenOf(parentNode).add(childNode);
    }

    /**
     * Returns all children of the parent node.
     *
     * @param parent
     *            the parent node to fetch the children of
     * @return all children of the parent
     */
    private Set<INode<C>> childrenOf(final INode<P> parent) {
        if (!this.relation.containsKey(parent)) {
            this.relation.put(parent, new HashSet<INode<C>>());
        }
        return this.relation.get(parent);
    }

    /**
     * Returns all children of the given parent node.
     *
     * The returned set of children is not modifiable.
     *
     * @param parent
     *            the parent node to fetch the children of
     * @return all children of the parent
     */
    public Set<INode<C>> getChildrenOf(final INode<P> parent) {
        return Collections.unmodifiableSet(this.childrenOf(parent));
    }

    @Override
    public IMutableTrace<P> addTrace(final boolean successful) {
        throw new IllegalStateException("Cannot add new trace in hierarchical spectra");
    }

    @Override
    public List<ITrace<P>> getTraces() {
        // if not yet stored add hierarchical traces for all available child traces to this spectra
        if (this.traceMap.size() != this.childSpectra.getTraces().size()) {
            for (final ITrace<C> childTrace : this.childSpectra.getTraces()) {
                if (!this.traceMap.containsKey(childTrace)) {
                    this.traceMap.put(childTrace, new HierarchicalTrace(this, childTrace));
                }
            }
        }

        // Due to some reason no direct cast from Collection<HierarchicalTrace> to Collection<ITrace<P>> is possible
        final List<ITrace<P>> hierarchicalTraces = new ArrayList<>();
        for (final HierarchicalTrace trace : this.traceMap.values()) {
            hierarchicalTraces.add(trace);
        }
        return hierarchicalTraces;
    }

    /**
     * Returns the child spectra of this hierarchical spectra.
     *
     * @return child spectra
     */
    public ISpectra<C> getChildSpectra() {
        return this.childSpectra;
    }

    /**
     * This trace implementation ensures the involvement of all child nodes of a parent node are compiled into a single
     * involvement information.
     */
    private class HierarchicalTrace implements ITrace<P> {

        /** contains the spectra this trace belongs to */
        private final ISpectra<P> spectra;

        /** Holds the associated child trace of this trace */
        private final ITrace<C> childTrace;

        /**
         * Proxy to parent constructor.
         *
         * @param spectra
         * @param successful
         */
        protected HierarchicalTrace(final ISpectra<P> spectra, final ITrace<C> childTrace) {
            super();
            this.spectra = spectra;
            this.childTrace = childTrace;
        }

        @Override
        public boolean isSuccessful() {
            return this.childTrace.isSuccessful();
        }

        @Override
        public ISpectra<P> getSpectra() {
            return this.spectra;
        }

        @Override
        public boolean isInvolved(final INode<P> node) {
            for (final INode<C> childNode : HierarchicalSpectra.this.childrenOf(node)) {
                if (this.childTrace.isInvolved(childNode)) {
                    return true;
                }
            }
            return false;
        }
    }
}
