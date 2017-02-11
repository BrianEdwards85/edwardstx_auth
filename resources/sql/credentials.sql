-- name: get-credentials-sql
SELECT email, hash, salt, secret
  FROM auth.credentials
  WHERE email = :email

-- name: set-credentials-sql!
UPDATE auth.credentials
   SET hash = :hash, salt = :salt
 WHERE email = :email 
