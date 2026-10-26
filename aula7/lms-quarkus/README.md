# LMS - Learning Management System (Quarkus + REST)

Projeto didático da **aula 7**: a API REST da aula 6 (CRUD de **Alunos**, **Trilhas** e **Docentes** gravados
em um **PostgreSQL** no **Docker**), agora com **relacionamentos entre as entidades**: cada trilha tem
**um docente responsável** e **uma lista de alunos matriculados**.

O que mudou em relação à aula 6:

- `modelo/Trilha`: novos campos `docente` (`@ManyToOne`) e `alunos` (`@ManyToMany`), com os métodos
  `adicionarAluno(...)` e `removerAluno(...)`.
- `servico/TrilhaService`: novos métodos `definirDocente`, `removerDocente`, `matricularAluno` e `desmatricularAluno`.
- `servico/AlunoService` e `DocenteService`: não deixam remover um aluno matriculado ou um docente responsável
  por uma trilha (`409`), em vez de deixar o banco recusar o DELETE (`500`).
- `repositorio/TrilhaRepository`: consultas `existeComDocente` e `existeComAluno`.
- `rest/TrilhaResource`: novos endpoints `/trilhas/{id}/docente/...` e `/trilhas/{id}/alunos/...`.
- `rest/dto/*Request`: validações de tamanho (`@Size`) e limite (`@Max`); as colunas no banco têm o mesmo tamanho máximo.
- Testes novos para os relacionamentos e para as validações.

## Pré-requisito: Docker

### 1. Instalar

| Sistema | Como instalar |
|---------|---------------|
| Windows | [Docker Desktop](https://docs.docker.com/desktop/setup/install/windows-install/) (ele pede para ativar o **WSL 2**; aceite e reinicie) |
| macOS   | [Docker Desktop](https://docs.docker.com/desktop/setup/install/mac-install/) (escolha Apple Silicon ou Intel) |
| Linux   | [Docker Engine](https://docs.docker.com/engine/install/) para a sua distribuição + o plugin `docker-compose-plugin` |

No **Linux**, depois de instalar, para usar o docker sem `sudo`:

```bash
sudo systemctl enable --now docker   # inicia o serviço do Docker (e em todo boot)
sudo usermod -aG docker $USER        # adiciona seu usuário ao grupo docker
# faça logout/login (ou reinicie) para valer
```

No **Windows/macOS**, basta abrir o Docker Desktop e esperar o ícone ficar "Engine running".

### 2. Conferir se está funcionando

```bash
docker --version          # ex.: Docker version 28.x
docker compose version    # ex.: Docker Compose version v2.x
docker run --rm hello-world
```

Se o `hello-world` imprimir *"Hello from Docker!"*, está tudo certo.

## Como rodar

```bash
# 1. sobe o PostgreSQL em segundo plano (na pasta onde está o docker-compose.yml)
docker compose up -d

# 2. sobe a aplicação (ela conecta em localhost:5432)
./mvnw quarkus:dev

# 3. testes: não precisam do docker compose, o Quarkus sobe um PostgreSQL
#    descartável sozinho (Dev Services). Só precisa do Docker rodando.
./mvnw test
```

- API: http://localhost:8080/alunos
- Swagger UI (testar pelo navegador): http://localhost:8080/q/swagger-ui
- Dev UI: http://localhost:8080/q/dev-ui
- Requisições prontas: `requisicoes.http` (IntelliJ ou VS Code REST Client)

## O banco de dados

Configuração (em `docker-compose.yml` e `application.properties`):

| Item     | Valor       |
|----------|-------------|
| Host     | `localhost` |
| Porta    | `5432`      |
| Banco    | `lms`       |
| Usuário  | `lms`       |
| Senha    | `lms`       |
| JDBC URL | `jdbc:postgresql://localhost:5432/lms` |

Tabelas criadas pelo Hibernate a partir das entidades:

| Tabela         | Colunas                                                          | De onde vem                     |
|----------------|------------------------------------------------------------------|---------------------------------|
| `aluno`        | `id`, `nome`, `email`                                            | `@Entity Aluno`                 |
| `docente`      | `id`, `nome`, `email`, `especialidade`                           | `@Entity Docente`               |
| `trilha`       | `id`, `nome`, `descricao`, `carga_horaria`, **`docente_id`** (FK) | `@Entity Trilha` + `@ManyToOne` |
| `trilha_aluno` | **`trilha_id`** (FK), **`aluno_id`** (FK)                        | `@ManyToMany` + `@JoinTable`    |

```
docente 1 ──── N trilha N ──── N aluno
          (docente_id)   (trilha_aluno)
```

> ⚠️ Com `quarkus.hibernate-orm.schema-management.strategy=drop-and-create`, as tabelas são **apagadas e recriadas**
> a cada vez que a aplicação sobe (e o `import.sql` roda de novo). Para os dados sobreviverem a um restart,
> troque para `update`. Em projetos reais usa-se uma ferramenta de migração (Flyway/Liquibase).

### Comandos úteis do Docker

```bash
docker compose ps                    # o container está rodando? (STATUS "healthy")
docker compose logs -f postgres      # ver os logs do banco
docker compose stop                  # para o container (os dados ficam)
docker compose down                  # remove o container (os dados ficam no volume lms-dados)
docker compose down -v               # remove container E volume (apaga todos os dados)

# abrir o psql (cliente de linha de comando) dentro do container
docker exec -it lms-postgres psql -U lms -d lms
#   \dt                  lista as tabelas
#   select * from aluno;
#   select * from trilha_aluno;   quem está matriculado em qual trilha
#   \q                   sai
```

Também dá para conectar por uma ferramenta gráfica (DBeaver, painel *Database* do IntelliJ, pgAdmin)
usando os dados da tabela acima.

### Problemas comuns

| Erro | Causa / solução |
|------|-----------------|
| `Cannot connect to the Docker daemon` | o Docker não está rodando: abra o Docker Desktop (ou `sudo systemctl start docker` no Linux) |
| `permission denied ... docker.sock` (Linux) | faltou `sudo usermod -aG docker $USER` + logout/login |
| `port is already allocated` / `address already in use` na 5432 | já existe um PostgreSQL na máquina; pare-o ou troque para `"5433:5432"` no compose e a URL para `localhost:5433` |
| `Connection to localhost:5432 refused` ao subir a aplicação | o container não está de pé: rode `docker compose up -d` e confira com `docker compose ps` |
| `Port 8080 already in use` | outra aplicação usa a 8080: `./mvnw quarkus:dev -Dquarkus.http.port=8081` |

## Endpoints

O mesmo contrato vale para `/alunos`, `/trilhas` e `/docentes`:

| Método | Caminho          | O que faz                                  | Sucesso                      | Erros possíveis      |
|--------|------------------|--------------------------------------------|------------------------------|----------------------|
| GET    | `/alunos`        | lista todos                                | `200 OK`                     | -                    |
| GET    | `/alunos/{id}`   | busca um                                   | `200 OK`                     | `404`                |
| POST   | `/alunos`        | cria                                       | `201 Created` + `Location`   | `400`, `409`, `415`  |
| PUT    | `/alunos/{id}`   | **substitui** o recurso (todos os campos)  | `200 OK`                     | `400`, `404`, `409`  |
| PATCH  | `/alunos/{id}`   | altera **só os campos enviados**           | `200 OK`                     | `400`, `404`, `409`  |
| DELETE | `/alunos/{id}`   | remove                                     | `204 No Content`             | `404`, `409`         |

> O `409` no DELETE acontece quando o aluno está matriculado em uma trilha (ou o docente é responsável por uma).

### Relacionamentos da trilha (novo)

| Método | Caminho                             | O que faz                                  | Sucesso                 | Erros possíveis |
|--------|-------------------------------------|--------------------------------------------|-------------------------|-----------------|
| PUT    | `/trilhas/{id}/docente/{docenteId}` | define (ou troca) o docente responsável    | `200 OK` + trilha       | `404`           |
| DELETE | `/trilhas/{id}/docente`             | deixa a trilha sem docente                 | `204 No Content`        | `404`           |
| POST   | `/trilhas/{id}/alunos/{alunoId}`    | matricula o aluno                          | `200 OK` + trilha       | `404`, `409`    |
| DELETE | `/trilhas/{id}/alunos/{alunoId}`    | desmatricula o aluno (idempotente)         | `204 No Content`        | `404`           |

Esses endpoints **não têm corpo**: os ids vão no caminho. Por isso aceitam requisição sem `Content-Type`.

```bash
curl -X PUT    http://localhost:8080/trilhas/1/docente/1
curl -X POST   http://localhost:8080/trilhas/1/alunos/2
curl -X DELETE http://localhost:8080/trilhas/1/alunos/2
```

### Corpo das requisições

```jsonc
// aluno
{"nome": "Ana Souza", "email": "ana@email.com"}
// docente
{"nome": "Carla Mendes", "email": "carla@ada.tech", "especialidade": "Java"}
// trilha
{"nome": "Back-end Java", "descricao": "Java, Quarkus e APIs REST", "cargaHoraria": 120}
```

### Validações do cadastro

| Campo                             | Regra                                        |
|-----------------------------------|----------------------------------------------|
| `nome` (aluno, docente, trilha)   | obrigatório, entre 3 e 100 caracteres        |
| `email` (aluno, docente)          | obrigatório, formato de email, até 150 caracteres |
| `especialidade` (docente)         | obrigatória, até 100 caracteres              |
| `descricao` (trilha)              | opcional, até 500 caracteres                 |
| `cargaHoraria` (trilha)           | obrigatória, entre 1 e 1000                  |

No POST/PUT todas as regras valem; no PATCH valem só para os campos enviados (`ValidadorPatch`).

### Resposta de uma trilha

O docente e os alunos vêm dentro do JSON da trilha:

```json
{
  "id": 1,
  "nome": "Back-end Java",
  "descricao": "Java, Quarkus e APIs REST",
  "cargaHoraria": 120,
  "docente": {"id": 1, "nome": "Carla Mendes", "email": "carla@ada.tech", "especialidade": "Java"},
  "alunos": [{"id": 2, "nome": "Bruno Lima", "email": "bruno@email.com"}]
}
```

### Códigos de status usados

| Código | Quando acontece                                                             |
|--------|-----------------------------------------------------------------------------|
| 200    | leitura ou atualização com sucesso (com corpo)                             |
| 201    | recurso criado; o header `Location` aponta para ele                         |
| 204    | remoção com sucesso (sem corpo)                                             |
| 400    | dados inválidos: campo obrigatório vazio, email inválido, tamanho ou limite fora da regra |
| 404    | o `id` não existe                                                           |
| 409    | conflito: email/nome já cadastrado, aluno já matriculado, ou remoção de aluno/docente em uso |
| 415    | `Content-Type` diferente de `application/json`                              |

Erros de negócio voltam como `{"status": 404, "erro": "Aluno com id 9 não encontrado(a)."}`.
Erros de validação (`@Valid`) usam o formato padrão do Quarkus, com a lista `violations`.

## Estrutura

```
src/main/java/br/com/ada/lms
├── modelo/      Aluno, Trilha, Docente           -> entidades JPA (tabelas) e o que é devolvido em JSON
├── rest/        *Resource                        -> rotas HTTP (camada fina)
│   ├── dto/     *Request, ErroDTO                -> o que entra e o que sai em caso de erro
│   └── ApiExceptionMapper                        -> exceção -> resposta HTTP 4xx
├── servico/     *Service, ValidadorPatch         -> regras de negócio + transações (@Transactional)
├── repositorio/ *Repository (Panache)            -> acesso ao banco (SELECT/INSERT/UPDATE/DELETE)
└── excecao/     ApiException e subclasses        -> 400, 404, 409
```

Fluxo de uma requisição: **Resource** → **Service** → **Repository** → PostgreSQL
(e se o Service lançar exceção → **ExceptionMapper** → resposta 4xx).

## Pontos para discutir em aula

1. **`@ManyToOne` x `@ManyToMany`**: por que o docente vira uma coluna (`docente_id`) na tabela `trilha`,
   mas os alunos precisam de uma tabela à parte (`trilha_aluno`)?
2. **Lazy x Eager e a `LazyInitializationException`**: a lista `alunos` está com `FetchType.EAGER`.
   O que acontece se voltar para o padrão (`LAZY`) e chamar o PATCH? (o Jackson monta o JSON depois que a
   transação fechou e não consegue mais buscar os alunos no banco)
3. **Chave estrangeira x checagem no service**: o banco já impede apagar um aluno matriculado.
   Por que checar antes no service? (`409` com mensagem clara em vez de `500`)
4. **Recurso aninhado**: por que `POST /trilhas/{id}/alunos/{alunoId}` em vez de mandar a lista de alunos
   no corpo do PUT da trilha? E por que o DELETE da matrícula é idempotente, mas matricular duas vezes dá `409`?
5. **Validação no DTO x tamanho da coluna**: por que `@Size(max = 100)` no DTO **e** `@Column(length = 100)`
   na entidade? (sem o `@Size`, um nome longo chegaria ao banco e daria `500`)
6. **Dirty checking**: no `matricularAluno` não chamamos `persist`. Quem gera o `INSERT` em `trilha_aluno`?
   Veja o SQL no console (`quarkus.hibernate-orm.log.sql=true`).

## Exercícios sugeridos

- Listar as trilhas de um aluno: `GET /alunos/{id}/trilhas` (dica: `@ManyToMany(mappedBy = "alunos")` em `Aluno`,
  e `@JsonIgnore` para não entrar em loop no JSON).
- Filtrar trilhas por docente: `GET /trilhas?docenteId=1`.
- Limitar a quantidade de alunos por trilha (ex.: 30) e responder `409` quando estiver cheia.
- Colocar no `import.sql` um docente responsável e alunos matriculados nas trilhas de exemplo.
- Trocar o `drop-and-create` por **Flyway** (`quarkus-flyway`) com um script `V1__criar_tabelas.sql`.
