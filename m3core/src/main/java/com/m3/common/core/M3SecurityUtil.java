package com.m3.common.core;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class M3SecurityUtil {
    private static final Logger _LOG = LoggerFactory.getLogger(M3SecurityUtil.class);

    private M3SecurityUtil() { throw new UnsupportedOperationException("No instantiation"); }

    public static String generateStrongKey(String clearpw) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        return generateStrongKey(clearpw.toCharArray());
    }
    public static String generateStrongKey(char[] clearpw) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 1000;
        byte[] salt = getSalt().getBytes(StandardCharsets.UTF_8);
        PBEKeySpec kspec = new PBEKeySpec(clearpw, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(kspec).getEncoded();
        return iterations + ":" + toHex(salt) + ":" + toHex(hash);
    }

    public static String getCertFingerPrint(Certificate cert) {
        byte[] digest = null;
        try {
            byte[] enc_certinfo = cert.getEncoded();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            digest = md.digest(enc_certinfo);
            return toHex(digest);
        } catch (Exception e) {
            _LOG.error("Exception: ", e);
        }
        return null;
    }

    public static char[] base64RandomUUID() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        ByteBuffer bb8 = Base64.getUrlEncoder().encode(bb);
        String ss8 = new String(bb8.array(), StandardCharsets.UTF_8);
        return ss8.toCharArray();
    }

    private static String getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Arrays.toString(salt);
    }

	private static String toHex(byte[] bary) throws NoSuchAlgorithmException {
	    BigInteger bi = new BigInteger(1, bary);
	    String hex = bi.toString(16);
	    int paddingLength = (bary.length * 2) - hex.length();
	    return (paddingLength > 0) ? String.format("%0" + paddingLength + "d", 0) + hex : hex;
    }
}
