import java.io.IOException;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.BitSet;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;

import gnu.getopt.Getopt;


public class DES_Skeleton {
	
	
	public static void main(String[] args) {
		
		StringBuilder inputFile = new StringBuilder();
		StringBuilder outputFile = new StringBuilder();
		StringBuilder keyChainFile = new StringBuilder();
		StringBuilder encrypt = new StringBuilder();
		
		pcl(args, inputFile, outputFile, keyChainFile, encrypt);
		
		if(keyChainFile.toString() != "" && encrypt.toString().equals("e")){
			encrypt(keyChainFile, inputFile, outputFile);
		} else if(keyChainFile.toString() != "" && encrypt.toString().equals("d")){
			decrypt(keyChainFile, inputFile, outputFile);
		}
		
		
	}
	

	private static void decrypt(StringBuilder keyChainFile, StringBuilder inputFile,
			StringBuilder outputFile) {
		try {
			PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			List<String> lines = Files.readAllLines(Paths.get(inputFile.toString()), Charset.defaultCharset());
			String IVStr = lines.get(0);
			lines.remove(0);
			String encryptedText;
			
			for (String line : lines) {
				encryptedText = DES_decrypt(IVStr, line);
				writer.print(encryptedText);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * TODO: You need to write the DES encryption here.
	 * @param line
	 */
	private static String DES_decrypt(String iVStr, String line) {
		
		return null;
	}


	private static void encrypt(StringBuilder keyStr, StringBuilder inputFile,
			StringBuilder outputFile) {
		
		try {
			PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			
			String encryptedText;
			for (String line : Files.readAllLines(Paths.get(inputFile.toString()), Charset.defaultCharset())) {
				encryptedText = DES_encrypt(line, keyStr);
				writer.print(encryptedText);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	/**
	 * TODO: You need to write the DES encryption here.
	 * @param line
	 */
	private static String DES_encrypt(String line,StringBuilder keyChain) {
		String key = "";
		try {
			for(String input : Files.readAllLines(Paths.get(keyChain.toString()), Charset.defaultCharset()))
			{
			key = input;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		key = key.substring(0, 16);
		System.out.println(key);
		String binary = new BigInteger(key,16).toString(2);
		System.out.println(binary);
		System.out.println(binary.length());
		BitSet keyBytes = new BitSet();
		keyBytes = keyBytes.valueOf(binary.getBytes());
		
		return null;
	}

	/*
	 * Generates a 64 bit key, checks it against list of weak keys from wikipedia, if it is a match calls itself
	 * if not prints the key in hexadecimal.
	 */
	@SuppressWarnings("static-access")
	static void genDESkey(){
		
		SecureRandom random = new SecureRandom();  //Create RNG instance
		
		byte[] bytes = new byte[7];//Change to 6 to get 56 bit key
		random.nextBytes(bytes);//Get 8 Random bytes = 64 bits
		byte[] weak1 = {(byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01};
		byte[] weak2 = {(byte)0xFE, (byte)0xFE, (byte)0xFE, (byte)0xFE, (byte)0xFE, (byte)0xFE, (byte)0xFE, (byte)0xFE};
		byte[] weak3 = {(byte)0xE0, (byte)0xE0, (byte)0xE0, (byte)0xE0, (byte)0xF1, (byte)0xF1, (byte)0xF1, (byte)0xF1};
		byte[] weak4 = {(byte)0x1F, (byte)0x1F, (byte)0x1F, (byte)0x0E, (byte)0x0E, (byte)0x0E, (byte)0x0E, (byte)0x0E};
		byte[] weak5 = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
		byte[] weak6 = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
		byte[] weak7 = {(byte)0xE1, (byte)0xE1, (byte)0xE1, (byte)0xE1, (byte)0xF0, (byte)0xF0, (byte)0xF0, (byte)0xF0};
		byte[] weak8 = {(byte)0x1E, (byte)0x1E, (byte)0x1E, (byte)0x0F, (byte)0x1E, (byte)0x1E, (byte)0x1E, (byte)0x1E};
		
		if(Arrays.equals(bytes, weak1) | Arrays.equals(bytes,  weak2) | Arrays.equals(bytes,  weak3) | 
				Arrays.equals(bytes,  weak4) | Arrays.equals(bytes,  weak5) | Arrays.equals(bytes,  weak6) | 
				Arrays.equals(bytes,  weak7) | Arrays.equals(bytes,  weak8) )
		{
				genDESkey();
		}
		else
		{
			String key = Base64.getEncoder().encodeToString(bytes);//Encode the Key
			System.out.println(key.format("%x", new BigInteger(1, key.getBytes())));
		}
		
		return;
	}


	/**
	 * This function Processes the Command Line Arguments.
	 * -p for the port number you are using
	 * -h for the host name of system
	 */
	private static void pcl(String[] args, StringBuilder inputFile,
							StringBuilder outputFile, StringBuilder keyChainFile, StringBuilder encrypt) {
		/*
		 * http://www.urbanophile.com/arenn/hacking/getopt/gnu.getopt.Getopt.html
		*/	
		Getopt g = new Getopt("Chat Program", args, "hke:d:i:o:");
		int c;
		String arg;
		while ((c = g.getopt()) != -1){
		     switch(c){
		     	  case 'o':
		        	  arg = g.getOptarg();
		        	  outputFile.append(arg);
		        	  break;
		     	  case 'i':
		        	  arg = g.getOptarg();
		        	  inputFile.append(arg);
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
		        	  genDESkey();
		        	  break;
		          case 'h':
		        	  callUseage(0);
		          case '?':
		            break; // getopt() already printed an error
		            //
		          default:
		              break;
		       }
		   }
		
	}
	
	private static void callUseage(int exitStatus) {
		
		String useage = "Improper command:  o i e d k h are supported options";
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}
	
}
