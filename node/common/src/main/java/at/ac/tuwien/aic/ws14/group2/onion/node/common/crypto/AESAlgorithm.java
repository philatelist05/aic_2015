package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * Created by Milan on 12.11.2014.
 */
public class AESAlgorithm {

    private final BlockCipher cipherBlock = new AESEngine();

    private PaddedBufferedBlockCipher paddedBufferedBlockCipher;
    private KeyParameter keyParameter;

    public AESAlgorithm(){

    }

    public void setPadding(BlockCipherPadding blockCipherPadding){
        this.paddedBufferedBlockCipher = new PaddedBufferedBlockCipher(cipherBlock, blockCipherPadding);
    }

    public void setKey(byte[] key){
        this.keyParameter = new KeyParameter(key);
    }

    private byte[] crypting(byte[] bytes, boolean encrypt) throws InvalidCipherTextException {
        paddedBufferedBlockCipher.init(encrypt, keyParameter);

        byte[] output = new byte[paddedBufferedBlockCipher.getOutputSize(bytes.length)];
        int result = paddedBufferedBlockCipher.processBytes(bytes, 0,bytes.length, output, 0);

        paddedBufferedBlockCipher.doFinal(output, result);

        return output;
    }

    public byte[] encrypt(byte[] bytes) throws InvalidCipherTextException {
        return crypting(bytes, true);
    }

    public byte[] decrypt(byte[] bytes) throws InvalidCipherTextException {
        return crypting(bytes, false);
    }


}
