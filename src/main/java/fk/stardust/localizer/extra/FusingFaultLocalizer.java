/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.localizer.extra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fk.stardust.localizer.IFaultLocalizer;
import fk.stardust.localizer.NormalizedRanking;
import fk.stardust.localizer.NormalizedRanking.NormalizationStrategy;
import fk.stardust.localizer.Ranking;
import fk.stardust.localizer.sbfl.Ample;
import fk.stardust.localizer.sbfl.Anderberg;
import fk.stardust.localizer.sbfl.ArithmeticMean;
import fk.stardust.localizer.sbfl.Cohen;
import fk.stardust.localizer.sbfl.Dice;
import fk.stardust.localizer.sbfl.Euclid;
import fk.stardust.localizer.sbfl.Fleiss;
import fk.stardust.localizer.sbfl.GeometricMean;
import fk.stardust.localizer.sbfl.Goodman;
import fk.stardust.localizer.sbfl.Hamann;
import fk.stardust.localizer.sbfl.Hamming;
import fk.stardust.localizer.sbfl.HarmonicMean;
import fk.stardust.localizer.sbfl.Jaccard;
import fk.stardust.localizer.sbfl.Kulczynski1;
import fk.stardust.localizer.sbfl.Kulczynski2;
import fk.stardust.localizer.sbfl.M1;
import fk.stardust.localizer.sbfl.M2;
import fk.stardust.localizer.sbfl.Ochiai;
import fk.stardust.localizer.sbfl.Ochiai2;
import fk.stardust.localizer.sbfl.Overlap;
import fk.stardust.localizer.sbfl.RogersTanimoto;
import fk.stardust.localizer.sbfl.Rogot1;
import fk.stardust.localizer.sbfl.Rogot2;
import fk.stardust.localizer.sbfl.RussellRao;
import fk.stardust.localizer.sbfl.Scott;
import fk.stardust.localizer.sbfl.SimpleMatching;
import fk.stardust.localizer.sbfl.Sokal;
import fk.stardust.localizer.sbfl.SorensenDice;
import fk.stardust.localizer.sbfl.Tarantula;
import fk.stardust.localizer.sbfl.Wong1;
import fk.stardust.localizer.sbfl.Wong2;
import fk.stardust.localizer.sbfl.Wong3;
import fk.stardust.localizer.sbfl.Zoltar;
import fk.stardust.traces.INode;
import fk.stardust.traces.ISpectra;

/**
 * Implements the Fusing Fault Localizers as proposed by Lucia, David Lo and Xin Xia.
 *
 * @param <T>
 */
public class FusingFaultLocalizer<T> implements IFaultLocalizer<T> {

    /** Holds all SBFL to fuse */
    private final List<IFaultLocalizer<T>> sbfl = new ArrayList<>();

    /** Chosen normalization strategy */
    private final NormalizationStrategy normalizationStrategy;
    /** Chosen selection strategy */
    private final SelectionTechnique selectionStrategy;
    /** Chosen data fusion strategy */
    private final DataFusionTechnique fusionStrategy;

    /**
     * Enum representing all available selection techniques
     */
    public enum SelectionTechnique {
        OVERLAP_RATE, BIAS_RATE
    }

    /**
     * Enum representing all available data fusion techniques.
     */
    public enum DataFusionTechnique {
        COMB_SUM, COMB_ANZ, COMB_MNZ
    }

    /**
     * Constructs a fusion fault localizer
     *
     * @param normalization
     *            strategy to use
     * @param selection
     *            strategy to use
     * @param dataFusion
     *            strategy to use
     */
    public FusingFaultLocalizer(final NormalizationStrategy normalization, final SelectionTechnique selection,
            final DataFusionTechnique dataFusion) {
        super();
        this.normalizationStrategy = normalization;
        this.selectionStrategy = selection;
        this.fusionStrategy = dataFusion;

        this.sbfl.add(new Ample<T>());
        this.sbfl.add(new Anderberg<T>());
        this.sbfl.add(new ArithmeticMean<T>());
        this.sbfl.add(new Cohen<T>());
        this.sbfl.add(new Dice<T>());
        this.sbfl.add(new Euclid<T>());
        this.sbfl.add(new Fleiss<T>());
        this.sbfl.add(new GeometricMean<T>());
        this.sbfl.add(new Goodman<T>());
        this.sbfl.add(new Hamann<T>());
        this.sbfl.add(new Hamming<T>());
        this.sbfl.add(new HarmonicMean<T>());
        this.sbfl.add(new Jaccard<T>());
        this.sbfl.add(new Kulczynski1<T>());
        this.sbfl.add(new Kulczynski2<T>());
        this.sbfl.add(new M1<T>());
        this.sbfl.add(new M2<T>());
        this.sbfl.add(new Ochiai<T>());
        this.sbfl.add(new Ochiai2<T>());
        this.sbfl.add(new Overlap<T>());
        this.sbfl.add(new RogersTanimoto<T>());
        this.sbfl.add(new Rogot1<T>());
        this.sbfl.add(new Rogot2<T>());
        this.sbfl.add(new RussellRao<T>());
        this.sbfl.add(new Scott<T>());
        this.sbfl.add(new SimpleMatching<T>());
        this.sbfl.add(new Sokal<T>());
        this.sbfl.add(new SorensenDice<T>());
        this.sbfl.add(new Tarantula<T>());
        this.sbfl.add(new Wong1<T>());
        this.sbfl.add(new Wong2<T>());
        this.sbfl.add(new Wong3<T>());
        this.sbfl.add(new Zoltar<T>());
    }

    @Override
    public String getName() {
        return String.format("F-%s-%s-%s", this.normalizationStrategy.toString(), this.selectionStrategy.toString(),
                this.fusionStrategy.toString());
    }

    @Override
    public Ranking<T> localize(final ISpectra<T> spectra) {
        final Map<IFaultLocalizer<T>, Ranking<T>> sbflRankings = new HashMap<>();
        // create ordinary rankings
        for (final IFaultLocalizer<T> fl : this.sbfl) {
            final Ranking<T> ranking = fl.localize(spectra);
            sbflRankings.put(fl, new NormalizedRanking<T>(ranking, this.normalizationStrategy));
        }

        // compute top-K nodes per ranking metric
        final int k = new Double(spectra.getNodes().size() * 0.1 < 10 ? 10 : spectra.getNodes().size() * 0.1)
        .intValue();
        final Map<IFaultLocalizer<T>, Set<INode<T>>> topK = this.topK(sbflRankings, k);


        // select techniques
        final List<IFaultLocalizer<T>> selected;
        switch (this.selectionStrategy) {
        case OVERLAP_RATE:
            selected = this.selectOverlapBased(spectra, sbflRankings, topK);
            break;
        case BIAS_RATE:
            selected = this.selectBiasBased(spectra, sbflRankings, topK);
            break;
        default:
            throw new RuntimeException("Selection strategy " + this.selectionStrategy.toString()
                    + " not implemented yet");
        }
        assert selected != null && selected.size() > 1;
        System.out.println("Selected " + selected.size());

        // combine
        switch (this.fusionStrategy) {
        case COMB_ANZ:
            return this.fuseCombAnz(spectra, selected, sbflRankings);

        case COMB_SUM:
            return this.fuseCombSum(spectra, selected, sbflRankings);

        default:
            throw new RuntimeException("Data fusion strategy " + this.fusionStrategy.toString()
                    + " not implemented yet");
        }
    }

    /**
     * Extracts the top K ranked nodes from all rankings and puts them in separate sets for each FL.
     *
     * @param rankings
     *            the rankings to extract the top nodes from
     * @param k
     *            number of nodes to extract
     * @return top k
     */
    protected Map<IFaultLocalizer<T>, Set<INode<T>>> topK(final Map<IFaultLocalizer<T>, Ranking<T>> rankings,
            final int k) {
        final Map<IFaultLocalizer<T>, Set<INode<T>>> topK = new HashMap<>();
        for (final IFaultLocalizer<T> fl : rankings.keySet()) {
            final Set<INode<T>> top = new HashSet<>();
            final Ranking<T> ranking = rankings.get(fl);
            for (final INode<T> node : ranking) {
                top.add(node);
                if (top.size() >= k) {
                    break;
                }
            }
            topK.put(fl, top);
        }
        return topK;
    }

    /**
     * Selects half of the input techniques that have as little overlap as possible
     *
     * @param spectra
     *            that backs the rankings
     * @param rankings
     *            rankings of different FL algorithms
     * @param topK
     *            all sets of topK nodes for each FL
     * @return selected algorithms based on ranking node overlap
     */
    protected List<IFaultLocalizer<T>> selectOverlapBased(final ISpectra<T> spectra,
            final Map<IFaultLocalizer<T>, Ranking<T>> rankings, final Map<IFaultLocalizer<T>, Set<INode<T>>> topK) {
        // add set containing all
        final Set<INode<T>> all = new HashSet<>();
        for (final Set<INode<T>> specific : topK.values()) {
            all.addAll(specific);
        }

        // add to sorted map
        final Map<IFaultLocalizer<T>, Double> sortby = new HashMap<>();
        for (final IFaultLocalizer<T> fl : rankings.keySet()) {
            // score
            final double oRate = new Double(all.size() - topK.get(fl).size()) / new Double(all.size());
            sortby.put(fl, oRate);
        }

        // select'em
        return this.selectUsingMap(sortby, 0.5, true);
    }

    /**
     * Selects half of the input techniques that are less similar towards the norm.
     *
     * @param spectra
     *            that backs the rankings
     * @param rankings
     *            rankings of different FL algorithms
     * @param topK
     *            all sets of topK nodes for each FL
     * @return selected algorithms based on ranking node overlap
     */
    protected List<IFaultLocalizer<T>> selectBiasBased(final ISpectra<T> spectra,
            final Map<IFaultLocalizer<T>, Ranking<T>> rankings, final Map<IFaultLocalizer<T>, Set<INode<T>>> topK) {

        // Create L_ALL
        final Map<INode<T>, Integer> lAll = new HashMap<>();
        for (final Set<INode<T>> specific : topK.values()) {
            for (final INode<T> curNode : specific) {
                if (!lAll.containsKey(curNode)) {
                    lAll.put(curNode, 0);
                }
                lAll.put(curNode, lAll.get(curNode) + 1);
            }
        }

        // calculate similarity
        final Map<IFaultLocalizer<T>, Double> bias = new HashMap<>();
        int lAllSum = 0;
        for (final Integer rankedIn : lAll.values()) {
            lAllSum += rankedIn * rankedIn;
        }
        for (final IFaultLocalizer<T> fl : rankings.keySet()) {
            int numSum = 0;
            final int lSum = topK.get(fl).size();
            for (final INode<T> curNode : topK.get(fl)) {
                numSum += lAll.get(curNode);
            }

            bias.put(fl, 1.0d - new Double(numSum) / (Math.sqrt(new Double(lSum)) * Math.sqrt(new Double(lAllSum))));
        }

        // select'em
        return this.selectUsingMap(bias, 0.5, false);
    }

    /**
     * Selects a number of techniques. Sorts the techniques in ascending order by the double values provided in sortby
     * if the asc parameter is set to true
     *
     * @param sortby
     *            the score to sort the techniques by
     * @param n
     *            the percentage of techniques to select
     * @param asc
     *            true to sort ascending, false to sort descending
     * @return selected techniques
     */
    private List<IFaultLocalizer<T>> selectUsingMap(final Map<IFaultLocalizer<T>, Double> sortby, final double n,
            final boolean asc) {
        final List<Map.Entry<IFaultLocalizer<T>, Double>> list = new LinkedList<>(sortby.entrySet());
        Collections.sort(list, (o1, o2) -> {
            if (asc) {
                return o1.getValue().compareTo(o2.getValue());
            } else {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        final Map<IFaultLocalizer<T>, Double> sorted = new LinkedHashMap<>();
        for (final Map.Entry<IFaultLocalizer<T>, Double> entry : list) {
            sorted.put(entry.getKey(), entry.getValue());
        }

        // select
        final List<IFaultLocalizer<T>> selected = new ArrayList<>();
        for (final IFaultLocalizer<T> fl : sorted.keySet()) {
            selected.add(fl);
            if (selected.size() >= sorted.size() * n) {
                break;
            }
        }
        return selected;
    }

    /**
     * Combine scores to final ranking using CombANZ technique
     *
     * @param spectra
     *            spectra ranking is backed on
     * @param selected
     *            selected techniques
     * @param rankings
     *            rankings of all techniques
     * @return new ranking
     */
    protected Ranking<T> fuseCombAnz(final ISpectra<T> spectra, final List<IFaultLocalizer<T>> selected,
            final Map<IFaultLocalizer<T>, Ranking<T>> rankings) {
        final Ranking<T> finalRanking = new Ranking<>();
        for (final INode<T> node : spectra.getNodes()) {
            double sum = 0;
            int nonZero = 0;
            for (final IFaultLocalizer<T> fl : selected) {
                final double score = rankings.get(fl).getSuspiciousness(node);
                sum += score;
                if (score != 0) {
                    nonZero++;
                }
            }
            double finalScore;
            if (nonZero == 0) {
                finalScore = 0.0d;
            } else {
                finalScore = 1.0d / nonZero * sum;
            }

            finalRanking.rank(node, finalScore);
        }
        return finalRanking;
    }

    /**
     * Combine scores to final ranking using CombSUM technique
     *
     * @param spectra
     *            spectra ranking is backed on
     * @param selected
     *            selected techniques
     * @param rankings
     *            rankings of all techniques
     * @return new ranking
     */
    protected Ranking<T> fuseCombSum(final ISpectra<T> spectra, final List<IFaultLocalizer<T>> selected,
            final Map<IFaultLocalizer<T>, Ranking<T>> rankings) {
        final Ranking<T> finalRanking = new Ranking<>();
        for (final INode<T> node : spectra.getNodes()) {
            double finalScore = 0;
            for (final IFaultLocalizer<T> fl : selected) {
                finalScore += rankings.get(fl).getSuspiciousness(node);
            }
            finalRanking.rank(node, finalScore);
        }
        return finalRanking;
    }


    protected Ranking<T> fuseCorrB(final ISpectra<T> spectra, final List<IFaultLocalizer<T>> selected,
            final Map<IFaultLocalizer<T>, Ranking<T>> rankings) {
        return null;
    }

    /**
     * Used to calculate the weighted sum of a set of fault localization techniques to create a new ranking.
     *
     * To be used by correlation based methods.
     *
     * @param spectra
     *            pectra ranking is backed on
     * @param weights
     *            selected techniques and their ranking
     * @param rankings
     *            rankings of all techniques
     * @return new ranking
     */
    private Ranking<T> fuseWeightedSum(final ISpectra<T> spectra, final Map<IFaultLocalizer<T>, Double> weights,
            final Map<IFaultLocalizer<T>, Ranking<T>> rankings) {
        final Ranking<T> ranking = new Ranking<>();
        for (final INode<T> node : spectra.getNodes()) {
            double score = 0;
            for (final IFaultLocalizer<T> fl : weights.keySet()) {
                score += weights.get(fl) + rankings.get(fl).getSuspiciousness(node);
            }
            ranking.rank(node, score);
        }
        return ranking;
    }

}
