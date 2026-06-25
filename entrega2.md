# Entrega 02 - Arquitetura REST e Mapeamento de Funcionalidades

# Estudo Fácil API

## Integrantes
- João Vitor Pandini
- Danner Demetrio
- Gabriel Ferrari

---

# Tema

Educação e Aprendizado 📚

---

# Descrição do Projeto

O Estudo Fácil é uma API REST desenvolvida para auxiliar estudantes no gerenciamento de suas atividades acadêmicas. O sistema permite o cadastro de matérias, tarefas e notas, além de fornecer informações para dashboards de acompanhamento do desempenho acadêmico.

---

# Entidades

## User

| Campo | Tipo |
|---------|---------|
| id | Long |
| name | String |
| email | String |
| password | String |

## Subject

| Campo | Tipo |
|---------|---------|
| id | Long |
| name | String |
| teacher | String |
| semester | String |
| description | String |

## Task

| Campo | Tipo |
|---------|---------|
| id | Long |
| title | String |
| description | String |
| dueDate | LocalDate |
| status | Enum |

## Grade

| Campo | Tipo |
|---------|---------|
| id | Long |
| value | Double |
| weight | Double |
| observation | String |

---

# Relacionamentos

```text
User
 │
 └── Subject
       ├── Task
       └── Grade
```

### Cardinalidades

- User 1:N Subject
- Subject 1:N Task
- Subject 1:N Grade

---

# Funcionalidades

## Usuários

- Cadastro de usuário
- Login
- Consulta de perfil

## Matérias

- Criar matéria
- Listar matérias
- Buscar matéria por ID
- Atualizar matéria
- Excluir matéria

## Tarefas

- Criar tarefa
- Listar tarefas
- Buscar tarefa por ID
- Atualizar tarefa
- Excluir tarefa
- Filtrar por status

## Notas

- Criar nota
- Listar notas
- Buscar nota por ID
- Atualizar nota
- Excluir nota

## Dashboard

- Média geral do aluno
- Quantidade de matérias cadastradas
- Quantidade de tarefas pendentes
- Quantidade de tarefas concluídas

---

# Arquitetura REST

## Autenticação

| Método | Endpoint | Descrição |
|----------|----------|----------|
| POST | /auth/register | Cadastro de usuário |
| POST | /auth/login | Login |
| GET | /auth/me | Perfil do usuário autenticado |

## Matérias

| Método | Endpoint | Descrição |
|----------|----------|----------|
| GET | /subjects | Listar matérias |
| GET | /subjects/{id} | Buscar matéria |
| POST | /subjects | Criar matéria |
| PUT | /subjects/{id} | Atualizar matéria |
| DELETE | /subjects/{id} | Excluir matéria |

## Tarefas

| Método | Endpoint | Descrição |
|----------|----------|----------|
| GET | /tasks | Listar tarefas |
| GET | /tasks/{id} | Buscar tarefa |
| POST | /tasks | Criar tarefa |
| PUT | /tasks/{id} | Atualizar tarefa |
| DELETE | /tasks/{id} | Excluir tarefa |

## Notas

| Método | Endpoint | Descrição |
|----------|----------|----------|
| GET | /grades | Listar notas |
| GET | /grades/{id} | Buscar nota |
| POST | /grades | Criar nota |
| PUT | /grades/{id} | Atualizar nota |
| DELETE | /grades/{id} | Excluir nota |

## Dashboard

| Método | Endpoint | Descrição |
|----------|----------|----------|
| GET | /dashboard | Estatísticas acadêmicas |

---

# Códigos HTTP

| Código | Descrição |
|----------|----------|
| 200 | OK |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request |
| 401 | Unauthorized |
| 404 | Not Found |
| 500 | Internal Server Error |

---

# Paginação, Ordenação e Filtros

## Paginação

```http
GET /subjects?page=0&size=10
```

## Ordenação

```http
GET /subjects?sort=name,asc
```

## Filtro por Status

```http
GET /tasks?status=PENDENTE
```

## Filtro por Semestre

```http
GET /subjects?semester=2026-1
```
