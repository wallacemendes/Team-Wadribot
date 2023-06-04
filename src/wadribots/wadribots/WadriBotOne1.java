package wadribots;

import java.awt.geom.Point2D;

import robocode.*;

import robocode.util.*;


public class WadriBotOne1 extends AdvancedRobot {
   
	double previousEnergy = 100;
    int movementDirection = 1;
    int gunDirection = 1;
    double distance = 200;
    int consecutiveMisses = 0; // initialize counter variable
	double battleFieldHeight, battleFieldWidth;


	
	
    public void run() {
    	
        // Move the radar and gun independently
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        battleFieldHeight = getBattleFieldHeight();
        battleFieldWidth = getBattleFieldWidth();

        // Inicia o loop principal
        while (true) {
        	setTurnRight(90 * movementDirection);
            setAhead(distance * movementDirection);
        	setTurnRadarRight(Double.POSITIVE_INFINITY);

            execute();
        	
        }
    }
    
    
    // Lida com o evento de escaneamento de um rob� inimigo
    public void onScannedRobot(ScannedRobotEvent e) {
    	 
    	 double changeInEnergy = previousEnergy - e.getEnergy();
         if (changeInEnergy > 0 && changeInEnergy <= 3) {
             movementDirection = -movementDirection;
             setAhead((e.getDistance() / 4 + 25) * movementDirection);
         }
         setTurnRadarLeft(getRadarTurnRemaining());
         setTurnGunRightRadians(e.getBearingRadians() + getHeadingRadians() - getGunHeadingRadians());
         previousEnergy = e.getEnergy();
    	
    	double oldEnemyHeading = 0;
    	double bulletPower = Math.min(3.0,getEnergy());
    	double myX = getX();
    	double myY = getY();
    	double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
    	double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
    	double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
    	double enemyHeading = e.getHeadingRadians();
    	double enemyHeadingChange = enemyHeading - oldEnemyHeading;
    	double enemyVelocity = e.getVelocity();
    	oldEnemyHeading = enemyHeading;

    	double deltaTime = 0;
    	double predictedX = enemyX, predictedY = enemyY;
    	while((++deltaTime) * (20.0 - 3.0 * bulletPower) < 
    	      Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
    		predictedX += Math.sin(enemyHeading) * enemyVelocity;
    		predictedY += Math.cos(enemyHeading) * enemyVelocity;
    		enemyHeading += enemyHeadingChange;
    		if(	predictedX < 18.0 
    			|| predictedY < 18.0
    			|| predictedX > battleFieldWidth - 18.0
    			|| predictedY > battleFieldHeight - 18.0){

    			predictedX = Math.min(Math.max(18.0, predictedX), 
    			    battleFieldWidth - 18.0);	
    			predictedY = Math.min(Math.max(18.0, predictedY), 
    			    battleFieldHeight - 18.0);
    			break;
    		}
    	}
    	double theta = Utils.normalAbsoluteAngle(Math.atan2(
    	    predictedX - getX(), predictedY - getY()));

    	setTurnRadarRightRadians(Utils.normalRelativeAngle(
    	    absoluteBearing - getRadarHeadingRadians()));
    	setTurnGunRightRadians(Utils.normalRelativeAngle(
    	    theta - getGunHeadingRadians()));
    	
    	
    	fire(Math.min(4, 1000 / e.getDistance()));    	
    	
    }
    
	public void onHitWall(HitWallEvent event) {
	       out.println("Ouch, I hit a wall bearing " + event.getBearing() + " degrees.");
	}
    
}
