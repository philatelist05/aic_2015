package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CTSBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public class AESAlgorithm {

    private final BlockCipher cipherBlock = new AESEngine();

    private CTSBlockCipher blockCipher;
    private KeyParameter keyParameter;

    static {
        synchronized (Security.class) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public AESAlgorithm(){
        this.blockCipher = new CTSBlockCipher(cipherBlock);
    }

    public void setKey(byte[] key){
        this.keyParameter = new KeyParameter(key);
    }

    /**
     * The minimum length of a string to be encrypted.
     */
    public int getMinimumLength() {
        return blockCipher.getBlockSize();   // required by CTS mode
    }

    private byte[] crypting(byte[] bytes, boolean encrypt) throws InvalidCipherTextException {
        blockCipher.init(encrypt, keyParameter);

        byte[] output = new byte[blockCipher.getOutputSize(bytes.length)];
        int result = blockCipher.processBytes(bytes, 0,bytes.length, output, 0);

        blockCipher.doFinal(output, result);

        return output;
    }

    public byte[] encrypt(byte[] bytes) throws InvalidCipherTextException {
        return crypting(bytes, true);
    }

    public byte[] decrypt(byte[] bytes) throws InvalidCipherTextException {
        return crypting(bytes, false);
    }
}
