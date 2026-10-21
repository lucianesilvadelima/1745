-- Dados iniciais: executado pelo Hibernate logo depois de criar as tabelas.
INSERT INTO aluno (nome, email) VALUES ('Ana Souza', 'ana@email.com');
INSERT INTO aluno (nome, email) VALUES ('Bruno Lima', 'bruno@email.com');

INSERT INTO docente (nome, email, especialidade) VALUES ('Carla Mendes', 'carla@ada.tech', 'Java');
INSERT INTO docente (nome, email, especialidade) VALUES ('Diego Rocha', 'diego@ada.tech', 'Banco de Dados');

INSERT INTO trilha (nome, descricao, carga_horaria) VALUES ('Back-end Java', 'Java, Quarkus e APIs REST', 120);
INSERT INTO trilha (nome, descricao, carga_horaria) VALUES ('Front-end', 'HTML, CSS e JavaScript', 80);
