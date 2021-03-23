package org.funnycoin.transactions;


import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Transaction {
    String ownerKey;
    String outputKey;
    float amount;
    public String transactionId;
    public String signature;
    public Transaction(String ownerKey, String outputKey, float amount, String signature) {
        this.signature = signature;
        this.outputKey = outputKey;
        this.ownerKey = ownerKey;
        this.amount = amount;
    }

    public String getOwnerKey() {
        return ownerKey;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public float getAmount() {
        return amount;
    }

    public boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("RSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes(UTF_8));

        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        return publicSignature.verify(signatureBytes);
    }

    public String sign(String plainText, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("RSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes(UTF_8));

        byte[] signature = privateSignature.sign();

        return Base64.getEncoder().encodeToString(signature);
    }

    public String getHash() {
        return applySha256(ownerKey + outputKey + amount + signature);
    }

    public String applySha256(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(input.getBytes("UTF-8"));

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
