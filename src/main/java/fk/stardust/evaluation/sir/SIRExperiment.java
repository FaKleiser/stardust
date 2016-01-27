/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.evaluation.sir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

import fk.stardust.localizer.Ranking;
import fk.stardust.traces.INode;
import fk.stardust.traces.ISpectra;
import fk.stardust.traces.Spectra;
import fk.stardust.util.CsvUtils;

public class SIRExperiment {

    private static final String INPUT_DIR = "experiments/SIR/input";
    private static final String OUTPUT_DIR = "experiments/SIR/output";
    private final File faults;

    public static void main(final String[] args) throws IOException {
        new SIRExperiment();
    }

    public SIRExperiment() throws IOException {
        System.out.println("Starting experiment");
        // init real fault locations file
        this.faults = new File(OUTPUT_DIR + "/sir-faults.csv");
        final String[] line = { "Program", "OriginalFile", "NodeID", "BestRanking", "WorstRanking", "MinWastedEffort",
                "MaxWastedEffort", "Suspiciousness", };
        Files.write(this.faults.toPath(),
                (CsvUtils.toCsvLine(line) + System.lineSeparator()).getBytes(Charset.forName("UTF-8")));

        // process each file
        final File input = new File(INPUT_DIR);
        Arrays.stream(input.listFiles((file, name) -> !name.startsWith(".") && name.endsWith(".txt"))).forEach(
                unchecked(file -> {
                    this.localize(file);
                }));
    }

    /**
     * This utility simply wraps a functional interface that throws a checked exception into a Java 8 Consumer
     */
    private static <T> Consumer<T> unchecked(final CheckedConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (final Exception e) { // NOCS
                throw new RuntimeException(e);
            }
        };
    }

    @FunctionalInterface
    private interface CheckedConsumer<T> {
        void accept(T t) throws Exception;
    }

    /**
     * Executes one input file
     *
     * @param input
     *            file
     * @throws IOException
     */
    private void localize(final File input) throws IOException {
        // start processing
        final String name = input.getName().substring(0, input.getName().length() - 4);
        System.out.println("Processing " + name);
        final String program = name.substring(0, name.indexOf("_"));

        // create ranking
        final SIRRankingProvider provider = new SIRRankingProvider(input);
        final Ranking<Integer> ranking = provider.getRanking();
        ranking.save(OUTPUT_DIR + "/" + name + "-ranking.txt");

        // append to faults file
        final Ranking<Integer>.RankingMetric m = ranking.getRankingMetrics(provider.getFault());
        final String[] line = { program, name, provider.getFault().toString(), Integer.toString(m.getBestRanking()),
                Integer.toString(m.getWorstRanking()), Double.toString(m.getMinWastedEffort()),
                Double.toString(m.getMaxWastedEffort()), Double.toString(m.getSuspiciousness()), };
        Files.write(this.faults.toPath(),
                (CsvUtils.toCsvLine(line) + System.lineSeparator()).getBytes(Charset.forName("UTF-8")),
                StandardOpenOption.APPEND);
    }

    class SIRRankingProvider {

        /** Holds the path to the block IF/IP/NF/NP file */
        private final File file;
        /** Contains the actual ranking */
        private Ranking<Integer> ranking;
        /** Contains the node of the real fault location */
        private INode<Integer> fault;

        public SIRRankingProvider(final File file) throws IOException {
            this.file = file;
            this.createRanking();
        }

        private void createRanking() throws IOException {
            // create variables
            final Stream<String> lines = Files.lines(this.file.toPath());
            final ISpectra<Integer> spectra = new Spectra<Integer>();
            final Integer[] failedNode = { null };
            final int[] curNode = { 0 };
            final Ranking<Integer> rank = new Ranking<Integer>();

            // parse lines
            lines.forEachOrdered(line -> {
                if (failedNode[0] == null) {
                    failedNode[0] = Integer.parseInt(line.trim());
                } else {
                    final INode<Integer> node = spectra.getNode(curNode[0]);
                    rank.rank(node, this.tarantula(line));

                    // hack for java not allowing access to non-final variables
                    curNode[0] += 1;
                }
            });

            // close & return
            lines.close();
            this.ranking = rank;
            this.fault = spectra.getNode(failedNode[0]);
        }

        private double tarantula(final String line) {
            // parse line
            final String[] parts = line.split(",");
            assert parts.length == 4;

            final int cIP = Integer.parseInt(parts[0].trim());
            final int cIF = Integer.parseInt(parts[1].trim());
            final int cNP = Integer.parseInt(parts[2].trim());
            final int cNF = Integer.parseInt(parts[3].trim());

            // tarantula
            final double part = new Double(cIF) / new Double(cIF + cNF);
            return part / new Double(part + cIP / new Double(cIP + cNP));
        }

        public Ranking<Integer> getRanking() {
            return this.ranking;
        }

        public INode<Integer> getFault() {
            return this.fault;
        }
    }

}
