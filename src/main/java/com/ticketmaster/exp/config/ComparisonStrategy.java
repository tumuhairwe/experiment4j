package com.ticketmaster.exp.config;

import java.util.Comparator;

public interface ComparisonStrategy<O> extends Comparator {

    static <O> ComparisonStrategy<O> strictEquality(){
        return new StrictEqualityComparisonStrategy<>();
    }
}
class StrictEqualityComparisonStrategy<O> implements ComparisonStrategy<O> {

    @Override
    public int compare(Object o1, Object o2) {
        return o1.hashCode() == o2.hashCode() ? 0 : 1;
    }
}
