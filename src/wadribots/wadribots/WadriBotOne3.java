package wadribots;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import robocode.*;
import robocode.util.*;


public class WadriBotOne3 extends AdvancedRobot {
   
	double previousEnergy = 100;
    int gunDirection = 1;
    double distance = 200;
	double battleFieldHeight, battleFieldWidth;
	static Rectangle2D fieldrect;
	double currentDirection = 1;

	//Fun��o auxiliar que diz se o rob� est� fora da zona segura do mapa
	public boolean out(double x, double y, double c)
	{
		return !fieldrect.contains(x*c+getX(), y*c+getY());
	}
	
    public void run() {
    	
    	//Cria um ret�ngulo de �rea segura no mapa
    	fieldrect = new Rectangle2D.Double(18D, 18D,
    			getBattleFieldWidth()-36D,
    			getBattleFieldHeight()-36D);
    	
        // Move o Radar e arma independentemente
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setColors(Color.RED, Color.BLACK, Color.blue);
        // Carrega o Tamanho do Mapa
        battleFieldHeight = getBattleFieldHeight();
        battleFieldWidth = getBattleFieldWidth();

        // Inicia o loop principal
        while (true) {
        	setTurnRadarRight(Double.POSITIVE_INFINITY);
        	double heading;           
       	if (out(Math.sin(heading = getHeadingRadians()),Math.cos(heading),currentDirection))
        		currentDirection = -currentDirection;
        	setTurnRight(90 * currentDirection);
            setAhead(distance * currentDirection);
            execute();
        	
        }
    }
    
    
    // Lida com o evento de escaneamento de um rob� inimigo
    public void onScannedRobot(ScannedRobotEvent e) {
    	 
    	//Calcula a diferen�a entre a energia anterior do  inimigo e energia atual. 
    	//Usado para detectar se o inimigo atirou
    	 double changeInEnergy = previousEnergy - e.getEnergy();
     	 double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
         if (changeInEnergy > 0 && changeInEnergy <= 3) {
        	 currentDirection = (Math.random() < .5)?-1:1;
        	 setAhead((e.getDistance() / 4 + 25) * currentDirection);
         }
         setTurnRadarLeft(getRadarTurnRemaining());
         setTurnGunRightRadians(e.getBearingRadians() + getHeadingRadians() - getGunHeadingRadians());
         previousEnergy = e.getEnergy();
         
         //Verifica se o rob� est� pr�ximo das paredes e muda de dire��o
         double distance = e.getDistance(); 
		 double rel;
		 if ((distanceFromCorner()> 200 || distanceFromCorner() > distance) || out(Math.sin(-absoluteBearing), Math.cos(-absoluteBearing), 30))
		 	rel = Math.PI/2;
		 else if ((distanceFromCorner() < 200) == currentDirection > 0)
			rel = Math.PI/3;
		 else
			rel = 2*Math.PI/3;
		 setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(e.getBearingRadians()-rel));
         
    	//obt�m informa��es do rob� e do inimigo
    	double oldEnemyHeading = 0;
    	double bulletPower =  Math.min(3.0,getEnergy());
    	double myX = getX();
    	double myY = getY();
    	absoluteBearing = getHeadingRadians() + e.getBearingRadians();
    	double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
    	double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
    	double enemyHeading = e.getHeadingRadians();
    	double enemyHeadingChange = enemyHeading - oldEnemyHeading;
    	double enemyVelocity = e.getVelocity();
    	oldEnemyHeading = enemyHeading;
    	//guess factor - prev� pr�xima posi��o do inimigo
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
    	fire(Math.min(3.0,getEnergy()));    	
    }
    
    //fun��o que calcula distancia das paredes
	private double distanceFromCorner()
	{
		double min = Double.POSITIVE_INFINITY;
		int i=0;
		do
		{
			min = Math.min(min, Point2D.distance(getX(), getY(), (i&1)*battleFieldWidth, (i>>1)*battleFieldHeight));
			i++;
		}
		while (i < 4);
		return min;
	}
	    
}
