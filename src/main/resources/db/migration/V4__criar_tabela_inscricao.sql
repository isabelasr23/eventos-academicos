CREATE TABLE inscricao (
    id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    evento_id UUID NOT NULL,
    data_inscricao TIMESTAMP NOT NULL,
    CONSTRAINT pk_inscricao PRIMARY KEY (id),
    CONSTRAINT fk_inscricao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_inscricao_evento FOREIGN KEY (evento_id) REFERENCES evento(id),
    CONSTRAINT uk_inscricao_usuario_evento UNIQUE (usuario_id, evento_id)
);

CREATE INDEX idx_inscricao_evento ON inscricao(evento_id);
