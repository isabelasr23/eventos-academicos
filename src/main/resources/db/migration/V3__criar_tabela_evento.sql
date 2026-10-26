CREATE TABLE evento (
    id UUID NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descricao VARCHAR(2000) NOT NULL,
    inicio TIMESTAMP NOT NULL,
    fim TIMESTAMP NOT NULL,
    capacidade INTEGER NOT NULL,
    categoria_id UUID NOT NULL,
    CONSTRAINT pk_evento PRIMARY KEY (id),
    CONSTRAINT fk_evento_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    CONSTRAINT ck_evento_capacidade CHECK (capacidade > 0),
    CONSTRAINT ck_evento_periodo CHECK (fim > inicio)
);

CREATE INDEX idx_evento_categoria ON evento(categoria_id);
