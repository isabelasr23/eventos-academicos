CREATE TABLE categoria (
    id UUID NOT NULL,
    nome VARCHAR(255) NOT NULL,
    CONSTRAINT pk_categoria PRIMARY KEY (id),
    CONSTRAINT uk_categoria_nome UNIQUE (nome)
);
