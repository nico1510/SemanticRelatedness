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
public class ScalarProduct extends DistanceMetric {
    
    @Override
    public double getDistance(VectorSpaceCentroid centroid0, VectorSpaceCentroid centroid1) {
        return sumCommonScores(centroid0, centroid1);
    }
}
