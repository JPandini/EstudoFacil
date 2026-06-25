# Estudo Fácil — Frontend

Frontend React para testar a API Estudo Fácil.

## Pré-requisitos

- Node.js 18+
- API rodando em `http://localhost:8080`

## Como rodar

Terminal 1 — API:
```powershell
cd estudo-facil-api
.\mvnw.cmd spring-boot:run
```

Terminal 2 — Frontend:
```powershell
cd estudo-facil-web
npm install
npm run dev
```

Acesse: **http://localhost:5173**

## Telas

- Login / Registro
- Painel (estatísticas)
- Matérias (CRUD)
- Tarefas (CRUD + filtros)
- Notas (CRUD + filtros)
- Perfil
