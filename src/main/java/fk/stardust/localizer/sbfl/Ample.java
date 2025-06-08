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
 * Ample fault localizer
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Ample<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Ample() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double left = (double) node.getIF() / (double) (node.getIF() + node.getNF());
        final double right = (double) node.getIS() / (double) (node.getIS() + node.getNS());
        return Math.abs(left - right);
    }

    @Override
    public String getName() {
        return "Ample";
    }

}
