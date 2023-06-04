package wadribots3;

import java.awt.geom.*;
import java.util.List;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;


public class WadriBotThree extends AdvancedRobot {
	
	List<WaveBullet> waves = new ArrayList<WaveBullet>();
	static int[] stats = new int[31]; // 31 is the number of unique GuessFactors we're using
	  // Note: this must be odd number so we can get
	  // GuessFactor 0 at middle.
	int direction = 1;
	double battleFieldHeight, battleFieldWidth;
    int movementDirection = 1;
    double distance = 400;
	double previousEnergy = 100;




	
    public void run() {
    	
        // Move the radar and gun independently
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        battleFieldHeight = getBattleFieldHeight();
        battleFieldWidth = getBattleFieldWidth();

        // Inicia o loop principal
        while (true) {
        	setTurnRight(0 * movementDirection);
            setAhead(distance * movementDirection);
        	setTurnRadarRight(Double.POSITIVE_INFINITY);

            execute();
        	
        }
    }




	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		// TODO Auto-generated method stub
		 double changeInEnergy = previousEnergy - e.getEnergy();
         if (changeInEnergy > 0 && changeInEnergy <= 3) {
             movementDirection = -movementDirection;
             setAhead((e.getDistance() / 4 + 25) * movementDirection);
         }
         setTurnRadarLeft(getRadarTurnRemaining());
         setTurnGunRightRadians(e.getBearingRadians() + getHeadingRadians() - getGunHeadingRadians());
         previousEnergy = e.getEnergy();
         
         //guessFactorBlock(e);
	}
	
	void guessFactorBlock(ScannedRobotEvent e) {
		
		// Enemy absolute bearing, you can use your one if you already declare it.
		double absBearing = getHeadingRadians() + e.getBearingRadians();

		// find our enemy's location:
		double ex = getX() + Math.sin(absBearing) * e.getDistance();
		double ey = getY() + Math.cos(absBearing) * e.getDistance();
		
		// Let's process the waves now:
		for (int i=0; i < waves.size(); i++)
		{
			WaveBullet currentWave = (WaveBullet)waves.get(i);
			if (currentWave.checkHit(ex, ey, getTime()))
			{
				waves.remove(currentWave);
				i--;
			}
		}
		
		double power = Math.min(3, Math.max( .1, 400/ e.getDistance() ));
		// don't try to figure out the direction they're moving 
		// they're not moving, just use the direction we had before
		if (e.getVelocity() != 0)
		{
			if (Math.sin(e.getHeadingRadians()-absBearing)*e.getVelocity() < 0)
				direction = -1;
			else
				direction = 1;
		}
		int[] currentStats = stats; // This seems silly, but I'm using it to
					    // show something else later
		WaveBullet newWave = new WaveBullet(getX(), getY(), absBearing, power,
                        direction, getTime(), currentStats);
		
		int bestindex = 15;	// initialize it to be in the middle, guessfactor 0.
		for (int i=0; i<31; i++)
			if (currentStats[bestindex] < currentStats[i])
				bestindex = i;
		
		// this should do the opposite of the math in the WaveBullet:
		double guessfactor = (double)(bestindex - (stats.length - 1) / 2)
                        / ((stats.length - 1) / 2);
		double angleOffset = direction * guessfactor * newWave.maxEscapeAngle();
                double gunAdjust = Utils.normalRelativeAngle(
                        absBearing - getGunHeadingRadians() + angleOffset);
                setTurnGunRightRadians(gunAdjust);
                if (setFireBullet(power) != null) {
                    waves.add(newWave);
                    newWave.printWave();
                    
                }
                if (getGunHeat() == 0 && gunAdjust < Math.atan2(9, e.getDistance()) && setFireBullet(power) != null) {
                	fire(power);
                }

	}
	
	


}



class WaveBullet {
    private double startX, startY, startBearing, power;
    private long   fireTime;
    private int    direction;
    private int[]  returnSegment;

    public WaveBullet(double x, double y, double bearing, double power,
            int direction, long time, int[] segment) {
        startX         = x;
        startY         = y;
        startBearing   = bearing;
        this.power     = power;
        this.direction = direction;
        fireTime       = time;
        returnSegment  = segment;
    }
    // More methods...
    
    public double getBulletSpeed()
	{
		return 20 - power * 3;
	}
	
	public double maxEscapeAngle()
	{
		return Math.asin(8 / getBulletSpeed());
	}
	
	public boolean checkHit(double enemyX, double enemyY, long currentTime)
	{
		// if the distance from the wave origin to our enemy has passed
		// the distance the bullet would have traveled...
		if (Point2D.distance(startX, startY, enemyX, enemyY) <= 
				(currentTime - fireTime) * getBulletSpeed())
		{
			double desiredDirection = Math.atan2(enemyX - startX, enemyY - startY);
			double angleOffset = Utils.normalRelativeAngle(desiredDirection - startBearing);
			double guessFactor =
				Math.max(-1, Math.min(1, angleOffset / maxEscapeAngle())) * direction;
			int index = (int) Math.round((returnSegment.length - 1) /2 * (guessFactor + 1));
			returnSegment[index]++;
			return true;
		}
		return false;
	}
	
	public void printWave() {
		System.out.println(this.startX +" / "+ this.startY +" / "+ this.startBearing +" / "+ this.power +" / "+ 
				this.direction +" / "+ this.fireTime);
	}

}

