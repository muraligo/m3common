package com.m3.common.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generate keys as follows:
 *
 * openssl genrsa -out private_key.pem 4096
 * openssl rsa -pubout -in private_key.pem -out public_key.pem
 * # convert private key to pkcs8 format in order to import it from Java
 * openssl pkcs8 -topk8 -in private_key.pem -inform pem -out private_key_pkcs8.pem -outform pem -nocrypt
 * 
 * and then use.
 * TODO Create equivalent BCFips version
 * 
 * @author museg
 *
 */
public class SimpleRsaKeyProvider extends AbstractRsaKeyProvider {
    private static Pattern PEM_DATA = Pattern.compile("-----BEGIN (.*)-----(.*)-----END (.*)-----", Pattern.DOTALL);

    @Override
    protected KeyPair parseKeyPair(String pemData) {
        Matcher m = PEM_DATA.matcher(pemData.strip());
        if (!m.matches()) {
            throw new IllegalArgumentException("String is not PEM encoded data");
        }
        String type = m.group(1);
        String rawcontent = m.group(2);
//        System.err.println(rawcontent);
        byte[] brawcontent = rawcontent.getBytes(StandardCharsets.UTF_8);
        final byte[] content = Base64.getDecoder().decode(brawcontent);

        PublicKey publicKey = null;
        PrivateKey privateKey = null;
        try {
            KeyFactory fact = KeyFactory.getInstance("RSA");
            if (type.equals("PRIVATE KEY")) {
                PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(content);
                privateKey = fact.generatePrivate(keySpecPKCS8);
                // following cast may fail
                RSAPrivateCrtKey rsaCertKey = (RSAPrivateCrtKey)privateKey;
                RSAPublicKeySpec keySpecRsaPub = new RSAPublicKeySpec(rsaCertKey.getModulus(), rsaCertKey.getPublicExponent());
                publicKey = fact.generatePublic(keySpecRsaPub);
            } else if (type.equals("PUBLIC KEY")) {
                X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(content);
                publicKey = fact.generatePublic(keySpecX509);
            }
            return new KeyPair(publicKey, privateKey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // TODO If we can do the below for BCFips too, we should push this to Abstract method
    public String readKeyContentFromClassPathFile(String filename) {
        try {
            URL url = getClass().getResource(filename);
            if (url == null)
                throw new IllegalArgumentException("File not found [" + filename + "]");
            String asread = new String(Files.readAllBytes(Paths.get(url.toURI())));
//            System.err.println(asread);
            return asread.replaceAll("\\n", "");
        } catch (URISyntaxException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
