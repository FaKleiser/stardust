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

import junit.framework.Assert;

import org.jdom.JDOMException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fk.stardust.evaluation.ibugs.IBugsFaultLocations.Bug;
import fk.stardust.evaluation.ibugs.IBugsFaultLocations.File;
import fk.stardust.evaluation.ibugs.IBugsFaultLocations.Line;
import fk.stardust.evaluation.ibugs.IBugsFaultLocations.Suspiciousness;

public class IBugsFaultLocationsTest {
    private IBugsFaultLocations p;

    @BeforeMethod
    private void setup() throws JDOMException, IOException {
        this.p = new IBugsFaultLocations("src/test/resources/fk/stardust/evaluation/ibugs/rflSample01.xml");
    }

    @AfterMethod
    private void teardown() {
        this.p = null;
    }


    @Test
    public void testNothingInside() {
        final int bugId = 102710;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(0, this.p.getBug(bugId).getFiles().size());
    }

    @Test
    public void testEmptyFile() {
        final int bugId = 102711;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(0, this.p.getBug(bugId).getFiles().size());
    }

    @Test
    public void testValidFilenameButNoLines() {
        final int bugId = 102712;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());
        Assert.assertEquals(0, this.p.getBug(bugId).getFiles().get(0).getLines().size());
    }

    @Test
    public void testInvalidFilenameButLines() {
        final int bugId = 102713;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(0, this.p.getBug(bugId).getFiles().size());
    }

    @Test
    public void testNoFileExtension() {
        final int bugId = 102714;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(0, this.p.getBug(bugId).getFiles().size());
    }

    @Test
    public void testSingleFileSingleLine() {
        final int bugId = 102715;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());

        final File firstFile = this.p.getBug(bugId).getFiles().get(0);
        Assert.assertEquals(1, firstFile.getLines().size());

        final Line firstLine = firstFile.getLines().get(0);
        Assert.assertEquals(1182, firstLine.getLine());
        Assert.assertEquals(Suspiciousness.HIGH, firstLine.getSuspiciousness());
        Assert.assertEquals("is now on else if", firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineEmptyComment() {
        final int bugId = 102716;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());

        final File firstFile = this.p.getBug(bugId).getFiles().get(0);
        Assert.assertEquals(1, firstFile.getLines().size());

        final Line firstLine = firstFile.getLines().get(0);
        Assert.assertEquals(1182, firstLine.getLine());
        Assert.assertEquals(Suspiciousness.HIGH, firstLine.getSuspiciousness());
        Assert.assertEquals("", firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineMissingComment() {
        final int bugId = 102717;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());

        final File firstFile = this.p.getBug(bugId).getFiles().get(0);
        Assert.assertEquals(1, firstFile.getLines().size());

        final Line firstLine = firstFile.getLines().get(0);
        Assert.assertEquals(1182, firstLine.getLine());
        Assert.assertEquals(Suspiciousness.HIGH, firstLine.getSuspiciousness());
        Assert.assertNull(firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineInvalidSuspiciousness() {
        final int bugId = 102718;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());

        final File firstFile = this.p.getBug(bugId).getFiles().get(0);
        Assert.assertEquals(1, firstFile.getLines().size());

        final Line firstLine = firstFile.getLines().get(0);
        Assert.assertEquals(1182, firstLine.getLine());
        Assert.assertEquals(Suspiciousness.UNKNOWN, firstLine.getSuspiciousness());
        Assert.assertEquals("added new else if branch", firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineMissingSuspiciousness() {
        final int bugId = 102719;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());

        final File firstFile = this.p.getBug(bugId).getFiles().get(0);
        Assert.assertEquals(1, firstFile.getLines().size());

        final Line firstLine = firstFile.getLines().get(0);
        Assert.assertEquals(1182, firstLine.getLine());
        Assert.assertEquals(Suspiciousness.UNKNOWN, firstLine.getSuspiciousness());
        Assert.assertEquals("added new else if branch", firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineInvalidLineNumber() {
        final int bugId = 102720;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());

        final File firstFile = this.p.getBug(bugId).getFiles().get(0);
        Assert.assertEquals(0, firstFile.getLines().size());
    }

    @Test
    public void testMultiFileMultiLine() {
        final int bugId = 102721;
        Assert.assertTrue(this.p.hasBug(bugId));
        final Bug bug = this.p.getBug(bugId);
        Assert.assertEquals(2, bug.getFiles().size());
        Assert.assertTrue(bug.hasFile("org/aspectj/weaver/patterns/PointcutRewriter.java"));
        Assert.assertTrue(bug
                .hasFile("org/aspectj/ajdt/internal/compiler/ast/ValidateAtAspectJAnnotationsVisitor.java"));
        Assert.assertFalse(bug.hasFile("nonexisting.java"));

        final File firstFile = bug.getFile("org/aspectj/weaver/patterns/PointcutRewriter.java");
        Assert.assertEquals(2, firstFile.getLines().size());

        final File secondFile = bug
                .getFile("org/aspectj/ajdt/internal/compiler/ast/ValidateAtAspectJAnnotationsVisitor.java");
        Assert.assertEquals(1, secondFile.getLines().size());

    }



}
