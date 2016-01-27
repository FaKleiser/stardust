/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.provider;

import fk.stardust.traces.HierarchicalSpectra;

/**
 * Interface used by classes that load or provide hierarchical spectra objects. Can be used in experiments for
 * automation.
 * 
 * @param <P>
 *            parent node identifier type
 * @param <C>
 *            child node identifier type
 */
public interface IHierarchicalSpectraProvider<P, C> {

    /**
     * Provides a spectra object.
     * 
     * @return spectra with traces and nodes
     * @throws Exception
     *             in case providing the spectra fails
     */
    public HierarchicalSpectra<P, C> loadHierarchicalSpectra() throws Exception;
}
