# Zefi API — Soul Up

API RESTful do **Zefi**, avatar inteligente do Soul Up que incentiva ações sustentáveis: o usuário conta o que fez no chat, o Zefi avalia, dá pontos, recomenda missões e o usuário troca os pontos por recompensas.

Desenvolvida com **Java 21 + Spring Boot 3.5** (camadas Model / DAO / BO / Resource) e **Oracle Database** via JDBC.

## Como Usar

| Item | Link |
|---|---|
| API publicada (back-end) | `[colar o link do deploy no Render/Railway]` |
| Front-end (Vercel) | `[colar o link da Vercel]` |
| Repositório GitHub | `[colar o link do repositório]` |

Teste rápido: `GET {URL_DA_API}/api/health` deve responder `{"status":"UP","servico":"Zefi API"}`.

## Estrutura

```
src/main/java/br/com/zefi
├── model/      classes que representam as tabelas
├── dao/        acesso ao Oracle (JDBC + PreparedStatement)
├── bo/         regras de negócio e validações
├── resource/   endpoints REST
├── dto/        objetos de entrada e saída da API
├── filter/     CORS, rate limit e autenticação
├── exception/  exceções e tratamento global de erros
├── service/    pontos, recomendação, sessão e segurança
└── conexao/    ConnectionFactory
sql/schema.sql  criação das tabelas e missões iniciais
```

## Como rodar localmente

**Requisitos:** JDK 21, Maven 3.9+, Oracle (ex.: Oracle XE em Docker) e IntelliJ IDEA ou VS Code.

1. Importe o projeto como projeto Maven (IntelliJ: *File > Open* na pasta, ou *Open as Project* no `pom.xml`).
2. Execute `sql/schema.sql` no seu Oracle para criar as tabelas e as missões iniciais.
3. Configure as variáveis de ambiente (os valores padrão servem só para desenvolvimento local):

| Variável | Para que serve | Padrão local |
|---|---|---|
| `DB_URL` | URL JDBC do Oracle | `jdbc:oracle:thin:@//localhost:1521/XEPDB1` |
| `DB_USER` | usuário do banco | `system` |
| `DB_PASSWORD` | senha do banco | definida em `ConnectionFactory` |
| `ADMIN_KEY` | chave do cabeçalho `X-Admin-Key` (escrita em missões) | `zefi-admin-local` |
| `FRONT_URL` | origem do front liberada no CORS | `https://zefi.vercel.app` |
| `PORT` | porta HTTP | `8080` |

4. Rode `ZefiApplication` pela IDE ou use `mvn spring-boot:run`.
5. Acesse `http://localhost:8080/api/health`.

## Deploy (Render)

1. Suba o projeto para o GitHub.
2. No Render, crie um **Web Service** a partir do repositório (ambiente *Docker*; o `Dockerfile` já está pronto).
3. Cadastre as variáveis `DB_URL`, `DB_USER`, `DB_PASSWORD`, `ADMIN_KEY` e `FRONT_URL`. O Oracle precisa estar acessível pela internet.
4. Health check path: `/api/health`.

## Autenticação

- `POST /api/usuarios/login` devolve um token; envie `Authorization: Bearer <token>` nas rotas `/api/usuarios/{cpf}/...`. O token só dá acesso ao próprio CPF.
- Cadastro, login, `GET /api/missoes`, `GET /api/recompensas` e `/api/health` são públicos.
- `POST`, `PUT` e `DELETE` em `/api/missoes` exigem o cabeçalho `X-Admin-Key`.

## Endpoints

Veja a tabela completa (verbos e códigos de status) na documentação em PDF. Resumo:

| Recurso | Endpoints |
|---|---|
| Usuários | `POST /api/usuarios`, `POST /api/usuarios/login`, `POST /api/usuarios/logout`, `GET/PUT/DELETE /api/usuarios/{cpf}`, `GET /api/usuarios/{cpf}/engajamento` |
| Ações (chat) | `POST/GET /api/usuarios/{cpf}/acoes`, `GET/PUT/DELETE /api/usuarios/{cpf}/acoes/{id}` |
| Recomendações | `GET /api/usuarios/{cpf}/recomendacao`, `GET /api/usuarios/{cpf}/recomendacoes`, `PUT .../recomendacoes/{id}/aceitar`, `DELETE .../recomendacoes/{id}` |
| Missões | `GET/POST /api/missoes`, `GET/PUT/DELETE /api/missoes/{id}` |
| Recompensas | `GET /api/recompensas`, `POST /api/usuarios/{cpf}/resgates` |
| Saúde | `GET /api/health` |

## Equipe

`[nomes, RMs e turma dos integrantes]`
