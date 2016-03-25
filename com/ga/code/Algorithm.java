package com.ga.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 算法运行类
 * @author gxw
 */
public class Algorithm {
    private int popuSize;//种群个体数
    private double crossRatio;//个体基因交叉率
    private double mutaRatio; //个体基因变异率
    private int iterLimits; //最多迭代次数
    private List<String> paramList = null;
    private int paramCounts[];
    private Set<String> combinationSet;// 记录两两参数值的原始组合
    private List<String[]> paramValList;//参数值数组
    private List<String> resultList = new ArrayList<String>();//生成测试用例集合
    private int geneLen; //记录个体基因的长度 
    private Population population;//种群
    private BestIndividual bestIndividual;//记录最佳的个体
    private int judgeTimes = 0;//每一轮中稳定的次数， 用以结束当前轮的用例筛选
    private int bestTimes = 0;
    private double lastMaxFitness = 0.0;
    /**
     * 初始化运行数据操作
     * @param paramMap：参数集
     */
    public boolean init(Map paramMap) {
        popuSize = Integer.parseInt(paramMap.get("popuSize").toString());
        crossRatio = Double.parseDouble(paramMap.get("crossRatio").toString());
        mutaRatio = Double.parseDouble(paramMap.get("mutaRatio").toString());
        iterLimits = Integer.parseInt(paramMap.get("iterLimits").toString());
        judgeTimes = Integer.parseInt(paramMap.get("judgeTimes").toString());
        combinationSet = new HashSet<String>();
        paramList = (List<String>)paramMap.get("paramList");
	int paramCount = paramList.size();//获取到参数的个数
	paramCounts = new int[paramCount];//记录每个参数的解空间个数
	paramValList = new ArrayList<String[]>();
	int index = 0;
	for(String line: paramList) {
            String paramName = "";
            String paramValues[] = null;
            if(line.contains(":")) {//英文冒号，两种冒号用以防止写错标识，便于得到正确的字符串切割
             paramName = line.split(":")[0];
	     paramValues = line.split(":")[1].split(" ");
            }
            else if(line.contains("：")) { //中文冒号
                 paramName = line.split("：")[0];
	     paramValues = line.split("：")[1].split(" ");
            }
            else {
                return false;
            }
                
	    paramCounts[index++] = paramValues.length;
	    paramValList.add(paramValues);
	}
	createCombinationSet(paramValList);//构造两两原始组合
        geneLen = geneCoding(paramCounts);//获取个体基因编码的基因长度
        bestIndividual = new BestIndividual();
        return true;
    }
    
        /**
     * @param paramMap:包括遗传算法计算所需要的参数和原始测试参数数据
     * @return 返回计算得到的数据集合
     */
    public List<String> run(Map paramMap) {
	
        //初始遗传算法需要的参数以及完成参数的表现型到基因的转换 
        if(init(paramMap) == false) {
            return null;
        }
        
        while(combinationSet.size() > 0) { //每次循环产生一个用例测试并且删除原始两两组合集中被该用例所包含的部分，直到原始两两集合完全被所发现的测试用例覆盖
            population = new Population(popuSize, geneLen);//初始化新的一代种群
            int iterTimes = iterLimits;
            List<Individual> individuals = population.getIndividuals();
            while (iterTimes-- > 0) {
            // 打乱种群的顺序,以便于随机进行染色体交换
            Collections.shuffle(individuals);
            for (int i = 0; i < popuSize - 1; i += 2) {
                // 交叉
                cross(individuals.get(i), individuals.get(i + 1));
                // 变异
                mutation(individuals.get(i));
            }
            // 种群更替
            if (selection() == true) {
                break;
             }
           }
          updateCombinationSet();//根据当前选择的最好的测试用例，删除两两原始集合中被包含的项集
        }
        return resultList;
    }
    
    
     /**
     * 交叉操作
     * @param indiv1：个体1
     * @param indiv2：个体2
     */
    public void cross(Individual indiv1, Individual indiv2) {
        boolean[] gene1 = indiv1.getGene();
        boolean[] gene2 = indiv2.getGene();
	//int geneLen = gene1.length();//获取个体基因的编码长度
       
	double x =  Math.random();
        if(x > crossRatio) return;
	//产生交换点
	int changePoint =(int)(x*geneLen);
	boolean tmp;
	for(int i = 0; i < changePoint; i++) {
	    tmp = gene1[i];
	    gene1[i] = gene2[i];
            gene2[i] = tmp;
	}
    }
    
     /**
     * 变异操作
     * @param individual：个体 
     */
    public void mutation(Individual individual) {
        boolean[] gene = individual.getGene();
	//产生变异点
        double x =  Math.random();
        if(x > mutaRatio) return;
	int mutatePoint =(int)(x* gene.length);
        gene[mutatePoint] = !gene[mutatePoint]; //对该位求反， 从而完成变异操作
    }
    
    private boolean selection() {
        Population nextGenerationPopulation = new Population(popuSize, geneLen);//新生下一代的种群个体
        //产生精英个体
        
        double[] cumulation = new double[popuSize];//记录当代中每个个体的适应度分布情况
        //拥有最佳适应度的个体基因
         Individual bestIndiv = population.getIndividuals().get(0);
        double maxFitness = getFitness(bestIndiv);
        cumulation[0] = maxFitness;
        for(int i = 1; i < popuSize; i++) {
            double fit = getFitness(population.getIndividuals().get(i));
            cumulation[i] = cumulation[i - 1] + fit;
            // 寻找当代的最优个体
            if(fit > maxFitness) {
                maxFitness = fit;
                bestIndiv = population.getIndividuals().get(i);
            }
        }
        
        // 轮盘法选择下一代
         Random rand = new Random(System.currentTimeMillis());
        for (int i = 0; i < popuSize; i++)
            try{
                nextGenerationPopulation.getIndividuals().set(i, population.getIndividuals().get(findByHalf(cumulation,
                    rand.nextDouble() * cumulation[popuSize - 1])));
            }
        catch(Exception e) {
        }
        
         //如果当前带最佳的个体小于或等于上一代，则认为上一代已经是最优的一代，返回true结束本轮测试用例的生成
        if(bestIndividual != null && maxFitness > 0.0 && bestTimes > judgeTimes) {
            bestIndividual.setBestIndiv(bestIndiv);
            bestIndividual.setFitness(maxFitness);
            return true;
        }
        if(maxFitness < lastMaxFitness) bestTimes++;
        else lastMaxFitness = maxFitness;
         bestIndividual.setBestIndiv(bestIndiv);
         bestIndividual.setFitness(maxFitness);
        return false;
    }
    
     // 折半查找
    public int findByHalf(double[] arr, double find) {
        if (find < 0 || find == 0 || find > arr[arr.length - 1])
            return -1;
        int min = 0;
        int max = arr.length - 1;
        int medium = min;
        do {
            if (medium == (min + max) / 2)
                break;
            medium = (min + max) / 2;
            if (arr[medium] < find)
                min = medium;
            else if (arr[medium] > find)
                max = medium;
            else
                return medium;
 
        } while (min < max);
        return max;
    }
    
    
     /**
     * 计算个体的适应度
     * @param individual
     * @return 
     */
    public double getFitness(Individual individual) {
	List<String> subCombineSet = cal(individual);
	return getF(subCombineSet);
    }
        /**
     * 求取该基因型个体对应的表现型组合，因为在求取个体适应度和最后在删减两两组合集时候都有这样的操作，故将其抽取为单独的方法
     * @param individual：个体
     * @return
     */
    private List<String> cal(Individual individual) {
	List<String>convertList = call(individual);
	List<String> subCombineSet = genSubCombineSet(convertList);
	return subCombineSet;
    }
    private List<String> call(Individual individual) {
	boolean[] gene = individual.getGene();
        int geneLen = gene.length;
	int len = paramCounts.length;
	String convertStrs[] = new String[len];
	List<String> convertList = new ArrayList<String>();
	//将基因型转换为表现型求适应度
	
	int index = 0;//记录基因下标
	for(Integer i = 0; i < len; i++) {
	    String bitStr = Integer.toBinaryString(paramCounts[i] - 1);
	    int num = bitStr.length();
	    String tmpStr = "";
	    for(int j = 0; j < num; j++) {
                if(gene[index + j] == true) {
                    tmpStr += 1;
                }
		else {
		    tmpStr += 0;
		}
	    }
	    index += num ;
	    Integer tmpVal =  Integer.valueOf(tmpStr, 2);
	    convertList.add(tmpVal.toString());
	}
        return convertList;
    }
    
    /**
     * 计算个体的覆盖率
     * @param subCombineSet
     * @return
     */
    private double getF(List<String> subCombineSet) {
	double fitness = 0.0;
	for(String str: subCombineSet) {
	    if(combinationSet.contains(str)) {
		fitness += 1;
	    }
	}
	return fitness;
    }
    
     /**
     * 求取该个体对应表现形式的所有k项组合
     * @param convertStr
     * @return
     */
    private List<String> genSubCombineSet(List<String> convertList) {
	List<String> subCombineSet = new LinkedList<String>();
	int count = convertList.size();
	for(int i = 0; i < count; i++) {
	    String str = i + " " + convertList.get(i) + "#";
	    for(int j = i + 1; j < count; j++) {
		subCombineSet.add(str + j + " " + convertList.get(j));
	    }
	}
	return subCombineSet;
    }
    
    /**
     * 在选择出当前轮的最佳测试用例后，删除其表现型所包含的所有两两组合
     */
   private void updateCombinationSet() {
      // int biLen = best_individual.size();
      // BI bi = best_individual.get(biLen - 1);
       Individual indiv = bestIndividual.getBestIndiv();//根据遗传算法获取到当前最佳的测试用例
       List<String> subCombineSet = cal(indiv);
       for(String subCombine: subCombineSet) {
	   if(combinationSet.contains(subCombine)) {
	       combinationSet.remove(subCombine);
	   }
       }
       String s = "";
       boolean[] gene = indiv.getGene();
       for(int i = 0; i < geneLen; i++) {
           if(gene[i] == true) {
              s += 1; 
           }
           else {
               s += 0;
           }
       }
      
        String resultStr = reConvert(indiv);
        if(resultStr != null)
             resultList.add(resultStr);
       System.out.println("cobinationSet:" + combinationSet.size());
   }
    
    
     /**
   * 构造两两组合集合
   * @param paramValList 参数值列表形式：[[val00 val02 val02][val11 val12 val13][....]]
   * @k 每个组合的个数
   */
  private void createCombinationSet(List<String[]> paramValList) {
	int count = paramValList.size();//参数个数
	for(int i = 0; i < count - 1; i++) {
	    int num1 = paramValList.get(i).length;//获取到当前参数的解空间个数
	    String combineStr = "";
	    for(int j = 0; j < num1; j++) {
		combineStr = i + " " + j + "#";
		for( int m = i + 1; m < count; m++) {
			int num2 = paramValList.get(m).length;//获取到当前参数的解空间个数
			for(int n = 0; n < num2; n++) {
			    combinationSet.add(combineStr + m + " " + n);
			}
		    }
	      }
	}
  }
  
   /**
     * 将基因型转换回表现型
     * @param individual：个体
     * @return 
     */
    private String reConvert(Individual individual) {
        String str = "";
        List<String> reConvertList  = call(individual);
        int index = 0;
        for(String reConvertStr: reConvertList) {
            Integer item = Integer.parseInt(reConvertStr);
            System.out.println(item);
            String s[] = paramValList.get(index); 
             System.out.println(s.toString());
             // System.out.print("s的长度个数" + s.length + "当前的 item " + item + "   " +  s.toString());
              for(int i = 0; i < s.length; i++) {
                   System.out.print(s[i] + "   ");
              }
              System.out.println();
              //try{
              if(s.length <= item) { //对于解空间为奇数时可能出现越界问题，比如某参数的解空间为3个 其对应为编码为2位，如果通过“11”转换就会的到item = 4这是不存在的
                  str = null;
                  return str;
              }
                 str += s[item] + "  ";
             // }
            ///  catch(Exception e) {
             //      System.out.print("s的长度个数" + s.length + "当前的 item " + item + "   " +  s.toString());
             // }
           index++;
        }
        System.out.println(str);
        return str;
    }
  
  /**
   * 获得的解空间的基因编码的长度
   * @param paramCounts：记录每个参数对应的解空间长度，并将每一个解空间个数映射为对应的二进制，再将所有的二进制依次进行拼接
   * @return 个体染色体的基因序列的长度，形式如：[1010110110101001001]的长度
   */
  private int geneCoding(int paramCounts[]) {
	String indivStr = "";
	for(Integer val: paramCounts) {
	    indivStr += val.toBinaryString(val - 1);
	}
	 return indivStr.length();
  }
  
}
