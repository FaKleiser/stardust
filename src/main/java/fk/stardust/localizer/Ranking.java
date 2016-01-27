/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.localizer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import fk.stardust.traces.INode;

/**
 * Class used to create a ranking of nodes with corresponding suspiciousness set.
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Ranking<T> implements Iterable<INode<T>> {

    /** Holds the actual ranking */
    protected final TreeSet<RankedElement> rankedNodes = new TreeSet<>(); // NOCS

    /** Holds the nodes with their corresponding suspiciousness */
    protected final Map<INode<T>, Double> nodes = new HashMap<>();

    /** caches the best ranking for each node */
    private Map<INode<T>, Integer> __cacheBestRanking;
    /** caches the worst ranking for each node */
    private Map<INode<T>, Integer> __cacheWorstRanking;

    /**
     * Create a new ranking.
     */
    public Ranking() {
        super();
    }

    /**
     * Adds a node with its suspiciousness to the ranking.
     *
     * @param node
     *            the node to add to the ranking
     * @param suspiciousness
     *            the determined suspiciousness of the node
     */
    public void rank(final INode<T> node, final double suspiciousness) {
        final double s = Double.isNaN(suspiciousness) ? Double.NEGATIVE_INFINITY : suspiciousness;
        this.rankedNodes.add(new RankedElement(node, s));
        this.nodes.put(node, s);
        this.outdateRankingCache();
    }

    /**
     * Returns the suspiciousness of the given node.
     *
     * @param node
     *            the node to get the suspiciousness of
     * @return suspiciousness
     */
    public double getSuspiciousness(final INode<T> node) {
        return this.nodes.get(node);
    }

    /**
     * Computes the wasted effort metric of a node in the ranking.
     *
     * This is equal to the number of nodes that are ranked higher than the given node.
     *
     * @param node
     *            the node to compute the metric for.
     * @return number of nodes ranked higher as the given node.
     */
    public int wastedEffort(final INode<T> node) {
        int position = 0;
        for (final RankedElement element : this.rankedNodes) {
            if (node.equals(element.node)) {
                return position;
            }
            position++;
        }
        throw new IllegalArgumentException(String.format("The ranking does not contain node '%s'.", node.toString()));
    }

    /**
     * Returns all ranking metrics for a given node.
     *
     * @param node
     *            the node to get the metrics for
     * @return metrics
     */
    public RankingMetric getRankingMetrics(final INode<T> node) {
        this.updateRankingCache();
        final Integer bestRanking = this.__cacheBestRanking.get(node);
        final Integer worstRanking = this.__cacheWorstRanking.get(node);
        assert bestRanking != null;
        assert worstRanking != null;
        final double nodeSuspiciousness = this.nodes.get(node);
        return new RankingMetric(node, bestRanking, worstRanking, nodeSuspiciousness);
    }

    /**
     * Outdates the ranking cache
     */
    protected void outdateRankingCache() {
        this.__cacheBestRanking = null;
        this.__cacheWorstRanking = null;
    }

    /**
     * Checks whether the ranking cache is outdated or not
     *
     * @return true if the cache is outdated, false otherwise
     */
    protected boolean isRankingCacheOutdated() {
        return this.__cacheBestRanking == null || this.__cacheWorstRanking == null;
    }

    /**
     * Updates the cached worst case and best case ranking if necessary
     */
    protected void updateRankingCache() {
        if (!this.isRankingCacheOutdated()) {
            return;
        }

        // update best case
        this.__cacheBestRanking = new HashMap<>();
        Integer bestRanking = null;
        int position = 0;
        Double preSuspiciousness = null;
        for (final RankedElement element : this.rankedNodes) {
            position++;
            if (preSuspiciousness == null || preSuspiciousness.compareTo(element.suspicousness) != 0) {
                bestRanking = position;
                preSuspiciousness = element.suspicousness;
            }
            this.__cacheBestRanking.put(element.node, bestRanking);
        }

        // update worst case
        this.__cacheWorstRanking = new HashMap<>();
        Integer worstRanking = null;
        position = this.rankedNodes.size() + 1;
        preSuspiciousness = null;
        for (final RankedElement element : this.rankedNodes.descendingSet()) {
            position--;
            if (preSuspiciousness == null || preSuspiciousness.compareTo(element.suspicousness) != 0) {
                worstRanking = position;
                preSuspiciousness = element.suspicousness;
            }
            this.__cacheWorstRanking.put(element.node, worstRanking);
        }
    }

    /**
     * Creates a new ranking with this ranking and the other ranking merged together.
     *
     * @param other
     *            the other ranking to merge with this ranking
     * @return merged ranking
     */
    public Ranking<T> merge(final Ranking<T> other) {
        final Ranking<T> merged = new Ranking<T>();
        merged.nodes.putAll(this.nodes);
        merged.rankedNodes.addAll(this.rankedNodes);
        merged.nodes.putAll(other.nodes);
        merged.rankedNodes.addAll(other.rankedNodes);
        merged.outdateRankingCache();
        return merged;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<INode<T>> iterator() {
        // mimic RankedElement iterator but pass node objects to the outside
        final Iterator<RankedElement> rankedIterator = this.rankedNodes.iterator();
        return new Iterator<INode<T>>() {

            @Override
            public boolean hasNext() {
                return rankedIterator.hasNext();
            }

            @Override
            public INode<T> next() {
                return rankedIterator.next().node;
            }

            @Override
            public void remove() {
                rankedIterator.remove();
            }
        };
    }

    /**
     * Saves the ranking result to a given file.
     *
     * @param filename
     *            the file name to save the ranking to
     * @throws IOException
     */
    public void save(final String filename) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filename);
            for (final RankedElement el : this.rankedNodes) {
                writer.write(String.format("%s: %f\n", el.node.toString(), el.suspicousness));
            }
        } catch (final Exception e) {
            throw new RuntimeException("Saving the ranking failed.", e);
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    /**
     * Class used to store node and suspiciousness in order to use the {@link SortedSet} interface for actual node
     * ordering.
     */
    protected final class RankedElement implements Comparable<RankedElement> {
        /** Node of the ranked element */
        protected final INode<T> node;
        /** Suspiciousness of the ranked element */
        protected final Double suspicousness;

        private RankedElement(final INode<T> node, final double suspiciousness) {
            super();
            this.node = node;
            this.suspicousness = suspiciousness;
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof Ranking.RankedElement)) {
                return false;
            }
            @SuppressWarnings("unchecked")
            final RankedElement el = (RankedElement) other;
            return this.node.equals(el.node) && this.suspicousness.equals(el.suspicousness);
        }

        @Override
        public int hashCode() {
            return this.node.getIdentifier().hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final RankedElement other) {
            final int compareTo = other.suspicousness.compareTo(this.suspicousness);
            if (compareTo != 0) {
                return compareTo;
            }
            // TODO: as TreeSet consideres compareTo == 0 as equal, we need to ensure all elements have a total order.
            return Integer.valueOf(other.hashCode()).compareTo(this.hashCode());
        }
    }

    /**
     * Holds all ranking information for a node.
     */
    public class RankingMetric {

        /** The node the ranking metris belong to */
        private final INode<T> node;
        /** the best possible ranking of the node */
        private final int bestRanking;
        /** the worst possible ranking of the node */
        private final int worstRanking;
        /** The suspiciousness of the node */
        private final double suspiciousness;

        /**
         * Create the ranking metric for a certain node.
         *
         * @param node
         *            The node the ranking metris belong to
         * @param bestRanking
         *            the best possible ranking of the node
         * @param worstRanking
         *            the worst possible ranking of the node
         * @param suspiciousness
         *            The suspiciousness of the node
         */
        protected RankingMetric(final INode<T> node, final int bestRanking, final int worstRanking,
                final double suspiciousness) {
            this.node = node;
            this.bestRanking = bestRanking;
            this.worstRanking = worstRanking;
            this.suspiciousness = suspiciousness;
        }

        /**
         * Returns the node this metrics belong to
         *
         * @return node
         */
        public INode<T> getNode() {
            return this.node;
        }

        /**
         * Returns the best possible ranking of the node
         *
         * @return bestRanking
         */
        public int getBestRanking() {
            return this.bestRanking;
        }

        /**
         * Returns the worst possible ranking of the node
         *
         * @return worstRanking
         */
        public int getWorstRanking() {
            return this.worstRanking;
        }

        /**
         * Returns the minimum wasted effort that is necessary to find this node with the current ranking.
         *
         * @return minWastedEffort
         */
        public double getMinWastedEffort() {
            return new Double(this.bestRanking - 1) / new Double(Ranking.this.nodes.size());
        }

        /**
         * Returns the maximum wasted effort that is necessary to find this node with the current ranking.
         *
         * @return maxWastedEffort
         */
        public double getMaxWastedEffort() {
            return new Double(this.worstRanking - 1) / new Double(Ranking.this.nodes.size());
        }

        /**
         * Returns the suspiciousness of the node.
         *
         * @return suspiciousness
         */
        public double getSuspiciousness() {
            return this.suspiciousness;
        }


    }

}
