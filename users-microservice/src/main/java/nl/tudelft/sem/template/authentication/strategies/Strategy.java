package nl.tudelft.sem.template.authentication.strategies;

import java.util.List;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import nl.tudelft.sem.template.authentication.models.FilterBookRequestModel;

public interface Strategy {
    void passToService(FilterBookRequestModel bookRequest);

    String getUnauthorizedErrorMessage();

    String getNotAuthorErrorMessage();

    List<Authority> getAllowedAuthorities();
}
