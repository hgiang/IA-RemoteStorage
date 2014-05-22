package setup;

import org.junit.Test;

import prove.ProofGen;
import audit.Challenge;
import audit.Verification;

public class SystemSetupTest {

	//@Test
	public void publicKeyAndMasterKeyGeneartionTest(){
		KeyGen keyGeneration = new KeyGen();
		keyGeneration.generateSystemKeys();
	}

	//@Test
	public void fileSetup(){
		FileSetup fileSetup = new FileSetup();
		fileSetup.fileSetupByAdmin("FileSetup.java", "src/main/java/setup/FileSetup.java");
	}
	
	//Test all algorithms 
	@Test
	public void runInOne(){
		KeyGen keyGeneration = new KeyGen();
		FileSetup fileSetup = new FileSetup();
		
		keyGeneration.generateSystemKeys();
		fileSetup.fileSetupByAdmin("FileSetup.java", "src/main/java/setup/FileSetup.java");
		
		Challenge challenge =  new Challenge();
		ProofGen proofGen =  new ProofGen();
		Verification verification = new Verification();
		
		challenge.challengeMsgGeneration(2);
		proofGen.proofGeneration("FileSetup.java");
		verification.verifyIntegrity("FileSetup.java");
	}
	
}
