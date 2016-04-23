import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import gnu.getopt.Getopt;


public class RSA_skeleton {
	private static BigInteger privateKey;
   	private static BigInteger publicKey;
   	private static String n;
   	private static String d;
   	private static String e; 
   	
	public static void main(String[] args){
		
		StringBuilder bitSizeStr = new StringBuilder();
		StringBuilder keyChainFile = new StringBuilder();
		StringBuilder m = new StringBuilder();
		StringBuilder encrypt = new StringBuilder();
		
		pcl(args, bitSizeStr, keyChainFile,m, encrypt);
		parseKey(keyChainFile, 1);
//		You are going to have to pull out the numbers from key Chain File
//		You can do this any way you want, this is just a suggested setup.
		StringBuilder eStr = new StringBuilder(e);
		StringBuilder nStr = new StringBuilder(n);
		StringBuilder dStr = new StringBuilder(d);
		
		if(!bitSizeStr.toString().equalsIgnoreCase("")){
			//This means you want to create a new key
			genRSAkey(bitSizeStr);
		}
		
		if(!eStr.toString().equalsIgnoreCase("")){

			RSAencrypt(m, nStr, eStr);
		}
		
		if(!dStr.toString().equalsIgnoreCase("")){

			RSAdecrypt(m, nStr, dStr);
		}
		
		
	}

	private static String parseKey (StringBuilder keyChainFile, int option)
	{
		String Skey = "";
		String Pkey = " ";
		String allLines = "";
		try {
			for(String input : Files.readAllLines(Paths.get(keyChainFile.toString()), Charset.defaultCharset()))
			{
				allLines += input;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println(allLines);
		if(allLines.equals("")) return "";
		
		String[] strings = new String[8];
		String owner = "";
		strings = allLines.split(" ");
		
		for(int i = 0; i < strings.length; i++)
		{
			if(strings[i].contains("PRIVATE"))
			{
				owner = strings[i+1];
				//System.out.println(owner);
				Skey = strings[i + 3];
				n = strings[i + 3]; //saving the value of n for encryption
				Skey += strings[i + 4];//This is here because there seems to be a space in the middle of the keys
				e = strings[i + 4];					   //Not sure if thats how its supposed to be or not
									   //There also doesnt seem to be a space at the end of one key before the next line starts
									   //Not sure if our keyFile is incorrect or not, but its a very likely possibility
				//System.out.println(Skey);
			}
		}
		
		for(int i = 0; i < strings.length; i++)
		{
			if(strings[i].contains("PUBLIC") && strings[i + 1].contains(owner) && strings[i + 1].contains("RSA"))
			{
				Pkey = strings[i + 3];
				n = strings[i + 3]
				Pkey += strings[i + 4];//This is here because there seems to be a space in the middle of the keys
				d = strings[i + 4];
				//System.out.println(Pkey);
			}
		}
		
		String output = "";
		if(option == 1)
			output = Skey;
		else 
			output = Pkey;
		return output;
	}

	private static void RSAencrypt(StringBuilder m, StringBuilder nStr, StringBuilder eStr) {
		//following the formula and using the values as big ints
		
		BigInteger n = new BigInteger(nStr.toString()); 
		BigInteger plaintext = new BigInteger(m.toString()); 
		BigInteger e = new BigInteger(eStr.toString()); 
		BigInteger cipher; 
		
		StringBuilder ciphertext =  new StringBuilder();
		
		cipher = plaintext.modPow(e, n); 
		
		ciphertext.append(cipher.toString());
		
		m = ciphertext; 
		System.out.println(m.toString(16)); 
		
	}

	private static void RSAdecrypt(StringBuilder cStr, StringBuilder nStr,
			StringBuilder dStr){
		BigInteger n = new BigInteger(nStr.toString()); 
		BigInteger c = new BigInteger(cStr.toString()); 
		BigInteger d = new BigInteger(dStr.toString()); 
		BigInteger plaintext; 
		
		StringBuilder ciphertext =  new StringBuilder();
		
		plaintext = c.modPow(d, n); 
		
		ciphertext.append(plaintext.toString());
		
		cStr = ciphertext; 
		System.out.println(cStr.toString(16)); 
	}
	
	private static void genRSAkey(StringBuilder bitSizeStr) {
   		BigInteger p;
   		BigInteger q;
   		BigInteger phi;
   		int bitLength;
   		BigInteger e; 
   		BigInteger n; 
   		BigInteger d; 
   		Random rnd = new Random(); //random for big integer
   		
		if(bitSizeStr.toString().equals("")){
			bitSizeStr.append("1024");
			bitLength = 1024;
			}
		else {
			bitLength = Integer.parseInt(bitSizeStr.toString());
		}
		
		
		p = BigInteger.probablePrime(bitLength/2, rnd);
		q = BigInteger.probablePrime(bitLength/2, rnd);
		n = p.multiply(q);
 
		phi = p.subtract(new BigInteger("1")).multiply(q.subtract(new BigInteger("1"))); 
			//computing phi with big integer arithmatic 
		e = BigInteger.probablePrime(16, rnd); 
			//create a random e
		
		//now compute e^-1 mod phi
		d = e.modInverse(phi);
		
		privateKey = d; 
		
		//now to print out both key pairs
		String hexValue = e.toString(16); 
		String hexnValue = n.toString(16); 
		System.out.println("public key e"+hexValue + " and n" +hexnValue +"\n");
		hexValue = d.toString(16); 
		hexnValue = n.toString(16); 
		System.out.println("private key d"+hexValue + " n" +hexnValue +"\n");
	}


	/**
	 * This function Processes the Command Line Arguments.
	 */
	private static void pcl(String[] args, StringBuilder bitSizeStr,
							StringBuilder keyChainFile , StringBuilder m, StringBuilder encrypt) {
		/*
		 * http://www.urbanophile.com/arenn/hacking/getopt/gnu.getopt.Getopt.html
		*/	
		Getopt g = new Getopt("Chat Program", args, "hke:d:i:b:");
		int c;
		String arg;
		while ((c = g.getopt()) != -1){
		     switch(c){
		     	  case 'i':
		        	  arg = g.getOptarg();
		        	  m.append(arg);
		        	  break;
		          case 'e':
		        	  arg = g.getOptarg();
		        	  keyChainFile.append(arg);
		        	  encrypt.append("e");
		        	  break;
		     	  case 'd':
		        	  arg = g.getOptarg();
		        	  keyChainFile.append(arg);
		        	  encrypt.append("d");
		        	  break;
		          case 'k':
		        	  break;
		     	  case 'b':
		        	  arg = g.getOptarg();
		        	  bitSizeStr.append(arg);
		        	  break;
		          case 'h':
		        	  callUsage(0);
		          case '?':
		            break; // getopt() already printed an error
		          default:
		              break;
		       }
		   }
	}
	
	private static void callUsage(int exitStatus) {
    
		String useage = "-h:				display this message\n" + 
		"-k -b <bit_size>:				generate a public/private key pair printed to std out\n" +
		"-e <key_chain_file> -i <plaintext>:		encrypt the plaintext value with the public key\n"
		+"-d <key_chain_file> -i <ciphertext_value>:	decrypt the cipher text value back to plaintext\n";
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}


}
