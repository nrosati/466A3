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
		String key = "";
		try {
			for(String input : Files.readAllLines(Paths.get(keyStr.toString()), Charset.defaultCharset()))
			{
			//key = input;
			//System.out.println(input);
			//System.out.println(input.contains("DES"));
			if(input.contains("DES"))
			{
				key = input;
			}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//key = key.substring(place, place + 16);
		key = key.substring(key.length()-16, key.length());
		//System.out.println(key);
		
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
		//printBitSet(keyBytes, 8);
		BitSet[] finalKeys = expandKey(keyBytes);
		//printBitSet(finalKeys[0], 6);
		byte[] iVB = Base64.getDecoder().decode(iVStr);
		BitSet iVBits = BitSet.valueOf(iVB);
		System.out.print("I.V = ");
		printBitSet(iVBits, 8);
		
		BigInteger lineBI = new BigInteger(line, 16);
		BitSet message = new BitSet();
		for(int j = 0; j < 64; j++)
		{
			message.set(j, lineBI.testBit(64 - j - 1));
		}
		System.out.print("Cipher = ");
		printBitSet(message, 8);
		
		BitSet[] reversedKeys = new BitSet[finalKeys.length];
		for(int i = 0; i < 16; i++)
		{
			reversedKeys[i] = finalKeys[15 -i];
		}
		//printBitSet(finalKeys[0], 8);
		//printBitSet(reversedKeys[15], 8);
		
		//Do we XOR each block with the IV?  We are only getting one block at a time here so cant xor it with previous block?
		//Keys are reveresed, IV bitset matches the IV bitset outputted from encryption
		//message input bitset matches encrypted output bitset of encryption
		//We can call desalg with the message bitset and the reversed keys
		BitSet decrypted = new BitSet();
		//XOR with IV?
		message.xor(iVBits);
		decrypted = desAlg(message, reversedKeys);
		
		
		//Then we gotta print the bitset out in ascii somehow
		System.out.print("Decrypted = ");
		printBitSet(decrypted, 8);
		String out = Base64.getEncoder().encodeToString(decrypted.toByteArray());
		//System.out.println(decrypted.toByteArray().toString(2));
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
	@SuppressWarnings({ "static-access", "deprecation" })
	private static String DES_encrypt(String line,StringBuilder keyChain) {
		String key = "";
		try {
			for(String input : Files.readAllLines(Paths.get(keyChain.toString()), Charset.defaultCharset()))
			{
			//key = input;
			//System.out.println(input);
			//System.out.println(input.contains("DES"));
			if(input.contains("DES"))
			{
				key = input;
			}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		key = key.substring(key.length()-16, key.length());
		//System.out.println(key);
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
		
		BitSet[] finalKeys = expandKey(keyBytes);
		/*for(int i = 0; i < 16; i++)
		{
			printBitSet(finalKeys[i], 6);
		}*/
		
		int stringSize = line.length();
		//System.out.println(stringSize);
		byte[] wholeLine = line.getBytes();
		//System.out.println(new String(wholeLine, 0));
		while(stringSize % 8 != 0)
		{
			stringSize++;
		}
		byte[][] mBytes = new byte[stringSize / 8][8];
		int place = 0;
		for(int i = 0; i < mBytes.length; i++)
		{
			for(int j = 0; j < 8; j++)
			{
				mBytes[i][j] = wholeLine[place];
				place++;
				
				if(place >= line.length())
					break;
			}
		}
		//System.out.println(new String(mBytes[0], 0));
		//System.out.println(new String(mBytes[1], 0));
		BigInteger[] bis = new BigInteger[mBytes.length];
		for(int i = 0; i < mBytes.length; i++)
		{
			bis[i] = new BigInteger(mBytes[i]);
		}
		
		BitSet[] mBits = new BitSet[mBytes.length];
		for(int i = 0; i < mBytes.length; i++)
		{
			mBits[i] = new BitSet();
			for(int j = 0; j < 64; j++)
			{
				mBits[i].set(j, bis[i].testBit(64 - j - 1));
			}
		}
		System.out.println("Binary Clear Text = ");
		printBitSet(mBits[0], 8);
		//Hard coding for comparing to website for testing
				/*String messageTest = "0123456789ABCDEF";
				BigInteger biTest = new BigInteger(messageTest, 16);
				BitSet bsTest = new BitSet(64);
				
				for(int i = 0; i < 64; i++)
				{
					bsTest.set(i, biTest.testBit(64 - i - 1));
				}*/
		//IV before would go here before IP
		//bsTest = desAlg(bsTest, finalKeys);
		//printBitSet(bsTest, 8);
		//BigInteger forPrint = new BigInteger(outputPrint(bsTest), 2);
		//System.out.println(outputPrint(bsTest));
		StringBuilder output = new StringBuilder();
		//output.append(forPrint.toString(16));
		//System.out.println(output.toString());
		//printBitSet(bsTest, 4);
		//printBitSet(mBits[0], 8);
		//printBitSet(mBits[1], 8);
		
		SecureRandom gen = new SecureRandom();
		byte[] IV = new byte[8];
		
		//System.out.print(key.format("%x", new BigInteger(1, output.getBytes())));
		gen.nextBytes(IV);
		String ivector = Base64.getEncoder().encodeToString(IV);//Encode the Key
		output.append(ivector);
		output.append("\n");
		//System.out.println("IV = " + output);
		BitSet iv = BitSet.valueOf(IV);
		
		System.out.print("IV = ");
		printBitSet(iv, 8);
		mBits[0].xor(iv);
		BitSet[] finalBits = new BitSet[mBits.length];
		finalBits[0] = new BitSet();
		finalBits[0] = desAlg(mBits[0], finalKeys);
		System.out.print("Encrypted block = ");
		printBitSet(finalBits[0], 8);
		output.append(outputPrint(finalBits[0]));
		output.append("\n");
		for(int i = 1; i < finalBits.length; i++)
		{
			finalBits[i] = new BitSet();
			mBits[i].xor(finalBits[i-1]);
			finalBits[i] = desAlg(mBits[i], finalKeys);
			//printBitSet(finalBits[i], 8);
			output.append(outputPrint(finalBits[i]));
			output.append("\n");
		}
		System.out.print(output.toString());		
		return output.toString();
	
	}
	
	private static String outputPrint(BitSet bits)
	{
		StringBuilder sb = new StringBuilder();
		/*byte[] bytes = bits.toByteArray();
		for(byte b : bytes)
		{
			sb.append(String.format("%02x ", b));
		}*/
		for(int i = 0; i < bits.size(); i++)
		{
			if(bits.get(i))
				sb.append("1");
			else
				sb.append("0");
		}
		BigInteger forPrint = new BigInteger(sb.toString(), 2);
		return forPrint.toString(16);
	}
	
	//This returns a bit set representing the encrypted block, will be stored in an array of bit sets
	private static BitSet desAlg(BitSet toEncrypt, BitSet[] finalKeys)
	{
		BitSet encrypted = new BitSet();
		//printBitSet(encrypted, 4);
		for(int i = 0; i < 64; i++)
		{
			encrypted.set(i, toEncrypt.get(SBoxes.IP[i] - 1));
		}
		//printBitSet(encrypted, 4);
		
		BitSet[] l = new BitSet[17];
		BitSet[] r = new BitSet[17];
		for(int i = 0; i < 17; i++)
		{
			l[i] = new BitSet();
			r[i] = new BitSet();
		}
		for(int i = 0; i < 64; i++)
		{
			if(i < 32)
			{
				l[0].set(i, encrypted.get(i));
			}
			else
				r[0].set(i -32, encrypted.get(i));
		}
		//printBitSet(l[0], 4);
		//printBitSet(r[0], 4);
		for(int i = 1; i < 17; i++)
		{
			l[i] = r[i-1];
			//printBitSet(finalKeys[i-1], 6);
			//printBitSet(l[i], 4);
			BitSet temp = new BitSet();
			temp = l[i-1];
			//System.out.println("i = " + i);
			//System.out.print("L n -1 = ");
			//printBitSet(temp,4);
			BitSet fromF = new BitSet();
			fromF = F(r[i-1], finalKeys[i-1]);//Keys used here
			//System.out.print("From F = ");
			//printBitSet(fromF, 4);
			temp.xor(fromF);
			//System.out.print("After xor = ");
			//printBitSet(temp, 4);
			r[i] = temp;
			//System.out.print("R[" + i + "] = ");
			//printBitSet(r[i], 4);
		}
		//System.out.println("After loop");
		//printBitSet(r[16], 4);
		//printBitSet(l[16], 4);
		
		BitSet cipher = new BitSet();
		for(int i = 0; i < 64; i++)
		{
			if(i < 32)
			{
				cipher.set(i, r[16].get(i));
			}
			else
				cipher.set(i, l[16].get(i - 32));
		}
		//printBitSet(cipher, 8);
		
		BitSet encrypt = new BitSet();
		for(int i = 0; i < 64; i++)
		{
			encrypt.set(i, cipher.get(SBoxes.FP[i] -1));
		}
		//printBitSet(encrypt, 8);
		return encrypt;//returns a bit set representing the encrypted block 
	}
	private static BitSet[] expandKey(BitSet keyBytes)
	{
		BitSet shortKey = new BitSet(56);
		for(int i = 0; i < 56; i++)
		{
			shortKey.set(i, keyBytes.get(SBoxes.PC1[i] -1));
		}
		//System.out.println(shortKey.length());
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
		//printBitSet(C[0], 7);
		//printBitSet(D[0], 7);

		for(int i = 1; i < 17; i++)
		{
			C[i] = leftShift(C[i-1], SBoxes.rotations[i - 1]);
			/*System.out.print("C ");
			System.out.print(i);
			System.out.print(" = ");
			printBitSet(C[i], 28);*/
			D[i] = leftShift(D[i-1], SBoxes.rotations[i -1]);
			/*System.out.print("D ");
			System.out.print(i);
			System.out.print(" = ");
			printBitSet(D[i], 28);*/
		}
		
		BitSet[] Kn = new BitSet[16];
		for(int i = 0; i < 16; i++)
		{
			Kn[i] = new BitSet(64);
		}
		
		for(int i = 0; i < 16; i++)
		{
			for(int j = 0; j < 56; j++)
			{
				if(j < 28)
				{
					Kn[i].set(j, C[i+1].get(j));
				}
				else
					Kn[i].set(j, D[i+1].get(j - 28));
			}
		}
		
		/*for(int i = 0; i < 16; i++)
		{
			System.out.print("Kn " + i + " = ");
			printBitSet(Kn[i], 56);
		}*/
		
		BitSet[] finalKeys = new BitSet[16];
		for(int i = 0; i < 16; i++)
		{
			finalKeys[i] = new BitSet();
			for(int j = 0; j < 48; j++)
			{
				finalKeys[i].set(j, Kn[i].get(SBoxes.PC2[j] -1));
			}
		}
		
		/*for(int i = 0; i < 16; i++)
		{
			System.out.print("Kn " + (i) +  " = ");
			printBitSet(finalKeys[i], 6);
		}*/
		
		return finalKeys;
		
	}
	
	private static BitSet leftShift(BitSet set, int shifts)
	{
		BitSet shifted = new BitSet();
		boolean bit = set.get(0);
		//System.out.println(bit);
		shifted = set.get(1, set.length());
		shifted.set(27, bit);
		if(shifts == 2)
		{
			bit = shifted.get(0);
			shifted = shifted.get(1, shifted.length());
			shifted.set(27, bit);
		}
		
		
		//printBitSet(shifted, 28);
		return shifted;
	}
	
	/*
	 * F Function, Ebit, Sboxes
	 */
	private static BitSet F(BitSet right, BitSet key)
	{
		//printBitSet(right, 4);
		BitSet e = new BitSet();
		for(int i = 0; i < 48; i++)
		{
			e.set(i, right.get(SBoxes.E[i] -1));
		}
		//printBitSet(e, 6);
		e.xor(key);
		//printBitSet(e, 6);
		
		BitSet[] sBits = new BitSet[8];
		
		int bc = 0;
		for(int i = 0; i < 8; i++)
		{
			sBits[i] = new BitSet();
			for(int j = 0; j < 6; j++)
			{
				 sBits[i].set(j, e.get(bc));
				 bc++;
			}
		}
		//printBitSet(sBits[1], 6);
		BitSet[] first = new BitSet[8];
		BitSet[] middle = new BitSet[8];
		
		for(int i = 0; i < 8; i++)
		{
			first[i] = new BitSet();
			middle[i] = new BitSet();
			first[i].set(0, sBits[i].get(0));
			first[i].set(1, sBits[i].get(5));
			middle[i].set(0, sBits[i].get(1));
			middle[i].set(1, sBits[i].get(2));
			middle[i].set(2, sBits[i].get(3));
			middle[i].set(3, sBits[i].get(4));
		}
		
		//printBitSet(first[0], 2);
		//printBitSet(middle[0], 4);
		//Gotta reverse the bit sets because when you put in big integer it reverses them
		BitSet[] shiftedFirst = new BitSet[8];
		BitSet[] shiftedMid = new BitSet[8];
		for(int i = 0; i < 8; i++)
		{
			shiftedFirst[i] = new BitSet();
			shiftedMid[i] = new BitSet();
			shiftedFirst[i].set(0, first[i].get(1));
			shiftedFirst[i].set(1, first[i].get(0));
			
			shiftedMid[i] = new BitSet();
			shiftedMid[i].set(0, middle[i].get(3));
			shiftedMid[i].set(1, middle[i].get(2));
			shiftedMid[i].set(2, middle[i].get(1));
			shiftedMid[i].set(3, middle[i].get(0));
			
		}
		//printBitSet(shiftedFirst[0], 2);
		//printBitSet(shiftedMid[0], 4);
		//System.out.println(new BigInteger(shiftedFirst[0].toByteArray()).intValue());
		BitSet sOut = new BitSet();
		int placer = 0;
		for(int i = 0; i < 8; i++)
		{
			int row = 0;
			int col = 0;
			if(shiftedFirst[i].get(0) != false || shiftedFirst[i].get(1) != false)
			{
				row = new BigInteger(shiftedFirst[i].toByteArray()).intValue();
			}
			if(shiftedMid[i].get(0) != false || shiftedMid[i].get(1) != false || shiftedMid[i].get(2) != false || 
					shiftedMid[i].get(3) != false)
			{
				col = new BigInteger(shiftedMid[i].toByteArray()).intValue();
			}
			
			int index = 0;//row * col + col;
			if(row == 0)
			{
				index = col;
			}
			else if(row == 1)
			{
				index = 16 + col;
			}
			else if(row == 2)
			{
				index = 32 + col;
			}
			else if(row == 3)
			{
				index = 48 + col;
			}
			//System.out.println("Row = " + row + " Col = " + col + " Index = " + index);
			byte[] s = {SBoxes.S[i][index]};
			//System.out.println(s[0]);
			BigInteger temp = new BigInteger(s);
			BitSet sbs = new BitSet();
			for(int j = 0; j < 64; j++)
			{
				sbs.set(j, temp.testBit(64 - j -1));
			}
			sbs.set(0, sbs.get(60));
			sbs.set(1, sbs.get(61));
			sbs.set(2, sbs.get(62));
			sbs.set(3, sbs.get(63));
			//sbs.clear(60, 64);
			//printBitSet(sbs, 4);
			for(int j = 0; j < 4; j++)
			{
				sOut.set(placer, sbs.get(j + 60));
				//System.out.println(sbs.get(j+60));
				placer++;
			}
			//System.out.println("Placer = " + placer);
			//printBitSet(sOut, 4);
		}
		//System.out.println("Printing sOut");
		//printBitSet(sOut, 4);
		
		BitSet perm = new BitSet();
		for(int i = 0; i < 32; i++)
		{
			perm.set(i, sOut.get(SBoxes.P[i] -1));
		}
		//printBitSet(perm, 4);
		return perm;
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
		
		byte[] bytes = new byte[6];
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
