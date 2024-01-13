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

DELETE FROM USERS WHERE id = '6bb7b811-9dad-11d1-80b4-00c04fd430c8';
INSERT INTO USERS (id, username, email,name, password_hash, authority, deactivated, private, two_factor_auth)
VALUES ('6bb7b811-9dad-11d1-80b4-00c04fd430c8',
        'authorescu',
        'author@email.com',
        'authorescu',
        '$2a$10$gLOuGk9WZgRVgTto/AVe1uPRmPOSMblfcj4I1NjAQrV/Q6XWCiM8e',
        'AUTHOR',
        false,
        true,
        false);

-- username: PrimeAdmin, password: Pass123!
-- username: authorescu, password: Pass123!