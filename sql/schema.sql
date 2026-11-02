-- =====================================================================
-- Zefi (Soul Up) - Script de criacao do banco de dados (Oracle)
-- Execute como o usuario configurado em DB_USER (ex.: SQL Developer).
-- Estrutura deduzida das consultas dos DAOs; confira com o seu MER.
-- =====================================================================

-- Limpeza (opcional, para recriar do zero)
-- DROP TABLE recomendacao_avatar CASCADE CONSTRAINTS;
-- DROP TABLE acao_sustentavel   CASCADE CONSTRAINTS;
-- DROP TABLE missao             CASCADE CONSTRAINTS;
-- DROP TABLE usuario            CASCADE CONSTRAINTS;

CREATE TABLE usuario (
    cpf                 VARCHAR2(11)  NOT NULL,
    senha               VARCHAR2(100) NOT NULL,          -- hash BCrypt
    nome                VARCHAR2(100) NOT NULL,
    email               VARCHAR2(150) NOT NULL,
    cidade              VARCHAR2(100) NOT NULL,
    pontos_acumulados   NUMBER(10)    DEFAULT 0 NOT NULL,
    categoria_interesse VARCHAR2(30)  NOT NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (cpf),
    CONSTRAINT uq_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_pontos CHECK (pontos_acumulados >= 0),
    CONSTRAINT ck_usuario_categoria CHECK (categoria_interesse IN
        ('energia', 'moda', 'alimentação', 'reciclagem', 'transporte'))
);

CREATE TABLE missao (
    id                NUMBER GENERATED ALWAYS AS IDENTITY,
    nome              VARCHAR2(100) NOT NULL,
    categoria         VARCHAR2(30)  NOT NULL,
    pontos_recompensa NUMBER(5)     NOT NULL,
    CONSTRAINT pk_missao PRIMARY KEY (id),
    CONSTRAINT ck_missao_categoria CHECK (categoria IN
        ('energia', 'moda', 'alimentação', 'reciclagem', 'transporte')),
    CONSTRAINT ck_missao_pontos CHECK (pontos_recompensa BETWEEN 1 AND 1000)
);

CREATE TABLE acao_sustentavel (
    id                  NUMBER GENERATED ALWAYS AS IDENTITY,
    cpf_usuario         VARCHAR2(11)  NOT NULL,
    descricao           VARCHAR2(500) NOT NULL,
    categoria_detectada VARCHAR2(30)  NOT NULL,          -- inclui 'geral' (sem palavra-chave reconhecida)
    pontos_atribuidos   NUMBER(5)     NOT NULL,
    data_registro       TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_acao PRIMARY KEY (id),
    CONSTRAINT fk_acao_usuario FOREIGN KEY (cpf_usuario) REFERENCES usuario (cpf),
    CONSTRAINT ck_acao_categoria CHECK (categoria_detectada IN
        ('energia', 'moda', 'alimentação', 'reciclagem', 'transporte', 'geral'))
);

CREATE TABLE recomendacao_avatar (
    id                 NUMBER GENERATED ALWAYS AS IDENTITY,
    cpf_usuario        VARCHAR2(11) NOT NULL,
    missao_sugerida_id NUMBER       NOT NULL,
    data_sugestao      TIMESTAMP    DEFAULT SYSTIMESTAMP NOT NULL,
    aceita             NUMBER(1)    DEFAULT 0 NOT NULL,   -- 0 = pendente, 1 = concluida
    CONSTRAINT pk_recomendacao PRIMARY KEY (id),
    CONSTRAINT fk_rec_usuario FOREIGN KEY (cpf_usuario) REFERENCES usuario (cpf),
    CONSTRAINT fk_rec_missao  FOREIGN KEY (missao_sugerida_id) REFERENCES missao (id),
    CONSTRAINT ck_rec_aceita CHECK (aceita IN (0, 1))
);

CREATE INDEX idx_acao_usuario_data ON acao_sustentavel (cpf_usuario, data_registro);
CREATE INDEX idx_rec_usuario_data  ON recomendacao_avatar (cpf_usuario, data_sugestao);

-- Missoes iniciais (catalogo usado pelas recomendacoes do Zefi)
INSERT INTO missao (nome, categoria, pontos_recompensa) VALUES ('Apagar as luzes dos cômodos vazios', 'energia', 10);
INSERT INTO missao (nome, categoria, pontos_recompensa) VALUES ('Trocar uma lâmpada por LED', 'energia', 30);
INSERT INTO missao (nome, categoria, pontos_recompensa) VALUES ('Comprar uma peça em brechó', 'moda', 25);
INSERT INTO missao (nome, categoria, pontos_recompensa) VALUES ('Reaproveitar uma roupa antiga', 'moda', 20);
INSERT INTO missao (nome, categoria, pontos_recompensa) VALUES ('Começar uma pequena horta', 'alimentação', 35);
INSERT INTO missao (nome, categoria, pontos_recompensa) VALUES ('Fazer compostagem em casa', 'alimentação', 30);
INSERT INTO missao (nome, categoria, pontos_recompensa) VALUES ('Separar o lixo reciclável', 'reciclagem', 15);
INSERT INTO missao (nome, categoria, pontos_recompensa) VALUES ('Levar o lixo eletrônico a um ponto de coleta', 'reciclagem', 40);
INSERT INTO missao (nome, categoria, pontos_recompensa) VALUES ('Ir ao trabalho de bicicleta ou a pé', 'transporte', 25);
INSERT INTO missao (nome, categoria, pontos_recompensa) VALUES ('Dar carona ou usar transporte público', 'transporte', 20);

COMMIT;
