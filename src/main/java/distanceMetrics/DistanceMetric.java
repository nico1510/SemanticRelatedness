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
public abstract class DistanceMetric {

    public abstract double getDistance(VectorSpaceCentroid centroid0, VectorSpaceCentroid centroid1);

    public static double sumCommonScores(VectorSpaceCentroid centroid0, VectorSpaceCentroid centroid1) {
        double sum = 0;
        double[] c0 = centroid0.getCentroid();
        double[] c1 = centroid1.getCentroid();
        for (int i = 0; i < c0.length && i < c1.length; i++) {
            sum += c1[i] * c0[i];
        }
        return sum;
    }
}
