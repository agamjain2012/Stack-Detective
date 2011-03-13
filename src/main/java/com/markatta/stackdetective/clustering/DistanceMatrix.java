package com.markatta.stackdetective.clustering;

import com.markatta.stackdetective.distance.DistanceCalculator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Dynamic distance matrix, that grows as elements are added. Keeps
 * the respektive distance maps sorted on distance at all times.
 * 
 * @author johan
 */
public class DistanceMatrix<T> {

    private final Map<T, SortedSet<Distance<T>>> distances = new HashMap<T, SortedSet<Distance<T>>>();

    private final DistanceCalculator<T> calculator;

    /**
     * @param calculator A calculator used for calculating the actual distances.
     */
    public DistanceMatrix(DistanceCalculator<T> calculator) {
        this.calculator = calculator;
    }

    public void add(T toAdd) {
        Set<Distance<T>> distancesForThis = getOrCreateDistanceSetFor(toAdd);

        // always 0 from itself
        distancesForThis.add(new Distance<T>(toAdd, toAdd, 0));


        for (T other : distances.keySet()) {
            if (other == toAdd) {
                // small optimization, never calculate distance to itself
                continue;
            }
            Set<Distance<T>> distancesForOther = getOrCreateDistanceSetFor(other);

            // from this to the other
            int distance = calculator.calculateDistance(toAdd, other);
            distancesForThis.add(new Distance<T>(toAdd, other, distance));

            // from other to this
            distance = calculator.calculateDistance(other, toAdd);
            distancesForOther.add(new Distance<T>(other, toAdd, distance));
        }

    }

    /**
     * Get the map of distances from the given stack trace and every other stacktrace.
     * If the map does not exist previously it is created and added to the distances map.
     */
    private SortedSet<Distance<T>> getOrCreateDistanceSetFor(T item) {
        if (!distances.containsKey(item)) {
            distances.put(item, new TreeSet<Distance<T>>());
        }

        return distances.get(item);
    }

    public SortedSet<Distance<T>> getDistancesFrom(T item) {
        return distances.get(item);
    }

    public int getDistanceBetween(T a, T b) {
        if (!distances.containsKey(a)) {
            throw new IllegalArgumentException("StackTrace a has not been added to the matrix");
        }
        if (!distances.containsKey(b)) {
            throw new IllegalArgumentException("StackTrace b has not been added to the matrix");
        }

        SortedSet<Distance<T>> distancesFromA = getDistancesFrom(a);
        for (Distance<T> distance : distancesFromA) {
            if (distance.getTo() == b) {
                return distance.getDistance();
            }
        }

        throw new IllegalStateException("Should not be possible, could not find the distance between a and b!?!");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + distances + "]";
    }
}