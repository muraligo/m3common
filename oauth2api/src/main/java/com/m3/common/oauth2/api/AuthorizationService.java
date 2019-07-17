package com.m3.common.oauth2.api;

public interface AuthorizationService {
    AuthorizationResponse handleAuthorizationCode();
    TokenResponse handlePassword();
    TokenResponse handleClientCredential();
    TokenResponse handleToken();
    // TODO for the above provide parameters

    public class AuthorizationResponse {
    	// TODO implement
    }

    public class TokenResponse {
        // TODO Implement
    }
}
