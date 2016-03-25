package com.ga.code;

/**
 * 个体：表示种群中每个个体的基因编码序列
 * @author gxw
 */
public class Individual {
    private boolean[] gene = null;//定义个体基因
    private double fitness;//记录个体的适应度值，用以在判断选择每一代较有的个体进行遗传给下一代
    /**
     * 创建一个随机基因序列的个体
     * @param geneLen:个体基因序列的长度
     */
    public Individual(int geneLen) {
        gene = new boolean[geneLen];//新建基因序列
        for(int i = 0; i < geneLen; i++) {
            if((int) Math.round(Math.random()) == 1) {
                gene[i] = true;
            }
            else {
                gene[i] = false;
            }
        }
    }

    /**
     * @return the gene
     */
    public boolean[] getGene() {
        return gene;
    }

    /**
     * @param gene the gene to set
     */
    public void setGene(boolean[] gene) {
        this.gene = gene;
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
