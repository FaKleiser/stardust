/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.evaluation.lines;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fk.stardust.evaluation.IBugsHierarchical;
import fk.stardust.localizer.IFaultLocalizer;
import fk.stardust.localizer.Ranking;
import fk.stardust.localizer.machinelearn.WekaFaultLocalizer;
import fk.stardust.provider.CoberturaProvider;
import fk.stardust.traces.IMutableTrace;
import fk.stardust.traces.INode;
import fk.stardust.traces.ISpectra;
import fk.stardust.traces.ITrace;
import fk.stardust.traces.Spectra;
import fk.stardust.util.CsvUtils;

/**
 * Evaluates the ranking of given traces by modifying single line involvements.
 */
public final class LineEvaluator {

    /** Result writer */
    private static FileWriter writer;
    /** Performance benchmarking */
    private static Map<String, Long> benchmarks = new HashMap<>();

    /**
     * Don't call this constructor
     */
    private LineEvaluator() {
    }

    /**
     * Performance benchmark method.
     *
     * Call once with an ID to track begin and call it again with the same ID to stop the benchmark. Method will print
     * both invocations to the console.
     *
     * @param id
     *            benchmark identifier
     */
    public static void perf(final String id) {
        if (!benchmarks.containsKey(id)) {
            final long begin = System.currentTimeMillis();
            benchmarks.put(id, begin);
            System.out.println(String.format("-- Begin: %s", id));
        } else {
            final long duration = System.currentTimeMillis() - benchmarks.get(id);
            System.out.println(String.format("-- End: %s, Duration: %fs", id, (double) duration / 1000d));
            benchmarks.remove(id);
        }
    }

    /**
     * Runs experiment
     *
     * @param args
     *            CLI args - dont use
     * @throws Exception
     *             in case the experiment fails
     */
    public static void main(final String[] args) throws Exception {

        // experiment parameters
        final int bugId = 36430;
        final String pathToTraceFolder = "traces";
        final String pathToResultFolder = "experiments";
        final int[] lineIFs = { 1, 3, 5, 10, 25 };
        final int[] lineISs = { 1, 3, 5, 10, 25 };
        final int maxSuccessfulTraces = 25;
        final int maxFailingTraces = 25;
        final IFaultLocalizer<String> localizer = new WekaFaultLocalizer<String>(IBugsHierarchical.NaiveBayes);

        // initialization
        writer = new FileWriter(pathToResultFolder + "/result-" + bugId + ".csv");
        writer.write(CsvUtils.toCsvLine(new String[] { "BugID", "line", "IF", "IS", "NF", "NS", "BestRanking",
                "WorstRanking", "MinWastedEffort", "MaxWastedEffort", "Suspiciousness", })
                + "\n");

        final CoberturaProvider provider = new CoberturaProvider();
        int added = 0;
        boolean success = false;
        for (final String path : traces(pathToTraceFolder + "/" + bugId + "/pre-fix", maxSuccessfulTraces
                + maxFailingTraces)) {
            if (added == maxFailingTraces) {
                success = true;
            }
            provider.addTraceFile(path, success);
            added++;
        }

        final ISpectra<String> original = provider.loadSpectra();
        assert original instanceof Spectra;
        System.out.println("Spectra loaded");
        int line = 0;
        for (final INode<String> node : original.getNodes()) {
            if (line % 100 == 0) {
                System.out.println(String.format("Progress: line %d of %d", line, original.getNodes().size()));
            }
            line++;

            final String identifier = node.getIdentifier();
            // create a clone
            perf("clone");
            final ISpectra<String> spectra = ((Spectra<String>) original).clone();
            perf("clone");

            // set node involvement to none
            for (final ITrace<String> trace : spectra.getTraces()) {
                ((IMutableTrace<String>) trace).setInvolvement(node, false);
            }

            for (final int lineIF : lineIFs) {
                for (final int lineIS : lineISs) {
                    int curIF = 0;
                    int curIS = 0;

                    // set enough traces to either failing or successful
                    for (final ITrace<String> trace : spectra.getTraces()) { // NOCS: sorry nested depth
                        if (trace.isSuccessful() && curIS < lineIS) {
                            ((IMutableTrace<String>) trace).setInvolvement(identifier, true);
                            curIS++;
                        } else if (!trace.isSuccessful() && curIF < lineIF) {
                            ((IMutableTrace<String>) trace).setInvolvement(identifier, true);
                            curIF++;
                        }
                    }

                    // assert we have enough traces of each kind
                    if (curIS != lineIS) {
                        throw new Exception(String.format("Could only involve %d of %d successful traces!", curIS,
                                lineIS));
                    }
                    if (curIF != lineIF) {
                        throw new Exception(String.format("Could only involve %d of %d failing traces!", curIF, lineIF));
                    }

                    // gather ranking position
                    perf("rank");
                    final Ranking<String> ranking = localizer.localize(spectra);
                    perf("rank");
                    final Ranking<String>.RankingMetric metric = ranking.getRankingMetrics(spectra.getNode(identifier));
                    writeLine(bugId, line, curIF, curIS, maxFailingTraces - curIF, maxSuccessfulTraces - curIS,
                            metric.getBestRanking(), metric.getWorstRanking(), metric.getMinWastedEffort(),
                            metric.getMaxWastedEffort(), metric.getSuspiciousness());
                }
            }
        }

        writer.flush();
        writer.close();
    }


    private static void writeLine(final int bugId, final int line, final int lineIF, final int lineIS,
            final int lineNF, final int lineNS, final int bestRanking, final int worstRanking,
            final double minWastedEffort, final double maxWastedEffort, final double suspiciousness) throws IOException {
        assert writer != null;
        final String csv = CsvUtils.toCsvLine(new String[] { Integer.toString(bugId), Integer.toString(line),
                Integer.toString(lineIF), Integer.toString(lineIS), Integer.toString(lineNF), Integer.toString(lineNS),
                Integer.toString(bestRanking), Integer.toString(worstRanking), Double.toString(minWastedEffort),
                Double.toString(maxWastedEffort), Double.toString(suspiciousness), });
        writer.write(csv + "\n");
        writer.flush();
    }

    private static List<String> traces(final String path, final int max) throws Exception {
        final List<String> traceFiles = new ArrayList<>();
        for (final File trace : new File(path).listFiles((FileFilter) pathname -> {
            if (!pathname.isFile()) {
                return false;
            }
            final String fileExtension = getFileExtension(pathname);
            if (0 != "xml".compareTo(fileExtension)) {
                return false;
            }
            if (!pathname.getName().matches("^[pf]_.+")) {
                return false;
            }
            return true;
        })) {
            traceFiles.add(trace.getAbsolutePath());
        }
        if (traceFiles.size() < max) {
            throw new Exception(String.format("Found only %d of %d trace files in %s.", traceFiles.size(), max, path));
        }
        return traceFiles.subList(0, max);
    }

    private static String getFileExtension(final File file) {
        final String name = file.getName();
        final int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf + 1);
    }
}
