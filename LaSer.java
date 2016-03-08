import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import javax.crypto.Cipher;

public class LaSer {
    public static final String ALGORITHM = "RSA";
    public static final String PRIVATE_KEY_FILE = "Priv(LA)";
    public static final String PUBLIC_KEY_VOTER = "Pub(Voter)";
    public static final String PUBLIC_KEY_VF = "Pub(VF)";
    public static int flag=0;
    public static void main(String argv[]) throws Exception{
	int count = 0;
    	String fileStatus = "Status";
    	String fileVerify = "verify";
    	int vnumber=0;
    	String line = "";
	String v="";
	int socketNumber=Integer.parseInt(argv[0]);
        ServerSocket listen = new ServerSocket(socketNumber);
      while(true){	
	Socket conn = listen.accept();
	ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
	ObjectOutputStream out =
	     new ObjectOutputStream(conn.getOutputStream());
	//Code for KEYS
	final PublicKey publicKeyVoter = genPbKey(PUBLIC_KEY_VOTER);
	final PublicKey publicKeyVf = genPbKey(PUBLIC_KEY_VF);	
	final PrivateKey privateKey = genPrKey(PRIVATE_KEY_FILE);	
	//Code to specify it is a LA Server to Voter     
	byte[] serverInfo = encrypt("LaServer", publicKeyVoter);
	out.writeObject(serverInfo);
	//Reading the messages from Voter client
	byte[] voterName= (byte[])in.readObject();
	byte[] voterSsn= (byte[])in.readObject();
	//Decrytion Code
	final String decryptVoterName = decrypt(voterName, privateKey);
	System.out.println("Voter Name:"+ decryptVoterName);
	final String decryptSsn = decrypt(voterSsn, privateKey);
	count = votingEligibility(fileStatus,decryptVoterName,decryptSsn);
        // Encrypt the string using the public key of Voter
	final byte[] encryptedNo = encrypt("No", publicKeyVoter);
	final byte[] encryptedNoVf = encrypt("No", publicKeyVf);
	if(count==0){//Not a vaild voter "Check once"
		out.writeObject(encryptedNo);
	}
	else{
		vnumber=getVnumber(fileVerify,decryptSsn);
		String serverMachine=argv[1];
		int serverPort=Integer.parseInt(argv[2]);
		Socket sock = new Socket(serverMachine,serverPort);
		ObjectOutputStream outvf = new
		ObjectOutputStream(sock.getOutputStream());
		ObjectInputStream invf =new 
		ObjectInputStream(sock.getInputStream());
		if(flag==1){ // Means that a new vnumber is generated
			
			final byte[] encryptedVnumber = encrypt(Integer.toString(vnumber), publicKeyVoter);
			out.writeObject(encryptedVnumber);
			byte[] vfInfo= (byte[])invf.readObject();
						
			byte[] clientInfo = encrypt("LaServer", publicKeyVf);
			outvf.writeObject(clientInfo);
			byte [] realSig=genSig(vnumber+"", privateKey);//Generating signature
			outvf.writeObject(realSig);
			final byte[] encryptedVnumberVf = encrypt(Integer.toString(vnumber), publicKeyVf);
			outvf.writeObject(encryptedVnumberVf);
		 }else{    // A new vnumber is not generated
			final byte[] encryptedVnumber = encrypt(Integer.toString(vnumber), publicKeyVoter);
			out.writeObject(encryptedVnumber);
			final byte[] encryptedVnumberVf = encrypt("Vnumber Exists", publicKeyVf);
			outvf.writeObject(encryptedVnumberVf);
			
		 }
		sock.close();			
	}
     
	  conn.close();
      }//while(true) ends
	
   }//End of Main Function
    public static byte[] genSig(String vnumber,PrivateKey privateKey){
		createTempFile((vnumber));
		byte[] realSig = null;
		// Code for generating the signature
	    try{
		Signature dsa = Signature.getInstance("MD5WithRSA");
		dsa.initSign(privateKey);
		FileInputStream fis = new FileInputStream("tempfile");
		BufferedInputStream bufin = new BufferedInputStream(fis);
		byte[] buffer = new byte[1024];
		int len;
		while ((len = bufin.read(buffer)) >= 0) {
    		dsa.update(buffer, 0, len);
		}
		bufin.close();
		realSig = dsa.sign();
	     }catch(Exception e){
	      }
	     return realSig;
    }
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

    public static int randInt(int min, int max) {

    // NOTE: Usually this should be a field rather than a method
    // variable so that it is not re-seeded every call.
    Random rand = new Random();

    // nextInt is normally exclusive of the top value,
    // so add 1 to make it inclusive
    int randomNum = rand.nextInt((max - min) + 1) + min;

    return randomNum;
    }
    public static int votingEligibility(String fileStatus,String inputSearch, String ssn) throws Exception {
                String line ="";
                int count=0;
                BufferedReader br;
                try {
                        br = new BufferedReader(new FileReader(fileStatus));
                        try {
                                while((line = br.readLine()) != null)
                                {
                                        String[] words = line.split(" ");
                                  if (words[0].equals(inputSearch) && words[1].equals(ssn) && words[2].equals("citizen"))
                                        {
                                                count = 1;
                                        }
                                }
                                br.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                          }
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                  }
                return count;
    }// end of method
     public static int getVnumber(String fileVerify,String inputSearch) throws Exception{
     	String fileTemp="temp";
	int randomNumber=0;
	String line="";
	BufferedReader br;
	BufferedWriter bw;
	try
	{
        br = new BufferedReader(new FileReader(fileVerify));
         try
         {
            bw = new BufferedWriter(new FileWriter(fileTemp));
          try 
          {
            while((line = br.readLine()) != null)
            {
                String[] words = line.split(" ");
                  if (words[0].equals(inputSearch) && words[1].equals("0"))  
                  {
                        randomNumber=randInt(10000000, 99999999);

                        line = line.replace(words[0]+" "+ words[1], words[0] +" "+ randomNumber);
                        bw.write(line+"\n");
			flag=1;
                  }
                  else if (words[0].equals(inputSearch) && (!words[1].equals("0")))
                  {
                        randomNumber=Integer.parseInt(words[1]);
                        bw.write(line+"\n");
                  }
                  else
                  {
                        bw.write(line+"\n");
                   }
            }
            
            br.close();
            bw.close();
          } catch (IOException e) {
                e.printStackTrace();
            }
         } catch (IOException e) {
                e.printStackTrace();
           }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
        File oldFile = new File(fileVerify);
        oldFile.delete();

      // And rename tmp file's name to old file name
      File newFile = new File(fileTemp);
      newFile.renameTo(oldFile);
	return randomNumber;
     }
     public static void createTempFile(String vnumber){
      		FileOutputStream fop = null;
		File file;
		try {
 
			file = new File("tempfile");
			fop = new FileOutputStream(file);
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			// get the content in bytes
			byte[] contentInBytes = vnumber.getBytes();
 
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
 
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
     }
    
}
