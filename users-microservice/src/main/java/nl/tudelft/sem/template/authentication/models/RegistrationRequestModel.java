package nl.tudelft.sem.template.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing a registration request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequestModel {
    private String username;
    private String email;
    private String password;
}