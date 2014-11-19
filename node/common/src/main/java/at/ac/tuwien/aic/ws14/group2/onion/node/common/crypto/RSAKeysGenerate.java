package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.*;

import sun.misc.BASE64Encoder;
import java.security.Security;

/**
 * Created by Milan on 18.11.2014.
 */
public class RSAKeysGenerate {

    private KeyPairGenerator keyPairGenerator;
   // private BASE64Encoder base64Encoder;

    static {
        synchronized (Security.class) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

        public RSAKeysGenerate() throws NoSuchProviderException, NoSuchAlgorithmException {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
           // base64Encoder = new BASE64Encoder();
        }

    public KeyPair generateKeys(long seed) {
       // try {

            SecureRandom secureRandom = createFixedRandom();
            //SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
            secureRandom.setSeed(seed);
            keyPairGenerator.initialize(1024, secureRandom);


            KeyPair keyPair = keyPairGenerator.generateKeyPair();
           /* Key publicKey = keyPair.getPublic();
            Key privateKey = keyPair.getPrivate();

            BufferedWriter out = new BufferedWriter(new FileWriter(publicKeyFile));
            out.write(base64Encoder.encode(privateKey.getEncoded()));
            out.close();

            out = new BufferedWriter(new FileWriter(privateKeyFile));
            out.write(base64Encoder.encode(privateKey.getEncoded()));
        } catch (Exception e) {
            System.out.println(e);
        }*/
            return keyPair;
    }

    public static SecureRandom createFixedRandom() {
        return new FixedRand();
    }

    private static class FixedRand extends SecureRandom {

        MessageDigest sha;
        byte[] state;

        FixedRand() {
            try {
                this.sha = MessageDigest.getInstance("SHA-1");
                this.state = sha.digest();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("can't find SHA-1!");
            }
        }

        public void nextBytes(byte[] bytes) {

            int off = 0;

            sha.update(state);

            while (off < bytes.length) {
                state = sha.digest();

                if (bytes.length - off > state.length) {
                    System.arraycopy(state, 0, bytes, off, state.length);
                } else {
                    System.arraycopy(state, 0, bytes, off, bytes.length - off);
                }

                off += state.length;

                sha.update(state);
            }
        }
    }

  /*  public static void main(String[] args) {

        String publicKeyFilename = null;
        String privateKeyFilename = null;

        RSAKeysGenerate generateRSAKeys = null;
        try {
            generateRSAKeys = new RSAKeysGenerate();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        if (args.length < 2) {
            System.err.println("Usage: java " + generateRSAKeys.getClass().getName() +
                    " Public_Key_Filename Private_Key_Filename");
            System.exit(1);
        }

        publicKeyFilename = args[0].trim();
        privateKeyFilename = args[1].trim();
        generateRSAKeys.generateKeys(publicKeyFilename, privateKeyFilename);

    }*/
}
