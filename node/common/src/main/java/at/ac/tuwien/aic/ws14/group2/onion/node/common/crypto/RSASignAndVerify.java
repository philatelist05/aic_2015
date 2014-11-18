package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

/**
 * Created by Milan on 18.11.2014.
 */
public class RSASignAndVerify {

    public RSASignAndVerify(){

    }

    public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initSign(key);
        signer.update(data);
        return (signer.sign());
    }

    public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initVerify(key);
        signer.update(data);
        return (signer.verify(sig));

    }
}
