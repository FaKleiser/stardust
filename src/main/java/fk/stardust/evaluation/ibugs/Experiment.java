/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.evaluation.ibugs;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import fk.stardust.evaluation.IExperiment;
import fk.stardust.localizer.IFaultLocalizer;
import fk.stardust.localizer.Ranking;
import fk.stardust.traces.INode;
import fk.stardust.traces.ISpectra;

/**
 * Class is used to conduct a failure localization experiment using iBugs.
 */
public class Experiment implements IExperiment {

    /** Holds the logger for this class */
    private final Logger log = Logger.getLogger(Experiment.class.getName());
    /** Holds the real fault locations */
    private final IBugsFaultLocations realFaults;

    // // EXPERIMENT SETUP // //

    /** iBugs bug id that was used to create the spectra */
    private final int bugId;
    /** Holds the fault localizer used by this experiment */
    private final IFaultLocalizer<String> localizer;
    /** Holds the spectra to localize faults on */
    private final ISpectra<String> spectra;
    /** True if experiment ran already, false if not */
    private boolean hasRun;


    // // EXPERIMENT RESULTS // //

    /** Holds the produced ranking */
    private Ranking<String> ranking;
    /** Holds the real fault locations */
    private Set<INode<String>> realFaultLocations;



    /**
     * Creates a new experiment.
     *
     * @param bugId
     *            iBugs bug id that was used to create the spectra
     * @param spectra
     *            the spectra to base the experiment on
     * @param localizer
     *            the fault localizer to use
     * @param realFaults
     *            to determine the real fault locations
     */
    public Experiment(final int bugId, final ISpectra<String> spectra, final IFaultLocalizer<String> localizer,
            final IBugsFaultLocations realFaults) {
        this.bugId = bugId;
        this.spectra = spectra;
        this.localizer = localizer;
        this.realFaults = realFaults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void conduct() throws Exception {
        if (this.hasRun) {
            throw new RuntimeException(
                    "Cannot run this experiment multiple times. Please initialize a new experiment instance.");
        }
        this.hasRun = true;

        // localize
        this.log.log(Level.INFO, "Begin: fault localization");
        final long begin = System.currentTimeMillis();
        this.ranking = this.localizer.localize(this.spectra);
        this.log.log(Level.INFO,
                String.format("End: fault localization. Duration: %d ms", System.currentTimeMillis() - begin));
        this.realFaultLocations = this.realFaults.getFaultyNodesFor(this.bugId, this.spectra);
    }

    /**
     * Returns the ranking produced by this experiment.
     *
     * @return the ranking
     */
    public Ranking<String> getRanking() {
        assert this.hasRun;
        return this.ranking;
    }

    /**
     * Returns the real fault location nodes of this experiment.
     *
     * @return the realFaultLocations
     */
    public Set<INode<String>> getRealFaultLocations() {
        assert this.hasRun;
        return this.realFaultLocations;
    }

    /**
     * Returns the fault localizer used by this experiment
     *
     * @return the localizer
     */
    public IFaultLocalizer<String> getLocalizer() {
        return this.localizer;
    }

    /**
     * Returns the bug id used by this experiment
     *
     * @return the bugId
     */
    public int getBugId() {
        return this.bugId;
    }

}
