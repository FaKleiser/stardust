/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.localizer.hierarchical;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fk.stardust.localizer.IFaultLocalizer;
import fk.stardust.localizer.Ranking;
import fk.stardust.traces.HierarchicalSpectra;
import fk.stardust.traces.INode;
import fk.stardust.traces.ISpectra;

public class LevelLocalizer<P, C> implements IHierarchicalFaultLocalizer<P, C> {

    /** Holds the fault localizers to use for each level. */
    private final List<IFaultLocalizer<?>> levelLocalizers = new ArrayList<>();

    public LevelLocalizer() {
        super();
    }

    /**
     * Adds a specific localizer for a single level.
     *
     * Level indexing starts at 0 (top level, also passed to localize()) and rises by 1 for each child level.
     *
     * @param level
     *            the level to specify the localizer for
     * @param localizer
     *            the actual localizer
     */
    public void setLevelLocalizer(final int level, final IFaultLocalizer<?> localizer) {
        this.levelLocalizers.add(level, localizer);
    }

    @Override
    public Ranking<?> localize(final HierarchicalSpectra<P, C> spectra) {
        int level = 0;
        ISpectra<?> cur = spectra;
        final List<Ranking<?>> levelRankings = new ArrayList<>();
        while (cur != null) {
            System.out.println(String.format("Lvl: %d, Hash: %d", level, cur.hashCode()));

            // try to create ranking of parent and child levels
            Ranking<?> curRanking;
            try {
                curRanking = this.localize(this.levelLocalizers.get(level), cur);
                levelRankings.add(curRanking);
            } catch (final IndexOutOfBoundsException e) {
                throw new RuntimeException(String.format(
                        "No fault localizer set for level %d of hierarchical spectra.", level), e);
            }

            // go hierarchical
            if (cur instanceof HierarchicalSpectra) {
                cur = ((HierarchicalSpectra<?, ?>) cur).getChildSpectra();
            } else {
                cur = null;
            }
            level++;
        }

        // create ranking
        final Ranking<?> ranking = new Ranking<>();
        this.addRecursive(ranking, spectra, new HashSet<INode<?>>(spectra.getNodes()), levelRankings, 0.0d);
        return ranking;
    }

    private <L> double getSuspiciousness(final Ranking<L> ranking, final INode<?> node) {
        @SuppressWarnings("unchecked")
        final INode<L> real = (INode<L>) node;
        return ranking.getSuspiciousness(real);
    }

    private <L, M> Set<INode<M>> getChildrenof(final HierarchicalSpectra<L, M> children, final INode<?> node) {
        @SuppressWarnings("unchecked")
        final INode<L> real = (INode<L>) node;
        return children.getChildrenOf(real);
    }

    private <L> void rank(final Ranking<L> ranking, final INode<?> node, final double suspiciousness) {
        @SuppressWarnings("unchecked")
        final INode<L> real = (INode<L>) node;
        ranking.rank(real, suspiciousness);
    }

    private void addRecursive(final Ranking<?> finalRanking, final ISpectra<?> spectra, final Set<INode<?>> curNodes,
            final List<Ranking<?>> rankings, final double score) {
        if (spectra instanceof HierarchicalSpectra) {
            // recurse branch - apply this for all children
            final HierarchicalSpectra<?, ?> hSpectra = (HierarchicalSpectra<?, ?>) spectra;
            for (final INode<?> curNode : curNodes) {
                this.addRecursive(finalRanking, hSpectra.getChildSpectra(),
                        new HashSet<INode<?>>(this.getChildrenof(hSpectra, curNode)),
                        rankings.subList(1, rankings.size()), score + this.getSuspiciousness(rankings.get(0), curNode));
            }
        } else {
            // abort branch - add all nodes with score added to their suspiciousness
            for (final INode<?> node : curNodes) {
                this.rank(finalRanking, node, score + this.getSuspiciousness(rankings.get(0), node));
            }
        }
    }

    /**
     * Hope that given fault localizer of level matches with generic type of level spectra.
     *
     * @param localizer
     * @param spectra
     * @return ranking of specific level
     */
    private <L> Ranking<L> localize(final IFaultLocalizer<L> localizer, final ISpectra<?> spectra) {
        @SuppressWarnings("unchecked")
        final ISpectra<L> real = (ISpectra<L>) spectra;
        return localizer.localize(real);
    }

    /**
     * Merges two rankings by hoping that data types fit together.
     *
     * @param one
     * @param two
     * @return merged rankings
     */
    private <M> Ranking<M> merge(final Ranking<M> one, final Ranking<?> two) {
        @SuppressWarnings("unchecked")
        final Ranking<M> real = (Ranking<M>) two;
        return one.merge(real);
    }

}
