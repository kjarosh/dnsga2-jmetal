package com.github.kjarosh.sao.dnsga2;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT1;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Kamil Jarosz
 */
public class Example extends AbstractAlgorithmRunner {
    private static final Problem<DoubleSolution> problem =
            new ZDT1();
    private static final CrossoverOperator<DoubleSolution> crossover =
            new SBXCrossover(0.9, 20.0);
    private static final MutationOperator<DoubleSolution> mutation =
            new PolynomialMutation(1.0 / problem.getNumberOfVariables(), 20.0);
    private static final SelectionOperator<List<DoubleSolution>, DoubleSolution> SELECTION =
            new BinaryTournamentSelection<>();

    private static final ListMigrationCoordinator<DoubleSolution> phasedMigrationCoordinator =
            new PhasedListMigrationCoordinator<>();
    private static final ListMigrationCoordinator<DoubleSolution> dummyMigrationCoordinator =
            new DummyListMigrationCoordinator<>();

    private static final int populationSize = 100;
    private static final boolean distributed = true;
    private static final int threads = 4;

    public static void main(String[] args) {
        MigrationCoordinator<List<DoubleSolution>> migrationCoordinator = distributed ?
                phasedMigrationCoordinator :
                dummyMigrationCoordinator;

        List<DoubleSolution> finalPopulation = new CopyOnWriteArrayList<>();
        List<Thread> pool = new ArrayList<>();
        for (int i = 0; i < threads; ++i) {
            final int threadNo = i;
            Algorithm<List<DoubleSolution>> algorithm = new DNSGAII<>(
                    problem,
                    2500,
                    populationSize,
                    populationSize,
                    populationSize,
                    crossover,
                    mutation,
                    SELECTION,
                    new DominanceComparator<>(),
                    new SequentialSolutionListEvaluator<>(),
                    100,
                    migrationCoordinator);
            migrationCoordinator.register(algorithm);

            Thread thread = new Thread(() -> {
                AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
                        .execute();

                List<DoubleSolution> population = algorithm.getResult();
                finalPopulation.addAll(population);
                long computingTime = algorithmRunner.getComputingTime();
                JMetalLogger.logger.info("Total execution time [" + threadNo + "]: " + computingTime + "ms");
            });
            thread.setName("DNSGAII-" + threadNo);
            pool.add(thread);
        }

        pool.forEach(Thread::start);
        pool.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });


        printFinalSolutionSet(finalPopulation);
    }
}
