DELETE FROM USERS WHERE id = '6ba7b811-9dad-11d1-80b4-00c04fd430c8';
INSERT INTO USERS (id, username, email, password_hash, authority, deactivated, private, two_factor_auth)
    VALUES ('6ba7b811-9dad-11d1-80b4-00c04fd430c8',
            'PrimeAdmin',
            'admin@email.com',
            '$2a$10$gLOuGk9WZgRVgTto/AVe1uPRmPOSMblfcj4I1NjAQrV/Q6XWCiM8e',
            'ADMIN',
            false,
            true,
            false);

-- username: PrimeAdmin, password: Pass123!