package audit;

import initialization.InitializationService;
import initialization.impl.InitializationServiceImpl;
import it.unisa.dia.gas.jpbc.Pairing;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import utilities.io.FileIOService;
import utilities.io.impl.FileIOServiceImpl;

import com.google.common.collect.Sets;

public class Challenge {
	private FileIOService fileIOService = new FileIOServiceImpl();
	private InitializationService initializationService = new InitializationServiceImpl();
	
	//get running data path map
	private Map<String,String> runningDataPaths =initializationService.getRunningDataPaths();
	
	
	/**
	 * Challenge message contains two random number a the randomly block id set
	 * */
	private void computeChallengeMessage(Pairing pairing, int challengedBlockNum) {
		// generate two random numbers
		BigInteger mu = new BigInteger(160, 12, new SecureRandom());
		BigInteger r = new BigInteger(160, 12, new SecureRandom());
		
		//generate challenge set
		int blockNum = Integer.valueOf(fileIOService.readFileToString("resources/running files/blocks/blockNum"));
		Set<Integer> challSet = Sets.newHashSet();
		Random rand = new Random();
		while (challSet.size() < challengedBlockNum) {
			challSet.add(rand.nextInt(blockNum));
		}
		
		String blockIds = "";
		for(Integer blockId:challSet){
			blockIds +=blockId+",";
		}
		
		//output challenge message
		fileIOService.outputDataToFile(blockIds.getBytes(), runningDataPaths.get("challenge message")+"challSet");
		fileIOService.outputDataToFile(mu.toByteArray(), runningDataPaths.get("challenge message")+"mu");
		fileIOService.outputDataToFile(r.toByteArray(), runningDataPaths.get("challenge message")+"r");
	}

	//Challenged Block Number, Depend on detection probability requirement
	public void challengeMsgGeneration(int challengedBlockNum) {
		Pairing pairing = initializationService.pairingInitialization();
		
		System.out.println("Generating Challenge Message...\n");
		
		try {
			computeChallengeMessage(pairing,challengedBlockNum);

			System.out.println("Challenge Message Generated Successfully!\n");
		} catch (Exception e) {
			fileIOService.logErrors(e);

			System.out.println("Challenge Message Generated Failed!!!\n\nPlease Check Error Logs For Detials!!!");
		}
		
	}

}
