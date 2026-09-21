CREATE TABLE books
(
  id                 BIGSERIAL PRIMARY KEY NOT NULL,
  author             VARCHAR(255)          NOT NULL,
  isbn               VARCHAR(20) UNIQUE    NOT NULL,
  price              NUMERIC(10, 2)        NOT NULL,
  title              VARCHAR(255)          NOT NULL,
  created_date       TIMESTAMP             NOT NULL,
  last_modified_date TIMESTAMP             NOT NULL,
  "version"          INTEGER               NOT NULL
);
