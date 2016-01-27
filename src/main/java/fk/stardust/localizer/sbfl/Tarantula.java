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
 * Tarantula fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Tarantula<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Tarantula() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        final double part = new Double(node.getIF()) / new Double(node.getIF() + node.getNF());
        return part / new Double(part + node.getIS() / new Double(node.getIS() + node.getNS()));
    }

    @Override
    public String getName() {
        return "tarantula";
    }

}
