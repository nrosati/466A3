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
	
	//Global - holds the previous cyphertext block for DES_decrypt
	static BitSet pBlock = new BitSet();
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
			writer.close();//Close writer after all the lines have been processed to write them to the file
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * String DES_Decrypt
	 * This method takes a String representation of an Initialization Vector, a String of cyphertext in ascii,
	 * and a String Builder linked with a file that contains a key.  This method takes the cyphertext given and decrypts it 
	 * using the key from keyStr and returns it as plain text.
	 * @param String iVStr
	 * @param String line
	 * @param KeyStr
	 */
	private static String DES_decrypt(String iVStr, String line, StringBuilder keyStr) {
		String key = "";
		try {
			for(String input : Files.readAllLines(Paths.get(keyStr.toString()), Charset.defaultCharset()))
			{
			if(input.contains("DES"))//Look for DES
			{
				key = input;//Since there is only one DES we can set it like this
			}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//flag to see if this is the first block we are decoding or not
		int flag = 0;
		//Check to see if the previous block has been used or not
		if(pBlock.cardinality() != 0)
			flag = 1;
		//Since we have the only line with DES we only want the key
		key = key.substring(key.length()-16, key.length());
		
		//Convert the key to a big integer
		BigInteger binary = new BigInteger(key,16);
		int length = binary.bitLength();
		//Make the length 64 for 64 bits
		if(length < 64)
		{
			int temp = 64 - length;
			length += temp;
		}
		/*Big Integer is BigEndian and BitSet is little so the bits are 
		 * reversed in the two Therefore we have to set the bits one by 
		 * one starting at the end of the BigInteger To the beginning of 
		 * the BitSet*/
		BitSet keyBytes = new BitSet(length);
		for(int i = 0; i < length; i++)
		{
			keyBytes.set(i, binary.testBit(length - i - 1));
		}
		
		BitSet[] finalKeys = expandKey(keyBytes);
		
		//IV comes from encrypt as Base64 encoded so decode it here into an 
		//array of bytes
		byte[] iVB = Base64.getDecoder().decode(iVStr);
		//Turn those bytes into a Bit Set
		BitSet iVBits = BitSet.valueOf(iVB);
		
		//Here the line is in ascii, have to convert it to binary bit set, same
		//process as used above for the key
		BigInteger lineBI = new BigInteger(line, 16);
		BitSet message = new BitSet();
		for(int j = 0; j < 64; j++)
		{
			message.set(j, lineBI.testBit(64 - j - 1));
		}
		
		
		//For decryption we run the algorithm with the keys reversed
		BitSet[] reversedKeys = new BitSet[finalKeys.length];
		for(int i = 0; i < 16; i++)
		{
			reversedKeys[i] = finalKeys[15 -i];
		}
		
		BitSet decrypted = new BitSet();
		
		BitSet temp = new BitSet();//temp to hold original message before xor
		temp = message;
		/*
		 * Here we run the algorithm and xor with the IV vector if its the first run
		 * or the previous cyphertext if its after the initial run. Temp is used to
		 * store the cyphertext to put into pBlock(the previous block global variable) 
		 * before it xor.  pBlock has to be set after Xor or you will xor the message
		 * with itself.  
		 *  
		 */
		if(flag == 1)
		{
			decrypted = desAlg(message, reversedKeys);
			decrypted.xor(pBlock);
			pBlock = temp;
		}
		else
		{
			pBlock = message;
			decrypted = desAlg(message, reversedKeys);
			decrypted.xor(iVBits);
		}
			
		
		/*
		 * To print the binary bitset out as plain text string we create a string that mirrors 
		 * the bitset.  I.e a 1 in the bitset correlates to a 1 character in the string.
		 */
		String bitString = "";
		for(int i = 0; i < decrypted.length(); i++)
		{
			if(decrypted.get(i))
			{
				bitString += '1';
			}
			else
				bitString += 0;
		}
		/*
		 * Then we go through our string 8 chars at a time and we use
		 * Integer.parseInt with the radix 2 to get a number out
		 * And we cast that number as a char and append the char
		 * to our output string
		 */
		String out = "";
		char nextChar;
		for(int i = 0; i < bitString.length() - 7; i+=8)
		{
			nextChar = (char)Integer.parseInt(bitString.substring(i, i+8), 2);
			out += nextChar;
					
		}
		return out;
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
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	/**
	 * Takes a line containing the entirety of a file and a keyChain file and encrypts them.
	 * The line is broken into 64 bit chunks, the key is in hexidecimal and is turned into binary.
	 * The key is expanded, then the message is encrypted.  Uses DES CBC mode.  
	 * @param line
	 * @param keyChain
	 */
	@SuppressWarnings({ })
	private static String DES_encrypt(String line,StringBuilder keyChain) {
		String key = "";
		try {
			for(String input : Files.readAllLines(Paths.get(keyChain.toString()), Charset.defaultCharset()))
			{
			//Look for the line that has DES, theres only one and set the String key to it
			if(input.contains("DES"))
			{
				key = input;
			}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//The actual key is the last 16 characters so take those
		key = key.substring(key.length()-16, key.length());
		//Convert to BigInteger to get bits
		BigInteger binary = new BigInteger(key,16);
	
		int length = binary.bitLength();
		if(length < 64)
		{
			int temp = 64 - length;
			length += temp;
		}
		/*
		 * Again BigInteger is BigE Bitset is littleE so we have to copy the bits one by one in reverse
		 */
		BitSet keyBytes = new BitSet(length);
		for(int i = 0; i < length; i++)
		{
			keyBytes.set(i, binary.testBit(length - i - 1));
		}
		/*
		 * Calls the expand keys method I wrote that transforms the 64 bit key into 
		 * 16 48 byte keys
		 */
		BitSet[] finalKeys = expandKey(keyBytes);
	
		/*
		 * Here we make an array of byte arrays, each byte array is 8 bytes and 
		 * we have as many as needed to fit the whole message.  Specifically
		 * we see how long the line is, and we increase it if necessary until
		 * it is divisible by 8.  Then thats how many arrays we need divided by 8.
		 */
		int stringSize = line.length();
		byte[] wholeLine = line.getBytes();//Create byte array of the whole line
		while(stringSize % 8 != 0)
		{
			stringSize++;
		}
		byte[][] mBytes = new byte[stringSize / 8][8];
		int place = 0;
		for(int i = 0; i < mBytes.length; i++)
		{
			//Here we break the byte array of the whole message into chunks of 8 bytes
			//So in our 2d array we have the whole message in 8 byte chunks(64 bits)
			for(int j = 0; j < 8; j++)
			{
				mBytes[i][j] = wholeLine[place];
				place++;
				
				if(place >= line.length())
					break;
			}
		}
		
		/*
		 * Here we go again with converting to a big integer just an array
		 * this time to hold each of the 8 byte arrays we just made
		 */
		BigInteger[] bis = new BigInteger[mBytes.length];
		for(int i = 0; i < mBytes.length; i++)
		{
			bis[i] = new BigInteger(mBytes[i]);
		}
		
		/*
		 * Our array of BitSets to hold the 64 bit chunks of the message
		 * from our byte arrays, now in BigIntegers.  Here we again copy
		 * bit by bit in reverse.
		 */
		BitSet[] mBits = new BitSet[mBytes.length];
		for(int i = 0; i < mBytes.length; i++)
		{
			mBits[i] = new BitSet();
			for(int j = 0; j < 64; j++)
			{
				mBits[i].set(j, bis[i].testBit(64 - j - 1));
			}
		}
		
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
		//System.out.println(outputPrint(bsTest));
		
		//We use a string builder to build our output
		StringBuilder output = new StringBuilder();
		
		//Generate Initialization Vector
		SecureRandom gen = new SecureRandom();
		byte[] IV = new byte[8];
		
		gen.nextBytes(IV);
		String ivector = Base64.getEncoder().encodeToString(IV);//Encode the Key for output
		output.append(ivector);
		output.append("\n");
		BitSet iv = BitSet.valueOf(IV);//Convert IV to bitSet
		
		/*
		 * Xor the first block with the iv
		 * create an array of bitsets to store the results
		 * run the first block through the algorithm after being
		 * xor with the IV
		 */
		mBits[0].xor(iv);
		BitSet[] finalBits = new BitSet[mBits.length];
		finalBits[0] = new BitSet();
		finalBits[0] = desAlg(mBits[0], finalKeys);
		
		//Calls a print method I wrote and appends it to the string builder
		output.append(outputPrint(finalBits[0]));
		output.append("\n");//Seperate by new line
		/*
		 * Now while we have message blocks to encrypt we xor with the previous block
		 * starting with the 2nd block, and send it through the des algorithm.  The
		 * cypherblock is appended to the string builder separated by new lines.
		 */
		for(int i = 1; i < finalBits.length; i++)
		{
			finalBits[i] = new BitSet();
			mBits[i].xor(finalBits[i-1]);
			finalBits[i] = desAlg(mBits[i], finalKeys);
			output.append(outputPrint(finalBits[i]));
			output.append("\n");
		}
				
		return output.toString();
	
	}
	/*
	 * Print method I wrote.  Creates a string that mirrors the bit set.
	 * So a 1 in the bit set means a 1 character at the same position
	 * but in a string.  Then we turn that String into a Big Integer 
	 * with the radix 2, then we call to string with the radix 16 and return that.
	 * The result is a hex string representing a binary bit set  
	 */
	private static String outputPrint(BitSet bits)
	{
		StringBuilder sb = new StringBuilder();

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
	
	/*
	 * This is the DES algorithm.   It takes the bitset to encrypt and the array
	 * of 16 keys.  Follows the algorithm of Left and Right and all that.
	 * Calls the function F for the Fiestal network.  Returns a 64 bit bitset 
	 * representing the cypherblock text.
	 */
	private static BitSet desAlg(BitSet toEncrypt, BitSet[] finalKeys)
	{
		BitSet encrypted = new BitSet();
		for(int i = 0; i < 64; i++)
		{
			encrypted.set(i, toEncrypt.get(SBoxes.IP[i] - 1));//Initial permutation
		}
		//Create arrays of left and right halves
		BitSet[] l = new BitSet[17];
		BitSet[] r = new BitSet[17];
		for(int i = 0; i < 17; i++)
		{
			l[i] = new BitSet();
			r[i] = new BitSet();
		}
		//Split text in half, set to initial halves
		for(int i = 0; i < 64; i++)
		{
			if(i < 32)
			{
				l[0].set(i, encrypted.get(i));
			}
			else
				r[0].set(i -32, encrypted.get(i));
		}
		
		/*
		 * Meat of the algorithm, sets left have to previous right half,
		 * sends previous right half through the fiestal network with the keys.
		 * Xor the previous left with the result and set as new right half
		 */
		for(int i = 1; i < 17; i++)
		{
			l[i] = r[i-1];
			
			BitSet temp = new BitSet();
			temp = l[i-1];
			
			BitSet fromF = new BitSet();
			fromF = F(r[i-1], finalKeys[i-1]);//Keys used here, 0 based so minus 1
			
			temp.xor(fromF);
		
			r[i] = temp;
			
		}
		
		//Combines the 16th right half and left half
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
		
		//Final permutation
		BitSet encrypt = new BitSet();
		for(int i = 0; i < 64; i++)
		{
			encrypt.set(i, cipher.get(SBoxes.FP[i] -1));
		}
		
		return encrypt;//returns a bit set representing the encrypted block 
	}
	/*
	 * This function goes through some permutations in SBoxes.java
	 * to turn the 64bit input key into 16 48 bit keys
	 */
	private static BitSet[] expandKey(BitSet keyBytes)
	{
		BitSet shortKey = new BitSet(56);
		for(int i = 0; i < 56; i++)
		{
			shortKey.set(i, keyBytes.get(SBoxes.PC1[i] -1));//First permutation, key now 56 bits
		}
		
		//Split into two halves
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
		
		//Shifts of the keys
		//Current keys are the previous keys shifted amount of times
		//Depends on rotations in SBoxes.java
		//Calls the leftShift function I wrote
		for(int i = 1; i < 17; i++)
		{
			C[i] = leftShift(C[i-1], SBoxes.rotations[i - 1]);
			D[i] = leftShift(D[i-1], SBoxes.rotations[i -1]);
		
		}
		
		//Create array of keys
		BitSet[] Kn = new BitSet[16];
		for(int i = 0; i < 16; i++)
		{
			Kn[i] = new BitSet(64);
		}
		
		//For each of the 16 keys split into 56 bit halves
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
		
		//Take our keys through one last permutation 
		BitSet[] finalKeys = new BitSet[16];
		for(int i = 0; i < 16; i++)
		{
			finalKeys[i] = new BitSet();
			for(int j = 0; j < 48; j++)
			{
				finalKeys[i].set(j, Kn[i].get(SBoxes.PC2[j] -1));//Keys now 48 bits
			}
		}
		
		
		return finalKeys;//Array of 16 48 bit keys
		
	}
	
	/*
	 * Left Shift function I wrote for shifting in BitSets
	 * Easier than converting to BigInteger then back to bit set
	 * Only works because maximum number of shifts is 2
	 */
	private static BitSet leftShift(BitSet set, int shifts)
	{
		BitSet shifted = new BitSet();
		boolean bit = set.get(0);//Get the first bit value
		/*
		 * returns a bit set from index to index, so from 1 to the end.
		 * then we set the last bit(27) to the first bit we took above.
		 */
		shifted = set.get(1, set.length());
		shifted.set(27, bit);
		//If shifts is 2 we just do it again
		if(shifts == 2)
		{
			bit = shifted.get(0);
			shifted = shifted.get(1, shifted.length());
			shifted.set(27, bit);
		}
		
		return shifted;
	}
	
	/*
	 * Fiestel Network, takes a bitset and a key and does some
	 * permutations on them as well as xor with the key passed in
	 */
	private static BitSet F(BitSet right, BitSet key)
	{
		//Permutate the bit set passed in(half a message block)
		BitSet e = new BitSet();
		for(int i = 0; i < 48; i++)
		{
			e.set(i, right.get(SBoxes.E[i] -1));
		}
		//Xor result with the key
		e.xor(key);
	
		//Here we break the 48 bit block into 8 blocks of 6 for the sboxes
		BitSet[] sBits = new BitSet[8];
		int bc = 0;//Persistent counter to keep space on original 48 bit block
		for(int i = 0; i < 8; i++)
		{
			sBits[i] = new BitSet();
			for(int j = 0; j < 6; j++)
			{
				 sBits[i].set(j, e.get(bc));
				 bc++;
			}
		}
		
		//Now we take the first and last bit and the middle 4 bits of
		//Each 6 bit block and store them in arrays
		BitSet[] first = new BitSet[8];
		BitSet[] middle = new BitSet[8];
		//Just done manually
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
		
		//Gotta reverse the bit sets because when you put in big integer it reverses them
		//Again just hard coded
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
		
		BitSet sOut = new BitSet();
		int placer = 0;
		
		for(int i = 0; i < 8; i++)
		{
			//Set them to 0 initially BigInt Doesnt like 0
			//So if its 0 we already have an int of that, if not BI will change it
			int row = 0;
			int col = 0;
			if(shiftedFirst[i].get(0) != false || shiftedFirst[i].get(1) != false)//Make sure not 0 so no error from BI
			{
				row = new BigInteger(shiftedFirst[i].toByteArray()).intValue();//Get int value for Sbox
			}
			if(shiftedMid[i].get(0) != false || shiftedMid[i].get(1) != false || shiftedMid[i].get(2) != false || 
					shiftedMid[i].get(3) != false)
			{
				col = new BigInteger(shiftedMid[i].toByteArray()).intValue();
			}
			
			//Some math to get the right index of the SBox we want from a row and col value
			int index = 0;
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
			
			byte[] s = {SBoxes.S[i][index]};//Get the Byte from the Sbox

			BigInteger temp = new BigInteger(s);//Turn it into a Big Integer
			BitSet sbs = new BitSet();
			//Turn the BigInteger into a bit set
			for(int j = 0; j < 64; j++)
			{
				sbs.set(j, temp.testBit(64 - j -1));
			}
			//For Some reason it kept setting the last 4 bits of the bitset
			//So just copy them to the first 4
			sbs.set(0, sbs.get(60));
			sbs.set(1, sbs.get(61));
			sbs.set(2, sbs.get(62));
			sbs.set(3, sbs.get(63));
			
			for(int j = 0; j < 4; j++)
			{
				sOut.set(placer, sbs.get(j + 60));//Set sOut our bit set containing all the bits from the Sboxes
				placer++;//Use placer to keep track of where we are in sOUt
			}
		}
		
		//Send through more permutations
		BitSet perm = new BitSet();
		for(int i = 0; i < 32; i++)
		{
			perm.set(i, sOut.get(SBoxes.P[i] -1));
		}
		
		return perm;
	}
	/*
	 * This was a print method I wrote to test output as I was working
	 * It takes a bit set and where you want spaces to go.
	 * So enter 8 will print out bits in groups of 8
	 * Goes through the bet set getting each bit and prints a 1 or a 0 then a new line at the end
	 */
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
		random.nextBytes(bytes);
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
			System.out.println(key.format("%x", new BigInteger(1, key.getBytes())));//print as hex
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
		
		String useage = "Improper command: Supported options are\n -h\n -k\n -e key_Chain_File -i input_file -o output_file\n"
				+ " -d key_Chain_File -i input_file -o output_file";
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}
	
}
