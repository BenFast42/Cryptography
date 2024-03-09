import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;

import java.security.SecureRandom;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.util.*;

public class receiver {
    private static int BUFFER_SIZE = 32 * 1024;
    static String IV = "AAAAAAAAAAAAAAAA";
    private static int BLOCK_SIZE = 16 * 1024;
    static int nBRDigSig;
    static int numBytesRead;
    static int plainLen;
   

    public static void main(String[] args) throws Exception {


//------KEYS------------------------------------------------------------------------------------------------------------------------------
  PublicKey XpubKey = readPubKeyFromFile("XPublic.key");
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
  
   } catch (FileNotFoundException NotFoundError) {

      System.out.println("Error, no such file");
      NotFoundError.printStackTrace();
    }

    Scanner scan = new Scanner(System.in);
    System.out.println("Input the name of the message file: "); //---------------------------------CHANGE THIS WHEN DONE TESTING---------------
    String userinput = scan.nextLine();
    scan.close();
    
//AES DECRYPTION------------------------------------------------------------------------------------------------------ 
    BufferedInputStream msgFile2 = new BufferedInputStream(new FileInputStream("message.aescipher"));
    BufferedOutputStream DSnMSG = new BufferedOutputStream(new FileOutputStream("message.ds-msg"));

    Cipher cipherAES = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
    SecretKeySpec key = new SecretKeySpec(AESsymKey.getBytes("UTF-8"), "AES");
    cipherAES.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));

    byte[] Ciphertext = new byte[BUFFER_SIZE];
    byte[] Decrypted;

    while (true) {

      numBytesRead = msgFile2.read(Ciphertext, 0, Ciphertext.length);
      
    if (numBytesRead <= 0) {
        break;
    }
    Decrypted = cipherAES.doFinal(Ciphertext, 0, numBytesRead);
    DSnMSG.write(Decrypted, 0, Decrypted.length);
    plainLen = plainLen + Decrypted.length; //This is to keep track of the Decrypted length

    if (numBytesRead < BLOCK_SIZE) {
      break;
    } 

  }
    msgFile2.close();
    DSnMSG.close();
  
//---------------------------------------------------------------------------------------------------------------------

    //Now i have the digital signature (The first 128 bytes) in digitalSig 
    byte[] digitalSig = new byte[128];
    byte[] plaintext = new byte[plainLen-128];

    BufferedInputStream digitalSignatureNMessage = new BufferedInputStream(new FileInputStream("message.ds-msg"));
    digitalSignatureNMessage.read(digitalSig,0,128);
   
    digitalSignatureNMessage.read(plaintext,0, (plaintext.length) );

    digitalSignatureNMessage.close();
    
    //Printing the Digital Signature
        System.out.println("digital signature (128-bytes):");
        for (int k=0, j=0; k<digitalSig.length; k++, j++) {
        System.out.format("%2X ", digitalSig[k]) ;
        if (j >= 15) {
        System.out.println("");
        j=-1; }
    }
    System.out.println("");

//Write plaintext into a file----------------------------------------------------------------------------------------------------------
 BufferedOutputStream plaintextOut = new BufferedOutputStream(new FileOutputStream(userinput));
    plaintextOut.write(plaintext, 0, plaintext.length);// Writes the cipher text to file to be access by receiver 
    plaintextOut.close();

//--------------------------------------RSA Decryption--------------------------------------------------------------------------------------------------
  SecureRandom random = new SecureRandom();
  Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
  cipher.init(Cipher.DECRYPT_MODE, XpubKey, random);

  byte [] msgDig = cipher.doFinal(digitalSig, 0, digitalSig.length); 

//Printing msg digest-------------------------------------------------------------
System.out.println("Digital Digest (32-bytes) (hash value):");
        for (int k=0, j=0; k<msgDig.length; k++, j++) {
        System.out.format("%2X ", msgDig[k]) ;
        if (j >= 15) {
        System.out.println("");
        j=-1; }
    }
    System.out.println("");

    //Rehashing the message to check if they match (the userinputted file should be the plaintext at this point---
    BufferedInputStream file = new BufferedInputStream(new FileInputStream(userinput));
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    DigestInputStream in = new DigestInputStream(file, md);
    int z;
    byte[] buffer = new byte[BUFFER_SIZE];
    do {
      z = in.read(buffer, 0, BUFFER_SIZE);
    } while (z == BUFFER_SIZE);
    md = in.getMessageDigest();
    in.close();
    byte[] hash = md.digest();
    
     //Printing locally hashed msg digest-------------------------------------------------------------
    System.out.println("Locally hashed Digital Digest (32-bytes):");
        for (int k=0, j=0; k<hash.length; k++, j++) {
        System.out.format("%2X ", hash[k]) ;
        if (j >= 15) {
        System.out.println("");
        j=-1; }
        }
        System.out.println("");

    //Check to see if the decrypted HASH is equal to the locally hashed message 
    //msgDig is decrypted byte array
    int count = 0;
    if (msgDig.length == hash.length) {
    for (int i = 0; i < msgDig.length ; i++){
        if (msgDig[i]!=hash[i]) {
            System.out.println("Error: The message digest's are not equal!");
            break;
            }
        if (msgDig[i]==hash[i]) {
            count += 1;
        }
        }
        if  (count == msgDig.length){
            System.out.println("The message digest's match!");
        }
        
        
        
    } 
}



    

  
    




    public static PublicKey readPubKeyFromFile(String keyFileName) 
      throws IOException {

    InputStream in = 
    receiver.class.getResourceAsStream(keyFileName);
    ObjectInputStream oin =
        new ObjectInputStream(new BufferedInputStream(in));

    try {
      BigInteger m = (BigInteger) oin.readObject();
      BigInteger e = (BigInteger) oin.readObject();

    //   System.out.println("Read from " + keyFileName + ": modulus = " + 
    //       m.toString() + ", exponent = " + e.toString() + "\n");

      RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
      KeyFactory factory = KeyFactory.getInstance("RSA");
      PublicKey key = factory.generatePublic(keySpec);

      return key;
    } catch (Exception e) {
      throw new RuntimeException("Spurious serialisation error", e);
    } finally {
      oin.close();
    }
  }

}

