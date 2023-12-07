package nl.tudelft.sem.template.authentication.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import nl.tudelft.sem.template.authentication.domain.providers.TimeProvider;
import nl.tudelft.sem.template.authentication.domain.user.Authority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
    @Value("${jwt.secret}")
    private transient String jwtSecret;
    /**
     * Time provider to make testing easier.
     */
    private final transient TimeProvider timeProvider;

    @Autowired
    public JwtService(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date(timeProvider.getCurrentTime().toEpochMilli()));
    }

    public String extractUsername(String jwtToken) {
        return extractClaim(jwtToken, Claims::getSubject);
    }

    private Date extractExpiration(String jwtToken) {
        return extractClaim(jwtToken, Claims::getExpiration);
    }

    public Authority extractAuthorization(String jwtToken) {
        return Authority.valueOf((String) extractClaim(jwtToken, claims -> claims.get("authorities", List.class)).get(0));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }
}
