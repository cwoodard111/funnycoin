package org.funnycoin.miner;
/**
 * Trust me there will be a reason for it being in a different package in the future, trust me.
 */

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class VerificationUtils {
    /**
     **
     * @param plainText
     * @param signature
     * @param publicKey
     * @return boolean
     * @throws Exception
     */
    public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("RSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes(UTF_8));

        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        return publicSignature.verify(signatureBytes);
    }
}
