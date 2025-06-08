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
import java.util.HashMap;
import java.util.Map;

import fk.stardust.evaluation.ExperimentRuntimeException;
import fk.stardust.provider.CoberturaProvider;
import fk.stardust.provider.ISpectraProvider;
import fk.stardust.traces.ISpectra;
import fk.stardust.util.FileUtils;

/**
 * Provides spectra using iBugs coverage traces for a specific BugID
 */
public class IBugsSpectraProvider implements ISpectraProvider<String> {

    /** contains the path to the iBugs trace folder */
    private final File root;
    /** contains the path to the trace folder of the specific bugId */
    private final File bugFolder;
    /** contains the bug id this experiment shall run with */
    private final int bugId;
    /** Number of failing traces to load */
    private final Integer failingTraces;
    /** Number of successful traces to load */
    private final Integer successfulTraces;

    /** Once loaded, we cache the spectra */
    private ISpectra<String> __cacheSpectra; // NOCS

    /**
     * Creates a new spectra provider. Take all traces available for the specified bug id
     * 
     * @param root
     *            path to the trace files
     * @param bugId
     *            bug id to run the experiment with
     */
    public IBugsSpectraProvider(final String root, final int bugId) {
        this(root, bugId, null, null);
    }

    /**
     * Creates a new spectra provider. Takes the specified number of traces for the given bug id to create the trace
     * 
     * @param root
     *            path to the trace files
     * @param bugId
     *            bug id to run the experiment with
     * @param failingTraces
     *            the number of required failing traces
     * @param successfulTraces
     *            the number of required successful traces
     */
    public IBugsSpectraProvider(final String root, final int bugId, final Integer failingTraces,
            final Integer successfulTraces) {
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
     * {@inheritDoc}
     */
    @Override
    public ISpectra<String> loadSpectra() throws Exception {
        if (this.__cacheSpectra == null) {
            final CoberturaProvider c = new CoberturaProvider();
            int loadedSuccess = 0;
            int loadedFailure = 0;

            // inject files into cobertura provider
            for (final Map.Entry<String, Boolean> trace : this.traces().entrySet()) {
                if (trace.getValue()) {
                    loadedSuccess++;
                } else {
                    loadedFailure++;
                }
                c.addTraceFile(trace.getKey(), trace.getValue());
            }

            // assert we have enough files loaded
            if (this.failingTraces != null && loadedFailure < this.failingTraces) {
                throw new ExperimentRuntimeException(String.format(
                        "Bug ID '%d' has only %d failing traces, but experiment requires at least %d.", this.bugId,
                        loadedFailure, this.failingTraces));
            }
            if (this.successfulTraces != null && loadedSuccess < this.successfulTraces) {
                throw new ExperimentRuntimeException(String.format(
                        "Bug ID '%d' has only %d successful traces, but experiment requires at least %d.", this.bugId,
                        loadedSuccess, this.successfulTraces));
            }

            // load spectra
            this.__cacheSpectra = c.loadSpectra();
        }
        return this.__cacheSpectra;
    }

    /**
     * Lists all traces of the given version and their corresponding success
     * state.
     * 
     * @return Map of absolute trace file names to their corresponding success
     *         (true) or failure (false) state.
     */
    private Map<String, Boolean> traces() {
        final Map<String, Boolean> traces = new HashMap<>();
        for (final File trace : this.bugFolder.listFiles(pathname -> {
            if (!pathname.isFile()) {
                return false;
            }
            final String fileExtension = FileUtils.getFileExtension(pathname);
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


}
