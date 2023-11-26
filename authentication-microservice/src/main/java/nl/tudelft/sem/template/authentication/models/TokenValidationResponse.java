package nl.tudelft.sem.template.authentication.models;

import nl.tudelft.sem.template.authentication.domain.user.Authority;

public class TokenValidationResponse {
    private final transient Authority authority;

    public TokenValidationResponse(Authority authority) {
        this.authority = authority;
    }
}
