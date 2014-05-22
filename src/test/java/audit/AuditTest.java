package audit;

import org.junit.Test;

import prove.ProofGen;

public class AuditTest {
	
	//@Test
	public void challenge(){
		Challenge challenge =  new Challenge();
		challenge.challengeMsgGeneration(2);
	}
	
	//@Test
	public void prove(){
		ProofGen proofGen =  new ProofGen();
		proofGen.proofGeneration("FileSetup.java");
	}
	
	//@Test
	public void verify(){
		Verification verification = new Verification();
		verification.verifyIntegrity("FileSetup.java");
	}
	
	//Perform a whole audit process test
	@Test
	public void runInOne(){
		Challenge challenge =  new Challenge();
		ProofGen proofGen =  new ProofGen();
		Verification verification = new Verification();
		
		challenge.challengeMsgGeneration(2);
		proofGen.proofGeneration("FileSetup.java");
		verification.verifyIntegrity("FileSetup.java");
	}
}
