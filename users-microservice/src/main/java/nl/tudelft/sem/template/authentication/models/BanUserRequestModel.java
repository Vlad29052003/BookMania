package nl.tudelft.sem.template.authentication.models;

import lombok.Getter;
import lombok.Setter;

public class BanUserRequestModel {
    private boolean isBanned;
    @Getter
    @Setter
    private String username;

    @SuppressWarnings("")
    public boolean getIsBanned() {
        return this.isBanned;
    }

    @SuppressWarnings("")
    public void setIsBanned(boolean isBanned) {
        this.isBanned = isBanned;
    }
}
