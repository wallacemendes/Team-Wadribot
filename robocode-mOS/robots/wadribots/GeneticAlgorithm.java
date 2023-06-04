package wadribots;

import java.util.Random;


public class GeneticAlgorithm {

    // Atributos da classe
    private int populationSize; // O número de indivíduos na população
    private int geneSize; // O número de genes em cada indivíduo
    private double mutationRate; // A probabilidade de um gene sofrer mutação
    private double crossoverRate; // A probabilidade de dois indivíduos se recombinarem
    private float[][] population;// Uma matriz que armazena os valores dos genes de cada indivíduo
    private float[] fitness = {0, 0, 0, 0, 0} ; // Um vetor que armazena o score de cada indivíduo na função-objetivo
    private float[] bestIndividual; // O melhor indivíduo da população atual
    private float bestFitness; // O score do melhor indivíduo na função-objetivo

    public GeneticAlgorithm(int populationSize, int geneSize, double mutationRate, double crossoverRate) {
    	this.populationSize = populationSize;
    	this.geneSize = geneSize;
    	this.mutationRate = mutationRate;
    	this.crossoverRate = crossoverRate;
        population = new float[populationSize][geneSize];
        fitness = new float[populationSize];
        Random random = new Random();

        for (int i = 0; i < populationSize; i++) {
            population[i][0] = 100 + random.nextFloat() * 500;
            population[i][1] = -1 + random.nextFloat() * 2;
            population[i][2] = -1 + random.nextFloat() * 2;
            population[i][3] = 1.0f + random.nextFloat() * 2.9f;
        }

        bestIndividual = new float[geneSize];
        bestFitness = -Float.MAX_VALUE;
        
        
        System.out.println("------------------------Construtor----------------------------");
        System.out.println("Best individual: ");
        System.out.println("distance: " + bestIndividual[0]);
        System.out.println("gunDirection: " + bestIndividual[1]);
        System.out.println("currentDirection: " + bestIndividual[2]);
        System.out.println("firePower: " + bestIndividual[3]);
        System.out.println("Score: " + bestFitness);
    }

    public void evaluate(int[] results, int generation) {
     
    	int i = generation;
    
            float distance = population[i][0];
            float gunDirection = population[i][1];
            float currentDirection = population[i][2];
            float firePower = population[i][3];

            int wallHits = results[0];
            int hitByBullet = results[1]; 
            int bulletHits = results[2];
            int bulletMissed = results[3]; 

            fitness[i] = wallHits*(-1) * 6 + hitByBullet*(-1) * 3 + bulletHits * 90 + bulletMissed*(-1) * 1;
        
    }
    
   
    public float[][] select() {
    

      
        float[][] parents = new float[populationSize][geneSize];

        Random random = new Random();


        for (int i = 0; i < populationSize; i++) {
            int index1 = random.nextInt(populationSize);
            int index2 = random.nextInt(populationSize);

            if (fitness[index1] > fitness[index2]) {
                parents[i] = population[index1];
            } else {
                parents[i] = population[index2];
            }
        }

        return parents;
    }
    
    public void reproduce(float[][] parents) {
   
        float[][] newPopulation = new float[populationSize][geneSize];

        Random random = new Random();

        for (int i = 0; i < populationSize; i += 2) {
            float[] parent1 = parents[i];
            float[] parent2 = parents[i + 1];

            float[] child1 = new float[geneSize];
            float[] child2 = new float[geneSize];

            if (random.nextDouble() < crossoverRate) {
                int cutPoint = 1 + random.nextInt(geneSize - 1);

                for (int j = 0; j < cutPoint; j++) {
                    child1[j] = parent1[j];
                    child2[j] = parent2[j];
                }

                for (int j = cutPoint; j < geneSize; j++) {
                    child1[j] = parent2[j];
                    child2[j] = parent1[j];
                }
            } else {
                for (int j = 0; j < geneSize; j++) {
                    child1[j] = parent1[j];
                    child2[j] = parent2[j];
                }
            }

            if (random.nextDouble() < mutationRate) {
                int geneToMutate1 = random.nextInt(geneSize);
                int geneToMutate2 = random.nextInt(geneSize);

                switch (geneToMutate1) {
                    case 0:
                        child1[0] = 100 + random.nextFloat() * 500;
                        break;
                    case 1: 
                        child1[1] = -1 + random.nextFloat() * 2;
                        break;
                    case 2: 
                    	child1[2] = -1 + random.nextFloat() * 2;
                        break;
                    case 3: 
                        child1[3] = 1.0f + random.nextFloat() * 2.9f;
                        break;
                }

                switch (geneToMutate2) {
                    case 0: 
                        child2[0] = 100 + random.nextFloat() * 500;
                        break;
                    case 1: 
                        child2[1] = -1 + random.nextFloat() * 2;
                        break;
                    case 2: 
                        child2[2] = -1 + random.nextFloat() * 2;
                        break;
                    case 3: 
                        child2[3] = 1.0f + random.nextFloat() * 2.9f;
                        break;
                }
            }


            newPopulation[i] = child1;
            newPopulation[i + 1] = child2;
        }

 
        population = newPopulation;
    }

    public void update() {
        for (int i = 0; i < populationSize; i++) {
            if (fitness[i] > bestFitness) {
                bestIndividual = population[i];
                bestFitness = fitness[i];
            }
        }

        // Imprime na tela os valores dos genes e o score do melhor indivíduo
//        System.out.println("Best individual: ");
//        System.out.println("distance: " + bestIndividual[0]);
//        System.out.println("gunDirection: " + bestIndividual[1]);
//        System.out.println("currentDirection: " + bestIndividual[2]);
//        System.out.println("firePower: " + bestIndividual[3]);
//        System.out.println("Score: " + bestFitness);
    }
    
    public float[] getBestIndividual() {
        return bestIndividual;
    }
    
    public void runAlgorithm(int[] results, int generation) {

  
            evaluate(results, generation);

            float[][] parents = select();

            reproduce(parents);

            update();

            System.out.println("ROUND "+ generation+ " Best individual: ");
            System.out.println("distance: " + bestIndividual[0]);
            System.out.println("gunDirection: " + bestIndividual[1]);
            System.out.println("currentDirection: " + bestIndividual[2]);
            System.out.println("firePower: " + bestIndividual[3]);
            System.out.println("Score: " + bestFitness);
        
    }


}
