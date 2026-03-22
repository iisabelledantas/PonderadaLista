---
id: intro
title: Introdução
sidebar_position: 1
slug: /
---

# Introdução

## Visão Geral

Este projeto implementa um pipeline de telemetria para coleta e armazenamento de dados de sensores IoT. O sistema recebe dados em tempo real através de uma API REST, processa as mensagens de forma assíncrona utilizando filas e persiste os dados em um banco de dados relacional.

## Objetivo

Desenvolver uma solução escalável e conteinerizada para ingestão de dados de sensores, garantindo:

- Recebimento de dados via API REST
- Processamento assíncrono com filas de mensagens
- Persistência dos dados em banco relacional
- Portabilidade e reprodutibilidade via containers Docker

## Contexto

O sistema foi desenvolvido como parte de um projeto de aprendizado de infraestrutura em nuvem AWS, integrando serviços gerenciados com uma aplicação backend em Kotlin. O fluxo principal segue a seguinte sequência:

```
Sensor → POST /api/telemetria/dados → Backend → SQS → Lambda → RDS PostgreSQL
```

## Tecnologias Utilizadas

### Backend
| Tecnologia | Versão | Finalidade |
|---|---|---|
| Kotlin | 1.9.25 | Linguagem principal |
| Spring Boot | 3.5.11 | Framework web |
| Gradle | 8.x | Build e gerenciamento de dependências |
| Jackson | - | Serialização/desserialização JSON |

### Infraestrutura AWS
| Serviço | Finalidade |
|---|---|
| Amazon SQS | Fila de mensagens assíncrona |
| AWS Lambda | Processamento serverless das mensagens |
| Amazon RDS (PostgreSQL 17) | Banco de dados relacional gerenciado |
| Amazon S3 | Armazenamento do JAR da Lambda |

### Containerização
| Tecnologia | Finalidade |
|---|---|
| Docker | Containerização dos serviços |
| Docker Compose | Orquestração local dos containers |

### Testes
| Tecnologia | Finalidade |
|---|---|
| JUnit 5 | Framework de testes unitários |
| Mockito | Mocks e stubs para testes unitários |
| k6 | Testes de carga e performance |
| Grafana | Visualização dos resultados do k6 |

## Estrutura do Projeto

```
telemetria/
├── src/
│   ├── main/kotlin/com/inteli/telemetria/
│   │   ├── config/          # Configurações AWS e beans Spring
│   │   ├── controller/      # Endpoints REST
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── messaging/
│   │   │   ├── consumer/    # Handler da Lambda (RequestHandler)
│   │   │   └── processor/   # Lógica de processamento e INSERT no banco
│   │   ├── service/         # Serviço de envio ao SQS
│   │   └── utils/           # Funções utilitárias (ex: formatação de URL JDBC)
│   └── test/kotlin/         # Testes unitários
├── Dockerfile               # Imagem do backend
├── docker-compose.yml       # Orquestração local
├── load-test/               # Script de teste de carga k6
└── build.gradle.kts         # Configuração do build
```

## Pré-requisitos

- JDK 21
- Docker e Docker Compose
- k6 (para testes de carga)
- Conta AWS com permissões para SQS, Lambda e RDS
