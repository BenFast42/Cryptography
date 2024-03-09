package RoughDraft.KeyGen;

import java.io.*;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;


import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.RSAPrivateKeySpec;

import java.math.BigInteger;

import java.util.*;

public class keygenerator{
    public static void main(String[] args) throws Exception {
  

      Scanner scan = new Scanner(System.in);
      String userinput;

      while (true) {
          System.out.println("Please enter a 16-character symmetric key: ");
           userinput = scan.nextLine();
          if (userinput.length() != 16) {
              System.out.println("This input is not exactly 16 characters long, try again");
          } else {
              System.out.println("Input taken!");
              break;
          }
      }

      scan.close();
  

  //Writing userinput to the symmertic.key file 
  BufferedWriter writer = new BufferedWriter(new FileWriter("symmetric.key"));
    writer.write(userinput);
  
    writer.close();

    System.out.println("Symmertric Key successfully created!");

    SecureRandom random = new SecureRandom();
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(1024, random);  
    KeyPair pair = generator.generateKeyPair();
    Key XpubKey = pair.getPublic();
    Key XprivKey = pair.getPrivate();
    
    SecureRandom random2 = new SecureRandom();
    KeyPairGenerator generator2 = KeyPairGenerator.getInstance("RSA");
    generator2.initialize(1024, random2);
    KeyPair pair2 = generator2.generateKeyPair();
    Key YpubKey = pair2.getPublic();
    Key YprivKey = pair2.getPrivate();

    //get the parameters of the keys: modulus and exponet
    KeyFactory factory = KeyFactory.getInstance("RSA");
    RSAPublicKeySpec XpubKSpec = factory.getKeySpec(XpubKey, RSAPublicKeySpec.class);
    RSAPrivateKeySpec XprivKSpec = factory.getKeySpec(XprivKey, RSAPrivateKeySpec.class);

    RSAPublicKeySpec YpubKSpec = factory.getKeySpec(YpubKey, RSAPublicKeySpec.class);
    RSAPrivateKeySpec YprivKSpec = factory.getKeySpec(YprivKey, RSAPrivateKeySpec.class);

    //save the parameters of the keys to the files
    saveToFile("XPublic.key", XpubKSpec.getModulus(), XpubKSpec.getPublicExponent());
    saveToFile("XPrivate.key", XprivKSpec.getModulus(), XprivKSpec.getPrivateExponent());

    saveToFile("YPublic.key", YpubKSpec.getModulus(), YpubKSpec.getPublicExponent());
    saveToFile("YPrivate.key", YprivKSpec.getModulus(), YprivKSpec.getPrivateExponent());

    System.out.println("X Public Key successfully created!");
    System.out.println("X Private Key successfully created!");
    System.out.println("Y Public Key successfully created!");
    System.out.println("Y Private Key successfully created!");

    }

  //save the prameters of the public and private keys to file
  public static void saveToFile(String fileName,
  BigInteger mod, BigInteger exp) throws IOException {

ObjectOutputStream oout = new ObjectOutputStream(
new BufferedOutputStream(new FileOutputStream(fileName)));

try {
oout.writeObject(mod);
oout.writeObject(exp);
} catch (Exception e) {
throw new IOException("Unexpected error", e);
} finally {
oout.close();
}
}

}