package com.github.kjarosh.sao.dnsga2;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.impl.selection.RankingAndCrowdingSelection;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Phaser;

/**
 * @author Kamil Jarosz
 */
public class PhasedListMigrationCoordinator<S extends Solution<?>> implements ListMigrationCoordinator<S> {
    private final Comparator<S> dominance;
    private final List<Algorithm<List<S>>> parties = new CopyOnWriteArrayList<>();
    private final Phaser phaser = new Phaser();
    private final MultithreadedDominanceRanking<S> ranking;

    private final Set<S> aggregatedSolution = new CopyOnWriteArraySet<>();
    private List<S> selected;

    public PhasedListMigrationCoordinator() {
        this.dominance = new DominanceComparator<>();
        this.ranking = new MultithreadedDominanceRanking<>(dominance);
    }

    @Override
    public List<S> gather(Algorithm<List<S>> party, List<S> solution) {
        int partyNo = parties.indexOf(party);
        if (partyNo < 0) {
            throw new IllegalStateException();
        }

        aggregatedSolution.addAll(solution);
        phaser.arriveAndAwaitAdvance();
        List<S> pop = new ArrayList<>(aggregatedSolution);
//
//        if (isMaster(party)) {
//            ranking.clear();
//        }
//
//        phaser.arriveAndAwaitAdvance();
//
//        ranking.computeRankingMultithreaded(pop, partyNo, parties.size(), phaser);
//
//        phaser.arriveAndAwaitAdvance();
//
// //       System.out.println("Performing migration...");
//        if (isMaster(party)) {
//            selected = selection(pop, ranking);
//        }

        if (isMaster(party)) {
            Ranking<S> ranking = new DominanceRanking<>(dominance);
            ranking.computeRanking(pop);
            selected = selection(pop, ranking);
        }

        phaser.arriveAndAwaitAdvance();
        aggregatedSolution.clear();

        int localSize = selected.size() / parties.size();
        List<S> local = selected.subList(partyNo * localSize, (partyNo + 1) * localSize);
        return new ArrayList<>(local);
    }

    private boolean isMaster(Algorithm<List<S>> party) {
        return parties.get(0) == party;
    }

    private List<S> selection(List<S> population, Ranking<S> ranking) {
        RankingAndCrowdingSelection<S> rankingAndCrowdingSelection =
                new MultithreadedRankingAndCrowdingSelection<>(ranking, population.size() / 2);
        return rankingAndCrowdingSelection.execute(population);
    }

    @Override
    public void register(Algorithm<List<S>> party) {
        if (!parties.contains(party)) {
            parties.add(party);
            phaser.register();
        }
    }
}
