package Competicao;

public class Genetica {
	/**
	 * Atributos dos gens s�o hardcoded aqui, cada precisa de:
	 * 
	 * public [type] [name] - acesso pela variavel publica
	 * mutationScale - escala de mutacao para o gen (tudo em vetor tem que ser em ordem)
	 * lowBound - o minimo que o gen pode chegar
	 * highBound - o maximo que o gen pode chegar
	 * geneScore - um espa�o para o score do gen
	 */ 
	public float nearWall; // 50 - 200
	public float distanceEnemy; // 50 - 200
	public float aim; // 1 - 5
	public float closeEnemy; // 50 - 500	
	public float notSoCloseEnemy; // 50 - 200
	static private int atributosNum = 5;
	
	// a base de aumento de cada gene (0 a 100% do valor para ser somado ou subtraido)
	static private float[] mutationScale = {20, 20, 2, 40, 20};
	
	// chance de mutacao
	static private double mutationChance = 0.1;
		
	// minimo e maximo que essas variaveis abrigam
	static private float[] lowBound = {50, 50, 1, 50, 50};
	static private float[] highBound = {200, 200, 5, 500, 200};

	// Score de adaptabilidade para um dado gen
	private int[] geneScore = {0, 0, 0, 0, 0};
	
	// Numero de tentativas antes de rodar a selecao 
	private int gensNum;
	private int geneAtual = 0;
	
	// Base inicial de valores
	private float[][] baseGenetica = { 
			{50, 70, 2, 200, 100}, 
			{100, 120, 2, 100, 200}, 
			{50, 70, 3, 300, 100}, 
			{100, 120, 4, 200, 100}, 
			{50, 70, 2, 400, 50} };
	
	Genetica() {
		gensNum = 5;
		// baseGenetica pode popular baseada no min-max e
		// quantos dados queremos (gensNum), mas � mais seguro
		// come�ar com uma base hardcoded
		//
		// baseGenetica = new float[gensNum][numero de variaveis do gen];
		// for (int i = 0; i < gensNum; i++) {
		// 		populate baseGenetica
		// }
		//
		
		// inicializar
		nearWall = baseGenetica[0][0];
		distanceEnemy = baseGenetica[0][1];
		aim = baseGenetica[0][2];
		closeEnemy = baseGenetica[0][3];
		notSoCloseEnemy = baseGenetica[0][4];
	}
	
	// Rankeia a base genetica baseada nos scores
	void selection() {
		float[][] auxBase = new float[gensNum][atributosNum];
		for (int j = 0; j < geneScore.length; j ++) {
			int geneNum = 0;
			int best = -1;
			for (int i = 0; i < geneScore.length; i++) {
				if (geneScore[i] > best) {
					geneNum = i;
					best = geneScore[i];
				}
			}
			geneScore[geneNum] = -1;
			auxBase[j] = baseGenetica[geneNum];
		}
		baseGenetica = auxBase;
	}
	
	// Metodo de crossover: media dos dois genes
	float[] crossoverAux(float[] gene1, float[] gene2) {
		float[] aux = new float[atributosNum]; 
		for (int i = 0; i < atributosNum; i++) {
			aux[i] = (gene1[i] + gene2[i]) / 2;
		}
		return aux;
	}
	
	// 1 sobrevive, pareamento de 1-2, 1-3, 2-3, 4-5
	void crossover() {
		float[][] auxBase = new float[gensNum][atributosNum];
		auxBase[0] = baseGenetica[0];
		auxBase[1] = crossoverAux(baseGenetica[0], baseGenetica[1]);
		auxBase[2] = crossoverAux(baseGenetica[0], baseGenetica[2]);
		auxBase[3] = crossoverAux(baseGenetica[1], baseGenetica[2]);
		auxBase[4] = crossoverAux(baseGenetica[3], baseGenetica[4]);
		
		baseGenetica = auxBase;
	}
	
	// 1 permanece puro, os outros tem uma probabilidade de sofrer muta��o
	void mutation() {
		for (int i = 1; i < gensNum; i++) {
			for (int j = 0; j < atributosNum; j++) {
				float mutationValue = 0;
				if (Math.random() > mutationChance) {
					mutationValue = (float)(Math.random() * mutationScale[j]);
					mutationValue *= Math.random() > 0.5 ? 1 : -1;
				}
				baseGenetica[i][j] += mutationValue;
				
				// Garante que os valores est�o dentro dos limites
				if (baseGenetica[i][j] > highBound[j]) {
					baseGenetica[i][j] = highBound[j];
				} else if (baseGenetica[i][j] < lowBound[j]){
					baseGenetica[i][j] = lowBound[j];
				}
			}
		}
	}
	
	// Testa o proximo gen e faz inicia o ciclo de evolucao caso todos tenham sido testados
	public void loadNextGene(int roundsBeforeShot) {
		geneScore[geneAtual] = roundsBeforeShot;
		if (geneAtual == gensNum - 1) {
			
			System.out.println();
			System.out.println();
			System.out.println("Gen values (before): ");
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 5; j++) {
					System.out.print(baseGenetica[i][j] + " / ");
				}
				System.out.println();
			}
			System.out.println();
			
			
			selection();
			crossover();
			mutation();
			geneAtual = 0;
			
			System.out.println("Gen values (after): ");
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 5; j++) {
					System.out.print(baseGenetica[i][j] + " / ");
				}
				System.out.println();
			}
		} else {
			geneAtual++;	
		}
		
		nearWall = baseGenetica[geneAtual][0];
		distanceEnemy = baseGenetica[geneAtual][1];
		
		// aim eh int?
		aim = Math.round(baseGenetica[geneAtual][2]);
		
		closeEnemy = baseGenetica[geneAtual][3];
		notSoCloseEnemy = baseGenetica[geneAtual][4];
	}
	
	public float getNearWall() {
		return this.nearWall;
	}
	
	public float getDistanceEnemy() {
		return this.distanceEnemy;
	}
	
	public float getAim() {
		return aim;
	}
	
	public float getCloseEnemy() {
		return closeEnemy;
	}
	
	public float getNotSoCloseEnemy() {
		return notSoCloseEnemy;
	}
	
	
}
