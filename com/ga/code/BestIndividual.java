package com.ga.code;

/**
 * 记录拥有最佳的适应度的个体
 * @author gxw
 */
public class BestIndividual {
    private Individual bestIndiv;
    private double fitness;

    /**
     * @return the bestIndiv
     */
    public Individual getBestIndiv() {
        return bestIndiv;
    }

    /**
     * @param bestIndiv the bestIndiv to set
     */
    public void setBestIndiv(Individual bestIndiv) {
        this.bestIndiv = bestIndiv;
    }

    /**
     * @return the fitness
     */
    public double getFitness() {
        return fitness;
    }

    /**
     * @param fitness the fitness to set
     */
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}
