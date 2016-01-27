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
 * Wong3 fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Wong3<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Wong3() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        if (node.getIS() <= 2) {
            return node.getIS();
        } else if (node.getIS() <= 10) {
            return 2.0d + 0.1d * (node.getIS() - 2.0d);
        } else {
            return 2.8d + 0.001d * (node.getIS() - 10.0d);
        }
    }

    @Override
    public String getName() {
        return "Wong3";
    }

}
