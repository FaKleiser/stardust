/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.util;

import java.io.File;

/**
 * Collection of handy utility methods for coping with files.
 *
 * @author Fabian Keller <dev@fabian-keller.de>
 */
public final class FileUtils {
    /**
     * Private constructor, no instances permitted
     */
    private FileUtils() {
        super();
    }

    /**
     * Returns the file extension of a file
     *
     * @param file to get extension of
     * @return file extension
     * @see http://stackoverflow.com/a/21974043/1262901
     */
    public static String getFileExtension(final File file) {
        return getFileExtension(file.getName());
    }

    /**
     * Returns the file extension of a file
     *
     * @param file to get extension of
     * @return file extension
     * @see http://stackoverflow.com/a/21974043/1262901
     */
    public static String getFileExtension(final String file) {
        final int lastIndexOf = file.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return file.substring(lastIndexOf + 1);
    }
}
