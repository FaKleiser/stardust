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
 * Goodman fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Goodman<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Goodman() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        return (2.0d * node.getIF() - node.getNF() - node.getIS())
                / (2.0d * node.getIF() + node.getNF() + node.getIS());
    }

    @Override
    public String getName() {
        return "Goodman";
    }

}
