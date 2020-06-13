package com.github.kjarosh.sao.dnsga2;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Kamil Jarosz
 */
public class DNSGAII<S extends Solution<?>>
        extends NSGAII<S> {
    private final int migrationGap;
    private final MigrationCoordinator<List<S>> migrationCoordinator;

    public DNSGAII(
            Problem<S> problem,
            int maxEvaluations,
            int populationSize,
            int matingPoolSize,
            int offspringPopulationSize,
            CrossoverOperator<S> crossoverOperator,
            MutationOperator<S> mutationOperator,
            SelectionOperator<List<S>, S> selectionOperator,
            Comparator<S> dominanceComparator,
            SolutionListEvaluator<S> evaluator,
            int migrationGap,
            MigrationCoordinator<List<S>> migrationCoordinator) {
        super(
                problem,
                maxEvaluations,
                populationSize,
                matingPoolSize,
                offspringPopulationSize,
                crossoverOperator,
                mutationOperator,
                selectionOperator,
                dominanceComparator,
                evaluator);
        this.migrationGap = migrationGap;
        this.migrationCoordinator = migrationCoordinator;
    }

    protected void migrate() {
        List<S> migrated = migrationCoordinator.gather(this, population);
        for (int i = 0; migrated.size() < population.size(); ++i) {
            migrated.add(population.get(i));
        }

        population = new ArrayList<>(migrated);
    }

    @Override
    public void updateProgress() {
        super.updateProgress();

        if (evaluations % migrationGap == 0) {
            migrate();
        }
    }
}
