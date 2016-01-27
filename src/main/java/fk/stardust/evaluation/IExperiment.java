/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.evaluation;

/**
 * Interface used to mark executable experiments.
 */
public interface IExperiment {

    /**
     * Conducts the experiment and saves the result.
     * 
     * @throws Exception
     *             in case the experiment fails
     */
    public void conduct() throws Exception;
}
