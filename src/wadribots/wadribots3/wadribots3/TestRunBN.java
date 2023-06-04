package wadribots3;

import org.eclipse.recommenders.jayes.inference.IBayesInferer;
import org.eclipse.recommenders.jayes.inference.junctionTree.JunctionTreeAlgorithm;
import org.eclipse.recommenders.jayes.BayesNode;
import java.util.Map;
import java.util.HashMap;




public class TestRunBN {
	
	public static BNTest redeBayes;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		redeBayes = BNTest.createBN();
		IBayesInferer inferer = new JunctionTreeAlgorithm ();
		inferer.setNetwork(redeBayes.net);
		Map<BayesNode,String> evidence = new HashMap<BayesNode,String>();		
		evidence.put(redeBayes.a, "true");	
		evidence.put(redeBayes.b, "two");
		inferer.setEvidence(evidence);
		double[] beliefsC = inferer.getBeliefs(redeBayes.c);
		
		for (double d : beliefsC) {
			System.out.println(d);
		}
		
	}

}
