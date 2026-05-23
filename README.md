<div align="center">
  <h1>Sistema de Agendamento</h1>
  <p><strong>API REST para gerenciamento de agendamentos em estabelecimentos comerciais</strong></p>

  <p>
    <img src="https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 17"/>
    <img src="https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=flat-square&logo=spring-boot&logoColor=white" alt="Spring Boot 4.0.6"/>
    <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL 8"/>
    <img src="https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white" alt="JWT"/>
    <img src="https://img.shields.io/badge/OpenAPI-3.0-85EA2D?style=flat-square&logo=swagger&logoColor=white" alt="OpenAPI 3"/>
    <img src="https://img.shields.io/badge/license-MIT-green?style=flat-square" alt="MIT License"/>
  </p>
</div>

## Sobre

API RESTful para gerenciamento completo de agendamentos em estabelecimentos comerciais. Oferece autenticação JWT, cadastro de profissionais e serviços, agendamento público (sem cadastro), dashboard com métricas, notificações em tempo real via SSE e envio de lembretes por e-mail.

## Funcionalidades

- **Autenticação JWT** com refresh token e blacklist de tokens
- **CRUD completo** de estabelecimentos, usuários, profissionais, serviços e agendamentos
- **Agendamento público** — clientes agendam sem necessidade de cadastro
- **Dashboard** com métricas e indicadores
- **Notificações em tempo real** via Server-Sent Events (SSE)
- **Lembretes automáticos** por e-mail via scheduler
- **Avaliações** dos clientes após o atendimento
- **Controle de horários** com bloqueios manuais e horários de funcionamento
- **Controle de acesso** por papéis (USER / ADMIN)
- **Documentação interativa** via Swagger UI

## Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Segurança | Spring Security + JWT (jjwt 0.12.6) |
| Banco de Dados | MySQL 8 + JPA / Hibernate |
| Documentação | Springdoc OpenAPI 3.0.2 (Swagger UI) |
| Build | Maven Wrapper |
| E-mail | Spring Mail (SMTP) |
| Utilitários | Lombok |

## Pré-requisitos

- Java 17+
- MySQL 8+
- Maven (opcional, usar wrapper incluso)

## Configuração

1. Clone o repositório:
```bash
git clone https://github.com/seu-usuario/sistema-agendamento.git
cd sistema-agendamento
```

2. Copie o arquivo de ambiente e preencha as variáveis:
```bash
cp .env.example .env
```

3. Configure o banco MySQL e as demais variáveis no `.env`.

4. Execute com Maven:
```bash
./mvnw spring-boot:run
```

A aplicação iniciará em `http://localhost:8080`.

## Variáveis de Ambiente

| Variável | Obrigatória | Padrão | Descrição |
|---|---|---|---|
| `DATABASE_USERNAME` | Não | `root` | Usuário do MySQL |
| `DATABASE_PASSWORD` | **Sim** | — | Senha do MySQL |
| `JWT_KEY` | **Sim** | — | Chave secreta para assinatura JWT |
| `JWT_EXPIRATION` | Não | `900000` (15 min) | Tempo de expiração do token (ms) |
| `JWT_REFRESH_EXPIRATION` | Não | `604800000` (7 dias) | Tempo de expiração do refresh token (ms) |
| `CORS_ALLOWED_ORIGINS` | Não | `http://localhost:5173` | Origens permitidas para CORS |
| `APP_URL` | Não | `http://localhost:5173` | URL da aplicação frontend |
| `MAIL_HOST` | Não | `smtp.gmail.com` | Servidor SMTP |
| `MAIL_PORT` | Não | `587` | Porta SMTP |
| `MAIL_USERNAME` | **Sim** | — | E-mail para envio |
| `MAIL_PASSWORD` | **Sim** | — | Senha do e-mail ou app password |
| `MAIL_FROM_NAME` | Não | `Sistema Agendamento` | Nome do remetente |

## Documentação da API

Com a aplicação rodando, acesse:

- **Swagger UI**: [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs)

## Estrutura do Projeto

```
src/main/java/com/sistemaagendamento/
├── config/          # Configurações (CORS, JWT, Jackson, Security)
├── controller/      # Endpoints REST
├── database/
│   ├── model/       # Entidades JPA
│   └── repository/  # Repositórios Spring Data
├── dto/             # Objetos de transferência de dados
├── enums/           # Enumerações
├── exception/       # Exceções customizadas
├── handler/         # Manipulador global de exceções
├── security/        # Regras de segurança
└── service/         # Lógica de negócios
```

## Scripts

```bash
# Iniciar aplicação
./mvnw spring-boot:run

# Executar testes
./mvnw test

# Compilar
./mvnw clean compile

# Gerar pacote JAR
./mvnw clean package
```

## Licença

Este projeto está licenciado sob a licença MIT — veja o arquivo [LICENSE](LICENSE) para detalhes.

---

<div align="center">
  <sub>Desenvolvido com ❤️</sub>
</div>
