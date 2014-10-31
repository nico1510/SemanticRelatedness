/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distanceMetrics;

import semanticRelatedness.VectorSpaceCentroid;

/**
 *
 * @author nico
 */
public class NormalizedRelevanceDistance extends DistanceMetric {
    
    @Override
    public double getDistance(VectorSpaceCentroid centroid0, VectorSpaceCentroid centroid1) {
        double log0, log1, logCommon, maxlog, minlog;

        log0 = centroid0.getScoreSumLog();
        log1 = centroid1.getScoreSumLog();

        double commonScore = sumCommonScores(centroid0, centroid1);

        if (commonScore == 0) {
            return 0.0;
        }
        logCommon = Math.log(commonScore);
        maxlog = Math.max(log0, log1);
        minlog = Math.min(log0, log1);

        return Math.exp(-1 * (maxlog - logCommon) / (Math.log(centroid0.numDocs()) - minlog));
    }

}
