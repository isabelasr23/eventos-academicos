CREATE TABLE usuario (
    id UUID NOT NULL,
    nome VARCHAR(255),
    email VARCHAR(255),
    senha VARCHAR(255),
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)
);
