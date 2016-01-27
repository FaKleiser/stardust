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
 * Represents a single node in a system.
 *
 * @param <T>
 *            type used to identify nodes in the system.
 */
public class Node<T> implements INode<T> {

    /** The identifier of this node */
    private final T identifier;

    /** The spectra this node belongs to */
    private final ISpectra<T> spectra;


    /** Holds the number of traces that were available in the spectra when the cache was created */
    private Integer __cacheTraceCount; // NOCS
    /** cache IF */
    private Integer __cacheIF; // NOCS
    /** cache IS */
    private Integer __cacheIS; // NOCS
    /** cache NF */
    private Integer __cacheNF; // NOCS
    /** cache IS */
    private Integer __cacheNS; // NOCS

    /**
     * Constructs the node
     *
     * @param identifier
     *            the identifier of this node
     * @param spectra
     *            the spectra this node belongs to
     */
    protected Node(final T identifier, final ISpectra<T> spectra) {
        this.identifier = identifier;
        this.spectra = spectra;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fk.stardust.traces.INode#getIdentifier()
     */
    @Override
    public T getIdentifier() {
        return this.identifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fk.stardust.traces.INode#getSpectra()
     */
    @Override
    public ISpectra<T> getSpectra() {
        return this.spectra;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fk.stardust.traces.INode#getNS()
     */
    @Override
    public int getNS() {
        if (this.cacheOutdated() || null == this.__cacheNS) {
            int count = 0;
            for (final ITrace<T> trace : this.spectra.getTraces()) {
                if (trace.isSuccessful() && !trace.isInvolved(this)) {
                    count++;
                }
            }
            this.__cacheNS = count;
        }
        return this.__cacheNS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fk.stardust.traces.INode#getNF()
     */
    @Override
    public int getNF() {
        if (this.cacheOutdated() || null == this.__cacheNF) {
            int count = 0;
            for (final ITrace<T> trace : this.spectra.getTraces()) {
                if (!trace.isSuccessful() && !trace.isInvolved(this)) {
                    count++;
                }
            }
            this.__cacheNF = count;
        }
        return this.__cacheNF;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fk.stardust.traces.INode#getIS()
     */
    @Override
    public int getIS() {
        if (this.cacheOutdated() || null == this.__cacheIS) {
            int count = 0;
            for (final ITrace<T> trace : this.spectra.getTraces()) {
                if (trace.isSuccessful() && trace.isInvolved(this)) {
                    count++;
                }
            }
            this.__cacheIS = count;
        }
        return this.__cacheIS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fk.stardust.traces.INode#getIF()
     */
    @Override
    public int getIF() {
        if (this.cacheOutdated() || null == this.__cacheIF) {
            int count = 0;
            for (final ITrace<T> trace : this.spectra.getTraces()) {
                if (!trace.isSuccessful() && trace.isInvolved(this)) {
                    count++;
                }
            }
            this.__cacheIF = count;
        }
        return this.__cacheIF;
    }

    /**
     * Check if the cache is outdated
     *
     * @return true if the cache is outdated, false otherwise.
     */
    private boolean cacheOutdated() {
        return null == this.__cacheTraceCount || this.__cacheTraceCount != this.spectra.getTraces().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.identifier.toString();
    }
}
