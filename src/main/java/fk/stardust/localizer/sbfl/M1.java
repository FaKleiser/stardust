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
 * M1 fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class M1<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public M1() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        return (double) (node.getIF() + node.getNS()) / (double) (node.getNF() + node.getIS());
    }

    @Override
    public String getName() {
        return "M1";
    }

}
