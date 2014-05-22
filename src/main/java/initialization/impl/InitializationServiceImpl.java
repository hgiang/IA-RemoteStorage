package initialization.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;

import initialization.InitializationService;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class InitializationServiceImpl implements InitializationService{
	public Pairing pairingInitialization(){
		PairingFactory.getInstance().setUsePBCWhenPossible(true);
		
		/*In this implementation, we use Type A symmetric pairing to achieve 1024 bits Dlog security,
		 *it can be changed according to the security requirement*/		
		Pairing pairing = PairingFactory
				.getPairing("resources/jpbc/propertities/pairing/a/a_181_603.properties");
		return pairing;
	}

	public Map<String, String> getRunningDataPaths() {
		Map<String, String> runningDataPaths = Maps.newHashMap();
		runningDataPaths.put("public keys", "resources/running files/keys/public keys/");
		runningDataPaths.put("master keys", "resources/running files/keys/master keys/");
		runningDataPaths.put("blocks", "resources/running files/blocks/");
		runningDataPaths.put("signatures", "resources/running files/signatures/");
		runningDataPaths.put("challenge message", "resources/running files/challenge msg/");
		runningDataPaths.put("proof info", "resources/running files/proof info/");
		
		// create paths if not exist
		for (String path : runningDataPaths.values()) {
			createFolder(path);
		}

		// create log file if not exist
		createFile("logs/Error Log.txt");

		return runningDataPaths;
	}

	private void createFolder(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	private void createFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
