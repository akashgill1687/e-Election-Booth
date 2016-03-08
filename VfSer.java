import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import javax.crypto.Cipher;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
        int count = 0;
	int checkVoteInf = 0;
        String fileVoter = "VoterNumber";
	String result = "Result";
	String fileHistory = "History";
	String temp="tempfile";
        String line = "";
	//Code for KEYS
	final PublicKey publicKeyVoter = genPbKey(PUBLIC_KEY_VOTER);
	final PublicKey publicKeyLa = genPbKey(PUBLIC_KEY_LA);	
	final PrivateKey privateKey = genPrKey(PRIVATE_KEY_VF);	

	//Code to check whether connected Client is La Server or VoterClient
	byte[] serverInfo = encrypt("VfServer", publicKeyVoter);
	out.writeObject(serverInfo);
	byte[] clientInfo= (byte[])in.readObject();
	final String decryptClientInfo = decrypt(clientInfo, privateKey);
	if(decryptClientInfo.equals("LaServer")){
		//Write code here for La server communication
		byte[] sigToVerify= (byte[])in.readObject();
		String verifies = verSig(sigToVerify,publicKeyLa,temp);
		byte[] clientLaInfo= (byte[])in.readObject();
		final String decryptClientLaInfo = decrypt(clientLaInfo, privateKey);
		createVoterFile(decryptClientLaInfo);
			
	}
	//Else connected Client is VoterClient ans sending Voter Client information
	//that this is VF server
       else{
        //Reading the messages from Voter client
	byte[] encryptedVnumber= (byte[])in.readObject();
	final String decryptVnumber = decrypt(encryptedVnumber, privateKey);
	count = vnumberMatch(fileVoter,decryptVnumber);
	
	if (count == 0)// Not a valid vnumber
	{
		final byte[] encryptedInvalid = encrypt("invalid", publicKeyVoter);
		out.writeObject(encryptedInvalid);	
	} 
	else{//Valid Vnumber
		final byte[] encryptedValid = encrypt("valid", publicKeyVoter);
		out.writeObject(encryptedValid);//Communication when entering options for voting
	     while(true){
		byte[] encryptedOption= (byte[])in.readObject();
		final String decryptOption = decrypt(encryptedOption, privateKey);
		
		if(decryptOption.equals("1"))
		{
			//Check the VoterNumber file whether voter has already voted or not
		     checkVoteInf = checkVoterNumber(fileVoter,decryptVnumber);
		     if(checkVoteInf == 1){//Voter has not voted			
			final byte[] encryptNotVoted = encrypt("notvoted", publicKeyVoter);
			out.writeObject(encryptNotVoted);
			byte[] encryptedOption2= (byte[])in.readObject();
			final String decryptOption2 = decrypt(encryptedOption2, privateKey);
			if(decryptOption2.equals("1")) resultUpdate(result,"Bob");
			else if (decryptOption2.equals("2")) resultUpdate(result,"John");
			
			//update the voterNumber file when voting is complete
			voterNumberFileUpdate(fileVoter,decryptVnumber);
			createHistoryFile(fileHistory,decryptVnumber);
		     }else{     //Voter has already voted
			 final byte[] encryptVoted = encrypt("voted", publicKeyVoter);
			 out.writeObject(encryptVoted);
		       }
		} 
		if (decryptOption.equals("2")){
			String historyDetails = checkHistoryFile(fileHistory,decryptVnumber);
			final byte[] encryptHistory = encrypt(historyDetails, publicKeyVoter);
			out.writeObject(encryptHistory);
		}
		if (decryptOption.equals("3")){
			String resultDetails = checkResultFile(result,"Bob");
			final byte[] encryptResult = encrypt(resultDetails, publicKeyVoter);
			out.writeObject(encryptResult);		
			String resultDetails2 = checkResultFile(result,"John");
			final byte[] encryptResult2 = encrypt(resultDetails2, publicKeyVoter);
			out.writeObject(encryptResult2);		
		}
	
	      }//While ends	
	}
       }
}
	catch(Exception e)
	{
	}
          
   }//End of Main Function
	//Method for Signature Verification
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
      public String checkResultFile(String fileResult,String vnumber) throws Exception {
                String line ="";
                String history="";
                BufferedReader br;
                try {
                        br = new BufferedReader(new FileReader(fileResult));
                        try {
                                while((line = br.readLine()) != null)
                                {
                                        String[] words = line.split(" ");
                                        if (words[0].equals(vnumber))
                                        {
                                                history = line;
                                        }
					
                                }
                                br.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                          }
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                  }
                return history;
    }// end of method
      public String checkHistoryFile(String fileHistory,String vnumber) throws Exception {
                String line ="";
                String history="";
		String firstHistory="No Voting History";
		File file =new File("History");
 
    		//if file doesnt exists, then create it
    		if(!file.exists()){
    			return firstHistory;
    		}
                BufferedReader br;
                try {
                        br = new BufferedReader(new FileReader("History"));
                        try {
                                while((line = br.readLine()) != null)
                                {
                                        String[] words = line.split(" ");
                                        if (words[0].equals(vnumber))
                                        {
                                                history = line;
						break;
                                        }
					else
					{
						history = "No Voting History, Vote First";						
					}
                                }
                                br.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                          }
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                  }
                return history;
    }// end of method
    public  void createHistoryFile(String fileHistory,String vnumber){
      		try{
 
    		File file =new File("History");
 
    		//if file doesnt exists, then create it
    		if(!file.exists()){
    			file.createNewFile();
    		}
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	   	//get current date time with Date()
	   	Date date = new Date();
    		//true = append file
    		FileWriter fileWritter = new FileWriter(file.getName(),true);
    	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		bufferWritter.write(vnumber +" " + dateFormat.format(date));
		bufferWritter.newLine();
		bufferWritter.close();
 
 
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }	

    public void resultUpdate(String result,String nomineeName) throws Exception {
                String line ="";
		String fileTemp="temporary";
		int num=0;
                BufferedReader br;
		BufferedWriter bw;
                try {
                        br = new BufferedReader(new FileReader(result));
		    try {
			bw = new BufferedWriter(new FileWriter(fileTemp));
                        try {
                                while((line = br.readLine()) != null)
                                {
                                        String[] words = line.split(" ");
                                        if (words[0].equals(nomineeName))
                                        {
                                                num = Integer.parseInt(words[1]);
						num = num + 1;
						line = line.replace(words[0]+" "+ words[1], words[0] +" "+ num);
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
	File oldFile = new File(result);
        oldFile.delete();

      // And rename tmp file's name to old file name
      File newFile = new File(fileTemp);
      newFile.renameTo(oldFile);
                
    }// end of method
    public void voterNumberFileUpdate(String fileVoter,String vnumber) throws Exception {
                String line ="";
		String fileTemp="temporary2";
		int num=0;
                BufferedReader br;
		BufferedWriter bw;
                try {
                        br = new BufferedReader(new FileReader(fileVoter));
		    try {
			bw = new BufferedWriter(new FileWriter(fileTemp));
                        try {
                                while((line = br.readLine()) != null)
                                {
                                        String[] words = line.split(" ");
                                        if (words[0].equals(vnumber))
                                        {
                                                num = Integer.parseInt(words[1]);
						num = 1;
						line = line.replace(words[0]+" "+ words[1], words[0] +" "+ num);
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
	File oldFile = new File(fileVoter);
        oldFile.delete();

      // And rename tmp file's name to old file name
      File newFile = new File(fileTemp);
      newFile.renameTo(oldFile);
                
    }// end of method
   public final PublicKey genPbKey(final String key){
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
    public final PrivateKey genPrKey(final String key){
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
    public  byte[] encrypt(String text, PublicKey key) {
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
  public int vnumberMatch(String fileVoter,String vnumber) throws Exception {
                String line ="";
                int counter=0;
                BufferedReader br;
		File file =new File(fileVoter);
 
    		//if file doesnt exists, then create it
    		if(!file.exists()){
    			return counter;
    		}
                try {
                        br = new BufferedReader(new FileReader(fileVoter));
                        try {
                                while((line = br.readLine()) != null)
                                {
                                        String[] words = line.split(" ");
                                        if (words[0].equals(vnumber))
                                        {
                                                counter = 1;
                                        }
                                }
                                br.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                          }
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                  }
                return counter;
    }// end of method
    public int checkVoterNumber(String fileVoter,String vnumber) throws Exception {
                String line ="";
                int counter=0;
                BufferedReader br;
                try {
                        br = new BufferedReader(new FileReader(fileVoter));
                        try {
                                while((line = br.readLine()) != null)
                                {
                                        String[] words = line.split(" ");
                                        if (words[0].equals(vnumber) && words[1].equals("0"))
                                        {
                                                counter = 1;
                                        }
                                }
                                br.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                          }
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                  }
                return counter;
    }// end of method
    public  void createVoterFile(String vnumber){
      		try{
 
    		File file =new File("VoterNumber");
 
    		//if file doesnt exists, then create it
    		if(!file.exists()){
    			file.createNewFile();
    		}
		/////////////////////
		String line ="";
                //int counter=0;
                BufferedReader br;
                try {
                        br = new BufferedReader(new FileReader("VoterNumber"));
                        try {
                                while((line = br.readLine()) != null)
                                {
                                        String[] words = line.split(" ");
                                        if (words[0].equals(vnumber))
                                        {
                                                return;
                                        }
                                }
                                br.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                          }
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                  }
		/////////////////// 
    		//true = append file
    		FileWriter fileWritter = new FileWriter(file.getName(),true);
    	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
    	        bufferWritter.write(vnumber + " 0");
		bufferWritter.newLine();
    	        bufferWritter.close();
 
 
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }	

}
