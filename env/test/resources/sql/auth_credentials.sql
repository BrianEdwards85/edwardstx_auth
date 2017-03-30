-- name: insert-credential<!
INSERT INTO auth.credentials (email,hash,secret,salt)
VALUES (:email,:hash,:secret,:salt)
