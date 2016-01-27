/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.localizer.sbfl;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.Test;

import fk.stardust.localizer.Ranking;
import fk.stardust.test.data.SimpleSpectraProvider;
import fk.stardust.traces.ISpectra;

public class TarantulaTest {

    @Test
    public void check() throws Exception {
        final ISpectra<String> s = new SimpleSpectraProvider().loadSpectra();
        final Tarantula<String> fl = new Tarantula<>();
        final Ranking<String> r = fl.localize(s);
        Assert.assertEquals(round(r.getSuspiciousness(s.getNode("S1"))), 0.333);
        Assert.assertEquals(round(r.getSuspiciousness(s.getNode("S2"))), 0.750);
        Assert.assertEquals(round(r.getSuspiciousness(s.getNode("S3"))), 0.429);

        Assert.assertEquals(r.wastedEffort(s.getNode("S1")), 2);
        Assert.assertEquals(r.wastedEffort(s.getNode("S2")), 0);
        Assert.assertEquals(r.wastedEffort(s.getNode("S3")), 1);
    }

    public static double round(final double d) {
        return round(d, 3);
    }

    /**
     * Round double to n decimal places
     * 
     * @see http://stackoverflow.com/a/24780468/1262901
     * 
     * @param d
     * @param decimalPlace
     * @return
     */
    public static double round(final double d, final int decimalPlace) {
        // see the Javadoc about why we use a String in the constructor
        // http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }
}
