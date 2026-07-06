# B2B Order Processing

Proyecto para procesamiento de ordenes B2B usando Event Driven Architecture. La solucion recibe ordenes por medio de un API de inserción de ordenes, publica los mensajes en Kafka y un worker reactivo procesa, enriquece, calcula impuestos y persiste el resultado en MongoDB.

## Arquitectura

El proyecto esta dividido en servicios independientes:

```text
b2b-order-processing/
├── order-ingest-api/   # API Java Spring Boot que publica ordenes en Kafka
├── worker/             # Worker Java Spring Boot reactivo que procesa las ordenes y las enriquece
├── clients-api/        # API NestJS de clientes con cache en Redis
├── products-api/       # API Go de productos con cache en Redis
├── mongo-init/         # Scripts de inicializacion de MongoDB
├── docker-compose.yml  # Stack completo local
└── B2B-Collection.postman_collection.json   #Colección de Postman con ejemplos de requests.
```

Flujo principal:

1. `order-ingest-api` recibe una orden por HTTP.
2. El API publica el mensaje en Kafka, en el topic `orders-topic`.
3. `worker` consume la orden de forma reactiva.
4. El worker valida campos obligatorios e idempotencia por `orderId`.
5. El worker consulta productos y clientes de forma reactiva.
6. Se calculan subtotal, impuestos y total.
7. La orden enriquecida se guarda en MongoDB.
8. Los errores no recuperables se publican en `orders-dlt`.

## Stack tecnico

- Java 21
- Spring Boot 3.2
- Spring WebFlux
- Reactor Kafka
- MongoDB Reactive
- Resilience4j Retry y Circuit Breaker
- Virtual Threads habilitados
- Lombok
- NestJS
- Go
- Kafka
- MongoDB
- Redis
- Docker Compose
- JUnit 5
- Mockito
- Testcontainers
- WireMock

## Servicios

| Servicio | Puerto local | Descripcion |
| --- | ---: | --- |
| order-ingest-api | 8080 | API para recibir ordenes y publicarlas en Kafka |
| products-api | 8081 | API de productos |
| clients-api | 8082 | API de clientes |
| mongo-express | 8083 | Interfaz grafica de MongoDB |
| redis-commander | 8084 | Interfaz grafica de Redis |
| order-worker | 8085 | Worker expuesto para health/actuator |
| kafka-ui | 8090 | Interfaz grafica de Kafka |
| Kafka externo | 9092 | Acceso externo a Kafka |
| MongoDB | 27017 | Base de datos MongoDB para ordenes procesadas |
| Redis | 6379 | Cache usado por APIs de productos y clientes |

URLs utiles:

- Kafka UI: `http://localhost:8090`
- Mongo Express: `http://localhost:8083`
- Redis Commander: `http://localhost:8084`
- Order Ingest API: `http://localhost:8080`
- Products API: `http://localhost:8081`
- Clients API: `http://localhost:8082`

## Levantar el proyecto

Requisitos:

- Docker Desktop ejecutandose
- Docker Compose

Comando principal:

```bash
docker compose up --build
```

Para levantar en segundo plano:

```bash
docker compose up --build -d
```

Para detener y eliminar volumenes:

```bash
docker compose down -v
```

> Nota: usar `docker compose down -v` reinicia tambien los datos de MongoDB y Redis. Esto permite validar la inicializacion desde cero.

## Kafka

Topics creados automaticamente por `kafka-init`:

- `orders-topic`: topic principal donde se publican las ordenes.
- `orders-dlt`: Dead Letter Topic para errores no recuperables.

## MongoDB

Base de datos:

```text
orders_db
```

Coleccion principal:

```text
enriched_orders
```

Credenciales locales:

```text
Usuario: b2b_user
Password: b2b_password
```

URI usada por el worker:

```text
mongodb://b2b_user:b2b_password@mongodb:27017/orders_db?authSource=admin
```

La inicializacion de MongoDB se encuentra en:

```text
mongo-init/
```

Estos scripts crean la estructura necesaria cuando el volumen de MongoDB esta vacio. Si se desea forzar nuevamente la inicializacion, ejecutar:

```bash
docker compose down -v
docker compose up --build
```

## Redis

Redis es usado como cache por las APIs de productos y clientes:

- `products-api`
- `clients-api`


## Request de ejemplo

Endpoint:

```http
POST http://localhost:8080/orders
Content-Type: application/json
```

Body:

```json
{
  "orderId": "ORD-2026-COL-00001",
  "clientId": "CLI-99821",
  "channel": "B2B",
  "createdAt": "2026-07-06T18:00:00Z",
  "items": [
    {
      "productId": "PRD-001",
      "quantity": 24,
      "unitPrice": 3500.00
    },
    {
      "productId": "PRD-008",
      "quantity": 12,
      "unitPrice": 8200.00
    },
    {
      "productId": "PRD-010",
      "quantity": 5,
      "unitPrice": 1000.00
    }
  ]
}
```

Resultado esperado:

- El mensaje se publica en `orders-topic`.
- El worker consume el mensaje.
- La orden enriquecida queda persistida en MongoDB.
- Si ocurre un error no recuperable, el mensaje se publica en `orders-dlt`.

## Reglas de negocio

Validaciones minimas:

- `orderId` es obligatorio.
- `clientId` es obligatorio.
- `items` no puede estar vacio.
- Cada item debe tener `productId`, `quantity` y `unitPrice`.

Idempotencia:

- La idempotencia se maneja por `orderId` en MongoDB.
- Si una orden ya fue procesada, el worker no la procesa nuevamente.
- Existe indice unico sobre `orderId` para evitar duplicados.

Calculo de impuestos:

| Categoria | Tasa |
| --- | ---: |
| GRAVADO | 19% |
| REDUCIDO | 5% |
| EXENTO | 0% |

## Worker

Variables principales:

```text
SERVER_PORT=8080
KAFKA_BOOTSTRAP_SERVERS=kafka:29092
ORDERS_TOPIC=orders-topic
ORDERS_DLT_TOPIC=orders-dlt
ORDERS_CONSUMER_GROUP=order-worker-group
MONGODB_URI=mongodb://b2b_user:b2b_password@mongodb:27017/orders_db?authSource=admin
PRODUCTS_API_BASE_URL=http://products-api:8081
CLIENTS_API_BASE_URL=http://clients-api:8082
```

## Pruebas

Cada servicio contiene sus propias pruebas.

### order-ingest-api

```bash
cd order-ingest-api
./mvnw test
```

### worker

```bash
cd worker
./mvnw test
```

El worker incluye:

- Pruebas unitarias para calculo de impuestos.
- Pruebas unitarias para validacion, idempotencia y DLT.
- Prueba E2E con Testcontainers usando Kafka y MongoDB.


### clients-api

```bash
cd clients-api
npm test
```

### products-api

```bash
cd products-api
go test ./...
```

## Coleccion de Postman

El repositorio incluye:

```text
B2B-Collection.postman_collection.json
```


## Build 

El proyecto esta preparado para que se pueda construir y levantar los servicios usando Docker Compose:

```bash
docker compose up --build
```

## Decisiones tecnicas

- Se usa un monorepo para facilitar la ejecucion completa del proyecto.
- Worker reactivo utilizando WebFlux.
- La cache se implementó en las APIs de Clientes y Productos para mantener simple el Worker.
- MongoDB se usa tanto para la persistencia de las ordenes enriquecidas, así como para el manejo de idempotencia.
- DLT permite inspeccionar errores no recuperables sin perder mensajes.
- Testcontainers valida el flujo real con contenedores temporales durante las pruebas.
