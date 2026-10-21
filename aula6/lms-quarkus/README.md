# LMS - Learning Management System (Quarkus + REST)

Projeto didático da **aula 6**: a mesma API REST da aula 4 (CRUD de **Alunos**, **Trilhas** e **Docentes**),
agora com os dados gravados em um banco **PostgreSQL** que roda dentro de um container **Docker**.

O que mudou em relação à aula 4:

- `pom.xml`: novas dependências `quarkus-hibernate-orm-panache` (JPA/Hibernate) e `quarkus-jdbc-postgresql` (driver).
- `modelo/`: as classes viraram **entidades JPA** (`@Entity`, `@Id`, `@Column`) → cada uma é uma tabela.
- `repositorio/`: novos `*Repository` (Panache) que fazem o acesso ao banco.
- `servico/`: o `Map` em memória saiu; os services usam os repositories e `@Transactional`.
- `application.properties`: configuração da conexão com o banco.
- `import.sql`: dados iniciais inseridos ao subir a aplicação.
- `docker-compose.yml`: o container do PostgreSQL.

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

Tabelas: `aluno`, `docente` e `trilha`, criadas pelo Hibernate a partir das entidades.

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
| DELETE | `/alunos/{id}`   | remove                                     | `204 No Content`             | `404`                |

### Corpo das requisições

```jsonc
// aluno
{"nome": "Ana Souza", "email": "ana@email.com"}
// docente
{"nome": "Carla Mendes", "email": "carla@ada.tech", "especialidade": "Java"}
// trilha
{"nome": "Back-end Java", "descricao": "Java, Quarkus e APIs REST", "cargaHoraria": 120}
```

### Códigos de status usados

| Código | Quando acontece                                                             |
|--------|-----------------------------------------------------------------------------|
| 200    | leitura ou atualização com sucesso (com corpo)                             |
| 201    | recurso criado; o header `Location` aponta para ele                         |
| 204    | remoção com sucesso (sem corpo)                                             |
| 400    | dados inválidos: campo obrigatório vazio, email inválido, carga horária ≤ 0 |
| 404    | o `id` não existe                                                           |
| 409    | conflito: email (aluno/docente) ou nome (trilha) já cadastrado             |
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

1. **Entidade x DTO**: por que `Aluno` (entidade) e `AlunoRequest` (o que chega na API) continuam separados?
2. **`@Transactional`**: o que acontece se uma exceção for lançada no meio do método? (rollback)
3. **Dirty checking**: no PUT/PATCH não chamamos nenhum `update(...)`. Quem gera o `UPDATE`?
   Ligue `quarkus.hibernate-orm.log.sql=true` e veja o SQL no console.
4. **`unique = true` na coluna x checagem no service**: por que manter os dois?
5. **`drop-and-create` x `update` x migrations**: qual usar em desenvolvimento e em produção?
6. **Por que os testes usam um banco separado** (Dev Services) e não o do docker compose?

## Exercícios sugeridos

- Trocar o `drop-and-create` por **Flyway** (`quarkus-flyway`) com um script `V1__criar_tabelas.sql`.
- Filtro por query param usando o repository: `GET /alunos?nome=ana` → `repository.find("lower(nome) like ?1", ...)`.
- Associar um docente a uma trilha com `@ManyToOne` (`Trilha.docente`) e retornar `404` se o docente não existir.
- Matricular alunos em trilhas com `@ManyToMany`: `POST /trilhas/{id}/alunos/{alunoId}`.
- Paginação: `GET /alunos?pagina=0&tamanho=10` com `repository.findAll().page(...)`.
