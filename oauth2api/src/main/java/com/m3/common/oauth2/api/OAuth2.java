package com.m3.common.oauth2.api;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
    String AUTHORIZATION_HEADER = "Authorization";
    String WWW_HEADER = "WWW-Authenticate";
    String ASSERT_TYPE_JWT_CLIENT_CREDENTIALS = "urn:ietf:params:oauth:client-assertion-type:jwt";

    enum TokenType {
        BEARER("Bearer"), 
        MAC("MAC");

        private final String _tokentype;
        TokenType(String toktype) {
            _tokentype = toktype;
        }

        public String toHtml() { return _tokentype; }
        public boolean amI(String toktype) { return _tokentype.equalsIgnoreCase(toktype); }

        public static TokenType getMe(String toktype) {
            if (BEARER.amI(toktype)) {
                return BEARER;
            } else if (MAC.amI(toktype)) {
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
            if (gntype == null) {
                return null;
            } else if (AUTHORIZATION_CODE.amI(gntype)) {
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

    enum AuthorizationType {
        BASIC("Basic"), 
        DIGEST("Digest");
    
        private final String _text;
        AuthorizationType(String txtvalue) {
            _text = txtvalue;
        }

        public String toHtml() { return _text; }
        public boolean amI(String txtvalue) { return _text.equalsIgnoreCase(txtvalue); }

        public static AuthorizationType getMe(String txtvalue) {
            if (BASIC.amI(txtvalue)) {
                return BASIC;
            } else if (DIGEST.amI(txtvalue)) {
                return DIGEST;
            }
            return null;
        }
    }

    class AuthorizationHeader {
        private String _realm = null;
        private String _name = null;
        private String _credential = null;

        public void decode(String headerValue) {
            if (headerValue == null || headerValue.isBlank()) return;
            headerValue = headerValue.strip();
            if (headerValue.startsWith(AuthorizationType.BASIC.toHtml())) {
                int valix = AuthorizationType.BASIC.toHtml().length() + 1; // for the space
                String encodedcredstr = headerValue.substring(valix);
                byte[] decodedcredb = Base64.getDecoder().decode(encodedcredstr.getBytes(StandardCharsets.UTF_8));
                String decodedcredstr = new String(decodedcredb);
                String[] credentials = null;
                if (decodedcredstr != null) credentials = decodedcredstr.split(":");
                if (credentials == null || credentials.length < 2) return;
                int ix = 0;
                if (credentials.length > 2) {
                    _realm = credentials[0].strip().substring("realm=".length());
                    ix++;
                }
                _name = credentials[ix].strip();
                _credential = credentials[ix].strip();
                if (_name != null && _name.isBlank()) _name = null;
                if (_credential != null && _credential.isBlank()) _credential = null;
            }
            // TODO support Digest
        }

        public String encode() {
        	// TODO implement
            return null;
        }

        public boolean isEmpty() { return (_name == null || _credential == null); }

        public String principal() { return _name; }
        public void principal(String value) { _name = value; }
        public String credential() { return _credential; }
        public void credential(String value) { _credential = value; }
        public String realm() { return _realm; }
        public void realm(String value) { _realm = value; }
    }

}
