package prove;

import java.math.BigInteger;
import java.util.Map;

import utilities.io.FileIOService;
import utilities.io.impl.FileIOServiceImpl;

import com.google.common.collect.Maps;

import initialization.InitializationService;
import initialization.impl.InitializationServiceImpl;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.util.Arrays;

public class ProofGen {
	private InitializationService initializationService = new InitializationServiceImpl();
	private FileIOService fileIOService = new FileIOServiceImpl();
	
	//get running data path map
	private Map<String,String> runningDataPaths =initializationService.getRunningDataPaths();
	
	
	//lowest and highest degrees
	private int polyCoefficientSize = 32;
	private int normalBlockElementNum = 32;
	
	//Block Size (bytes)
	private int blockLength = 4096;
	//Element Size (bytes)
	private int elementLength = 128;
	
	private BigInteger[] polynomialDivision(BigInteger r,BigInteger[] coefficientVecA){
		
		//the size increase by one, since we will have a constant term
		BigInteger[] coefficients = new BigInteger[polyCoefficientSize+1];
		for(int i =0;i<coefficients.length;i++){
			coefficients[i] = BigInteger.ZERO;
		}
		for(int i=2;i<polyCoefficientSize+2;i++){
			for(int j=0;j<i;j++){
				//constant term
				coefficients[j] = coefficients[j].add(coefficientVecA[i-2].multiply(r.pow(i-j-1)));
			}
		}
		
		return coefficients;
	}
	
	private BigInteger[] generateCoefficientVecA(Map<Integer,byte[]> blocksMap, BigInteger mu){
		BigInteger[] coefficientVecA =  new BigInteger[polyCoefficientSize];
		//initialize coefficientVecA
		for(int i=0;i<coefficientVecA.length;i++){
			coefficientVecA[i] = BigInteger.ZERO;
		}
		
		
		for (Integer blockId : blocksMap.keySet()) {
			try {
				byte[] block = blocksMap.get(blockId);
				
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

					coefficientVecA[i] = coefficientVecA[i].add(blockEleBigInt.multiply(mu.pow(blockId+1)));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return coefficientVecA;
	}
	
	/**
	 * Proof information contains three element phy,y and sigma
	 * */
	private void computeProofInfo(Pairing pairing,String fileName){
		//read challenge message
		BigInteger mu = new BigInteger(fileIOService.readFile(runningDataPaths.get("challenge message")+"mu"));
		BigInteger r = new BigInteger(fileIOService.readFile(runningDataPaths.get("challenge message")+"r"));
		String[] challBlockIds = fileIOService.readFileToString(runningDataPaths.get("challenge message")+"challSet").split(",");
		
		//read public key
		Element g = pairing.getG1().newElement().getImmutable().setToRandom();
		g.setFromBytes(fileIOService.readFile(runningDataPaths.get("public keys")+"g"));
		BigInteger q = new BigInteger(fileIOService.readFile(runningDataPaths.get("public keys")+"q"));
		Element[] gAlphas = new Element[polyCoefficientSize+1];
		for(int i = 0;i<gAlphas.length;i++){
			Element gAlpha = pairing.getG1().newElement().getImmutable().setToRandom();
			gAlpha.setFromBytes(fileIOService.readFile(runningDataPaths.get("public keys")+"gAlpha_"+(i+1)));
			gAlphas[i] = gAlpha;
		}
		
		//read blocks and signatures
		Element[] signatures = new Element[challBlockIds.length];
		Map<Integer,byte[]> blocksMap = Maps.newHashMap();
		for(int i =0;i<challBlockIds.length;i++){
			int challBlockId = Integer.valueOf(challBlockIds[i]);
			Element sig = pairing.getG1().newElement().getImmutable().setToRandom();
			sig.setFromBytes(fileIOService.readFile(runningDataPaths.get("signatures")+"/sig_"+fileName+"_"+challBlockId));
			signatures[i] = sig;
			blocksMap.put(challBlockId,fileIOService.readFile(runningDataPaths.get("blocks")+"block_"+challBlockId));
		}
		
		
		//compute proof information
		BigInteger[] coefficientVecA =  generateCoefficientVecA(blocksMap,mu);
		BigInteger[] coefficientVecW =  polynomialDivision(r,coefficientVecA);
		Element sigma = pairing.getG1().newElement().getImmutable().setToOne();
		Element phy = pairing.getG1().newElement().getImmutable().setToOne();
		phy.set(g);
		//constant term
		phy.pow(coefficientVecW[0]);
		BigInteger y = BigInteger.ZERO;
		
		for(int i=0;i<challBlockIds.length;i++){
			//compute sigma
			Element sig = pairing.getG1().newElement().set(signatures[i]);
			sigma.mul(sig.pow((mu.pow(Integer.valueOf(challBlockIds[i])+1)).mod(q)));
		}
		
		for(int j =0;j<coefficientVecA.length;j++){
			//compute y
			y = y.add(coefficientVecA[j].multiply(r.pow(j+2)));
		
			//compute phy
			Element gAlpha = pairing.getG1().newElement().getImmutable().setToRandom();
			gAlpha.set(gAlphas[j]);
			phy.mul(gAlpha.pow(coefficientVecW[j+1].mod(q)));
		}

		
		//output proofinformation
		fileIOService.outputDataToFile(sigma.toBytes(), runningDataPaths.get("proof info")+"sigma");
		fileIOService.outputDataToFile(phy.toBytes(), runningDataPaths.get("proof info")+"phy");
		fileIOService.outputDataToFile(y.toByteArray(), runningDataPaths.get("proof info")+"y");
	}
	
	public void proofGeneration(String fileName){
		Pairing pairing = initializationService.pairingInitialization();
		
		System.out.println("Generating Proof Information...\n");
		
		try {
			computeProofInfo(pairing,fileName);

			System.out.println("Proof Information Generated Successfully!\n");
		} catch (Exception e) {
			fileIOService.logErrors(e);

			System.out.println("Proof Information Generated Failed!!!\n\nPlease Check Error Logs For Detials!!!");
		}
		
	}
}
