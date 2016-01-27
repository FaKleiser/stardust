/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.util;

import junit.framework.Assert;

import org.testng.annotations.Test;

public class FileUtilsTest {

    @Test
    public void getFileExtension() {
        Assert.assertEquals("java", FileUtils.getFileExtension("sample.java"));
        Assert.assertEquals("java", FileUtils.getFileExtension("path/to/sample.java"));
        Assert.assertEquals("java", FileUtils.getFileExtension("windows\\path\\to\\sample.java"));
        Assert.assertEquals("xml", FileUtils.getFileExtension("path/to/sample.xml"));
        Assert.assertEquals("", FileUtils.getFileExtension("path/to/sample"));
    }
}
