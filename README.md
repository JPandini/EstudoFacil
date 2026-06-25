# Estudo Fácil

## Descrição do projeto e integrantes

O **Estudo Fácil** é um sistema para organização acadêmica de estudantes. O projeto é um monorepo com duas partes:

- **`estudo-facil-api/`** — API REST em Spring Boot (matérias, tarefas, notas, dashboard, auditoria)
- **`estudo-facil-web/`** — interface web em React para consumir a API

Cada usuário gerencia suas próprias informações acadêmicas de forma isolada, com autenticação JWT.

**Integrantes:**

- João Vitor Pandini
- Danner Demetrio
- Gabriel Ferrari

## Descrição do problema

Estudantes universitários costumam organizar disciplinas, prazos de entrega e notas em planilhas, cadernos ou aplicativos separados. Isso dificulta:

- Acompanhar tarefas pendentes e datas de entrega por matéria
- Calcular médias ponderadas ao longo do semestre
- Ter uma visão geral do desempenho acadêmico

O **Estudo Fácil** centraliza essas informações em um único sistema: o aluno cadastra matérias, registra tarefas e notas, consulta médias e visualiza um painel com o resumo do semestre — tudo vinculado à sua conta.

## Tecnologias utilizadas

| Camada | Tecnologias |
|--------|-------------|
| Backend | Java 17, Spring Boot 4, Spring Security, JWT (jjwt), JPA/Hibernate, H2, Bean Validation, Lombok |
| Frontend | React, TypeScript, Vite, React Router |
| Documentação | SpringDoc OpenAPI (Swagger UI) |
| Testes | JUnit 5, MockMvc, Spring Security Test, H2 (profile `test`) |
| Ferramentas | Maven, Git, Node.js/npm |

## Limitações do projeto

- **Sem deploy em produção** — Docker, Kubernetes e hospedagem em nuvem estão fora do escopo; o projeto roda apenas em ambiente local.
- **Banco H2 em memória (dev)** — os dados são perdidos ao reiniciar a API em desenvolvimento.
- **URL da API fixa no frontend** — o client React aponta para `http://localhost:8080` (sem configuração por ambiente).
- **Sem notificações** — não há alertas de prazo, e-mail ou push.
- **Sem app mobile** — apenas interface web responsiva.
- **Auditoria parcial** — logs registram apenas operações em **Matéria** (CREATE, UPDATE, DELETE), não em tarefas ou notas.
- **Sem perfis de acesso** — `GET /usuarios` lista todos os usuários cadastrados; não há distinção entre admin e estudante.

## Entidades

O modelo de dados possui 5 entidades JPA com os seguintes relacionamentos:

```
Usuario (1) ──< Materia (1) ──< Tarefa
                      │
                      └──< Nota

AuditLog (independente — registra alterações em Matéria)
```

### Usuario

Responsável pelo acesso e isolamento dos dados acadêmicos.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Long | Chave primária |
| nome | String | Nome do estudante |
| email | String | Único, salvo em lowercase |
| senha | String | Hash BCrypt |
| criadoEm | LocalDateTime | Data de cadastro |
| atualizadoEm | LocalDateTime | Última atualização |

### Materia

Disciplina cadastrada pelo usuário.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Long | Chave primária |
| nome | String | Nome da disciplina |
| professor | String | Nome do professor |
| semestre | String | Formato `AAAA-1` ou `AAAA-2` |
| descricao | TEXT | Descrição livre |
| usuario_id | FK | Dono da matéria |

### Tarefa

Atividade acadêmica vinculada a uma matéria.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Long | Chave primária |
| titulo | String | Título da tarefa |
| descricao | TEXT | Detalhes |
| status | Enum | `PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDA` |
| dataEntrega | LocalDate | Prazo de entrega |
| materia_id | FK | Matéria vinculada |

### Nota

Avaliação com peso para cálculo de média ponderada.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Long | Chave primária |
| valor | Double | Valor de 0 a 10 |
| peso | Double | Peso da avaliação (soma por matéria ≤ 10) |
| observacao | String | Observação livre |
| materia_id | FK | Matéria vinculada |

### AuditLog

Registro de auditoria de operações em matérias.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Long | Chave primária |
| entity | Enum | `MATERIA` |
| action | Enum | `CREATE`, `UPDATE`, `DELETE` |
| entityId | Long | ID da matéria afetada |
| userEmail | String | Email do usuário autenticado |
| timestamp | LocalDateTime | Data/hora do evento |

## Rotas da API

A documentação **completa e interativa** de todas as rotas (parâmetros, schemas e testes) está disponível no **Swagger**:

**URL:** `http://localhost:8080/swagger-ui.html`

Para testar rotas protegidas, faça login em `POST /auth/login`, copie o token e clique em **Authorize** no Swagger (Bearer JWT).

### Resumo das rotas

| Grupo | Método | Rota | Auth | Descrição |
|-------|--------|------|------|-----------|
| Auth | POST | `/auth/registrar` | Não | Cadastrar usuário |
| Auth | POST | `/auth/login` | Não | Login (retorna JWT) |
| Auth | GET | `/auth/perfil` | Sim | Perfil do usuário logado |
| Auth | PUT | `/auth/perfil` | Sim | Atualizar perfil |
| Auth | DELETE | `/auth/conta` | Sim | Excluir conta |
| Usuários | GET | `/usuarios` | Sim | Listar usuários (paginado) |
| Usuários | GET | `/usuarios/{id}` | Sim | Buscar usuário por ID |
| Matérias | GET | `/materias` | Sim | Listar matérias do usuário |
| Matérias | GET | `/materias/{id}` | Sim | Buscar matéria por ID |
| Matérias | GET | `/materias/{id}/media` | Sim | Média ponderada da matéria |
| Matérias | POST | `/materias` | Sim | Criar matéria |
| Matérias | PUT | `/materias/{id}` | Sim | Atualizar matéria |
| Matérias | DELETE | `/materias/{id}` | Sim | Excluir matéria |
| Tarefas | GET | `/tarefas` | Sim | Listar tarefas do usuário |
| Tarefas | GET | `/tarefas/{id}` | Sim | Buscar tarefa por ID |
| Tarefas | POST | `/tarefas` | Sim | Criar tarefa |
| Tarefas | PUT | `/tarefas/{id}` | Sim | Atualizar tarefa |
| Tarefas | DELETE | `/tarefas/{id}` | Sim | Excluir tarefa |
| Notas | GET | `/notas` | Sim | Listar notas do usuário |
| Notas | GET | `/notas/{id}` | Sim | Buscar nota por ID |
| Notas | POST | `/notas` | Sim | Criar nota |
| Notas | PUT | `/notas/{id}` | Sim | Atualizar nota |
| Notas | DELETE | `/notas/{id}` | Sim | Excluir nota |
| Painel | GET | `/painel` | Sim | Dashboard acadêmico |
| Auditoria | GET | `/log` | Sim | Logs de auditoria do usuário |

### Exemplo 1 — Login

**Requisição:**

```http
POST /auth/login
Content-Type: application/json

{
  "email": "joao@email.com",
  "senha": "minhasenha123"
}
```

**Resposta (200):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "usuario": {
    "id": 1,
    "nome": "João Silva",
    "email": "joao@email.com",
    "criadoEm": "2026-06-21T10:00:00"
  }
}
```

### Exemplo 2 — Criar matéria

**Requisição:**

```http
POST /materias
Authorization: Bearer <token>
Content-Type: application/json

{
  "nome": "Engenharia de Software",
  "professor": "Prof. Carlos",
  "semestre": "2026-1",
  "descricao": "Boas práticas de desenvolvimento"
}
```

**Resposta (201):**

```json
{
  "id": 1,
  "nome": "Engenharia de Software",
  "professor": "Prof. Carlos",
  "semestre": "2026-1",
  "descricao": "Boas práticas de desenvolvimento",
  "usuarioId": 1,
  "criadoEm": "2026-06-21T14:30:00",
  "atualizadoEm": "2026-06-21T14:30:00"
}
```

### Exemplo 3 — Dashboard

**Requisição:**

```http
GET /painel
Authorization: Bearer <token>
```

**Resposta (200):**

```json
{
  "totalMaterias": 3,
  "tarefasPendentes": 2,
  "tarefasConcluidas": 5,
  "tarefasEmAndamento": 1,
  "mediaGeral": 7.8,
  "proximasTarefas": [
    {
      "id": 10,
      "titulo": "Trabalho final",
      "status": "PENDENTE",
      "dataEntrega": "2026-07-15",
      "materiaId": 1,
      "materiaNome": "Engenharia de Software"
    }
  ],
  "materiasComMedia": [
    {
      "materiaId": 1,
      "nomeMateria": "Engenharia de Software",
      "mediaPonderada": 8.5
    }
  ]
}
```

## Exemplos de erros HTTP

A API retorna erros padronizados em JSON via `GlobalExceptionHandler`.

### Tabela de códigos

| Código | Situação |
|--------|----------|
| 200 | Operação realizada com sucesso |
| 201 | Recurso criado com sucesso |
| 204 | Recurso excluído (sem corpo na resposta) |
| 400 | Dados inválidos ou regra de negócio violada |
| 401 | Não autenticado (credenciais ou token inválido) |
| 403 | Acesso negado |
| 404 | Recurso não encontrado |
| 415 | Content-Type não suportado |
| 500 | Erro interno do servidor |

### 400 — Erro de validação

Ocorre quando campos obrigatórios estão ausentes ou em formato inválido.

```json
{
  "status": 400,
  "message": "Erro de validação",
  "errors": {
    "email": "Email inválido",
    "senha": "Senha deve ter no mínimo 6 caracteres"
  }
}
```

### 400 — Regra de negócio

```json
{
  "status": 400,
  "message": "Soma dos pesos das notas não pode ultrapassar 10"
}
```

### 401 — Credenciais inválidas

```json
{
  "status": 401,
  "message": "Email ou senha inválidos"
}
```

### 401 — Token ausente ou inválido

```json
{
  "status": 401,
  "message": "Não autorizado. Token ausente ou inválido."
}
```

### 403 — Acesso negado

```json
{
  "status": 403,
  "message": "Acesso negado"
}
```

### 404 — Recurso não encontrado

```json
{
  "status": 404,
  "message": "Matéria não encontrada"
}
```

### 500 — Erro interno

```json
{
  "status": 500,
  "message": "Erro interno do servidor"
}
```

## Como executar o projeto localmente

### Pré-requisitos

- Java 17+
- Maven 3.9+ (ou usar o wrapper `mvnw` incluído)
- Node.js 18+ e npm (apenas para o frontend)
- Git

### Passo a passo

**1. Clone o repositório**

```bash
git clone https://github.com/JPandini/EstudoFacil.git
cd EstudoFacil
```

**2. Configure as variáveis de ambiente**

```bash
cd estudo-facil-api
cp .env.example .env
```

| Variável | Descrição | Padrão (dev) |
|----------|-----------|--------------|
| `JWT_SECRET` | Chave secreta JWT (mín. 256 bits) | valor de exemplo no `.env.example` |
| `JWT_EXPIRATION` | Expiração do token em ms | `86400000` (24h) |

> Em desenvolvimento, o banco H2 em memória é usado automaticamente — não é necessário configurar variáveis de banco.

**3. Execute a API**

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

API disponível em `http://localhost:8080`.

**4. Execute o frontend (opcional)**

```bash
cd estudo-facil-web
npm install
npm run dev
```

Frontend disponível em `http://localhost:5173`.

### URLs úteis

| Recurso | URL |
|---------|-----|
| API | `http://localhost:8080` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| H2 Console (dev) | `http://localhost:8080/h2-console` |
| Frontend | `http://localhost:5173` |

**H2 Console:** JDBC `jdbc:h2:mem:estudofacil` · User `sa` · Password `estudofacil`

## Outros conteúdos relevantes

### Autenticação JWT

- Login em `POST /auth/login` retorna token Bearer
- Token enviado no header `Authorization: Bearer <token>` em todas as rotas protegidas
- Senhas armazenadas com BCrypt; sessão stateless (sem cookies)

### Documentação Swagger

Documentação interativa com todos os endpoints, schemas de entrada/saída e botão **Authorize** para testar com JWT: `http://localhost:8080/swagger-ui.html`

### Testes automatizados

Suite de 33 testes de integração com MockMvc e H2 em memória:

```bash
cd estudo-facil-api
.\mvnw.cmd test
```

| Classe de teste | Cobertura |
|-----------------|-----------|
| `AuthControllerTest` | Registro, login, perfil, atualização, exclusão, 401 |
| `MateriaControllerTest` | CRUD, ownership, média ponderada |
| `TarefaControllerTest` | CRUD, filtros, 401, 404 |
| `NotaControllerTest` | CRUD, filtro por matéria, validação de peso |
| `UsuarioControllerTest` | Listagem paginada, busca por ID, 401, 404 |
| `PainelControllerTest` | Dashboard agregado, 401 |
| `AuditLogControllerTest` | Logs CREATE/UPDATE/DELETE e filtros |
| `NotaServiceTest` | Isolamento de notas por usuário |
| `TarefaServiceTest` | Validação de data de entrega no passado |
| `DashboardServiceTest` | Cálculo de médias ponderadas |

### Auditoria

Operações em matérias (criar, editar, excluir) geram logs consultáveis em `GET /log`, com filtros por `entity`, `action` e `entityId`. A tela de auditoria no frontend (`/auditoria`) exibe os logs em tabela ou JSON.

### Dashboard

`GET /painel` retorna totais de matérias e tarefas por status, média geral ponderada, próximas tarefas não concluídas e média individual por matéria.

### Frontend

Interface React com as telas: login, registro, painel, matérias, tarefas, notas, auditoria e perfil. O JWT é armazenado em `localStorage` e enviado automaticamente pelo client HTTP.

### Arquitetura

```
Controller  →  Service  →  Repository  →  Banco de dados (H2)
               ↑
           DTOs (JSON)
               ↑
           Entidades JPA
```

### Documentação técnica complementar

Para detalhes adicionais de implementação (segurança, services, configuração de profiles), consulte [DOCUMENTACAO-BACKEND.md](DOCUMENTACAO-BACKEND.md).
