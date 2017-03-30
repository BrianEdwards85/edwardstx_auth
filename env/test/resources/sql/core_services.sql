-- name: insert-service<!
INSERT INTO core.services (service,public_key)
VALUES (:service,:public_key)
