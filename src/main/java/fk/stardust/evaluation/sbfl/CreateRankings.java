/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.evaluation.sbfl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.JDOMException;

import fk.stardust.evaluation.ibugs.Experiment;
import fk.stardust.evaluation.ibugs.IBugsFaultLocations;
import fk.stardust.evaluation.ibugs.IBugsSpectraProvider;
import fk.stardust.localizer.IFaultLocalizer;
import fk.stardust.localizer.Ranking;
import fk.stardust.localizer.Ranking.RankingMetric;
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
import fk.stardust.provider.ISpectraProvider;
import fk.stardust.traces.INode;
import fk.stardust.traces.ISpectra;
import fk.stardust.util.CsvUtils;

/**
 * Experiment setup to compute and store the ranking of several bugs of the iBugs AspectJ bug repository with multiple
 * fault localization algorithms.
 */
public class CreateRankings {

    private static final int CONCURRENT_EXPERIMENTS = 2;

    /** Bug IDs to create rankings for */
    private final int[] bugIds;
    /** fault localizers to use in order to create ranking */
    private final List<IFaultLocalizer<String>> faultLocalizers = new ArrayList<>();
    /** Path to results */
    private final String resultPath;
    /** Contains the real fault locations for all iBugs bugs */
    private final IBugsFaultLocations realFaults;

    /** Holds the logger for the experiment executor */
    private final Logger logger = Logger.getLogger(CreateRankings.class.getName());

    private final ISpectraProviderFactory<String> spectraProviderFactory;

    /**
     * Setup experiment
     *
     * @throws IOException
     * @throws JDOMException
     */
    public CreateRankings() throws JDOMException, IOException {
        // settings
        final String tracePath = "traces";
        this.resultPath = "experiments/issta-2015";

        // bug ids to run experiments for
        this.bugIds = new int[] { 28919, 28974, 29186, 29959, 30168, 32463, 33635, 34925, 36430, 36803, 37576, 37739,
                38131, 39626, 39974, 40192, 40257, 40380, 40824, 42539, 42993, 43033, 43194, 43709, 44117, 46298,
                47318, 49657, 50776, 51320, 51929, 52394, 54421, 54965, 55341, 57436, 57666, 58520, 59596, 59895,
                61411, 62227, 64069, 64331, 67592, 69011, 70008, 71377, 71878, 72150, 72528, 72531, 72671, 73433,
                74238, 77799, 81846, 81863, 82134, 82218, 83563, 86789, 87376, 88652, 96371, 99168, 100227, 104218,
                107299, 109016, 109614, 113257, 114875, 115251, 115275, 116626, 116949, 118192, 118715, 118781, 119353,
                119451, 119539, 119543, 120351, 120474, 122370, 122728, 123695, 124654, 124808, 125480, 125699, 125810,
                128128, 128237, 128655, 128744, 129566, 130837, 130869, 131505, 131932, 131933, 132130, 135001, 136665,
                138143, 138219, 138223, 138286, 141956, 142165, 145086, 145693, 145950, 146546, 147701, 148409, 150671,
                151673, 151845, 152257, 152388, 152589, 152631, 153490, 153535, 153845, 154332, 155148, 155972, 156904,
                156962, 158412, 161217, };

        // add file logger
        this.logger.addHandler(new FileHandler(this.resultPath + "-log.txt"));

        // fault localizers to use
        // this.faultLocalizers.add(new
        // FusingFaultLocalizer<String>(NormalizationStrategy.ZeroOne,
        // SelectionTechnique.OVERLAP_RATE, DataFusionTechnique.COMB_ANZ));
        // this.faultLocalizers.add(new
        // FusingFaultLocalizer<String>(NormalizationStrategy.ZeroOne,
        // SelectionTechnique.BIAS_RATE, DataFusionTechnique.COMB_ANZ));
        this.addDefaultFaultLocalizers();

        this.realFaults = new IBugsFaultLocations(tracePath + "/realfaultlocations.xml");

        this.spectraProviderFactory = bugId -> new IBugsSpectraProvider(tracePath, bugId);
    }

    /**
     * Initialize CreateRankings experiment
     *
     * @param spectraProviderFactory
     * @param resultsFolder
     * @param bugIds
     * @param logFile
     * @param faultLocalizers
     * @param realFaultsFile
     * @throws SecurityException
     * @throws IOException
     * @throws JDOMException
     */
    public CreateRankings(final ISpectraProviderFactory<String> spectraProviderFactory, final String resultsFolder,
            final int[] bugIds, final String logFile, final List<IFaultLocalizer<String>> faultLocalizers,
            final String realFaultsFile) throws SecurityException, IOException, JDOMException {
        this.spectraProviderFactory = spectraProviderFactory;
        this.resultPath = resultsFolder;
        this.bugIds = bugIds;
        this.logger.addHandler(new FileHandler(logFile));
        faultLocalizers.addAll(faultLocalizers);
        this.realFaults = new IBugsFaultLocations(realFaultsFile);
    }

    /**
     * Adds a bunch of default fault localizers.
     */
    public void addDefaultFaultLocalizers() {
        this.faultLocalizers.add(new Ample<String>());
        this.faultLocalizers.add(new Anderberg<String>());
        this.faultLocalizers.add(new ArithmeticMean<String>());
        this.faultLocalizers.add(new Cohen<String>());
        this.faultLocalizers.add(new Dice<String>());
        this.faultLocalizers.add(new Euclid<String>());
        this.faultLocalizers.add(new Fleiss<String>());
        this.faultLocalizers.add(new GeometricMean<String>());
        this.faultLocalizers.add(new Goodman<String>());
        this.faultLocalizers.add(new Hamann<String>());
        this.faultLocalizers.add(new Hamming<String>());
        this.faultLocalizers.add(new HarmonicMean<String>());
        this.faultLocalizers.add(new Jaccard<String>());
        this.faultLocalizers.add(new Kulczynski1<String>());
        this.faultLocalizers.add(new Kulczynski2<String>());
        this.faultLocalizers.add(new M1<String>());
        this.faultLocalizers.add(new M2<String>());
        this.faultLocalizers.add(new Ochiai<String>());
        this.faultLocalizers.add(new Ochiai2<String>());
        this.faultLocalizers.add(new Overlap<String>());
        this.faultLocalizers.add(new RogersTanimoto<String>());
        this.faultLocalizers.add(new Rogot1<String>());
        this.faultLocalizers.add(new Rogot2<String>());
        this.faultLocalizers.add(new RussellRao<String>());
        this.faultLocalizers.add(new Scott<String>());
        this.faultLocalizers.add(new SimpleMatching<String>());
        this.faultLocalizers.add(new Sokal<String>());
        this.faultLocalizers.add(new SorensenDice<String>());
        this.faultLocalizers.add(new Tarantula<String>());
        this.faultLocalizers.add(new Wong1<String>());
        this.faultLocalizers.add(new Wong2<String>());
        this.faultLocalizers.add(new Wong3<String>());
        this.faultLocalizers.add(new Zoltar<String>());
    }

    /**
     * Initialize and run experiment
     *
     * @param args
     *            CLI arguments
     * @throws InterruptedException
     *             in case the experiment was interrupted
     * @throws IOException
     *             in case the experiment failed
     * @throws JDOMException
     *             in case the experiment failed
     */
    public static void main(final String[] args) throws InterruptedException, JDOMException, IOException {
        new CreateRankings().run();
    }

    /**
     * Runs all experiments.
     *
     * @throws InterruptedException
     *             in case the experiment was interrupted
     */
    public void run() throws InterruptedException {
        final ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_EXPERIMENTS);
        this.section("Beginning experiments");
        this.text("About to execute " + this.bugIds.length + " experiments.");

        // submit all experiments
        int submitted = 0;
        for (final int bugId : this.bugIds) {
            boolean dontExecute = true;
            for (final IFaultLocalizer<String> fl : this.faultLocalizers) {
                dontExecute &= this.resultExists(bugId, fl.getName());
            }

            if (dontExecute) {
                this.text(String.format("Skipping bug %d, as all results already exist.", bugId));
            } else {
                // create and submit experiment
                executor.submit(new ExperimentExecutor(bugId));
                submitted++;
            }
        }
        this.text("Submitted " + submitted + " of " + this.bugIds.length + " experiments.");

        // await experiment completion
        executor.shutdown();
        if (!executor.awaitTermination(2, TimeUnit.DAYS)) {
            this.logger.log(Level.SEVERE, "Experiment did not complete within 2 days.");
        }
    }

    /**
     * Determines whether the result exists for a certain bug and FL combination
     *
     * @param bugId
     *            the bug id of the experiment
     * @param faultLocalizer
     *            the fault localizer name used by the experiment
     * @return true if the result already exists for the experiment, false otherwise
     */
    public boolean resultExists(final int bugId, final String faultLocalizer) {
        return this.resultsFile(bugId, faultLocalizer, "ranking.csv").exists();
    }

    /**
     * Returns a result file for an experiment and ensures any necessary folders where this file will reside, exist.
     *
     * @param bugId
     *            the bug id of the experiment
     * @param faultLocalizer
     *            the fault localizer name used by the experiment
     * @param filename
     *            the name of the result file
     * @return resultFile
     */
    public File resultsFile(final int bugId, final String faultLocalizer, final String filename) {
        // filepath: <resultFolder>/<FLName>/<bugId>/<filename>
        final File file = new File(String.format("%s/%d/%s/%s", this.resultPath, bugId, faultLocalizer, filename));
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    /**
     * Returns a result file for an experiment and ensures any necessary folders where this file will reside, exist.
     *
     * @param experiment
     *            the experiment to create the result file for
     * @param filename
     *            the name of the result file
     * @return resultFile
     */
    public File resultsFile(final Experiment experiment, final String filename) {
        return this.resultsFile(experiment.getBugId(), experiment.getLocalizer().getName(), filename);
    }

    /**
     * Log a new logical section
     *
     * @param section
     *            section header
     */
    public void section(final String section) {
        this.logger.log(Level.INFO, "=== " + section + " ===");
    }

    /**
     * Log text
     *
     * @param text
     *            to print
     */
    public void text(final String text) {
        this.logger.log(Level.INFO, ">  " + text);
    }

    /**
     * Executes an experiment and saves the results.
     */
    private class ExperimentExecutor implements Runnable {

        /** Holds the experiment to run */
        private final int bugId;
        /** Holds the benchmarks */
        private final Map<String, Long> benchmarks = new HashMap<>();

        /** Initialize executor */
        public ExperimentExecutor(final int bugId) {
            this.bugId = bugId;
        }

        /**
         * Take benchmark
         *
         * @param id
         *            to identify benchmark
         * @return duration or -1 if just created benchmark
         */
        private String bench(final String id) {
            final long now = System.currentTimeMillis();
            if (this.benchmarks.containsKey(id)) {
                // existing benchmark
                final long duration = now - this.benchmarks.get(id);
                this.benchmarks.remove(id);
                return String.format("%f s", new Double(duration / 1000.0d));
            } else {
                this.benchmarks.put(id, now);
                return null;
            }
        }

        @Override
        public void run() {
            this.bench("whole");
            try {
                this.bench("load_spectra");
                CreateRankings.this.logger.log(Level.INFO, String.format("Loading spectra for %d", this.bugId));
                final ISpectraProvider<String> spectraProvider = CreateRankings.this.spectraProviderFactory
                        .factory(this.bugId);
                final ISpectra<String> spectra = spectraProvider.loadSpectra();
                CreateRankings.this.logger.log(Level.INFO,
                        String.format("Loaded spectra for %d in %s", this.bugId, this.bench("load_spectra")));

                // run all SBFL
                for (final IFaultLocalizer<String> fl : CreateRankings.this.faultLocalizers) {
                    // skip if result exists
                    if (CreateRankings.this.resultExists(this.bugId, fl.getName())) {
                        continue;
                    }

                    try {
                        final Experiment experiment = new Experiment(this.bugId, spectra, fl,
                                CreateRankings.this.realFaults);
                        this.bench("single_experiment");
                        this.runSingleExperiment(experiment);
                        CreateRankings.this.logger.log(Level.INFO, String.format(
                                "Finished experiment for SBFL %s with bug id %d in %s", fl.getName(), this.bugId,
                                this.bench("single_experiment")));

                    } catch (final Exception e) { // NOCS
                        CreateRankings.this.logger.log(Level.WARNING, String.format(
                                "Experiments for SBFL %s with bug id %d could not be finished due to exception.",
                                fl.getName(), this.bugId), e);
                    }
                }
            } catch (final Exception e) { // NOCS
                CreateRankings.this.logger.log(Level.WARNING,
                        String.format("Experiments for bug id %d could not be finished due to exception.", this.bugId),
                        e);
            } finally {
                CreateRankings.this.logger.log(Level.INFO,
                        String.format("Finishing all experiments for %d in %s.", this.bugId, this.bench("whole")));
            }
        }

        public void runSingleExperiment(final Experiment experiment) {
            FileWriter rankingWriter = null;
            FileWriter faultWriter = null;
            try {
                CreateRankings.this.logger.log(Level.FINE, "Begin executing experiment");
                experiment.conduct();
                final Ranking<String> ranking = experiment.getRanking();

                final String csvHeader = CsvUtils.toCsvLine(new String[] { "BugID", "Line", "IF", "IS", "NF", "NS",
                        "BestRanking", "WorstRanking", "MinWastedEffort", "MaxWastedEffort", "Suspiciousness", });

                // store ranking
                rankingWriter = new FileWriter(CreateRankings.this.resultsFile(experiment, "ranking.csv"));
                rankingWriter.write(csvHeader + "\n");
                for (final INode<String> node : ranking) {
                    final String metricLine = this.metricToCsvLine(ranking.getRankingMetrics(node), experiment);
                    rankingWriter.write(metricLine + "\n");
                }

                // store metrics of real faults in separate file
                faultWriter = new FileWriter(CreateRankings.this.resultsFile(experiment, "realfaults.csv"));
                faultWriter.write(csvHeader + "\n");
                for (final INode<String> node : experiment.getRealFaultLocations()) {
                    final String metricLine = this.metricToCsvLine(ranking.getRankingMetrics(node), experiment);
                    faultWriter.write(metricLine + "\n");
                }

            } catch (final Exception e) { // NOCS
                CreateRankings.this.logger.log(Level.SEVERE, "Executing experiment failed!", e);
            } finally {
                if (null != rankingWriter) {
                    try {
                        rankingWriter.flush();
                        rankingWriter.close();
                    } catch (final IOException e) {
                        CreateRankings.this.logger.log(Level.WARNING, "Failed closing ranking writer", e);
                    }
                }
                if (null != faultWriter) {
                    try {
                        faultWriter.flush();
                        faultWriter.close();
                    } catch (final IOException e) {
                        CreateRankings.this.logger.log(Level.WARNING, "Failed closing real fault location writer", e);
                    }
                }
                CreateRankings.this.logger.log(Level.FINE, "End executing experiment");
            }
        }

        /**
         * Helper to turn a {@link RankingMetric} into a CSV compatible line.
         *
         * @param m
         *            the metric to convert
         * @return csv line
         */
        private String metricToCsvLine(final Ranking<String>.RankingMetric m, final Experiment experiment) {
            final INode<String> n = m.getNode();
            final String[] parts = new String[] { Integer.toString(experiment.getBugId()), n.getIdentifier(),
                    Integer.toString(n.getIF()), Integer.toString(n.getIS()), Integer.toString(n.getNF()),
                    Integer.toString(n.getNS()), Integer.toString(m.getBestRanking()),
                    Integer.toString(m.getWorstRanking()), Double.toString(m.getMinWastedEffort()),
                    Double.toString(m.getMaxWastedEffort()), Double.toString(m.getSuspiciousness()), };
            return CsvUtils.toCsvLine(parts);
        }

    }

    /**
     * Factories a spectra provider
     *
     * @param <T>
     *            node identifier type
     */
    public interface ISpectraProviderFactory<T> {

        /**
         * Create spectra provider
         *
         * @param bugId
         *            the bug ID to load
         * @return provider
         */
        public ISpectraProvider<T> factory(int bugId);
    }
}
