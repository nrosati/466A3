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


@SuppressWarnings("unused")
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
				encryptedText = DES_decrypt(IVStr, line, keyChainFile);
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
	@SuppressWarnings("static-access")
	private static String DES_decrypt(String iVStr, String line, StringBuilder keyStr) {
	
		
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
	@SuppressWarnings("static-access")
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
		key = key.substring(key.length()-16, key.length());
		System.out.println(key);
		BigInteger binary = new BigInteger(key,16);
	
		int length = binary.bitLength();
		if(length < 64)
		{
			int temp = 64 - length;
			length += temp;
		}
		BitSet keyBytes = new BitSet(length);//Maybe be 64?
		for(int i = 0; i < length; i++)
		{
			keyBytes.set(i, binary.testBit(length - i - 1));
		}
		//System.out.println(keyBytes.size());
		//printBitSet(keyBytes, 8);
		
		keyBytes = expandKey(keyBytes);
		
		return null;
	
	}
	
	private static BitSet expandKey(BitSet keyBytes)
	{
		BitSet shortKey = new BitSet(56);
		for(int i = 0; i < 56; i++)
		{
			shortKey.set(i, keyBytes.get(SBoxes.PC1[i] -1));
		}
		System.out.println(shortKey.length());
		//printBitSet(shortKey, 7);
		
		BitSet[] C = new BitSet[17];
		BitSet[] D = new BitSet[17];
		
		for(int i = 0; i < 17; i++)
		{
			C[i] = new BitSet();
			D[i] = new BitSet();
		}
		for(int i = 0; i < 56; i++)
		{
			if(i < 28)
				C[0].set(i, shortKey.get(i));
			else
				D[0].set(i - 28, shortKey.get(i));
		}
		printBitSet(C[0], 7);
		printBitSet(D[0], 7);

		for(int i = 1; i < 17; i++)
		{
			leftShift(C[i-1], SBoxes.rotations[i]);
			leftShift(D[i-1], SBoxes.rotations[i]);
		}
		
		
		
		return shortKey;
		
	}
	
	private static BitSet leftShift(BitSet set, int shifts)
	{
		BitSet shifted = new BitSet();
		for(int i = 0; i < shifts; i++)
		{
			boolean bit = set.get(0);
			System.out.println(bit);
			shifted = set.get(1, set.length());
			shifted.set(27, bit);
		}
		
		printBitSet(shifted, 28);
		return shifted;
	}
	
	/*
	 * F Function, Ebit, Sboxes
	 */
	private static BitSet F (BitSet right, BitSet key)
	{
		BitSet E = new BitSet(48);
		for(int i = 0; i < 32; i++)
		{
			E.set(i, right.get(SBoxes.E[i]));
		}
		
		BitSet temp = new BitSet(48);
		temp = right;
		temp.xor(key);
		
		BitSet[] blocks = new BitSet[8];
		
		int count = 0;
		for(int i = 0; i < 48; i++)
		{
			blocks[i] = new BitSet(6);
			
			for(int j = 0; j < 6; j++)
			{
				blocks[i].set(j, temp.get(count));
				count++;
			}
		}
		
		BitSet sOut = new BitSet(32);
		int place = 0;
		for(int i = 0; i < 8; i++)
		{
			byte[] bytes = blocks[i].toByteArray();
			byte[] first =  {bytes[0], bytes[5]};
			byte[] middle = {bytes[1], bytes[2], bytes[3], bytes[4]};
			BigInteger rowBi = new BigInteger(first);
			BigInteger colBi = new BigInteger(middle);
			int row = rowBi.intValue();
			int col = colBi.intValue();
			
			//BitSet Sout = new BitSet(32);
			long soutint = SBoxes.S[i][(row * 15) + col];
			BigInteger bis = BigInteger.valueOf(soutint);
			for(int j = 0; j < 4; j++)
			{
				sOut.set(place, bis.testBit(j));
				place++;
			}
			
		}
		BitSet SOUTFINALLY = new BitSet(32);
		for(int i = 0; i < 32; i++)
		{
			SOUTFINALLY.set(i, sOut.get(SBoxes.P[i]));
		}
		return SOUTFINALLY;
	}
	private static void printBitSet(BitSet set, int space)
	{
		int spacing = space;
		for(int i = 0; i < set.size(); i++)
		{
			if(i > 0 && i % space == 0)
				System.out.print(" ");
			if(set.get(i))
				System.out.print("1");
			else
				System.out.print("0");
		}
		System.out.println();
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
