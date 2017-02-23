-- name: get-service-key-sql
SELECT public_key
  FROM core.services
 WHERE service = :service
