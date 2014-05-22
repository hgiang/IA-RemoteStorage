package audit;

import initialization.InitializationService;
import initialization.impl.InitializationServiceImpl;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

import java.math.BigInteger;
import java.util.Map;

import utilities.hash.Sha1Hash;
import utilities.io.FileIOService;
import utilities.io.impl.FileIOServiceImpl;

public class Verification {
	private InitializationService initializationService = new InitializationServiceImpl();
	private FileIOService fileIOService = new FileIOServiceImpl();
	private Sha1Hash sha1Hash = new Sha1Hash();
	
	//get running data path map
	private Map<String,String> runningDataPaths =initializationService.getRunningDataPaths();
	
	
	private void verify(Pairing pairing,String fileName){
		BigInteger mu = new BigInteger(fileIOService.readFile(runningDataPaths.get("challenge message")+"mu"));
		BigInteger r = new BigInteger(fileIOService.readFile(runningDataPaths.get("challenge message")+"r"));
		String[] challBlockIds = fileIOService.readFileToString(runningDataPaths.get("challenge message")+"challSet").split(",");
		
		//read public key
		Element g = pairing.getG1().newElement().getImmutable().setToRandom();
		Element u = pairing.getG1().newElement().getImmutable().setToRandom();
		Element kappa = pairing.getG1().newElement().getImmutable().setToRandom();
		Element nu = pairing.getG1().newElement().getImmutable().setToRandom();
		BigInteger q = new BigInteger(fileIOService.readFile(runningDataPaths.get("public keys")+"q"));

		g.setFromBytes(fileIOService.readFile(runningDataPaths.get("public keys")+"g"));
		u.setFromBytes(fileIOService.readFile(runningDataPaths.get("public keys")+"u"));
		kappa.setFromBytes(fileIOService.readFile(runningDataPaths.get("public keys")+"kappa"));
		nu.setFromBytes(fileIOService.readFile(runningDataPaths.get("public keys")+"nu"));
		
		//read proof
		Element phy = pairing.getG1().newElement().getImmutable().setToRandom();
		Element sigma = pairing.getG1().newElement().getImmutable().setToRandom();
		
		BigInteger y = new BigInteger(fileIOService.readFile(runningDataPaths.get("proof info")+"y"));
		
		phy.setFromBytes(fileIOService.readFile(runningDataPaths.get("proof info")+"phy"));
		sigma.setFromBytes(fileIOService.readFile(runningDataPaths.get("proof info")+"sigma"));
		
		//compute eta
		BigInteger eta = BigInteger.ZERO;
		for(int i =0;i<challBlockIds.length;i++){
			String blockName = fileName+"_"+challBlockIds[i];
			eta = eta.add(mu.pow(Integer.valueOf(challBlockIds[i])+1).multiply(sha1Hash.hashGen(blockName)));
		}
		
		//compute left hand
		Element lefthand = pairing.getGT().newElement().setToRandom();
		Element tempKappa = pairing.getG1().newElement().getImmutable().setToRandom();
		tempKappa.set(kappa);
		lefthand.set(pairing.pairing(u.pow(eta.mod(q)), tempKappa));
		tempKappa.set(kappa);
		lefthand.mul(pairing.pairing(phy, kappa.pow(r.negate().mod(q)).mul(nu)).mul(pairing.pairing(g,tempKappa.pow(y.mod(q)))));
		
		//compute right hand
		Element righthand = pairing.getGT().newElement().setToRandom();
		righthand.set(pairing.pairing(sigma,g));
		
		//verify
		if(lefthand.isEqual(righthand)){
			System.out.println(fileName+" is Stored Correctly on the Server!");
		}else{
			System.out.println("Warning: "+fileName+" is Corrupted on the Servier!!! Please check it!");
		}
	}
	
	public void verifyIntegrity(String fileName){
		Pairing pairing = initializationService.pairingInitialization();
		
		System.out.println("Start Verification...\n");
		
		try {
			verify(pairing, fileName);
		} catch (Exception e) {
			fileIOService.logErrors(e);

			System.out.println("Verification Process Failed!!!\n\nPlease Check Error Logs For Detials!!!");
		}
		
	}
}
