package wadribots;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

import org.jpl7.Query;
import org.jpl7.Term;

public class PrologTest extends AdvancedRobot {

	public PrologTest() {}
	
	
	public void run() {
		if (!Query.hasSolution("consult('test.pl).")) {
			System.out.println("Consult failed");
		}
		
		while(true) {
			turnRight(90);
			ahead(50);
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		Query q = new Query("atacar", new Term[] {
								new org.jpl7.Float(e.getDistance())
						}
				
				);
		
		if (q.hasSolution()) {
			fire(1);
		}
		
		Double bearing = e.getBearing();
		turnRight(bearing);
		
		ahead(e.getDistance()/10);
	}
}
