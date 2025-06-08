/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.provider;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import fk.stardust.traces.HierarchicalSpectra;
import fk.stardust.traces.ISpectra;
import fk.stardust.traces.IMutableTrace;
import fk.stardust.traces.Spectra;

/**
 * Loads cobertura.xml files to {@link Spectra} objects where each covered line is represented by one node and each file
 * represents one trace in the resulting spectra.
 */
public class CoberturaProvider implements ISpectraProvider<String>, IHierarchicalSpectraProvider<String, String> {

    /** List of trace files to load. Boolean flag indicates whether the trace is successful or not */
    private final Map<String, Boolean> files = new HashMap<>();

    /**
     * Create a cobertura provider.
     */
    public CoberturaProvider() {
        super();
    }

    /**
     * Adds a trace file to the provider.
     *
     * @param file
     *            path to a cobertura xml file
     * @param successful
     *            true if the trace file contains a successful trace, false if the trace file contains a failing trace
     * @throws IOException
     */
    public void addTraceFile(final String file, final boolean successful) throws IOException {
        if (!this.fileToString(file).matches(".*hits=\"[1-9].*")) {
            System.err.println(String.format("Did not add file %s as it did not execute a single node.", file));
            return;
        }
        this.files.put(file, successful);
    }

    private String fileToString(final String filename) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(filename));
        final StringBuilder builder = new StringBuilder();
        String line;

        // For every line in the file, append it to the string builder
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    @Override
    public ISpectra<String> loadSpectra() throws Exception {
        final Spectra<String> spectra = new Spectra<>();
        for (final Map.Entry<String, Boolean> traceFile : this.files.entrySet()) {
            this.loadSingleTrace(traceFile.getKey(), traceFile.getValue(), spectra);
        }
        return spectra;
    }

    /**
     * Loads a single trace file to the given spectra as line spectra.
     *
     * @param spectra
     *            the spectra to add the trace file to
     * @param file
     *            path to the trace xml file to load
     * @param successful
     *            true if the trace file contains a successful trace, false if the trace file contains a failing trace
     * @throws JDOMException
     *             in case the xml file cannot be loaded
     * @throws IOException
     *             in case the xml file cannot be loaded
     */
    private void loadSingleTrace(final String file, final boolean successful, final Spectra<String> spectra)
            throws JDOMException, IOException {
        this.loadSingleTrace(file, successful, spectra, null, null, null);
    }

    /**
     * Loads a single trace file to the given spectra.
     *
     * @param lineSpectra
     *            the spectra to add the trace file to
     * @param file
     *            path to the trace xml file to load
     * @param successful
     *            true if the trace file contains a successful trace, false if the trace file contains a failing trace
     * @throws JDOMException
     *             in case the xml file cannot be loaded
     * @throws IOException
     *             in case the xml file cannot be loaded
     */
    private void loadSingleTrace(final String file, final boolean successful, final Spectra<String> lineSpectra,
            final HierarchicalSpectra<String, String> methodSpectra,
            final HierarchicalSpectra<String, String> classSpectra,
            final HierarchicalSpectra<String, String> packageSpectra) throws JDOMException, IOException {
        final IMutableTrace<String> trace = lineSpectra.addTrace(successful);
        final SAXBuilder saxBuilder = new SAXBuilder();
        final Document doc = saxBuilder.build(file);
        final boolean createHierarchicalSpectra = methodSpectra != null && classSpectra != null
                && packageSpectra != null;

        // loop over all packages of the trace file
        for (final Element pckg : doc.getRootElement().getChild("packages").getChildren()) {
            final String packageName = pckg.getAttributeValue("name");

            // loop over all classes of the package
            for (final Element clss : pckg.getChild("classes").getChildren()) {
                final String className = clss.getAttributeValue("filename");

                // if necessary, create hierarchical spectra
                if (createHierarchicalSpectra) {
                    packageSpectra.setParent(packageName, className);
                }

                // loop over all methods of the class
                for (final Element method : clss.getChild("methods").getChildren()) {
                    final String methodName = method.getAttributeValue("name") + method.getAttributeValue("signature");
                    final String methodIdentifier = String.format("%s:%s", className, methodName);

                    // if necessary, create hierarchical spectra
                    if (createHierarchicalSpectra) {
                        classSpectra.setParent(className, methodIdentifier);
                    }

                    // loop over all lines of the method
                    for (final Element line : method.getChild("lines").getChildren()) {

                        // set node involvement
                        final String lineIdentifier = createNodeIdentifier(className, line.getAttributeValue("number"));
                        final boolean involved = Integer.parseInt(line.getAttributeValue("hits")) > 0;
                        trace.setInvolvement(lineIdentifier, involved);

                        // if necessary, create hierarchical spectra
                        if (createHierarchicalSpectra) {
                            methodSpectra.setParent(methodIdentifier, lineIdentifier);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a node identifier using the given classname and line number
     *
     * @param className
     *            class name of node
     * @param lineNumber
     *            line number of node
     * @return node identifier
     */
    public static String createNodeIdentifier(final String className, final int lineNumber) {
        return createNodeIdentifier(className, String.valueOf(lineNumber));
    }

    /**
     * Creates a node identifier using the given classname and line number
     *
     * @param className
     *            class name of node
     * @param lineNumber
     *            line number of node
     * @return node identifier
     */
    private static String createNodeIdentifier(final String className, final String lineNumber) {
        return String.format("%s:%s", className, lineNumber);
    }

    @Override
    public HierarchicalSpectra<String, String> loadHierarchicalSpectra() throws Exception {
        // create spectras
        final Spectra<String> lineSpectra = new Spectra<>();
        final HierarchicalSpectra<String, String> methodSpectra = new HierarchicalSpectra<>(lineSpectra);
        final HierarchicalSpectra<String, String> classSpectra = new HierarchicalSpectra<>(methodSpectra);
        final HierarchicalSpectra<String, String> packageSpectra = new HierarchicalSpectra<>(classSpectra);

        for (final Map.Entry<String, Boolean> traceFile : this.files.entrySet()) {
            this.loadSingleTrace(traceFile.getKey(), traceFile.getValue(), lineSpectra, methodSpectra, classSpectra,
                    packageSpectra);
        }
        return packageSpectra;
    }
}
