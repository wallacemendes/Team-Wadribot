package wadribots3;

import java.util.Arrays;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.inference.IBayesInferer;
import org.eclipse.recommenders.jayes.inference.junctionTree.JunctionTreeAlgorithm;


public class BNTest {

	BayesNet net;
	BayesNode a;
	BayesNode b;
	BayesNode c;
	
	public BNTest() {
		this.net = new BayesNet();
		this.a = net.createNode("a");
		this.b = net.createNode("b");
		this.c = net.createNode("c");
		
		a.addOutcome("true");
		a.addOutcome("false");
		a.setProbabilities(0.2, 0.8);
		
		b.addOutcome("one");
		b.addOutcome("two");
		b.addOutcome("three");
		b.setParents(Arrays.asList(a));
		b.setProbabilities(
				0.1 , 0.4 , 0.5,
				0.3 , 0.4 , 0.3
		);
		
		c.addOutcomes("true", "false");
		c.setParents(Arrays.asList(a, b));
		c.setProbabilities(
				// a == true
				0.1 , 0.9, // b == one
				0.0 , 1.0, // b == two
				0.5 , 0.5, // b == three
				// a == false
				0.2 , 0.8, // b == one
				0.0 , 1.0, // b == two
				0.7 , 0.3 // b == three
		);
		
		
	}
	
	public static BNTest createBN() {
		return new BNTest();
	}
	

	
}