/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.localizer;

import fk.stardust.traces.INode;

public class NormalizedRanking<T> extends Ranking<T> {

    public enum NormalizationStrategy {
        ZeroOne,

        ReciprocalRank,
    }

    /** Holds the strategy to use */
    private final NormalizationStrategy strategy;
    private Double __suspMax;
    private Double __suspMin;

    public NormalizedRanking(final Ranking<T> toNormalize, final NormalizationStrategy strategy) {
        super();
        this.strategy = strategy;
        this.nodes.putAll(toNormalize.nodes);
        this.rankedNodes.addAll(toNormalize.rankedNodes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSuspiciousness(final INode<T> node) {
        return this.getRankingMetrics(node).getSuspiciousness();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RankingMetric getRankingMetrics(final INode<T> node) {
        final RankingMetric metric = super.getRankingMetrics(node);
        final double susNorm = this.normalizeSuspiciousness(metric);
        return new RankingMetric(metric.getNode(), metric.getBestRanking(), metric.getWorstRanking(), susNorm);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected void outdateRankingCache() {
        super.outdateRankingCache();
        this.__suspMax = null;
        this.__suspMin = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isRankingCacheOutdated() {
        return super.isRankingCacheOutdated() || this.__suspMax == null || this.__suspMin == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateRankingCache() {
        if (!this.isRankingCacheOutdated()) {
            return;
        }
        super.updateRankingCache();
        this.updateSuspMinMax();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ranking<T> merge(final Ranking<T> other) {
        // FIXME: incorrect, need to return instance of NormalizedRanking
        return super.merge(other);
    }

    private double normalizeSuspiciousness(final RankingMetric metric) {
        final double curSusp = metric.getSuspiciousness();
        switch (this.strategy) {
        case ReciprocalRank:
            return 1.0d / metric.getWorstRanking();
        case ZeroOne:
            this.updateRankingCache();

            if (Double.isInfinite(curSusp)) {
                if (curSusp < 0) {
                    return 0.0d;
                } else {
                    return 1.0d;
                }
            }
            if (Double.isNaN(curSusp)) {
                return 0.0d;
            }
            if (this.__suspMax.compareTo(this.__suspMin) == 0) {
                return 0.5d;
            }
            return (curSusp - this.__suspMin) / (this.__suspMax - this.__suspMin);
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }

    private void updateSuspMinMax() {
        // max susp
        double suspMax;
        RankedElement max = this.rankedNodes.first();
        while (max != null && (Double.isNaN(max.suspicousness) || Double.isInfinite(max.suspicousness))) {
            max = this.rankedNodes.higher(max);
        }
        if (max == null) {
            suspMax = 1.0d;
        } else {
            suspMax = max.suspicousness;
        }
        assert !Double.isInfinite(suspMax) && !Double.isNaN(suspMax);

        // min susp
        double suspMin;
        RankedElement min = this.rankedNodes.last();
        while (min != null && (Double.isNaN(min.suspicousness) || Double.isInfinite(min.suspicousness))) {
            min = this.rankedNodes.lower(min);
        }
        if (min == null) {
            suspMin = 1.0d;
        } else {
            suspMin = min.suspicousness;
        }
        assert !Double.isInfinite(suspMin) && !Double.isNaN(suspMin);
        this.__suspMax = suspMax;
        this.__suspMin = suspMin;
    }
}
