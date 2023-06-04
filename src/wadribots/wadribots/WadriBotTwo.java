package wadribots;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import GeneticAlgorithm;
import robocode.*;
import robocode.util.*;

public class WadriBotTwo extends AdvancedRobot {

    final int POPULATION_SIZE = 6;
    final int GEN_SIZE = 4;
    final double MUTATION_RATE = 0.1;
    final double CROSSOVER_RATE = 0.5;

    private static final double MAX_ENERGY_CHANGE = 3.0;
    private static final double MAX_BULLET_POWER = 3.0;
    private static final double BULLET_SPEED_CONSTANT = 20.0;

    static private GeneticAlgorithm genetica; 
    private float[] geneticIndividual;

    double previousEnergy = 100;
    double battleFieldHeight, battleFieldWidth;
    static Rectangle2D fieldrect;
    double currentDirection;
    double distance;
    double firePower;
    double gunDirection;

    static int round = 0;
    int bulletHits;
    int bulletMissed;
    int wallHits;
    int hitByBullet;
    static int[] results = {0,0,0,0};

    public void run() {
        fieldrect = new Rectangle2D.Double(18D, 18D,
                getBattleFieldWidth()-36D,
                getBattleFieldHeight()-36D);

        bulletHits = 0;
        bulletMissed = 0;
        wallHits = 0;
        hitByBullet = 0;
        results = new int[GEN_SIZE];
        
        if(genetica instanceof GeneticAlgorithm) {
        	System.out.println("Já Existe");
        }else {
            genetica = new GeneticAlgorithm(POPULATION_SIZE, GEN_SIZE, MUTATION_RATE, CROSSOVER_RATE);
            System.out.println("Criou");
        }

        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setColors(Color.RED, Color.BLACK, Color.blue);

        battleFieldHeight = getBattleFieldHeight();
        battleFieldWidth = getBattleFieldWidth();

        while (true) {
            geneticIndividual = genetica.getBestIndividual();
            distance = geneticIndividual[0];
            gunDirection = geneticIndividual[1];
            currentDirection = geneticIndividual[2];
            firePower = geneticIndividual[3];

            setTurnRadarRight(Double.POSITIVE_INFINITY);

            setTurnRight(90 * currentDirection);
            setAhead(distance * currentDirection);
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        checkEnergyChange(e);
        setGunDirection(e);
        guessEnemyPosition(e);
    }

    private void checkEnergyChange(ScannedRobotEvent e) {
        double changeInEnergy = previousEnergy - e.getEnergy();
        if (changeInEnergy > 0 && changeInEnergy <= MAX_ENERGY_CHANGE) {
            reverseDirection(e);
        }
        previousEnergy = e.getEnergy();
    }

    private void reverseDirection(ScannedRobotEvent e) {
        currentDirection = -currentDirection;
        setAhead((e.getDistance() / 4 + 25) * currentDirection);
    }

    private void setGunDirection(ScannedRobotEvent e) {
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        setTurnRadarLeft(getRadarTurnRemaining());
        setTurnGunRightRadians(e.getBearingRadians() + getHeadingRadians() - getGunHeadingRadians());
    }

    private void guessEnemyPosition(ScannedRobotEvent e) {
        double oldEnemyHeading = 0;
        double myX = getX();
        double myY = getY();
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        double 	enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
        double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
        double enemyHeading = e.getHeadingRadians();
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        double enemyVelocity = e.getVelocity();
        oldEnemyHeading = enemyHeading;

        double deltaTime = 0;
        double predictedX = enemyX, predictedY = enemyY;
        while((++deltaTime) * (BULLET_SPEED_CONSTANT - MAX_BULLET_POWER * firePower) < 
              Point2D.Double.distance(myX, myY, predictedX, predictedY)){      
            predictedX += Math.sin(enemyHeading) * enemyVelocity;
            predictedY += Math.cos(enemyHeading) * enemyVelocity;
            enemyHeading += enemyHeadingChange;
            checkPrediction(predictedX, predictedY);
        }
        double theta = Utils.normalAbsoluteAngle(Math.atan2(
            predictedX - getX(), predictedY - getY()));

        setTurnRadarRightRadians(Utils.normalRelativeAngle(
            absoluteBearing - getRadarHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()) + gunDirection/20);

        fire(firePower);   
    }

    private void checkPrediction(double predictedX, double predictedY) {
        if(predictedX < 18.0 || predictedY < 18.0 || predictedX > battleFieldWidth - 18.0  || predictedY > battleFieldHeight - 18.0){
            predictedX = Math.min(Math.max(18.0, predictedX), 
                battleFieldWidth - 18.0); 
            predictedY = Math.min(Math.max(18.0, predictedY), 
                battleFieldHeight - 18.0);
        }
    }

    public void onRoundEnded(RoundEndedEvent event) {
        results[0] = wallHits;
        results[1] = hitByBullet;
        results[2] = bulletHits;
        results[3] = bulletMissed;
        System.out.println("ROUND: "+ round+ " wallHits " + wallHits + " hitByBullet " + hitByBullet + " bulletHits " + bulletHits+ " bulletMissed "+ bulletMissed);
    	genetica.runAlgorithm(getResults(), round);
    	if (round< POPULATION_SIZE-1) {
    		round++;
    	}else {
    		round = 0;
    	}
    }
    @Override
    public void onBattleEnded(BattleEndedEvent event) {
        round = 0;
    }
    
    public void onHitWall(HitWallEvent event) {
        wallHits++;
    }
    
    public void onHitByBullet(HitByBulletEvent event) {
        hitByBullet++;
    }
    public void onBulletHit(BulletHitEvent event) {
        bulletHits++;
    }
    public void onBulletMissed(BulletMissedEvent event) {
        bulletMissed++;
    }
    public int getBulletHits() {
        return this.bulletHits;
    }
    
    public int getBulletMissed() {
        return this.bulletMissed;
    }
    public int getWallHits() {
        return this.wallHits;
    }
    public int getHitByBullets() {
        return this.hitByBullet;
    }
    
    public static int[] getResults() {
        return results;
    }
    
    private double distanceFromCorner() {
        double min = Double.POSITIVE_INFINITY;
        int i=0;
        do {
            min = Math.min(min, Point2D.distance(getX(), getY(), (i&1)*battleFieldWidth, (i>>1)*battleFieldHeight));
            i++;
        }
        while(i < 4);
        return min;
    }
}

