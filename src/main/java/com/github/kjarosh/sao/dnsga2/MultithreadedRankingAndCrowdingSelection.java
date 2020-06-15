package com.github.kjarosh.sao.dnsga2;

import org.uma.jmetal.operator.impl.selection.RankingAndCrowdingSelection;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.solutionattribute.Ranking;

import java.util.List;

public class MultithreadedRankingAndCrowdingSelection<S extends Solution<?>> extends RankingAndCrowdingSelection<S> {
    private final Ranking<S> ranking;
    private final int solutionsToSelect;

    public MultithreadedRankingAndCrowdingSelection(Ranking<S> ranking, int solutionsToSelect) {
        super(solutionsToSelect, null);
        this.ranking = ranking;
        this.solutionsToSelect = solutionsToSelect;
    }

    @Override
    public List<S> execute(List<S> solutionList) throws JMetalException {
        if (null == solutionList) {
            throw new JMetalException("The solution list is null");
        } else if (solutionList.isEmpty()) {
            throw new JMetalException("The solution list is empty");
        } else if (solutionList.size() < this.solutionsToSelect) {
            throw new JMetalException("The population size (" + solutionList.size() + ") is smaller thanthe solutions to selected (" + this.solutionsToSelect + ")");
        } else {
            return this.crowdingDistanceSelection(ranking);
        }
    }
}
