CREATE TABLE `USERS`
(
    `ID` BIGINT NOT NULL AUTO_INCREMENT,
    `USERNAME` VARCHAR(64) NOT NULL,
    `PASSWORD` VARCHAR(64) NOT NULL,
    `EMAIL` VARCHAR(128) NOT NULL,
    `ENABLED` BOOLEAN NOT NULL,
    PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `AUTHORITIES`
(
    `ID` BIGINT NOT NULL AUTO_INCREMENT,
    `USER` BIGINT NOT NULL,
    `AUTHORITY` VARCHAR(16) NOT NULL,
    PRIMARY KEY (ID),
    FOREIGN KEY (USER) REFERENCES USERS(ID) ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `FILES`
(
    `ID` BIGINT NOT NULL AUTO_INCREMENT,
    `NAME` VARCHAR(64) NOT NULL,
    `TYPE` VARCHAR(8) NOT NULL,
    `OWNER` BIGINT NOT NULL,
    `PUBLIC` BOOLEAN NOT NULL,
    PRIMARY KEY (ID),
    FOREIGN KEY (OWNER) REFERENCES USERS(ID) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `SPECIFICATIONS`
(
    `ID` BIGINT NOT NULL AUTO_INCREMENT,
    `PARENT` BIGINT NOT NULL,
    `CHILD` BIGINT NOT NULL,
    PRIMARY KEY (ID),
    FOREIGN KEY (PARENT) REFERENCES FILES(ID) ON UPDATE RESTRICT ON DELETE RESTRICT,
    FOREIGN KEY (CHILD) REFERENCES FILES(ID) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO USERS (USERNAME, PASSWORD, EMAIL, ENABLED)
VALUES ('Jooji', 'admin', 'Jooji.Kolcak@Gmail.com', TRUE);

INSERT INTO AUTHORITIES (USER, AUTHORITY)
VALUES (1, 'administrator');

INSERT INTO FILES (NAME, TYPE, OWNER, PUBLIC)
VALUES ('example', 'dbm', 1, TRUE);

INSERT INTO FILES (NAME, TYPE, OWNER, PUBLIC)
VALUES ('example', 'sqlite', 1, TRUE);

INSERT INTO FILES (NAME, TYPE, OWNER, PUBLIC)
VALUES ('test', 'dbm', 1, FALSE);

INSERT INTO FILES (NAME, TYPE, OWNER, PUBLIC)
VALUES ('test', 'sqlite', 1, FALSE);

INSERT INTO SPECIFICATIONS (PARENT, CHILD)
VALUES (1, 2);

INSERT INTO SPECIFICATIONS (PARENT, CHILD)
VALUES (3, 4);