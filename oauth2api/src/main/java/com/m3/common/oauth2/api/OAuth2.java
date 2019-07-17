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

    String GRANT_TYPE = "grant_type";
    String CLIENT_ID = "client_id";
    String CLIENT_SECRET = "clent_secret";
    String USERNAME = "username";
    String PASSWORD = "password";
    String SCOPE = "scope";
    String STATE = "state";
    String ASSERTION = "assertion";
    String ASSERTION_TYPE = "assertion_type";
    String REDIRECT_URI = "redirect_uri";
    String RESPONSE_TYPE = "response_type";

    enum TokenType {
        BEARER("Bearer"), 
        MAC("MAC");

        private String _tokentype;
        TokenType(String grantType) {
            _tokentype = grantType;
        }

        public String toHtml() {
            return _tokentype;
        }

        public boolean amI(String grantType) {
            return _tokentype.equalsIgnoreCase(grantType);
        }

        public static TokenType getMe(String grantType) {
            if (BEARER.amI(grantType)) {
                return BEARER;
            } else if (MAC.amI(grantType)) {
                return MAC;
            }
            return null;
        }
    }

    enum GrantType {
        AUTHORIZATION_CODE("authorization_code"), 
        REFRESH_TOKEN("refresh_token"), 
        CLIENT_CREDENTIALS("client_credentials"), 
        JWT_BEARER("urn:ietf:params:oauth:grant-type:jwt-bearer"), 
        PASSWORD("password"), 
        IMPLICIT("implicit");

        private String _granttype;
        GrantType(String grantType) {
            _granttype = grantType;
        }

        public String toHtml() {
            return _granttype;
        }

        public boolean amI(String gntype) {
            return _granttype.equalsIgnoreCase(gntype);
        }

        public static GrantType getMe(String gntype) {
            if (AUTHORIZATION_CODE.amI(gntype)) {
                return AUTHORIZATION_CODE;
            } else if (REFRESH_TOKEN.amI(gntype)) {
                return REFRESH_TOKEN;
            } else if (CLIENT_CREDENTIALS.amI(gntype)) {
                return CLIENT_CREDENTIALS;
            } else if (JWT_BEARER.amI(gntype)) {
                return JWT_BEARER;
            } else if (PASSWORD.amI(gntype)) {
                return PASSWORD;
            } else if (IMPLICIT.amI(gntype)) {
                return IMPLICIT;
            }
            return null;
        }
    }

}
