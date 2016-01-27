/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.evaluation.ibugs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import fk.stardust.evaluation.IExperiment;
import fk.stardust.localizer.Ranking;
import fk.stardust.localizer.hierarchical.IHierarchicalFaultLocalizer;
import fk.stardust.provider.CoberturaProvider;
import fk.stardust.traces.HierarchicalSpectra;
import fk.stardust.traces.INode;
import fk.stardust.traces.ISpectra;

/**
 * Class is used to conduct a failure localization experiment using iBugs.
 */
public class HierarchicalExperiment implements IExperiment {

    /** Holds the fault localizer used by this experiment */
    private final IHierarchicalFaultLocalizer<String, String> localizer;
    /** contains the path to the iBugs trace folder */
    private final File root;
    /** contains the path to the trace folder of the specific bugId */
    private final File bugFolder;
    /** contains the bug id this experiment shall run with */
    private final int bugId;
    /** Number of failing traces to load */
    private final int failingTraces;
    /** Number of successful traces to load */
    private final int successfulTraces;
    /** Holds a compiled pattern to match diff output for all churned line numbers and ranges */
    private static final Pattern diffLineMatcher = createDiffLineMatcher();

    public HierarchicalExperiment(final IHierarchicalFaultLocalizer<String, String> localizer, final String root,
            final int bugId, final int failingTraces, final int successfulTraces) {
        this.localizer = localizer;
        this.root = new File(root);
        this.bugId = bugId;
        this.bugFolder = new File(this.root.getAbsolutePath() + "/" + bugId + "/pre-fix");
        this.failingTraces = failingTraces;
        this.successfulTraces = successfulTraces;

        // assert folders exist
        if (!this.root.isDirectory()) {
            throw new RuntimeException(String.format("Specified iBugs trace root folder '%s' is not a valid directory",
                    root));
        }
        if (!this.bugFolder.isDirectory()) {
            throw new RuntimeException(String.format(
                    "Specified iBugs trace folder '%s' for bugId '%d' is not a valid directory", root, bugId));
        }
    }

    /**
     * Initializes a regex pattern that extracts affected line numbers of the left file in a diff string.
     *
     * @return
     */
    private static Pattern createDiffLineMatcher() {
        // create regex for diff matching
        final String lineOrRange = "(?<%s>\\d+)(,(?<%s>\\d+))?";
        final String left = String.format(lineOrRange, "LeftBegin", "LeftEnd");
        final String right = String.format(lineOrRange, "RightBegin", "RightEnd");
        return Pattern.compile(String.format("^%s(?<operator>[acd])%s", left, right), Pattern.MULTILINE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void conduct() throws Exception {
        final CoberturaProvider c = new CoberturaProvider();
        int loadedSuccess = 0;
        int loadedFailure = 0;

        // inject files into cobertura provider
        for (final Map.Entry<String, Boolean> trace : this.traces(this.bugId).entrySet()) {
            if (trace.getValue()) {
                loadedSuccess++;
            } else {
                loadedFailure++;
            }
            c.addTraceFile(trace.getKey(), trace.getValue());
        }

        // assert we have enough files loaded
        if (loadedFailure < this.failingTraces) {
            throw new RuntimeException(String.format(
                    "Bug ID '%d' has only %d failing traces, but experiment requires at least %d.", this.bugId,
                    loadedFailure, this.failingTraces));
        }
        if (loadedSuccess < this.successfulTraces) {
            throw new RuntimeException(String.format(
                    "Bug ID '%d' has only %d successful traces, but experiment requires at least %d.", this.bugId,
                    loadedSuccess, this.successfulTraces));
        }

        // load spectra
        final HierarchicalSpectra<String, String> s = c.loadHierarchicalSpectra();

        // localize
        System.out.println("Begin localization");
        final IHierarchicalFaultLocalizer<String, String> t = this.localizer;
        @SuppressWarnings("unchecked")
        final Ranking<String> ranking = (Ranking<String>) t.localize(s);

        // save
        ranking.save("__ranking-hierarchical.txt");
        System.out.println("Saved ranking");

        // create report
        final Set<INode<String>> realFaults = this.getRealFaultLocations(s);
        System.out.println("== Report ==");
        System.out.println(String.format("Node count: %d", s.getNodes().size()));
        System.out.println(String.format("Real Faults: %d", realFaults.size()));

        int maxWastedEffort = 0;
        INode<String> lastExaminedNode = null;
        for (final INode<String> realFault : realFaults) {
            final int we = ranking.wastedEffort(realFault);
            if (we > maxWastedEffort) {
                maxWastedEffort = we;
                lastExaminedNode = realFault;
            }
        }

        System.out.println(String.format("Wasted Effort: %d", maxWastedEffort));
        System.out.println(String.format("Percentage examined: %f", new Double(maxWastedEffort * 100)
        / new Double(s.getNodes().size())));
        System.out.println(String.format("Last examined node: %s", lastExaminedNode.toString()));
    }

    /**
     * Lists all traces of the given version and their corresponding success state.
     *
     * @param version
     *            the version to get all available traces of.
     * @return Map of absolute trace file names to their corresponding success (true) or failure (false) state.
     */
    private Map<String, Boolean> traces(final int version) {
        final Map<String, Boolean> traces = new HashMap<>();
        for (final File trace : this.bugFolder.listFiles((FileFilter) pathname -> {
            if (!pathname.isFile()) {
                return false;
            }
            final String fileExtension = HierarchicalExperiment.this.getFileExtension(pathname);
            if (0 != "xml".compareTo(fileExtension)) {
                return false;
            }
            if (!pathname.getName().matches("^[pf]_.+")) {
                return false;
            }
            return true;
        })) {
            final boolean success = trace.getName().matches("^p_.+");
            traces.put(trace.getAbsolutePath(), success);
        }
        return traces;
    }

    /**
     * Returns the file extension of a file
     *
     * @see http://stackoverflow.com/a/21974043/1262901
     * @param file
     *            to get extension of
     * @return file extension
     */
    private String getFileExtension(final File file) {
        final String name = file.getName();
        final int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf + 1);
    }

    /**
     * Determines the real fault locations of the bugId of this experiment instance.
     *
     * In order to do that, the repository.xml file is being parsed, which contains a "fix-diff". The affected line
     * numbers of that diff are extracted and the corresponding nodes returned.
     *
     * @param spectra
     * @return set of nodes that are the real fault location
     * @throws JDOMException
     * @throws IOException
     */
    private Set<INode<String>> getRealFaultLocations(final ISpectra<String> spectra) throws JDOMException, IOException {
        final Set<INode<String>> locations = new HashSet<>();
        final String repository = this.root.getAbsolutePath() + "/repository.xml";
        final Document doc = new SAXBuilder().build(repository);

        // loop over all packages of the trace file
        for (final Object bugObject : doc.getRootElement().getChildren()) {
            final Element bug = (Element) bugObject;

            // skip irrelevant bugs
            if (this.bugId != Integer.parseInt(bug.getAttributeValue("id"))) {
                continue;
            }

            // get files
            for (final Object fileObject : bug.getChild("fixedFiles").getChildren()) {
                final Element file = (Element) fileObject;
                final String filename = file.getAttributeValue("name");
                final String extension = filename.substring(filename.length() - 5);
                if (extension.compareTo(".java") != 0 || filename.toLowerCase().indexOf("test") != -1) {
                    continue;
                }
                final String className = this.resolveFileName(filename);
                final Set<Integer> lines = findChurnedLinesInDiff(file.getText());

                // get node for each churned line
                for (final int line : lines) {
                    final String nodeName = String.format("%s:%d", className, line);
                    if (!spectra.hasNode(nodeName)) {
                        System.err.println(String.format("Node %s could not be found in spectra.", nodeName));
                        // throw new RuntimeException(String.format("Node %s could not be found in spectra.",
                        // nodeName));
                        continue;
                    }
                    locations.add(spectra.getNode(nodeName));
                }
            }
        }
        return locations;
    }

    /**
     * Extract all affected lines in the left file of a while diff output
     *
     * @param diff
     *            the diff string
     * @return set of churned line numbers
     */
    protected static Set<Integer> findChurnedLinesInDiff(final String diff) {
        final Set<Integer> churned = new TreeSet<>();
        final Matcher m = diffLineMatcher.matcher(diff);
        while (m.find()) {
            // extract affected lines in left file of single diff block, e.g. 146a150,197 ~> 146
            final int begin = Integer.parseInt(m.group("LeftBegin"));
            final int end = m.group("LeftEnd") == null ? begin : Integer.parseInt(m.group("LeftEnd"));
            for (int line = begin; line <= end; line++) {
                churned.add(line);
            }
        }
        return churned;
    }

    /**
     * In the repository.xml class the file names of the fixed files are given, whereas cobertura prints the real java
     * class names into the XML file. This method converts the file path into its corresponding class name.
     *
     * @param fileName
     * @return corresponding class name of the file name
     */
    private String resolveFileName(final String fileName) {
        // remove prefix
        final String[] prefixes = new String[] { "org.aspectj/modules/weaver/src/",
                "org.aspectj/modules/org.aspectj.ajdt.core/src/", "org.aspectj/modules/tests/src/", };
        String file = null;
        for (final String prefix : prefixes) {
            if (fileName.startsWith(prefix)) {
                file = fileName.substring(prefix.length());
            }
        }
        if (file == null) {
            throw new RuntimeException("Filename cannot be resolved to a class name: " + fileName);
        }

        // remove file ending .java
        if (!file.endsWith(".java")) {
            throw new RuntimeException("Expected filename to resolve to end with .java, but found: " + fileName);
        }
        file = file.substring(0, file.length() - 5);

        // transform dashes to dots
        return file.replace("/", ".");
    }


}
