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
public class CosineDistance extends DistanceMetric {
    
    @Override
    public double getDistance(VectorSpaceCentroid centroid0, VectorSpaceCentroid centroid1) {
        double commonScore = sumCommonScores(centroid0, centroid1);

        if (commonScore == 0) {
            return 0.0;
        }
        double lengthNorm = (centroid0.getVectorLength() * centroid1.getVectorLength());
        
        return commonScore / lengthNorm;
    }
}
