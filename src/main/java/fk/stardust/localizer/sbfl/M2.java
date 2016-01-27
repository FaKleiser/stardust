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
 * M2 fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class M2<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public M2() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        return new Double(node.getIF())
                / new Double(node.getIF() + node.getNS() + 2.0d * (node.getNF() + node.getIS()));
    }

    @Override
    public String getName() {
        return "M2";
    }

}
