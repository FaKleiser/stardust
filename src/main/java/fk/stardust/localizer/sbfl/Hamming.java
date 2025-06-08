/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.localizer.sbfl;

import fk.stardust.traces.INode;

/**
 * Hamming fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Hamming<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Hamming() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        return (double) (node.getIF() + node.getNS());
    }

    @Override
    public String getName() {
        return "Hamming";
    }

}
