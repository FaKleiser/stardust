/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.evaluation.ibugs;


/**
 * Collection of utility methods for coping with iBugs specific issues.
 */
public final class IBugsUtils {
    /**
     * Private constructor, no instances permitted
     */
    private IBugsUtils() {
        super();
    }

    /**
     * In the repository.xml class the file names of the fixed files are given,
     * whereas cobertura prints the real java
     * class names into the XML file. This method converts the file path into
     * its corresponding class name.
     * 
     * @param fileName
     * @return corresponding class name of the file name
     */
    public static String resolveFileName(final String fileName) {
        // remove prefix
        final String[] prefixes = new String[] { "org.aspectj/modules/weaver/src/",
                "org.aspectj/modules/org.aspectj.ajdt.core/src/", "org.aspectj/modules/tests/src/", };
        String file = null;
        for (final String prefix : prefixes) {
            if (fileName.startsWith(prefix)) {
                file = fileName.substring(prefix.length());
            }
        }
        if (file == null && fileName.startsWith("org/aspectj/")) {
            file = fileName;
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
