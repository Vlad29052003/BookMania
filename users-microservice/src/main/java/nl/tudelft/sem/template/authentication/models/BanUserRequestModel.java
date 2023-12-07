package nl.tudelft.sem.template.authentication.models;

import lombok.Data;

@Data
public class BanUserRequestModel {
    private boolean isBanned;
    private String username;
}
