package com.m3.common.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractRsaKeyProvider {
    private static final Pattern SSH_PUB_KEY = Pattern.compile("ssh-(rsa|dsa) ([A-Za-z0-9/+]+=*) (.*)");
    private static String BEGIN = "-----BEGIN";

    protected final String _sshkey;

    // TODO Ensure we can do the below for BCFips too
    public AbstractRsaKeyProvider(String sshkeyfile) {
        try {
            URL url = getClass().getResource(sshkeyfile);
            if (url == null)
                throw new IllegalArgumentException("File not found [" + sshkeyfile + "]");
            String asread = new String(Files.readAllBytes(Paths.get(url.toURI())));
//            System.err.println(asread);
            _sshkey = asread.replaceAll("\\n", "");
        } catch (URISyntaxException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String sshKey() { return _sshkey; }

    public RSAPublicKey parsePublicKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Invalid key");
        }
        key = key.strip();
        Matcher m = SSH_PUB_KEY.matcher(key);
        if (m.matches()) {
            String alg = m.group(1);
            String encKey = m.group(2);
            //String id = m.group(3);
            if (!"rsa".equalsIgnoreCase(alg)) {
                throw new IllegalArgumentException("Only RSA is currently supported, but algorithm was " + alg);
            }
            return parseSSHPublicKey(encKey);
        } else if (!key.startsWith(BEGIN)) {
            // Assume it's the plain Base64 encoded ssh key without the "ssh-rsa" at the start
            return parseSSHPublicKey(key);
        }
        KeyPair kp = parseKeyPair(key);
        if (kp.getPublic() == null) {
            throw new IllegalArgumentException("Key data does not contain a public key");
        }
        return (RSAPublicKey) kp.getPublic();
    }

    public abstract KeyPair parseKeyPair(String key);

    private RSAPublicKey parseSSHPublicKey(String encKey) {
        final byte[] PREFIX = new byte[] {0,0,0,7, 's','s','h','-','r','s','a'};
        byte[] benckey = encKey.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(benckey));

        byte[] prefix = new byte[11];
        try {
            if (in.read(prefix) != 11 || !Arrays.equals(PREFIX, prefix))
                throw new IllegalArgumentException("SSH key prefix not found");
            BigInteger e = new BigInteger(readBigInteger(in));
            BigInteger n = new BigInteger(readBigInteger(in));
            return createPublicKey(n, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    RSAPublicKey createPublicKey(BigInteger n, BigInteger e) {
        try {
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(n, e));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static byte[] readBigInteger(ByteArrayInputStream in) throws IOException {
        byte[] b = new byte[4];
        if (in.read(b) != 4)
            throw new IOException("Expected length data as 4 bytes");
        int l = (b[0] << 24) | (b[1] << 16) | (b[2] << 8) | b[3];

        b = new byte[l];
        if (in.read(b) != l)
            throw new IOException("Expected " + l + " key bytes");

        return b;
    }
}
