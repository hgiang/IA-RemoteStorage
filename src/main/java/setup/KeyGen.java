package setup;

import initialization.InitializationService;
import initialization.impl.InitializationServiceImpl;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utilities.io.FileIOService;
import utilities.io.impl.FileIOServiceImpl;

import com.google.common.collect.Maps;

/**
 * The Key Generation Class used to generate public keys, master keys of the system. 
 * **/
public class KeyGen {
	private FileIOService fileIOService = new FileIOServiceImpl();
	private InitializationService initializationService = new InitializationServiceImpl();
	
	//get running data path map
	private Map<String,String> runningDataPaths =initializationService.getRunningDataPaths();
	
	private void publicAndMasterKeysGeneration(Pairing pairing){
		//q is the order of the Group G1, g and u are two random generators of G1
		BigInteger q = pairing.getG1().getOrder();
		Element g = pairing.getG1().newElement().getImmutable().setToRandom();
		Element u = pairing.getG1().newElement().getImmutable().setToRandom();
		
		//alpha and epsilon_0 are two are random numbers
		BigInteger alpha = new BigInteger(160, 12, new SecureRandom());
		BigInteger epsilon_0 = new BigInteger(160, 12, new SecureRandom());
		
		Element kappa = pairing.getG1().newElement().getImmutable().set(g);
		Element nu = pairing.getG1().newElement().getImmutable().set(g);
		kappa.pow(epsilon_0);
		nu.pow(epsilon_0.multiply(alpha));

		/*
		 * generate g^Aplaha^x, the degree "x" can be adjusted according the
		 * system requirement. Here, we set it as 32, since we use 4K block and
		 * 1024bits element, so we need g^alpha^2 to g^alpha^33, g^alpha is used
		 * to generate proof information
		 */
		List<Element> polyTerms = new ArrayList<Element>();
		for (int i = 1; i < 34; i++) {
			Element polyTerm  = pairing.getG1().newElement().getImmutable().setToRandom();
			polyTerm.set(g);
			
			polyTerms.add(polyTerm.pow(alpha.pow(i).mod(q)));
		}
		
		// save public keys
		Map<String,byte[]> publicKeyMap = Maps.newHashMap();
		publicKeyMap.put(runningDataPaths.get("public keys")+"g", g.toBytes());
		publicKeyMap.put(runningDataPaths.get("public keys")+"u", u.toBytes());
		publicKeyMap.put(runningDataPaths.get("public keys")+"q", q.toByteArray());
		publicKeyMap.put(runningDataPaths.get("public keys")+"nu", nu.toBytes());
		publicKeyMap.put(runningDataPaths.get("public keys")+"kappa", kappa.toBytes());
		
		for(int i=1;i<polyTerms.size()+1;i++){
			publicKeyMap.put(runningDataPaths.get("public keys")+"gAlpha_"+i, polyTerms.get(i-1).toBytes());
		}
		
		for(String outputFileName:publicKeyMap.keySet()){
			fileIOService.outputDataToFile(publicKeyMap.get(outputFileName),outputFileName);
		}
		
		//save master keys
		Map<String, byte[]> msKeyMap = Maps.newHashMap();
		msKeyMap.put(runningDataPaths.get("master keys")+"epsilon_0",epsilon_0.toByteArray());
		msKeyMap.put(runningDataPaths.get("master keys")+"alpha",alpha.toByteArray());
		
		for(String outputFileName:msKeyMap.keySet()){
			fileIOService.outputDataToFile(msKeyMap.get(outputFileName),outputFileName);
		}
		
	}
	
	// generate public keys and master keys
	public void generateSystemKeys() {
		// Initialize the system
		Pairing pairing = initializationService.pairingInitialization();

		System.out.println("Generating Keys...\n");

		try {
			publicAndMasterKeysGeneration(pairing);

			System.out.println("Keys Generated Successfully!\n");
		} catch (Exception e) {
			fileIOService.logErrors(e);

			System.out.println("Generate Keys Failed!!!\n\nPlease Check Error Logs For Detials!!!");
		}
	}
}
