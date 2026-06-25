# Estudo Fácil — Documentação do Sistema (Backend)

API REST para organização acadêmica de estudantes. Este documento descreve a arquitetura, o código e o funcionamento do **backend** (`estudo-facil-api`), com referência breve ao frontend que consome a API.

**Stack:** Spring Boot 4.0.6 · Java 17 · JPA/Hibernate · Spring Security · JWT · H2 (dev/test)

**Integrantes:** João Vitor Pandini · Danner Demetrio · Gabriel Ferrari

---

## Índice

1. [Visão geral](#1-visão-geral)
2. [Estrutura do projeto](#2-estrutura-do-projeto)
3. [Modelo de dados](#3-modelo-de-dados)
4. [Endpoints da API](#4-endpoints-da-api)
5. [Camada de serviços e regras de negócio](#5-camada-de-serviços-e-regras-de-negócio)
6. [Segurança e autenticação JWT](#6-segurança-e-autenticação-jwt)
7. [Validações e tratamento de erros](#7-validações-e-tratamento-de-erros)
8. [Auditoria de matérias](#8-auditoria-de-matérias)
9. [Configuração e profiles](#9-configuração-e-profiles)
10. [Testes automatizados](#10-testes-automatizados)
11. [Swagger / OpenAPI](#11-swagger--openapi)
12. [Como executar](#12-como-executar)
13. [Frontend (referência)](#13-frontend-referência)

---

## 1. Visão geral

O backend segue arquitetura em camadas:

```
Cliente (React) → Controller → Service → Repository → Banco de dados
                       ↓
                  DTOs (JSON)
                       ↓
                 Entidades JPA
```

**Pacote base:** `br.com.estudofacil.estudo_facil_api`

| Camada | Pacote | Responsabilidade |
|--------|--------|------------------|
| Controllers | `controller/` | Endpoints REST, `@Valid`, autenticação JWT |
| Services | `service/` | Regras de negócio, transações, ownership |
| Repositories | `repository/` | Spring Data JPA |
| Entidades | `entity/` | Modelo relacional |
| DTOs | `dto/request`, `dto/response` | Contratos JSON |
| Segurança | `security/` | JWT, filtros, resposta 401 |
| Config | `config/` | Security, CORS, OpenAPI |
| Exceções | `exception/` | Tratamento global de erros |
| Utilitários | `util/` | Normalização de email e semestre |
| Enums | `enums/` | Status de tarefa, auditoria |

**Princípio central:** cada usuário só acessa seus próprios dados (matérias, tarefas, notas e logs de auditoria).

---

## 2. Estrutura do projeto

```
Estudo-Facil/
├── estudo-facil-api/                    ← BACKEND (foco deste documento)
│   ├── src/main/java/.../estudo_facil_api/
│   │   ├── EstudoFacilApiApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── WebConfig.java
│   │   │   └── OpenApiConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── MateriaController.java
│   │   │   ├── TarefaController.java
│   │   │   ├── NotaController.java
│   │   │   ├── PainelController.java
│   │   │   └── LogController.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── UsuarioService.java
│   │   │   ├── MateriaService.java
│   │   │   ├── TarefaService.java
│   │   │   ├── NotaService.java
│   │   │   ├── DashboardService.java
│   │   │   └── AuditService.java
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── security/
│   │   ├── exception/
│   │   ├── util/
│   │   └── enums/
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── application-dev.properties
│   │   └── application-prod.properties
│   ├── src/test/java/                   ← Testes MockMvc (33 testes)
│   └── pom.xml
│
└── estudo-facil-web/                    ← Frontend React (consome a API)
```

---

## 3. Modelo de dados

### Diagrama de relacionamentos

```
Usuario (1) ──< Materia (1) ──< Tarefa
                      │
                      └──< Nota

AuditLog  (independente — registra CREATE/UPDATE/DELETE em Materia)
```

### 3.1 Usuario — tabela `usuarios`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Long | PK, auto-increment |
| nome | String | Nome do estudante |
| email | String | Único; salvo em lowercase |
| senha | String | Hash BCrypt |
| criadoEm | LocalDateTime | `@CreationTimestamp` |
| atualizadoEm | LocalDateTime | `@UpdateTimestamp` |

**Relacionamento:** `OneToMany` com `Materia` (`cascade ALL`, `orphanRemoval`). Excluir usuário remove matérias, tarefas e notas em cascata.

### 3.2 Materia — tabela `materias`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Long | PK |
| nome | String | Obrigatório |
| professor | String | Opcional |
| semestre | String | Formato `AAAA-1` ou `AAAA-2` |
| descricao | TEXT | Opcional |
| usuario_id | FK | Dono da matéria |

**Índices:** `usuario_id`, `semestre`.

### 3.3 Tarefa — tabela `tarefas`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| titulo | String | Obrigatório |
| descricao | TEXT | Opcional |
| dataEntrega | LocalDate | Opcional |
| status | TaskStatus | PENDENTE, EM_ANDAMENTO, CONCLUIDA |
| materia_id | FK | Matéria vinculada |

### 3.4 Nota — tabela `notas`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| valor | Double | 0 a 10 |
| peso | Double | Positivo; soma por matéria ≤ 10 |
| observacao | String | Até 500 caracteres |
| materia_id | FK | Matéria vinculada |

### 3.5 AuditLog — tabela `audit_logs`

Registra automaticamente operações em matérias.

| Campo JSON | Coluna DB | Descrição |
|------------|-----------|-----------|
| id | id | PK |
| entity | entidade | `MATERIA` |
| action | acao | CREATE, UPDATE, DELETE |
| entityId | entidade_id | ID da matéria afetada |
| userEmail | user_email | Email do usuário autenticado |
| timestamp | registrado_em | Data/hora do evento |

---

## 4. Endpoints da API

**Base URL:** `http://localhost:8080`  
**Autenticação:** header `Authorization: Bearer <token>` (exceto rotas públicas)  
**Content-Type:** `application/json`

### 4.1 Autenticação — `/auth`

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| POST | `/auth/registrar` | Não | Cadastro de usuário |
| POST | `/auth/login` | Não | Login; retorna JWT |
| GET | `/auth/perfil` | Sim | Dados do usuário logado |
| PUT | `/auth/perfil` | Sim | Atualizar nome, email e/ou senha |
| DELETE | `/auth/conta` | Sim | Excluir conta (exige senha atual) |

**Exemplo login — resposta:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "usuario": {
    "id": 1,
    "nome": "João",
    "email": "joao@email.com",
    "criadoEm": "2026-06-21T10:00:00"
  }
}
```

### 4.2 Matérias — `/materias`

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/materias` | Lista paginada (`?semestre=2026-1&page=0&size=10`) |
| GET | `/materias/{id}` | Busca por ID (ownership) |
| GET | `/materias/{id}/media` | Média ponderada da matéria |
| POST | `/materias` | Cria matéria + log CREATE |
| PUT | `/materias/{id}` | Atualiza + log UPDATE |
| DELETE | `/materias/{id}` | Exclui + log DELETE |

### 4.3 Tarefas — `/tarefas`

| Método | Rota | Query params | Descrição |
|--------|------|--------------|-----------|
| GET | `/tarefas` | `materiaId`, `status`, paginação | Lista tarefas do usuário |
| GET | `/tarefas/{id}` | — | Busca por ID |
| POST | `/tarefas` | — | Cria tarefa |
| PUT | `/tarefas/{id}` | — | Atualiza |
| DELETE | `/tarefas/{id}` | — | Exclui |

**Status possíveis:** `PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDA`

### 4.4 Notas — `/notas`

| Método | Rota | Query params | Descrição |
|--------|------|--------------|-----------|
| GET | `/notas` | `materiaId`, paginação | Lista notas do usuário |
| GET | `/notas/{id}` | — | Busca por ID |
| POST | `/notas` | — | Cria nota |
| PUT | `/notas/{id}` | — | Atualiza |
| DELETE | `/notas/{id}` | — | Exclui |

### 4.5 Painel — `/painel`

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/painel` | Dashboard acadêmico do usuário |

**Resposta inclui:**

- Total de matérias
- Tarefas pendentes, em andamento e concluídas
- Média geral ponderada
- Próximas 5 tarefas (não concluídas, por data de entrega)
- Lista de matérias com média individual

### 4.6 Auditoria — `/log`

| Método | Rota | Query params | Descrição |
|--------|------|--------------|-----------|
| GET | `/log` | `entity`, `action`, `entityId`, paginação | Logs do usuário autenticado |

**Exemplo de registro:**

```json
{
  "id": 1,
  "entity": "MATERIA",
  "action": "UPDATE",
  "entityId": 15,
  "userEmail": "usuario@email.com",
  "timestamp": "2026-06-28T20:35:00"
}
```

---

## 5. Camada de serviços e regras de negócio

### UsuarioService

Centraliza `buscarPorEmail()` com normalização via `EmailUtil`. Usado por todos os services autenticados.

### AuthService

- Normaliza email no registro e login (`trim` + `lowercase`)
- Senha com BCrypt
- Emite JWT via `JwtUtil`
- Atualização de perfil: campos opcionais; troca de senha exige `senhaAtual`
- Exclusão de conta: exige `senhaAtual`; cascade remove matérias/tarefas/notas

### MateriaService

- CRUD com **ownership** (`findByIdAndUsuarioId`)
- Normaliza semestre via `SemestreUtil` (`2026-01` → `2026-1`)
- Chama `AuditService.registrar()` em create, update e delete (mesma transação)

### TarefaService

- Só opera sobre matérias do usuário logado
- Ao **criar**, valida que `dataEntrega` não seja no passado
- Status padrão: `PENDENTE`

### NotaService

- Listagem **sempre filtrada por usuário** (`findByUsuarioId`) — correção de segurança
- Valida que soma dos pesos por matéria não ultrapasse **10**
- Impede criar/editar nota em matéria de outro usuário

### DashboardService

- Contagens de tarefas por status
- Média geral: `SUM(valor × peso) / SUM(peso)`
- 5 próximas tarefas não concluídas
- Média por matéria para gráficos no frontend

### AuditService

- `registrar(entity, action, entityId, userEmail)` — persiste log
- `listar(...)` — consulta paginada escopada ao email do JWT

---

## 6. Segurança e autenticação JWT

### Fluxo

```
Request
  → JwtAuthenticationFilter (lê Bearer token)
  → SecurityContext (email do usuário)
  → Controller (@AuthenticationPrincipal UserDetails)
  → Service (ownership por email)
```

Se token ausente ou inválido em rota protegida:

```
JwtAuthenticationEntryPoint → 401 JSON
{ "status": 401, "message": "Não autorizado. Token ausente ou inválido." }
```

### Componentes

| Classe | Arquivo | Função |
|--------|---------|--------|
| JwtUtil | `security/JwtUtil.java` | Gera/valida token HS256 |
| JwtAuthenticationFilter | `security/JwtAuthenticationFilter.java` | Intercepta requests |
| JwtAuthenticationEntryPoint | `security/JwtAuthenticationEntryPoint.java` | Resposta 401 padronizada |
| SecurityConfig | `config/SecurityConfig.java` | Rotas públicas vs protegidas |
| WebConfig | `config/WebConfig.java` | CORS para `http://localhost:5173` |

### Rotas públicas

- `POST /auth/registrar`
- `POST /auth/login`
- `/swagger-ui/**`, `/v3/api-docs/**`
- `/h2-console/**` (somente profile `dev`)

### Configuração JWT

```properties
jwt.secret=${JWT_SECRET:estudofacil-secret-key-must-be-at-least-256-bits-long-for-hs256}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

Default: expiração de 24 horas (86400000 ms).

---

## 7. Validações e tratamento de erros

### DTOs com Bean Validation

| DTO | Regras principais |
|-----|-------------------|
| RegisterRequestDTO | nome ≤100; email válido; senha 6–255 |
| MateriaRequestDTO | nome obrigatório ≤100; semestre `AAAA-1` ou `AAAA-01` |
| TarefaRequestDTO | título ≤200; `@FutureOrPresent` em dataEntrega |
| NotaRequestDTO | valor 0–10; peso positivo; observação ≤500 |
| AtualizarPerfilRequestDTO | campos opcionais; nova senha 6–255 |
| ExcluirContaRequestDTO | senhaAtual obrigatória |

### GlobalExceptionHandler

| Exceção | HTTP | Resposta |
|---------|------|----------|
| ResourceNotFoundException | 404 | `{ status, message }` |
| BusinessException | 400 | `{ status, message }` |
| MethodArgumentNotValidException | 400 | `{ status, message, errors: { campo: msg } }` |
| BadCredentialsException | 401 | Email ou senha inválidos |
| AccessDeniedException | 403 | Acesso negado |
| HttpMediaTypeNotSupportedException | 415 | Content-Type deve ser application/json |
| Exception (genérico) | 500 | Erro interno (logado com `@Slf4j`) |

---

## 8. Auditoria de matérias

Integrada ao domínio principal — não é cenário artificial.

**Quando registra:**

| Operação | Momento | action |
|----------|---------|--------|
| Criar matéria | Após `save` | CREATE |
| Atualizar matéria | Após `save` | UPDATE |
| Excluir matéria | Antes de `delete` | DELETE |

**Consulta:** `GET /log` retorna apenas logs do **usuário autenticado**.

**Filtros:** `entity=MATERIA`, `action=CREATE|UPDATE|DELETE`, `entityId=15`

**Arquivos envolvidos:**

- `entity/AuditLog.java`
- `enums/AuditAction.java`, `enums/AuditEntity.java`
- `repository/AuditLogRepository.java`
- `service/AuditService.java`
- `controller/LogController.java`
- Integração em `service/MateriaService.java`

---

## 9. Configuração e profiles

| Profile | Ativação | Banco | Observações |
|---------|----------|-------|-------------|
| `dev` | Default (`application.properties`) | H2 memória | H2 Console, show-sql |
| `prod` | `SPRING_PROFILES_ACTIVE=prod` | PostgreSQL | Variáveis `DB_*` |
| `test` | Testes (`@ActiveProfiles("test")`) | H2 isolado | `create-drop` |

### Arquivos de configuração

| Arquivo | Conteúdo |
|---------|----------|
| `application.properties` | Profile default, JWT, JPA base |
| `application-dev.properties` | H2, console, show-sql |
| `application-prod.properties` | PostgreSQL, dialect |
| `application-test.properties` | H2 para testes |

### H2 Console (dev)

- URL: `http://localhost:8080/h2-console`
- JDBC: `jdbc:h2:mem:estudofacil`
- User: `sa` · Senha: `estudofacil`

---

## 10. Testes automatizados

Local: `estudo-facil-api/src/test/java`

| Classe | Cobertura |
|--------|-----------|
| `AuthControllerTest` | register, login, perfil, update, delete, 401 |
| `MateriaControllerTest` | CRUD, ownership, média ponderada |
| `TarefaControllerTest` | CRUD, filtros por status/materia, 401, 404 |
| `NotaControllerTest` | CRUD, filtro por matéria, validação de peso |
| `UsuarioControllerTest` | listagem paginada, busca por ID, 401, 404 |
| `PainelControllerTest` | dashboard agregado, 401 |
| `AuditLogControllerTest` | logs CREATE/UPDATE/DELETE, filtros, isolamento |
| `NotaServiceTest` | listagem só do usuário logado |
| `TarefaServiceTest` | data de entrega no passado |
| `DashboardServiceTest` | cálculo de médias ponderadas |
| `EstudoFacilApiApplicationTests` | context load |

**Total:** 33 testes automatizados.

**Executar:**

```powershell
cd estudo-facil-api
.\mvnw.cmd test
```

Usa `@SpringBootTest` + `MockMvc` + H2 em memória.

---

## 11. Swagger / OpenAPI

- **URL:** `http://localhost:8080/swagger-ui.html`
- **Config:** `config/OpenApiConfig.java`
- Bearer JWT documentado para testar endpoints autenticados
- Dependência: `springdoc-openapi-starter-webmvc-ui`

---

## 12. Como executar

### Backend (desenvolvimento)

```powershell
cd estudo-facil-api
.\mvnw.cmd spring-boot:run
```

API disponível em `http://localhost:8080`.

### Frontend

```powershell
cd estudo-facil-web
npm install
npm run dev
```

Frontend em `http://localhost:5173` — consome a API via `http://localhost:8080`.

---

## 13. Frontend (referência)

React + Vite + TypeScript em `estudo-facil-web/`.

| Rota | Página | Endpoint principal |
|------|--------|-------------------|
| `/login` | LoginPage | POST `/auth/login` |
| `/registrar` | RegisterPage | POST `/auth/registrar` |
| `/painel` | PainelPage | GET `/painel` |
| `/materias` | MateriasPage | CRUD `/materias` |
| `/tarefas` | TarefasPage | CRUD `/tarefas` |
| `/notas` | NotasPage | CRUD `/notas` |
| `/auditoria` | LogPage | GET `/log` (tabela + JSON) |
| `/perfil` | PerfilPage | GET/PUT `/auth/perfil` |

O JWT é armazenado em `localStorage` e enviado automaticamente pelo `api/client.ts`.

---

## Dependências principais (pom.xml)

| Dependência | Uso |
|-------------|-----|
| spring-boot-starter-web | API REST |
| spring-boot-starter-data-jpa | ORM |
| spring-boot-starter-security | Autenticação |
| spring-boot-starter-validation | Bean Validation |
| jjwt 0.12.6 | Tokens JWT |
| h2 | Banco dev/test |
| springdoc-openapi | Swagger |
| lombok | Boilerplate |
| spring-boot-devtools | Hot reload (dev) |

---

*Documento gerado com base no código-fonte do repositório Estudo-Facil.*
