import gnu.getopt.Getopt;


public class RSA_skeleton {

	public static void main(String[] args){
		
		StringBuilder bitSizeStr = new StringBuilder();
		StringBuilder keyChainFile = new StringBuilder();
		StringBuilder m = new StringBuilder();
		StringBuilder encrypt = new StringBuilder();
		
		pcl(args, bitSizeStr, keyChainFile,m, encrypt);
		
//		You are going to have to pull out the numbers from key Chain File
//		You can do this any way you want, this is just a suggested setup.
		StringBuilder eStr = new StringBuilder();
		StringBuilder nStr = new StringBuilder();
		StringBuilder dStr = new StringBuilder();
		
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



	private static void RSAencrypt(StringBuilder m, StringBuilder nStr, StringBuilder eStr) {
		// TODO Auto-generated method stub
	}

	private static void RSAdecrypt(StringBuilder cStr, StringBuilder nStr,
			StringBuilder dStr){
		// TODO Auto-generated method stub
	}
	
	private static void genRSAkey(StringBuilder bitSizeStr) {
		// TODO Auto-generated method stub
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
    
		String useage = "";
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}


}
