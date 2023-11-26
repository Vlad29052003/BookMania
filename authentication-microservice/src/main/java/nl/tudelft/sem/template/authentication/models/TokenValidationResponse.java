package nl.tudelft.sem.template.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.user.Authority;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenValidationResponse {
    private transient Authority authority;
}
