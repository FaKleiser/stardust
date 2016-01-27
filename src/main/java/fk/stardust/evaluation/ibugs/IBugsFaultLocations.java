/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.evaluation.ibugs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import fk.stardust.provider.CoberturaProvider;
import fk.stardust.traces.INode;
import fk.stardust.traces.ISpectra;
import fk.stardust.util.FileUtils;

/**
 * Used to store the real fault locations of iBugs.
 */
public class IBugsFaultLocations {

    /** Holds the logger for this class */
    private final Logger logger = Logger.getLogger(IBugsFaultLocations.class.getName());

    /** Holds all bugs with their locations */
    private final Map<Integer, Bug> bugs = new TreeMap<>();

    /**
     * Create new real fault locations object
     *
     * @param file
     *            xml file storing the real faults
     * @throws JDOMException
     *             in case we cannot parse the fault file
     * @throws IOException
     *             in case we cannot parse the fault file
     */
    public IBugsFaultLocations(final String file) throws JDOMException, IOException {
        this(new java.io.File(file));
    }

    /**
     * Create new real fault locations object
     *
     * @param file
     *            xml file storing the real faults
     * @throws JDOMException
     *             in case we cannot parse the fault file
     * @throws IOException
     *             in case we cannot parse the fault file
     */
    public IBugsFaultLocations(final java.io.File file) throws JDOMException, IOException {
        this.parse(file);
    }

    /**
     * Parses a real fault locations file.
     *
     * @param faultLocationFile
     *            the file to parse
     * @throws IOException
     * @throws JDOMException
     */
    private void parse(final java.io.File faultLocationFile) throws JDOMException, IOException {
        final Document doc = new SAXBuilder().build(faultLocationFile);

        // loop over all bugs of the real fault locations file
        for (final Object bugObject : doc.getRootElement().getChildren()) {
            final Element bug = (Element) bugObject;

            // create bug object
            Bug curBug;
            try {
                curBug = new Bug(Integer.parseInt(bug.getAttributeValue("id")));
            } catch (final NumberFormatException e1) {
                this.logger.log(Level.INFO,
                        String.format("Could not parse ID while parsing %s", faultLocationFile.getAbsolutePath()), e1);
                continue;
            }
            this.bugs.put(curBug.getId(), curBug);


            // get files
            for (final Object fileObject : bug.getChildren()) {
                final Element file = (Element) fileObject;
                final String filename = file.getAttributeValue("name");

                // ensure we have java extension and no test sources
                if (filename == null || FileUtils.getFileExtension(filename).compareTo("java") != 0
                        || filename.toLowerCase(Locale.getDefault()).indexOf("test") != -1) {
                    continue;
                }

                // create fault file object
                final File curFile = new File(filename);
                curBug.addFile(curFile);


                // get involved lines
                for (final Object lineObj : file.getChildren()) {
                    final Element line = (Element) lineObj;

                    // parse line info
                    int lineNumber;
                    try {
                        lineNumber = Integer.parseInt(line.getText().trim());
                    } catch (final NumberFormatException | NullPointerException e1) {
                        this.logger.log(Level.INFO, String.format("Could not parse line number '%s' while parsing %s",
                                line.getText(), faultLocationFile.getAbsolutePath()), e1);
                        continue;
                    }

                    Suspiciousness suspiciousness = null;
                    try {
                        suspiciousness = Suspiciousness.valueOf(line.getAttributeValue("suspiciousness").trim()
                                .toUpperCase());
                    } catch (final Exception e) { // NOCS
                        this.logger.log(
                                Level.INFO,
                                String.format("Could not parse suspiciousness '%s' while parsing %s",
                                        line.getAttributeValue("suspiciousness"), faultLocationFile.getAbsolutePath()),
                                        e);
                    } finally {
                        if (suspiciousness == null) {
                            suspiciousness = Suspiciousness.UNKNOWN;
                        }
                    }
                    final String comment = line.getAttributeValue("comment");

                    // add it to file
                    curFile.add(new Line(lineNumber, suspiciousness, comment));
                }
            }
        }
    }

    /**
     * Check whether a bug id is contained in this set.
     *
     * @param bugId
     *            the bug id to check
     * @return true if fault info is contained, false otherwise
     */
    public boolean hasBug(final int bugId) {
        return this.bugs.containsKey(bugId);
    }

    /**
     * Returns a certain bug
     *
     * @param bugId
     *            the bug to get
     * @return bug fault info object
     */
    public Bug getBug(final int bugId) {
        return this.bugs.get(bugId);
    }

    /**
     * Returns all fauly nodes of a spectra for a given bug id.
     *
     * @param bugId
     *            the bug id to get the fauly lines of
     * @param spectra
     *            to fetch the nodes from
     * @return list of faulty nodes
     */
    public Set<INode<String>> getFaultyNodesFor(final int bugId, final ISpectra<String> spectra) {
        final Set<INode<String>> locations = new HashSet<>();
        if (!this.hasBug(bugId)) {
            return locations;
        }
        // get node for each churned line
        for (final File file : this.getBug(bugId).getFiles()) {
            for (final Line line : file.getLines()) {
                final String nodeId = CoberturaProvider.createNodeIdentifier(file.getName(), line.getLine());
                if (spectra.hasNode(nodeId)) {
                    locations.add(spectra.getNode(nodeId));
                } else {
                    this.logger.log(Level.WARNING, String.format("Node %s could not be found in spectra.", nodeId));
                }
            }
        }
        return locations;
    }

    /**
     * Represents the suspiciousness of a fault location / the confidence that a given fault location really is a fault.
     */
    public enum Suspiciousness {
        /** high suspiciousness */
        HIGH,
        /** normal suspiciousness */
        NORMAL,
        /** low suspiciousness */
        LOW,
        /** suspiciousness not set */
        UNKNOWN,
    }

    /**
     * Holds all faulty files for a bug
     */
    public class Bug {
        /** the iBugs bug id of the bug */
        private final int id;
        /** all files containing real fault locations for this bug */
        private final Map<String, File> files = new HashMap<>();

        /**
         * Creates a new bug
         *
         * @param bugId
         *            iBugs bug id of this bug
         */
        public Bug(final int bugId) {
            super();
            this.id = bugId;
        }

        /**
         * Adds a faulty file to this bug
         *
         * @param file
         *            to add
         */
        protected void addFile(final File file) {
            this.files.put(file.getName(), file);
        }


        /**
         * return the iBugs bug id for this bug
         *
         * @return the id
         */
        public int getId() {
            return this.id;
        }

        /**
         * Returns all files
         *
         * @return the files
         */
        public List<File> getFiles() {
            return new ArrayList<>(this.files.values());
        }

        /**
         * Check if filename exists
         *
         * @param filename
         *            to check
         * @return true if exists, false otherwise
         */
        public boolean hasFile(final String filename) {
            return this.files.containsKey(filename);
        }

        /**
         * Get file fault info object of certain file
         *
         * @param filename
         *            to get
         * @return file
         */
        public File getFile(final String filename) {
            return this.files.get(filename);
        }

    }

    /**
     * Create a file that contains faults.
     */
    public class File {
        /** file name */
        private final String name;
        /** all fault locations in this file */
        private final Map<Integer, Line> lines = new TreeMap<>();

        /**
         * Construct new file
         *
         * @param name
         *            file name
         */
        protected File(final String name) {
            super();
            this.name = name;
        }

        /**
         * Add line to this file
         *
         * @param line
         *            to add
         */
        protected void add(final Line line) {
            this.lines.put(line.getLine(), line);
        }

        /**
         * returns the filename
         *
         * @return the name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Returns the class name of this file
         *
         * @return class name
         */
        public String getClassName() {
            return IBugsUtils.resolveFileName(this.name);
        }

        /**
         * get all faulty lines of this file
         *
         * @return the lines
         */
        public List<Line> getLines() {
            return new ArrayList<>(this.lines.values());
        }
    }

    /**
     * Represents a real fault location
     */
    public class Line {
        /** line number */
        private final int lineNumber;
        /** suspiciousness of this line / confidence that this line really is a fault */
        private final Suspiciousness suspiciousness;
        /** comment why this line is suspicious */
        private final String comment;

        /**
         * Create new line
         *
         * @param line
         *            line number
         * @param suspiciousness
         *            suspiciousness of this line / confidence that this line really is a fault
         * @param comment
         *            comment why this line is suspicious
         */
        protected Line(final int line, final Suspiciousness suspiciousness, final String comment) {
            super();
            this.lineNumber = line;
            this.suspiciousness = suspiciousness;
            this.comment = comment;
        }

        /**
         * line number
         *
         * @return the line
         */
        public int getLine() {
            return this.lineNumber;
        }

        /**
         * suspiciousness of this line / confidence that this line really is a fault
         *
         * @return the suspiciousness
         */
        public Suspiciousness getSuspiciousness() {
            return this.suspiciousness;
        }

        /**
         * comment why this line is suspicious
         *
         * @return the comment
         */
        public String getComment() {
            return this.comment;
        }


    }

}
