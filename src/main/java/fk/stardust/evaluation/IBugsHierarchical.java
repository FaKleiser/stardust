/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package fk.stardust.evaluation;

import fk.stardust.evaluation.ibugs.HierarchicalExperiment;
import fk.stardust.localizer.hierarchical.LevelLocalizer;
import fk.stardust.localizer.sbfl.Tarantula;

public class IBugsHierarchical {

    public static final String ZeroR = "weka.classifiers.rules.ZeroR";
    public static final String OneR = "weka.classifiers.rules.OneR";
    public static final String NaiveBayes = "weka.classifiers.bayes.NaiveBayes";
    public static final String RandomTree = "weka.classifiers.trees.RandomTree";
    public static final String RandomForest = "weka.classifiers.trees.RandomForest";
    public static final String J48 = "weka.classifiers.trees.J48";

    public static void main(final String[] args) throws Exception {
        final LevelLocalizer<String, String> ll = new LevelLocalizer<>();
        ll.setLevelLocalizer(0, new Tarantula<>()); // pkg
        ll.setLevelLocalizer(1, new Tarantula<>()); // class
        ll.setLevelLocalizer(2, new Tarantula<>()); // methd
        ll.setLevelLocalizer(3, new Tarantula<>()); // line
        new HierarchicalExperiment(ll, "traces", 156904, 40, 40).conduct();
    }
}
