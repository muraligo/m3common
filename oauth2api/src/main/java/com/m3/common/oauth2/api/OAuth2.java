package com.m3.common.oauth2.api;

/**
 * This interface holds constants shared by Auth Service (IdP), Client, and Resource
 * 
 * @author museg
 *
 */
public interface OAuth2 {
    // see Proof Key for Code Exchange (PKCE) RFC 7636
    public static final String CODE_CHALLENGE_METHOD_S256 = "S256";
    public static final int MIN_CODE_VERIFIER_LENGTH = 43;
    public static final int MAX_CODE_VERIFIER_LENGTH = 128;
    public static final int DEFAULT_CODE_VERIFIER_ENTROPY = 64;
    public static final int MIN_CODE_VERIFIER_ENTROPY = 32;
    public static final int MAX_CODE_VERIFIER_ENTROPY = 96;

}
