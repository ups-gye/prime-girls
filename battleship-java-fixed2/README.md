# ⚓ Batalla Naval - Sistema Distribuido Cliente-Servidor
### Java 11 · Java Swing · TCP Sockets · PostgreSQL · JPA/Hibernate

---

## Estructura del proyecto

```
nombre-grupo/
├── build.bat              ← Compilar todo (Windows)
├── build.sh               ← Compilar todo (Linux/Mac)
├── code/
│   ├── pom.xml            ← Parent Maven
│   ├── model/             ← Clases compartidas (GameMessage, Usuario, Board, Ship, GameRoom)
│   ├── server-hub/        ← Servidor Central con hilos (puerto 9090)
│   ├── client/            ← Cliente Swing (Login, Lobby, Colocar Barcos, Batalla)
│   ├── server-monitor/    ← Monitor Admin Swing (4 salas en tiempo real)
│   └── sql/
│       └── ddl.sql        ← Script DDL para PostgreSQL
└── docs/                  ← (Agregar documentos del proyecto aquí)
```

---

## Requisitos

| Herramienta | Versión mínima |
|-------------|---------------|
| Java JDK    | 11            |
| Maven       | 3.6           |
| PostgreSQL  | 13+ (Neon OK) |

---

## Configuración de la base de datos

### 1. Ejecutar el DDL

En tu consola de **Neon** (o PostgreSQL local), ejecuta el archivo `code/sql/ddl.sql`.
Esto crea la tabla `usuarios` e inserta usuarios de prueba.

### 2. Configurar persistence.xml

Edita el archivo:
```
code/server-hub/src/main/resources/META-INF/persistence.xml
```

Cambia estas 3 líneas con tus datos de Neon:
```xml
<property name="javax.persistence.jdbc.url"
          value="jdbc:postgresql://TU-HOST.neon.tech:5432/TU-BASE?sslmode=require"/>
<property name="javax.persistence.jdbc.user"     value="TU_USUARIO"/>
<property name="javax.persistence.jdbc.password" value="TU_CONTRASEÑA"/>
```

---

## Compilar

### Windows
```cmd
build.bat
```

### Linux / Mac
```bash
chmod +x build.sh && ./build.sh
```

Esto genera 3 JARs ejecutables:
- `code/server-hub/target/ServerHub.jar`
- `code/client/target/Client.jar`
- `code/server-monitor/target/Monitor.jar`

---

## Ejecutar

### 1. Primero: iniciar el servidor
```cmd
java -jar code/server-hub/target/ServerHub.jar
```
Verás:
```
╔══════════════════════════════════════╗
║     BATALLA NAVAL - SERVIDOR HUB     ║
╠══════════════════════════════════════╣
║  Puerto : 9090                       ║
║  Estado : EN LÍNEA                   ║
╚══════════════════════════════════════╝
```

### 2. Iniciar clientes (uno por jugador)
```cmd
java -jar code/client/target/Client.jar
```
- Ingresa IP del servidor y puerto `9090`
- Haz clic en **Conectar**
- Inicia sesión o regístrate

### 3. Iniciar el Monitor (opcional, requiere contraseña admin)
```cmd
java -jar code/server-monitor/target/Monitor.jar
```
- Contraseña por defecto: `admin123`
- Muestra las 4 salas con estadísticas en tiempo real

---

## Protocolo de comunicación TCP

### Cliente → Servidor
| Mensaje | Descripción |
|---------|-------------|
| `LOGIN:usuario:contraseña` | Iniciar sesión |
| `REGISTER:user:pass:nombre:apellido:avatar` | Registrar usuario |
| `MONITOR_LOGIN:admin123` | Login como monitor |
| `GET_ROOMS` | Listar salas disponibles |
| `CREATE_ROOM` | Crear sala nueva |
| `JOIN_ROOM:12` | Unirse a la sala ID 12 |
| `PLACE_SHIPS:datos_serializados` | Enviar posición de barcos |
| `SHOOT:fila:columna` | Disparar a coordenada |
| `LEAVE_ROOM` | Salir de la sala |
| `GET_STATUS` | Pedir estado actual (monitor) |

### Servidor → Cliente
| Mensaje | Descripción |
|---------|-------------|
| `LOGIN_OK:victorias:derrotas:datos` | Login exitoso |
| `LOGIN_FAIL:motivo` | Login fallido |
| `ROOM_LIST:salas` | Lista de salas |
| `ROOM_CREATED:id:nombre` | Sala creada |
| `ROOM_JOINED:id:nombre:oponente` | Unido a sala |
| `SHIPS_OK` | Tablero aceptado |
| `GAME_START:nombre_oponente` | ¡Juego iniciado! |
| `YOUR_TURN` | Es tu turno |
| `OPPONENT_TURN` | Turno del oponente |
| `SHOT_RESULT:HIT/MISS/SUNK:f:c` | Resultado de tu disparo |
| `OPPONENT_SHOT:HIT/MISS/SUNK:f:c` | Disparo del oponente |
| `GAME_OVER:WIN/LOSE` | Fin del juego |
| `OPPONENT_LEFT` | Oponente desconectado |
| `STATUS_UPDATE:salas` | Actualización para monitor |

---

## Barcos (según PDF)

| Tipo | Tamaño | Cantidad |
|------|--------|----------|
| Portaaviones | 5 | 1 |
| Acorazado | 4 | 2 |
| Crucero | 3 | 1 |
| Destructor | 2 | 2 |
| **Total** | **17 celdas** | **6 barcos** |

---

## Usuarios de prueba (incluidos en ddl.sql)

| Usuario | Contraseña |
|---------|-----------|
| luis.almeida | pass123 |
| maria.perez | pass123 |
| henry.paz | pass123 |
| monica.pita | pass123 |
| gina.alcides | pass123 |
| angel.torres | pass123 |

**Monitor admin:** contraseña `admin123`
