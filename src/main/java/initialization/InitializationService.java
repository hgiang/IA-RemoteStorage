package initialization;

import java.util.Map;

import it.unisa.dia.gas.jpbc.Pairing;

public interface InitializationService {
	Pairing pairingInitialization();
	
	Map<String,String> getRunningDataPaths();
}
