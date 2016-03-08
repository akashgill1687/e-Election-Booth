Name and Email : Akash Gill <agill5@binghamton.edu>
The language used is Java
Platform is Linux.
To execute the program : 
	Type the following commands in command line:
	1. Type make
		Program will run on three terminals:
	2. First start the VF server on 1st Terminal by typing : 
		java VfSer <VF server's port number>
	3. Then start the LA server on 2nd Terminal by typing :
		java LaSer <LA server's port number> <VF server's domain> <VF server's port>
	4. Then connect Voter Client to LA server on 3rd Terminal by typing :
		java VoterCli <LA server's domain> <LA server's port> 
	
	Voter-cli will prompt the voter to enter name and SSN.
	
	Please enter name and SSN according to Status file. Also enteries are Case-Sensitive.
	
	After entering name and SSN, Voter-cli will send encrypted name and SSN to LA server.
	Then LA server will decrypt the message and print the name of the voter on the screen.
	The LA server will check whether the voter is a citizen.
	If not, LA sends “no” to voter-cli and voter-cli will prints “you are not eligible to vote” and terminates
	the connection with LA.
	Otherwise, the server will check whether the user already has a validation number.
	If not, the server will randomly generates an 8-digit validation number vnumber and send
	encrypted vnumber to the voter, stores vnumber in file verify, connects to VF, and
	sends signature and encrypted vnumber to the VF server.
	Otherwise, the server sends the vnumber in file verify to voter-cli.
	Voter-cli will decrypt the message, print vnumber, and terminate the connection with LA.

	5. Then connect Voter Client to VF server by typing :
		java VoterCli <VF server's domain> <VF server's port>
	Voter will be asked to enter validation number.
	After entering vnumber, voter-cli sends encrypted vnumber to the VF server
	VF server checks whether vnumber is "Valid" or not. 
	If not, the VF server sends a message “invalid” to voter-cli and voter-cli prints
	“Invalid verification number” and terminates the connection.
	Then you have to repeat step 5 in order to connect to VF again.
	After entering valid vnumber :
		Please enter a number (1-4)
		1. Vote
		2. My vote history
		3. View the latest results
		4. Quit
	Entering will check whether voter has already voted or not first. if voted it will display "Already voted ".
	Else: Please enter a number (1-2)
		1. Bob
		2. John
	Entering 1 or 2 will update the results of voting.
		2. My vote history --> will display time and date when voter voted.
		3. View the latest results --> will display latest results 
		4. Quit --> will terminate the connection.
	
	Files used or updated during the process are Status,verify,VoterNumber, History and Result.

Core code for encryption/decrption:
	ALGORITHM is "RSA".
	Code for encryption is method encrypt:
    public  byte[] encrypt(String text, PublicKey key) {
    byte[] cipherText = null;
    try {
      // get an RSA cipher object and print the provider
      final Cipher cipher = Cipher.getInstance(ALGORITHM);//final is used for cipher generated can't be changed
      // encrypt the plain text using the public key
      cipher.init(Cipher.ENCRYPT_MODE, key);//cipher intialization
      cipherText = cipher.doFinal(text.getBytes());// converting text into bytes 
    } catch (Exception e) {
      e.printStackTrace();
    }
    return cipherText;
    }    
	Code for decryption is method decrypt: 
    public  String decrypt(byte[] text, PrivateKey key) {
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
    
	Signature is also used :
    	Code for generating Signature is in method genSig:
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
	Code for verifying signature is in method verSig:
	public static String verSig(byte[] sigToVerify, PublicKey publicKeyLa,String temp){
		byte[] signVerify = null;
		signVerify = sigToVerify;
	     try{
		Signature sig = Signature.getInstance("MD5WithRSA");
		sig.initVerify(publicKeyLa);
		FileInputStream datafis = new FileInputStream(temp);
		BufferedInputStream bufin = new BufferedInputStream(datafis);

		byte[] buffer = new byte[1024];
		int len;
		while (bufin.available() != 0) {
    		len = bufin.read(buffer);
    		sig.update(buffer, 0, len);
		}
		bufin.close();
		
		boolean verifies = sig.verify(signVerify);
		if(verifies) return "yes";
		else return "no";
		}
		catch (Exception ex) {
      			ex.printStackTrace();
			return "no";
    		}		
	}

Core code for implementing the concurrent server:

public class VfSer {
	public static void main(String argv[])throws Exception
	{
		int socketNumber=Integer.parseInt(argv[0]);
        	ServerSocket listen = new ServerSocket(socketNumber);
		while(true)
		{
			Socket conn = listen.accept();
			new MyThread(conn);
		}
	}

}


class MyThread implements Runnable
{
    	public final String ALGORITHM = "RSA";
    	public final String PRIVATE_KEY_VF = "Priv(VF)";
    	public final String PUBLIC_KEY_VOTER = "Pub(Voter)";
    	public final String PUBLIC_KEY_LA = "Pub(LA)";	
 	private ObjectInputStream in;
	private ObjectOutputStream out;
	public MyThread(Socket conn)
	{
		try
		{
			in = new ObjectInputStream(conn.getInputStream());
			out = new ObjectOutputStream(conn.getOutputStream());
	  		(new Thread(this)).start();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}	
	}
	public void run()
	{
       try{
	//Code for VF server
	}
	catch(Exception e)
	{
	}
          
}

Important notes: My submission do contain the source code,README file and important files like Status, verify and Result by default.Please keep them while executing the program, you can check them as all are initialized. Rest all files are generated during runtime like History , VoterNumber and any other temporary files. Also, Please enter name and SSN according to Status file. Enteries are Case-Sensitive.
