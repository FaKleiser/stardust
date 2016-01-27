/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.test.data;

import fk.stardust.provider.ISpectraProvider;
import fk.stardust.traces.IMutableTrace;
import fk.stardust.traces.Spectra;

/**
 * Provides a small and simple spectra for testing.
 * 
 * Test data taken from Table 1 from:
 * 
 * Lee Naish, Hua Jie Lee, and Kotagiri Ramamohanarao. 2011. A model for spectra-based software diagnosis. ACM
 * Trans. Softw. Eng. Methodol. 20, 3, Article 11 (August 2011), 32 pages. DOI=10.1145/2000791.2000795
 * http://doi.acm.org/10.1145/2000791.2000795
 * 
 * @see http://dl.acm.org/citation.cfm?id=2000795
 * 
 * @author Fabian Keller <dev@fabian-keller.de>
 */
public class SimpleSpectraProvider implements ISpectraProvider<String> {

    @Override
    public Spectra<String> loadSpectra() throws Exception {
        final Spectra<String> s = new Spectra<>();

        final IMutableTrace<String> t1 = s.addTrace(false);
        t1.setInvolvement("S1", true);
        t1.setInvolvement("S2", true);

        final IMutableTrace<String> t2 = s.addTrace(false);
        t2.setInvolvement("S2", true);
        t2.setInvolvement("S3", true);

        final IMutableTrace<String> t3 = s.addTrace(true);
        t3.setInvolvement("S1", true);

        final IMutableTrace<String> t4 = s.addTrace(true);
        t4.setInvolvement("S1", true);
        t4.setInvolvement("S2", true);
        t4.setInvolvement("S3", true);

        final IMutableTrace<String> t5 = s.addTrace(true);
        t5.setInvolvement("S1", true);
        t5.setInvolvement("S3", true);

        return s;
    }

}
