CREATE TABLE devices
(
    id         SERIAL PRIMARY KEY,
    id_device  integer      NOT NULL,
    sender     VARCHAR(255) NOT NULL,
    created_at varchar(255)
);
