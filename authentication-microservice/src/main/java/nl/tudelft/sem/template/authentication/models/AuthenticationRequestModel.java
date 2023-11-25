package nl.tudelft.sem.template.authentication.models;

import lombok.Data;

/**
 * Model representing an authentication request.
 */
@Data
public class AuthenticationRequestModel {
    private String identifier;
    private String password;
}