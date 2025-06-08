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
 * Zoltar fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Zoltar<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Zoltar() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double denomPart = (10000d * node.getNF() * node.getIS()) / (double) node.getIF();
        return (double) node.getIF() / (double) (node.getIF() + node.getNF() + node.getIS() + denomPart);
    }

    @Override
    public String getName() {
        return "Zoltar";
    }

}
