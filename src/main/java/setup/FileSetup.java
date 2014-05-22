package setup;

import initialization.InitializationService;
import initialization.impl.InitializationServiceImpl;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.util.Arrays;

import java.math.BigInteger;
import java.util.Map;

import utilities.hash.Sha1Hash;
import utilities.io.FileIOService;
import utilities.io.impl.FileIOServiceImpl;

import com.google.common.collect.Maps;

public class FileSetup {
	private FileIOService fileIOService = new FileIOServiceImpl();
	private InitializationService initializationService = new InitializationServiceImpl();
	private Sha1Hash sha1Hash = new Sha1Hash();
	
	//Block Size (bytes)
	private int blockLength = 4096;
	//Element Size (bytes)
	private int elementLength = 128;
	//block ElementNum =blockLength/elementLength
	private int normalBlockElementNum = 32;
	
	//get running data path map
	private Map<String,String> runningDataPaths =initializationService.getRunningDataPaths();
	
	private Map<String,byte[]> processFile(String inputFileName,String inputFilePath) {
		Map<String,byte[]> blocks = Maps.newHashMap();
		
		try {

			byte[] inputBytes = fileIOService.readFile(inputFilePath);
			
			int blockNum = inputBytes.length / blockLength + 1;
			
			for (int i = 0; i < blockNum; i++) {
				String blockIndex = inputFileName+"_"+i;
				
				byte[] block;
				
				// check if reach the last block
				if (i == blockNum - 1) {
					block = Arrays.copyOfRange(inputBytes, i * blockLength, inputBytes.length);
				} else {
					block = Arrays.copyOfRange(inputBytes, i * blockLength, (i+1) * blockLength);
				}
				
				blocks.put(blockIndex,block);
				
				//save blocks
				fileIOService.outputDataToFile(block, runningDataPaths.get("blocks")+"block_"+i);
				//save block number
				byte[] blockNums = (blockNum+"").getBytes();
				fileIOService.outputDataToFile(blockNums, runningDataPaths.get("blocks")+"blockNum");
			}

		} catch (Exception e) {
			System.out.println("There are Errors in File Processing!!!\n\nPlease Check Error Logs For Detials!!!");
		}
		
		return blocks;
	}
	
	private void signatureGenOfAdmin(Pairing pairing, Map<String,byte[]> blocks){
		
		//Read public keys and master keys
		Element g = pairing.getG1().newElement().getImmutable().setToRandom();
		Element u = pairing.getG1().newElement().getImmutable().setToRandom();
		
		g.setFromBytes(fileIOService.readFile(runningDataPaths.get("public keys")+"g"));
		u.setFromBytes(fileIOService.readFile(runningDataPaths.get("public keys")+"u"));
		
		BigInteger q = new BigInteger(fileIOService.readFile(runningDataPaths.get("public keys")+"q"));
		
		//read master keys
		BigInteger epslon0 = new BigInteger(fileIOService.readFile(runningDataPaths.get("master keys")+"epsilon_0"));
		BigInteger alpha = new BigInteger(fileIOService.readFile(runningDataPaths.get("master keys")+"alpha"));
		
		
		
		//Signature Generation
		for (String blockId : blocks.keySet()) {
			try {
				BigInteger beta = BigInteger.ZERO;
				byte[] block = blocks.get(blockId);

				//last data block may smaller than 4K, so may have less elements
				int blockElementNum = block.length < blockLength?block.length/elementLength+1:normalBlockElementNum;
				for (int i = 0; i < blockElementNum; i++) {
					BigInteger blockEleBigInt = BigInteger.ZERO;
					byte[] blockEle;
					
					if (i < blockElementNum - 1) {
						blockEle = Arrays.copyOfRange(block, i
								* elementLength, (i + 1) * elementLength);
					} else {
						blockEle = Arrays.copyOfRange(block, i
								* elementLength, block.length);
					}
					try{
						blockEleBigInt = new BigInteger(blockEle);
					}catch(Exception e){
						//skip no value block
						continue;
					}
					beta = beta.add(blockEleBigInt.multiply(alpha
							.pow(i + 2)));
				}

				Element blockSig = pairing.getG1().newElement().getImmutable().set(g);
				Element blockSigUPart = pairing.getG1().newElement().getImmutable().setToRandom().set(u);

				blockSigUPart.pow(sha1Hash.hashGen(blockId));

				blockSig.pow(beta.mod(q));
				blockSig = blockSig.mul(blockSigUPart);
				blockSig = blockSig.pow(epslon0);
				
				fileIOService.outputDataToFile(blockSig.toBytes(),runningDataPaths.get("signatures")+"/sig_" + blockId);
			} catch (Exception e) {
				System.out.println("There are Errors in File Processing!!!\n\nPlease Check Error Logs For Detials!!!");
			}
		}
	}
	
	public void fileSetupByAdmin(String fileName,String filepath){
		Pairing pairing  = initializationService.pairingInitialization();
		
		System.out.println("File Setup...\n");
		
		try{
			Map<String,byte[]> blocks = processFile(fileName, filepath);
			signatureGenOfAdmin(pairing,blocks);
			
			System.out.println("File Setup Successfully!\n");
		}catch(Exception e){
			fileIOService.logErrors(e);

			System.out.println("File Setup Failed!!!\n\nPlease Check Error Logs For Detials!!!");
		}
	}
	
}
