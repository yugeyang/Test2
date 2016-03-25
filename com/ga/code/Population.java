
package com.ga.code;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * 种群类：记录每一代的种群
 * @author gxw
 */
public class Population {
    private List<Individual> individuals;//每代种群所包含的个体
    /**
     * 默认构造函数
     */
    public Population() {
        
    }
    
    public Population(int popuSize, int geneLen) {
        individuals  = new ArrayList<Individual>(); //新生种群
        for(int i = 0; i < popuSize; i++) {
            Individual individual = new Individual(geneLen);
            individuals.add(individual);
        }
    }

    /**
     * @return the individuals
     */
    public List<Individual> getIndividuals() {
        return individuals;
    }

    /**
     * @param individuals the individuals to set
     */
    public void setIndividuals(List<Individual> individuals) {
        this.individuals = individuals;
    }
}
