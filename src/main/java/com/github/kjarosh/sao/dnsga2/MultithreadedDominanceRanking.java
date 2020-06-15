package com.github.kjarosh.sao.dnsga2;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.impl.OverallConstraintViolationComparator;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.GenericSolutionAttribute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Phaser;

public class MultithreadedDominanceRanking<S extends Solution<?>> extends GenericSolutionAttribute<S, Integer> implements Ranking<S> {
    private final Comparator<S> dominanceComparator;
    private static final Comparator<Solution<?>> CONSTRAINT_VIOLATION_COMPARATOR = new OverallConstraintViolationComparator<>();
    private List<ArrayList<S>> rankedSubPopulations;
    private int[] dominateMe;
    private List<List<Integer>> iDominate;
    private ArrayList<List<Integer>> front;

    public MultithreadedDominanceRanking(Comparator<S> comparator) {
        this.dominanceComparator = comparator;
        this.rankedSubPopulations = new ArrayList<>();
    }

    public Ranking<S> computeRanking(List<S> solutionSet) {
        return this;
    }

    public Ranking<S> computeRankingMultithreaded(List<S> solutionSet, int partyNo, int parties, Phaser phaser) {

        if (partyNo == 0) {
            dominateMe = new int[solutionSet.size()];
            iDominate = new ArrayList<>(solutionSet.size());
            front = new ArrayList<>(solutionSet.size() + 1);

            int i;
            for (i = 0; i < solutionSet.size() + 1; ++i) {
                front.add(new CopyOnWriteArrayList<>());
            }

            for (i = 0; i < solutionSet.size(); ++i) {
                iDominate.add(new CopyOnWriteArrayList<>());
                dominateMe[i] = 0;
            }
        }

        phaser.arriveAndAwaitAdvance();

        int i;
        int chunk = (solutionSet.size() - 1) / parties;
        for (i = chunk * partyNo; i < chunk * (partyNo + 1); ++i) {
            for (int q = i + 1; q < solutionSet.size(); ++q) {
                int flagDominate = CONSTRAINT_VIOLATION_COMPARATOR.compare(solutionSet.get(i), solutionSet.get(q));
                if (flagDominate == 0) {
                    flagDominate = this.dominanceComparator.compare(solutionSet.get(i), solutionSet.get(q));
                }

                if (flagDominate == -1) {
                    (iDominate.get(i)).add(q);
                    dominateMe[q]++;
                } else if (flagDominate == 1) {
                    (iDominate.get(q)).add(i);
                    dominateMe[i]++;
                }
            }
        }

        for (i = chunk * partyNo; i < chunk * (partyNo + 1); ++i) {
            if (dominateMe[i] == 0) {
                (front.get(0)).add(i);
                (solutionSet.get(i)).setAttribute(this.getAttributeIdentifier(), 0);
            }
        }
        phaser.arriveAndAwaitAdvance();

        if (partyNo == 0) {
            i = 0;

            int index;
            Iterator<Integer> it1;
            while ((front.get(i)).size() != 0) {
                ++i;
                it1 = (front.get(i - 1)).iterator();

                while (it1.hasNext()) {
                    Iterator<Integer> it2 = (iDominate.get(it1.next())).iterator();

                    while (it2.hasNext()) {
                        index = it2.next();
                        dominateMe[index]--;
                        if (dominateMe[index] == 0) {
                            (front.get(i)).add(index);
                            (solutionSet.get(index)).setAttribute(this.getAttributeIdentifier(), i);
                        }
                    }
                }
            }

            this.rankedSubPopulations = new ArrayList<>();

            for (index = 0; index < i; ++index) {
                this.rankedSubPopulations.add(index, new ArrayList<>((front.get(index)).size()));
                it1 = (front.get(index)).iterator();

                while (it1.hasNext()) {
                    (this.rankedSubPopulations.get(index)).add(solutionSet.get((Integer) it1.next()));
                }
            }
        }

        return this;
    }

    public List<S> getSubfront(int rank) {
        if (rank >= this.rankedSubPopulations.size()) {
            throw new JMetalException("Invalid rank: " + rank + ". Max rank = " + (this.rankedSubPopulations.size() - 1));
        } else {
            return (List<S>) this.rankedSubPopulations.get(rank);
        }
    }

    public void clear() {
        this.rankedSubPopulations.clear();
    }

    public int getNumberOfSubfronts() {
        return this.rankedSubPopulations.size();
    }

}
