# Sistema de Votaciones Estudiantiles - Monolito Modular Universitario

Bienvenido al repositorio del **Sistema de Votaciones Estudiantiles**, una solución de software empresarial basada en una arquitectura de **Monolito Modular** construida sobre **Java 21** y **Spring Boot 3**.

El sistema garantiza elecciones universitarias seguras, auditables y transparentes aplicando principios fundamentales de:
- **Secreto del Voto**: Desacoplamiento criptográfico total entre el votante y la papeleta depositada en la urna anónima.
- **Idempotencia Transaccional**: Protección contra peticiones duplicadas o reintentos de red usando llaves `Idempotency-Key`.
- **Prevención de Fraude**: Detección y rechazo de doble voto por estudiante en el padrón electoral.
- **Trazabilidad Asíncrona**: Registro de eventos de auditoría mediante colas de mensajería **RabbitMQ** y base de datos relacional **PostgreSQL**.

---

## 🏛️ Arquitectura del Monolito Modular y Fronteras de API

El sistema está estructurado en 4 módulos de negocio independientes integrados armónicamente:

```
src/main/java/com/universidad/votaciones/
├── VotacionesApplication.java          # Punto de entrada y seeder de datos iniciales
├── academicmock/                       # Módulo 1: Censo Académico Universitario
│   ├── AcademicApi.java               # << Fachada Pública Oficial (API del Módulo) >>
│   ├── controller/                     # Controladores HTTP del censo
│   ├── model/                          # Modelos de datos del censo
│   └── service/                        # Registro en memoria de 12.000 estudiantes
├── auth/                               # Módulo 2: Autenticación, Usuarios y Padrón
│   ├── AuthApi.java                   # << Fachada Pública Oficial (API del Módulo) >>
│   ├── controller/                     # Endpoints de administradores y votantes
│   ├── model/                          # Entidades JPA (Usuario, Votante, Administrador)
│   ├── repository/                     # Repositorios JPA
│   └── service/                        # Lógica de seguridad y contraseñas Argon2
├── voting/                             # Módulo 3: Motor Transaccional de Votaciones
│   ├── VotacionApi.java               # << Fachada Pública Oficial (API del Módulo) >>
│   └── internal/                       # Capa interna estrictamente encapsulada
│       ├── controller/                 # Endpoints REST de votación y tarjetón
│       ├── dto/                        # Commands y DTOs
│       ├── exception/                  # Excepciones de negocio y de concurrencia
│       ├── model/                      # Papeletas anónimas y transacciones idempotentes
│       ├── repository/                 # Urna electrónica y repositorio de votos
│       └── service/                    # Lógica transaccional de depósito de votos
└── audit/                              # Módulo 4: Auditoría y Trazabilidad Transaccional
    ├── AuditApi.java                  # << Fachada Pública Oficial (API del Módulo) >>
    ├── config/                         # Configuración de RabbitMQ (Exchanges, Queues, DLQ)
    ├── controller/                     # Consulta de logs de auditoría
    ├── model/                          # Entidad y enum de acciones de auditoría
    ├── repository/                     # Repositorio JPA de eventos
    └── service/                        # Consumidores AMQP y emisores asíncronos
```

### 🔒 Regla de Encapsulamiento Estricto
Cada módulo encapsula internamente sus controladores, servicios, repositorios y entidades. Ningún módulo accede a las clases internas de otro.
Toda interacción intermodular ocurre **única y exclusivamente** a través de las fachadas públicas situadas en la raíz de cada paquete:
1. `AcademicApi`: Provee verificación del estado académico y existencia en el censo oficial.
2. `AuthApi`: Valida si el estudiante tiene credenciales válidas y se encuentra en estado `HABILITADO`. Se conecta con `AcademicApi` y `AuditApi`.
3. `VotacionApi`: Coordina la emisión de votos, comprueba que el votante esté habilitado (`AuthApi`), que continúe activo en el censo (`AcademicApi`), evita el doble voto, emite la papeleta anónima y notifica el evento al módulo de auditoría (`AuditApi`).
4. `AuditApi`: Expone métodos de alto nivel (`registrarExito`, `registrarFallo`) para publicar eventos hacia RabbitMQ sin acoplar detalles de infraestructura.

---

## 🚀 Requisitos Previos

- **Docker Desktop** (con Docker Compose v2+)
- **Java JDK 21** (opcional si se ejecuta con Docker)
- **Postman** (o cURL / HTTP Client)

---

## 🐳 Ejecución con Docker Compose (Recomendado)

La infraestructura contiene 3 contenedores orquestados:
1. `votaciones-postgres`: Base de datos PostgreSQL 16 en el puerto `5432`.
2. `votaciones-rabbitmq`: Broker RabbitMQ 3 en el puerto `5672` (AMQP) y `15672` (Management Web UI).
3. `votaciones-app`: La aplicación Spring Boot en el puerto `8080`.

### Pasos para iniciar:

```bash
# 1. Clonar o ubicarse en el directorio del proyecto:
cd votaciones

# 2. Construir e iniciar todos los contenedores en segundo plano:
docker compose up -d --build

# 3. Verificar el estado de los contenedores (deben figurar Up / Healthy):
docker compose ps

# 4. Ver los logs de arranque de la aplicación:
docker compose logs -f app
```

Una vez que la aplicación muestre `Started VotacionesApplication in ... seconds`, el sistema estará completamente operativo en `http://localhost:8080`.

### Detener el entorno Docker:
```bash
docker compose down
```
*(Si desea reiniciar los volúmenes de base de datos limpios, agregue la bandera `-v`: `docker compose down -v`)*.

---

## 💻 Ejecución Local con Gradle (Desarrollo)

Si desea ejecutar la aplicación localmente fuera del contenedor de la aplicación pero usando los servicios de base de datos y mensajería en Docker:

```bash
# 1. Levantar únicamente PostgreSQL y RabbitMQ:
docker compose up -d postgres rabbitmq

# 2. Iniciar la aplicación con Gradle:
# En Linux / macOS:
./gradlew bootRun

# En Windows (PowerShell / CMD):
.\gradlew.bat bootRun
```

---

## 🧪 Pruebas Unitarias y End-to-End (E2E)

El proyecto cuenta con un test automatizado End-to-End (`VotacionesEndToEndTest`) que valida el flujo completo entre los 4 módulos manteniendo el encapsulamiento por APIs:
- **Fase 1**: Verificación de censo oficial con `AcademicApi`.
- **Fase 2**: Registro y habilitación de administradores y votantes mediante `AuthApi`.
- **Fase 3**: Rechazo transaccional de votos para estudiantes no autorizados.
- **Fase 4**: Emisión exitosa de voto, depósito anónimo y soporte de idempotencia.
- **Fase 5**: Detección y bloqueo inmediato ante intentos de doble voto o fraude.
- **Fase 6**: Escrutinio independiente de la urna electrónica y consistencia matemática de los votos.
- **Fase 7**: Simulación completa vía protocolo HTTP REST (MockMvc).

Para ejecutar todas las pruebas automatizadas:

```bash
# En Linux / macOS:
./gradlew test

# En Windows:
.\gradlew.bat test
```

---

## 📬 Entorno de Pruebas en Postman

Se han generado dos archivos listos para importar en Postman:
1. `votaciones_postman_collection.json`: Colección con todas las peticiones organizadas secuencialmente según el flujo de uso real del sistema electoral, con validaciones automáticas `pm.test` en cada paso.
2. `votaciones_postman_environment.json`: Entorno con las variables predeterminadas (`baseUrl: http://localhost:8080`, identificadores de prueba, códigos de censo).

### Cómo importar y ejecutar en Postman:
1. Abra **Postman**.
2. Haga clic en **Import** (esquina superior izquierda).
3. Seleccione los archivos:
   - `votaciones_postman_collection.json`
   - `votaciones_postman_environment.json`
4. Seleccione el entorno activo **"Votaciones Estudiantiles - Local"**.
5. Para ejecutar la suite completa automáticamente:
   - Haga clic derecho en la colección **"Sistema de Votaciones Estudiantiles - Monolito Modular E2E"** -> **Run collection**.
   - Presione **Run**. ¡Todas las pruebas pasarán en verde!

---

## 🗺️ Mapa de Endpoints del Sistema

### 1. Salud del Sistema
| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/actuator/health` | Estado del servicio, conexión a Postgres y RabbitMQ |

### 2. Módulo Académico (`academicmock`)
| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/academic/estudiantes/{codigo}/validar` | Valida si un estudiante está habilitado en el censo oficial |
| `GET` | `/api/academic/estudiantes/{codigo}` | Consulta la información académica (nombre, carrera) |
| `GET` | `/api/academic/estudiantes/cantidad` | Retorna el total de estudiantes en el censo oficial (12.000) |

### 3. Módulo Autenticación y Usuarios (`auth`)
| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/auth/administradores` | Registra un nuevo administrador del sistema electoral |
| `GET` | `/api/auth/administradores` | Lista los administradores registrados |
| `POST` | `/api/auth/votantes` | Registra una cuenta de votante para un estudiante del censo |
| `GET` | `/api/auth/votantes/codigo/{codigo}` | Obtiene los datos del votante |
| `PATCH`| `/api/auth/votantes/{id}/estado?estado=HABILITADO` | Cambia el estado del votante (`HABILITADO` / `INHABILITADO`) |
| `GET` | `/api/auth/votantes/habilitado/{codigo}` | Retorna si el estudiante está habilitado para sufragar (`true`/`false`) |

### 4. Módulo de Votación (`voting`)
| Método | Ruta | Header / Parámetros | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/votaciones/candidatos` | - | Lista los candidatos oficiales del tarjetón |
| `GET` | `/api/v1/votaciones/estudiante/{id}/ha-votado` | - | Retorna si un estudiante ya ejerció su voto |
| `POST`| `/api/v1/votaciones/votar` | Header: `Idempotency-Key` | Emite el voto de forma anónima e idempotente |
| `GET` | `/api/v1/votaciones/resultados` | - | Consulta el escrutinio de la urna anónima |

---

## 🛡️ Principios de Seguridad y Resiliencia Implementados

1. **Idempotencia Transaccional con Llave Única**: Cada petición de voto puede incluir un encabezado `Idempotency-Key`. Si la red falla y el cliente reintenta, el sistema detecta la llave previa y devuelve exactamente el mismo recibo sin sumar un voto extra a la urna.
2. **Desacoplamiento Criptográfico de la Urna**: Las tablas `transacciones_voto_idempotente` (que guardan el estudiante para evitar que vote dos veces) y `papeletas_voto` (que guardan el voto por el candidato en la urna) están totalmente separadas. No existe llave foránea ni vínculo entre el estudiante y el candidato que eligió.
3. **Hashing Robusto**: Credenciales protegidas mediante `Argon2PasswordEncoder` con BouncyCastle.
4. **Mensajería con Dead-Letter Queue (DLQ)**: En caso de que un mensaje de auditoría falle al procesarse, es enrutado automáticamente a la cola de reintentos / fallos `auditoria.eventos.dlq`.
