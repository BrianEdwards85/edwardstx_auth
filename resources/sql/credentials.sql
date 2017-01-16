-- name: get-credentials-sql
SELECT email, hash, secret
  FROM auth.credentials
  WHERE email = :email
