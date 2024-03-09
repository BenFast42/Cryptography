#  Symmetric-key Encrypted Message and its Digital Signature

This project acts as a simulation for basic cryptography techniques between a "Sender" and a "Receiver". This encryption method is a way of sending and receiving a message with integrity being perserved. 
(If data is not altered in transit, then the integrity of the original message is persereved)
This project was originally hosted on a virtual machine within a server at MSU Denver that used a Unix OS and the files were copied from the KeyGen directory into the other directories.


# In the simulation
The key generator program is used to generate a 16-byte symmetric key (via user input), and is also used to generate Public/Private key pairs for the sender and receiver (for a total of five keys).
These keys are proceeded with an X for the sender and a Y for the receiver. In this project, only the X keys are used in the actual programs.
The keys are then transfered to their expected places:

- Both the sender and receiver directories will receiver the symmertric key
- The XPrivate key is given to sender (the senders own private key)
- The XPublic key is given to the receiver (the senders public key)

# The programs are meant to function as follows
Run the sender program, and enter the filename that you used to encrypt (text.txt in this test case)

> On the sender side:
- The message (text.txt) is hashed by SHA-256 into a digital digest.
- The digital digest is then RSA Encrypted using the X Private key to create the digital signature. 
- Then the (Digital Signature concatenated with the Message) is AES encrypted in using the Symmetric key to create the ciphertext.
  
After the sender program has completed the file "message.aescipher" is transferred to the receiver directory for use.

> On the receiver side:
> 
The receiver program is ran and the user is prompted to enter the name of the file the program will create after decrypting the cipertext. (Usually its the same as the original messages name, i.e. test.txt)
- The ciphertext is AES-Decrypted back into the (digital signature || Message)
- Digital signature is RSA Decrypted using the X Public key back into the digital digest.
- The message (which is the remaining data after the digital signture is removed and decrypted) from the AES-Decryption is then locally hashed with SHA-256 to create another digital digest
- The program compares the digital digest (from decryption) with the locally hashed digital digest and if they match then the **integrity** of the message was preserved.
