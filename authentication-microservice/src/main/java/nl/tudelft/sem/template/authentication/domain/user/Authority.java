package nl.tudelft.sem.template.authentication.domain.user;

import org.springframework.security.core.GrantedAuthority;

public enum Authority implements GrantedAuthority {
    REGULAR_USER("REGULAR_USER"),
    AUTHOR("AUTHOR"),
    ADMIN("ADMIN");

    private final String authority;

    Authority(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}
