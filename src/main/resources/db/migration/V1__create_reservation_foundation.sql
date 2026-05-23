CREATE TABLE vaga (
    id UUID PRIMARY KEY,
    status VARCHAR(20) NOT NULL CHECK (status IN ('LIVRE', 'CONFIRMADA')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE reserva (
    id UUID PRIMARY KEY,
    vaga_id UUID NOT NULL REFERENCES vaga(id),
    cliente_id UUID NOT NULL,
    request_id UUID NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL CHECK (status = 'CONFIRMADA'),
    confirmed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX ux_reserva_confirmada_vaga
    ON reserva (vaga_id)
    WHERE status = 'CONFIRMADA';

CREATE TABLE resultado_requisicao (
    request_id UUID PRIMARY KEY,
    vaga_id UUID NOT NULL,
    cliente_id UUID NOT NULL,
    resultado VARCHAR(30) NOT NULL CHECK (resultado IN ('CONFIRMADA', 'CONFLITO_VAGA')),
    http_status INTEGER NOT NULL CHECK (http_status IN (201, 409)),
    response_body JSONB NOT NULL,
    reserva_id UUID REFERENCES reserva(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_resultado_resposta_consistente CHECK (
        (resultado = 'CONFIRMADA' AND http_status = 201 AND reserva_id IS NOT NULL)
        OR
        (resultado = 'CONFLITO_VAGA' AND http_status = 409 AND reserva_id IS NULL)
    )
);
