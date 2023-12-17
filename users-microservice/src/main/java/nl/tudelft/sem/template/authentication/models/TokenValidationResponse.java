package nl.tudelft.sem.template.authentication.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.user.Authority;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenValidationResponse {
    private transient UUID id;
    private transient Authority authority;
}
