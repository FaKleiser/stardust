/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.localizer.hierarchical;

import fk.stardust.localizer.Ranking;
import fk.stardust.traces.HierarchicalSpectra;

/**
 * Interface used to implement fault localization algorithms.
 * 
 * @param <P>
 *            parent node identifier type
 * @param <C>
 *            child node identifier type
 */
public interface IHierarchicalFaultLocalizer<P, C> {

    /**
     * Creates a fault location ranking for all nodes in the given spectra.
     * 
     * @param spectra
     *            the spectra to perform the fault localization on
     * @return nodes ranked by suspiciousness of actually causing the failure
     */
    Ranking<?> localize(HierarchicalSpectra<P, C> spectra);
}
