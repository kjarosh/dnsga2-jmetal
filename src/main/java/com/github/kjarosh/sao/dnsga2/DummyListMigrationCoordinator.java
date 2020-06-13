package com.github.kjarosh.sao.dnsga2;

import org.uma.jmetal.algorithm.Algorithm;

import java.util.List;

/**
 * @author Kamil Jarosz
 */
public class DummyListMigrationCoordinator<S> implements ListMigrationCoordinator<S> {
    @Override
    public List<S> gather(Algorithm<List<S>> party, List<S> solution) {
        return solution;
    }

    @Override
    public void register(Algorithm<List<S>> party) {
        // do nothing
    }
}
