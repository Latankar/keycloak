package org.keycloak.jose.jws;

import org.keycloak.common.VerificationException;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.models.KeycloakSession;

import java.security.PublicKey;
import java.security.Signature;

public class KeyPairSignatureVerifierContext implements SignatureVerifierContext {

    private final String algorithm;
    private final String javaAlgorithm;
    private final KeyWrapper key;

    public KeyPairSignatureVerifierContext(KeycloakSession session, String kid, String algorithm, String javaAlgorithm) throws SignatureException {
        this.algorithm = algorithm;
        this.javaAlgorithm = javaAlgorithm;
        this.key = session.keys().getKey(session.getContext().getRealm(), kid, KeyUse.SIG, algorithm);
        if (key == null) {
            throw new SignatureException("Key not found");
        }
    }

    @Override
    public String getKid() {
        return key.getKid();
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public boolean verify(byte[] data, byte[] signature) throws VerificationException {
        try {
            Signature verifier = Signature.getInstance(javaAlgorithm);
            verifier.initVerify((PublicKey) key.getVerifyKey());
            verifier.update(data);
            return verifier.verify(signature);
        } catch (Exception e) {
            throw new VerificationException("Signing failed", e);
        }
    }

}