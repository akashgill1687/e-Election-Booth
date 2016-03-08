import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import javax.crypto.Cipher;

class VoterCli {
    public static final String ALGORITHM = "RSA";
    //public static String PRIVATE_KEY_VOTER;// = "Priv(Voter)";////////////////////
    public static final String PUBLIC_KEY_LA = "Pub(LA)";
    public static final String PUBLIC_KEY_VF = "Pub(VF)";
    public static void main(String argv[]) throws Exception {
	String serverSentence;
	String input;
	String shellcmdoutput;
	String serverMachine=argv[0];
	int serverPort=Integer.parseInt(argv[1]);
	Socket sock = new Socket(serverMachine,serverPort);
	ObjectOutputStream out = new
	ObjectOutputStream(sock.getOutputStream());
	ObjectInputStream in =
		new ObjectInputStream(sock.getInputStream());
	 BufferedReader stdIn =
                new BufferedReader(
            new InputStreamReader(System.in));
	//Code for KEYS
	final PublicKey publicKeyVf = genPbKey(PUBLIC_KEY_VF);
	final PublicKey publicKeyLa = genPbKey(PUBLIC_KEY_LA);	
	PrivateKey privateKey;// = genPrKey(PRIVATE_KEY_VOTER);///////////////	
	//Code to check whether connected Server is La Server or Vf Server
	byte[] serverInfo= (byte[])in.readObject();
	final String decryptServerInfo = decrypt(serverInfo, privateKey);
     if(decryptServerInfo.equals("LaServer")){
	System.out.print("Enter Name: ");
	String inputName=stdIn.readLine();
	privateKey= genPrKey("Priv("+inputName+")");/////////////////
	System.out.println("Private Key of voter "+"Priv("+inputName+")");////////////
	System.out.print("Enter SSN: ");
	String inputSsn=stdIn.readLine();
        // Encrypt the string using the public key LA Server
        final byte[] encryptedName = encrypt(inputName, publicKeyLa);
        final byte[] encryptedSsn = encrypt(inputSsn, publicKeyLa);
	//System.out.println("Encrypted Name:"+ encryptedName.toString());
	out.writeObject(encryptedName);
	out.writeObject(encryptedSsn);
	//Reading messages from LA Server	
	byte[] encryptedVnumber= (byte[])in.readObject();
	//Decryption Code	
	final String decryptVnumber = decrypt(encryptedVnumber, privateKey);
	if(decryptVnumber.equals("No"))	System.out.println("You are not eligible to vote");
	else System.out.println("Vnumber is:"+ decryptVnumber);
      }//When connected with Vf server
	else{
	byte[] vcinfvf = encrypt("VoterClient", publicKeyVf);
	out.writeObject(vcinfvf);
	
	System.out.print("Enter Validation Number: ");
	String inputVnumber=stdIn.readLine();
	// Encrypt the string using the public key of VF Server
        final byte[] encryptedVnumber = encrypt(inputVnumber, publicKeyVf);
	out.writeObject(encryptedVnumber);
	//Reading messages from VF Server
	byte[] encryptVfmessage = (byte[])in.readObject();
	final String decryptVfmessage = decrypt(encryptVfmessage, privateKey);
		if(decryptVfmessage.equals("invalid"))	System.out.println("Invalid Verification Number");
		else{//Valid V Number
		      while(true){			
			votingDetailSteps();			
			String inputOption=stdIn.readLine();
			if(inputOption.equals("4")) break;//Option 4
			final byte[] encryptedOption = encrypt(inputOption, publicKeyVf);
			out.writeObject(encryptedOption);//Sending option number to Vf server
		       if(inputOption.equals("1")){//Option 1
			byte[] encryptVotedOrNot = (byte[])in.readObject();
			final String decryptVotedOrNot = decrypt(encryptVotedOrNot, privateKey);
			if(decryptVotedOrNot.equals("voted")){
				System.out.println("you have already voted");		
			}
			else if(decryptVotedOrNot.equals("notvoted")){
				nomineeNames();
				String inputOption2=stdIn.readLine();
				final byte[] encryptedOption2 = encrypt(inputOption2, publicKeyVf);
				out.writeObject(encryptedOption2);
			}
		       }//Option 1 ends
			if(inputOption.equals("2")) {//Option 2
					byte[] encryptHistory = (byte[])in.readObject();
					final String decryptHistory = decrypt(encryptHistory, privateKey);
					System.out.println(decryptHistory);
			}
			if(inputOption.equals("3")) {//Option 3
					byte[] encryptHistory = (byte[])in.readObject();
					final String decryptHistory = decrypt(encryptHistory, privateKey);
					System.out.println(decryptHistory);
					byte[] encryptHistory2 = (byte[])in.readObject();
					final String decryptHistory2 = decrypt(encryptHistory2, privateKey);
					System.out.println(decryptHistory2);
			}
			
		     }//while ends
			
		}
	
	
	
	}
	sock.close();
  }//End of Main Method

   public static final PublicKey genPbKey(final String key){
	PublicKey pbKey = null;
	try{
   	ObjectInputStream inputStreampub = null;
	inputStreampub = new ObjectInputStream(new FileInputStream(key));
        pbKey = (PublicKey) inputStreampub.readObject();
	} catch (Exception e) {
      		e.printStackTrace();
         }
	return pbKey;
    }
    public static final PrivateKey genPrKey(final String key){
	PrivateKey prKey = null;
	try{
   	ObjectInputStream inputStreampr = null;
	inputStreampr = new ObjectInputStream(new FileInputStream(key));
        prKey = (PrivateKey) inputStreampr.readObject();
	} catch (Exception e) {
      		e.printStackTrace();
         }
	return prKey;
    }
  // method for encryption
  public static byte[] encrypt(String text, PublicKey key) {
    byte[] cipherText = null;
    try {
      // get an RSA cipher object and print the provider
      final Cipher cipher = Cipher.getInstance(ALGORITHM);
      // encrypt the plain text using the public key
      cipher.init(Cipher.ENCRYPT_MODE, key);
      cipherText = cipher.doFinal(text.getBytes());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return cipherText;
  }
  public static String decrypt(byte[] text, PrivateKey key) {
    byte[] dectyptedText = null;
    try {
      // get an RSA cipher object and print the provider
      final Cipher cipher = Cipher.getInstance(ALGORITHM);

      // decrypt the text using the private key
      cipher.init(Cipher.DECRYPT_MODE, key);
      dectyptedText = cipher.doFinal(text);

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return new String(dectyptedText);
    }
    public static void votingDetailSteps(){
	System.out.println("Please enter a number (1-4)");
	System.out.println("1. Vote");
	System.out.println("2. My vote history");
	System.out.println("3. View the latest results");
	System.out.println("4. Quit");	
    }
    public static void nomineeNames(){
	System.out.println("Please enter a number (1-2)");
	System.out.println("1. Bob");
	System.out.println("2. John");
    }

}

