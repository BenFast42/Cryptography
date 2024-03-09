import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.spec.RSAPrivateKeySpec;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.PrivateKey;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;


/* 3168 = aes.cipher 
 * In the sender’s program in the directory “Sender”,  calculate AES-En Kxy (  RSA-En Kx– (SHA256 (M)) || M  )
*/
 
 public class sender{
    private static int BUFFER_SIZE = 32 * 1024;
    static String IV = "AAAAAAAAAAAAAAAA";
    static int numbBytesReadDS;
    
    
    public static void main(String[] args) throws Exception {
    
    //------KEYS------------------------------------------------------------------------------------------------------------------------------
    PrivateKey XprivKey = readPrivKeyFromFile("XPrivate.key");
    String AESsymKey = new String();

    
    // AFTER THIS TRY/CATCH, THE SYMMETRIC KEY IS READY --------------------------------------------------------------------------------------
    try {
      File keyFile = new File("symmetric.key");
      Scanner scan2 = new Scanner(keyFile);
     while (scan2.hasNextLine()) {
      String symmKey = scan2.nextLine();
      AESsymKey = symmKey;
    }
    scan2.close();
    //System.out.println("AES SYmmertric key: " + AESsymKey);//------------------------------test
  
   } catch (FileNotFoundException NotFoundError) {

      System.out.println("Error, no such file");
      NotFoundError.printStackTrace();
    }
    
    Scanner scan = new Scanner(System.in);
    System.out.println("Input the name of the message file: ");// ---------------------------------CHANGE THIS WHEN DONE TESTING---------------
    String userinput = scan.nextLine();

    System.out.println("Do you want to invert the 1st byte in SHA256(M)? (Y or N)");
    String flip = scan.nextLine();
    scan.close();

    //---------------------------------SHA-256 digital Digest-----------------------------------------------------------------------------------------
    BufferedInputStream file = new BufferedInputStream(new FileInputStream(userinput));
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    DigestInputStream in = new DigestInputStream(file, md);
    int i;
    byte[] buffer = new byte[BUFFER_SIZE];
    do {
      i = in.read(buffer, 0, BUFFER_SIZE);
    } while (i == BUFFER_SIZE);
    md = in.getMessageDigest();
    in.close();
     byte[] hash = md.digest();
     

    //When testing, the outputted plaintext byte[](from reciever) should be equal (except for the first bit)-------USER Y/N Byte flip--------------
    if (flip.equalsIgnoreCase("Y")){
        hash[0] = (byte) ~hash[0];

        BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream("message.dd"));
        byte[] mdout = hash;
        bout.write(mdout, 0, mdout.length);
        bout.close();

    } else if (flip.equalsIgnoreCase("N")) {
        BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream("message.dd"));
        byte[] mdout = hash;
        bout.write(mdout, 0, mdout.length);
        bout.close();

    } else {

        System.out.println("ERROR - Input not recognized, Neither \"Y\" nor \"N\" was input ");
    }

    //------------Printing digital Digest----------------------------------------------------------------------------------------------------------------
    System.out.println("digit digest (32 bytes) (hash value):");
    for (int k=0, j=0; k<hash.length; k++, j++) {
      System.out.format("%2X ", hash[k]) ;
      if (j >= 15) {
        System.out.println("");
        j=-1;
      }
    }
    System.out.println("");  

  //--------------------------------------RSA Encryption--------------------------------------------------------------------------------------------------
  //RSA can only encrypt 117 bytes at a time. 
    SecureRandom random = new SecureRandom();
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, XprivKey, random);

    byte [] msgDig = cipher.doFinal(hash, 0, hash.length); 

      //Printing the Digital Signature
      System.out.println("digital signature (128-bytes):");
      for (int k=0, j=0; k<msgDig.length; k++, j++) {
      System.out.format("%2X ", msgDig[k]) ;
      if (j >= 15) {
      System.out.println("");
      j=-1;
      }
    }
    System.out.println(""); 

   BufferedOutputStream msgDigout = new BufferedOutputStream(new FileOutputStream("message.ds-msg"));
        //This gives the Digital Signature || Message as we want 
        msgDigout.write(msgDig, 0, msgDig.length);// Writes the Message Digest
        
  

  //Inputting the message piece by piece and store it in message.ds-msg-------------------------------------------------------
    BufferedInputStream msgFile = new BufferedInputStream(new FileInputStream(userinput));
    byte[] message = new byte[BUFFER_SIZE]; //Buffer size because this is the max a file can be.
    int numBytesRead;
    
    while (true) {
      numBytesRead = msgFile.read(message, 0, message.length);
      
    if (numBytesRead <= 0) {
        break;
    }

     
        msgDigout.write(message, 0, numBytesRead);

     if (numBytesRead < message.length) {
      break;
    }
  }

    msgDigout.close();
    msgFile.close();
 

    //AES Encryption --------------------------------------------------------------------------------------------------------------------------
    Cipher cipherAES = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
    SecretKeySpec key = new SecretKeySpec(AESsymKey.getBytes("UTF-8"), "AES");
    cipherAES.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));

    //buffered
    BufferedInputStream messageDS = new BufferedInputStream(new FileInputStream("message.ds-msg"));
    BufferedOutputStream  aesCipher = new BufferedOutputStream(new FileOutputStream("message.aescipher"));
    byte[] plaintext = new byte[BUFFER_SIZE]; 
    byte[] Encrypted;  
    
    while (true) {
 
      numbBytesReadDS = messageDS.read(plaintext, 0, plaintext.length); 

       if (numbBytesReadDS <= 0) {
        break;
    }

     Encrypted = cipherAES.doFinal(plaintext, 0, numbBytesReadDS);
     aesCipher.write(Encrypted, 0, Encrypted.length);

      if (numbBytesReadDS < plaintext.length) {
        break;
      }

    } 
  
    messageDS.close();
    aesCipher.close();

  }
 
     
   
    public static PrivateKey readPrivKeyFromFile(String keyFileName) 
      throws IOException {

    InputStream in = 
        sender.class.getResourceAsStream(keyFileName);
    ObjectInputStream oin =
        new ObjectInputStream(new BufferedInputStream(in));

    try {
      BigInteger m = (BigInteger) oin.readObject();
      BigInteger e = (BigInteger) oin.readObject();

    //   System.out.println("Read from " + keyFileName + ": modulus = " + 
    //       m.toString() + ", exponent = " + e.toString() + "\n");

      RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
      KeyFactory factory = KeyFactory.getInstance("RSA");
      PrivateKey key = factory.generatePrivate(keySpec);

      return key;
    } catch (Exception e) {
      throw new RuntimeException("False serialisation error", e);
    } finally {
      oin.close();
    }
  }

}


 


 