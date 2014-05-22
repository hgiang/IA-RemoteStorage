IA-RemoteStorage
================

IA-RemoteStorage is the prototype implementation of an efficient Remote Data Integrity Auditing tool for outsourced data storage (e.g., cloud storage). 

The background and theoretical design of this tool are available in paper:
["PCPOR: Public and Constant-Cost Proofs of Retrievability in Cloud"](http://ualr.edu/jxyuan/pdfs/PCPOR.pdf) [[bib]](http://dl.acm.org/citation.cfm?id=2484408).

This tool is developed using The Java Pairing-Based Cryptography Library [JPBC](http://gas.dia.unisa.it/projects/jpbc/#.U3vExPldV8E). All required JPBC jars are already included in the project folder. You can directly use [Maven](http://maven.apache.org/) to setup this tool.

Note: This tool may have some unknown bugs, please feel free to contact me (jxyuan@ualr.edu) if you have any questions and suggestions. 



Basic Installation
------------------

As JPBC Libraries involved in this project are not in Maven Central Repository, you need to install it by yourself as:

mvn install:install-file -Dfile=jpbc-api-1.2.1.jar -DgroupId=it.unisa.dia.gas -DartifactId=jpbc-api -Dversion=1.2.1 -Dpackaging=jar

mvn install:install-file -Dfile=jpbc-pbc-1.2.1.jar -DgroupId=it.unisa.dia.gas -DartifactId=jpbc-pbc -Dversion=1.2.1 -Dpackaging=jar

mvn install:install-file -Dfile=jpbc-plaf-1.2.1.jar -DgroupId=it.unisa.dia.gas -DartifactId=jpbc-plaf -Dversion=1.2.1 -Dpackaging=jar

mvn install:install-file -Dfile=jna-3.2.5.jar -DgroupId=it.unisa.dia.gas -DartifactId=jna -Dversion=3.2.5 -Dpackaging=jar


Basic Usage
------------------
1. To use this software for remote data integrity auditing, the user first need to generate the public keys and master       keys for the system with the "KeyGen" class. User should set path to save his/her keys.
 
2. By using the "FileSetup", use can process the file for storage and generate the corresponding signatures and data    blocks.

3. To perform an integrity auditing, any entity with the public key can generate the challenge message with the "Challenge" class.

4. On receiving the challenge message and public keys, the storage server with data block should be able to generate the 
   proof information.

5. The final integrity verification can be performed by the auditing entity with the "Verification" Class.


Test Cases
------------------
Simple unit tests for all important classes and functions are provided in the project's test folder.
