package Competicao;
import robocode.*;
import java.awt.Color;
import robocode.util.*;
import Competicao.EnemyBot;
import Competicao.Genetica;
import java.awt.geom.Point2D;
import org.jpl7.Query;
import org.jpl7.Term;

/**
 * HarryBotter - a robot by (Team Harry)
 */
public class HarryBotter extends AdvancedRobot
{
	private boolean movingForward;
	private boolean inWall;
	private int timeAfterShot = 0;
	private byte radarDirection = 1;
	private Query q;
	
	// Genetic Variables
	private float nearWall = 50;
	private float distanceEnemy = 70;
	private float aim = 2;
	private float closeEnemy = 200;
	private float notSoCloseEnemy = 100;

	private EnemyBot enemy = new EnemyBot();
	private Genetica genetica = new Genetica();
	
	public void run() {

		if (!Query.hasSolution("consult('ProBot.pl').")){
			System.out.println("Consult failed");
		}
		
		q = new Query("checkWall",new Term[] {
			new org.jpl7.Float(getX()),
			new org.jpl7.Float(getY()),
			new org.jpl7.Float(getBattleFieldWidth()),
			new org.jpl7.Float(getBattleFieldHeight()),
			new org.jpl7.Float(nearWall)
		});
		
		this.inWall = q.hasSolution();	
		
		setRoboColors();
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
		setAhead(40000);
		setTurnRadarRight(360);
		this.movingForward = true;
		
		// Robot main loop
		while(true) {
		
			q = new Query("checkWall",new Term[] {
				new org.jpl7.Float(getX()),
				new org.jpl7.Float(getY()),
				new org.jpl7.Float(getBattleFieldWidth()),
				new org.jpl7.Float(getBattleFieldHeight()),
				new org.jpl7.Float(nearWall)
			});
			if(q.hasSolution() && !this.inWall)
				reverseDirection();

			this.inWall = q.hasSolution();
			
			q = new Query("checkRadar",new Term[] {
				new org.jpl7.Float(getRadarTurnRemaining())
			});		
			
			if(q.hasSolution()) {
				setTurnRadarRight(360);			
			}
			
			shoot();
			scan();
			
			
			//doMovement();
			
			execute();
			
			timeAfterShot += 1;
			
			if(timeAfterShot % 10 == 0) {
				genetica.loadNextGene(timeAfterShot);
				updateGenetica();
			}
				
		
		}
	}
	
	public void updateGenetica() {
	
		nearWall = genetica.getNearWall();
		distanceEnemy = genetica.getDistanceEnemy();
		aim = genetica.getAim();
		closeEnemy = genetica.getCloseEnemy();
		notSoCloseEnemy = genetica.getNotSoCloseEnemy();
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		double absoluteBearing = getHeading() + e.getBearing();
		double bearingFromGun = Utils.normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
		double bearingFromRadar = Utils.normalRelativeAngleDegrees(absoluteBearing - getRadarHeading());
		
		if (this.movingForward) {
			setTurnRight(Utils.normalRelativeAngleDegrees(e.getBearing() + 80));
			
		} else {
			setTurnRight(Utils.normalRelativeAngleDegrees(e.getBearing() + 100));
		}
		
		q = new Query("checkEnemy",new Term[] {
			new org.jpl7.Float(e.getDistance()),
			new org.jpl7.Float(enemy.getDistance()),
			new org.jpl7.Float(distanceEnemy),
			new org.jpl7.Atom(e.getName()),
			new org.jpl7.Atom(enemy.getName())
		});

		if(q.hasSolution() || enemy.none())
			enemy.update(e, this);
	}
	
	public void onRobotDeath(RobotDeathEvent e) {
		if (e.getName().equals(enemy.getName())) {
			enemy.reset();
		}
	} 

	
	void shoot() {

		if (enemy.none())
			return;

		double firePower = Math.min(500 / enemy.getDistance(), 3);
		double bulletSpeed = 20 - firePower * 3;
		long time = (long)(enemy.getDistance() / bulletSpeed);

		double nextX = enemy.getFutureX(time);
		double nextY = enemy.getFutureY(time);
		double absDeg = absoluteBearing(getX(), getY(), nextX, nextY);
		
		setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));
		
		q = new Query("checkFire",new Term[] {
			new org.jpl7.Float(getGunHeat()),
			new org.jpl7.Float(0),
			new org.jpl7.Float(Math.abs(getGunTurnRemaining())),
			new org.jpl7.Float(aim),
		});
		
		if(q.hasSolution() || enemy.none())
			setFire(firePower);
	}
	
double absoluteBearing(double x1, double y1, double x2, double y2) {
		double xo = x2-x1;
		double yo = y2-y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;

		if (xo > 0 && yo > 0) {
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { 
			bearing = 360 + arcSin; 
		} else if (xo > 0 && yo < 0) { 
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) {
			bearing = 180 - arcSin; 
		}

		return bearing;
	}
	
	void scan() {
		if (enemy.none()) {
			setTurnRadarRight(36000);
		} else {
			double turn = getHeading() - getRadarHeading() + enemy.getBearing();
			turn += 30 * radarDirection;
			setTurnRadarRight(turn);
			radarDirection *= -1;

		}
	}

	void doMovement() {
		
		q = new Query("farEnemy",new Term[] {
			new org.jpl7.Float(enemy.getDistance()),
			new org.jpl7.Float(closeEnemy),
		});
		if (q.hasSolution())
			setAhead(enemy.getDistance() / 2);
			
		q = new Query("closeEnemy",new Term[] {
			new org.jpl7.Float(enemy.getDistance()),
			new org.jpl7.Float(notSoCloseEnemy),
		});
		if (q.hasSolution())
			setBack(enemy.getDistance());
	}

	double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
	
	private void setRoboColors() {
	
		setBodyColor(Color.RED);
		setGunColor(Color.WHITE);
		setRadarColor(Color.BLACK);
		setBulletColor(Color.GREEN);
		setScanColor(Color.RED);
	}
	
	public void reverseDirection() {
		if (this.movingForward) {
			setBack(40000);
			this.movingForward = false;
		} else {
			setAhead(40000);
			this.movingForward = true;
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
		
		reverseDirection();
	}

	public void onHitWall(HitWallEvent e) {
	
		reverseDirection();
	}
	
	public void onHitRobot(HitRobotEvent e) {
	
		if(e.isMyFault()) 
			reverseDirection();
			
		timeAfterShot = 0;
	}	
}
