package com.github.kjarosh.sao.dnsga2;

import org.uma.jmetal.algorithm.Algorithm;

/**
 * @author Kamil Jarosz
 */
public interface MigrationCoordinator<S> {
    S gather(Algorithm<S> party, S solution);

    void register(Algorithm<S> party);
}
