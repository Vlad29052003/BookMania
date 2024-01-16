package nl.tudelft.sem.template.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing an authentication request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequestModel {
    private String username;
    private String password;
}