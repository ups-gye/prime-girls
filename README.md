<div align="center">

```
╔═══════════════════════════════════════════════════════════════╗
║                                                               ║
║    ⚓  B A T A L L A   N A V A L  ⚓                         ║
║         Sistema Distribuido Cliente-Servidor                  ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```

![Java](https://img.shields.io/badge/Java-11+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13+-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Swing](https://img.shields.io/badge/Java%20Swing-GUI-007396?style=for-the-badge&logo=java&logoColor=white)
![JPA](https://img.shields.io/badge/JPA%2FHibernate-ORM-59666C?style=for-the-badge&logo=hibernate&logoColor=white)

---

### 👩‍💻 Equipo Prime Girls

| Integrante | GitHub |
|-----------|--------|
| 🎯 Danna Montece | — |
| ⚓ Maydene Madero | — |
| 🚢 Emily Aspiazu | — |
| 🛳️ Dánery Toledo | — |

</div>

---

## 📖 Descripción

**Batalla Naval** es un sistema distribuido multijugador en tiempo real desarrollado en Java. Permite a múltiples jugadores conectarse desde diferentes computadoras a un servidor central, crear o unirse a salas de juego, colocar su flota estratégicamente y competir en turnos alternados hasta hundir toda la flota enemiga.

El sistema mantiene un **historial persistente** de victorias, derrotas y puntos de cada jugador en una base de datos PostgreSQL, accesible únicamente desde el servidor (nunca desde el cliente).

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    ARQUITECTURA MVC                          │
├──────────────┬──────────────────────┬───────────────────────┤
│    MODEL     │        VIEW          │      CONTROLLER        │
│  model/      │   client/views/      │  client/controllers/  │
│  ─────────   │   ──────────────     │  ─────────────────    │
│  Usuario     │   ConnectView        │  LoginController      │
│  Board       │   LobbyView          │  LobbyController      │
│  Ship        │   BoardSetupView     │  GameController       │
│  GameRoom    │   GameView           │                       │
│  GameMessage │                      │                       │
└──────────────┴──────────────────────┴───────────────────────┘
```

```
        ┌──────────────┐         TCP :9090        ┌────────────────┐
        │   CLIENT A   │◄────────────────────────►│                │
        │  (Jugador 1) │                           │   SERVER HUB   │
        └──────────────┘                           │   (Puerto 9090)│
                                                   │                │
        ┌──────────────┐         TCP :9090         │  ┌──────────┐ │
        │   CLIENT B   │◄────────────────────────►│  │ PostgreSQL│ │
        │  (Jugador 2) │                           │  │  (Neon)  │ │
        └──────────────┘                           │  └──────────┘ │
                                                   │                │
        ┌──────────────┐         TCP :9090         │                │
        │   MONITOR    │◄────────────────────────►│                │
        │  (Admin GUI) │                           └────────────────┘
        └──────────────┘
```

---

## 📁 Estructura del Proyecto

```
battleship-java-fixed2/
│
├── 📄 compilar.bat          ← Compilar todo desde cero (Windows)
├── 📄 servidor.bat          ← Lanzar servidor
├── 📄 jugar.bat             ← Lanzar cliente
├── 📄 monitor.bat           ← Lanzar monitor
├── 📄 build.sh              ← Compilar (Linux/Mac)
│
└── code/
    ├── 📦 model/            ← Clases compartidas (Model en MVC)
    │   └── battleship/model/
    │       ├── Usuario.java
    │       ├── Board.java
    │       ├── Ship.java
    │       ├── GameRoom.java
    │       └── GameMessage.java
    │
    ├── 🖥️ server-hub/       ← Servidor Central con hilos
    │   └── battleship/server/
    │       ├── GameServer.java      ← ServerSocket, puerto 9090
    │       ├── ClientHandler.java   ← Hilo por cliente
    │       ├── RoomManager.java     ← Gestión de salas (máx. 4)
    │       └── UsuarioDAO.java      ← Acceso a BD con JPA
    │
    ├── 💻 client/           ← Cliente Swing (MVC completo)
    │   └── battleship/client/
    │       ├── views/               ← Capa View
    │       │   ├── ConnectView.java      Login + Registro
    │       │   ├── LobbyView.java        Sala de espera
    │       │   ├── BoardSetupView.java   Colocar barcos
    │       │   └── GameView.java         Batalla en tiempo real
    │       ├── controllers/         ← Capa Controller
    │       │   ├── LoginController.java
    │       │   ├── LobbyController.java
    │       │   └── GameController.java
    │       └── components/
    │           └── BoardPanel.java       Tablero 10x10
    │
    ├── 📊 server-monitor/   ← GUI de administración
    │   └── battleship/monitor/
    │       └── ServerMonitor.java   4 salas + stats en tiempo real
    │
    └── 🗄️ sql/
        └── ddl.sql          ← Script DDL PostgreSQL
```

---

## 🚀 Instalación y Ejecución

### Prerrequisitos

| Herramienta | Versión | Verificar |
|-------------|---------|-----------|
| ☕ Java JDK | 11+ | `java -version` |
| 📦 Apache Maven | 3.6+ | `mvn -version` |
| 🐘 PostgreSQL | Neon cloud | https://neon.tech |

### 1️⃣ Configurar Base de Datos

Ejecutar `code/sql/ddl.sql` en tu consola de Neon o PostgreSQL local.

Editar `code/server-hub/src/main/resources/META-INF/persistence.xml`:
```xml
<property name="javax.persistence.jdbc.url"
          value="jdbc:postgresql://TU-HOST/TU-BD?sslmode=require"/>
<property name="javax.persistence.jdbc.user"     value="TU_USUARIO"/>
<property name="javax.persistence.jdbc.password" value="TU_CONTRASEÑA"/>
```

### 2️⃣ Compilar

```cmd
compilar.bat
```

### 3️⃣ Ejecutar (en orden)

```
┌─────────────────────────────────────────┐
│  Computadora A (Servidor)               │
│  > servidor.bat                         │
│  > monitor.bat  (misma máquina)         │
├─────────────────────────────────────────┤
│  Computadora B, C... (Clientes)         │
│  > jugar.bat                            │
│  → Ingresar IP de Computadora A         │
└─────────────────────────────────────────┘
```

---

## 🎮 Flujo del Juego

```
  [Login / Registro]
         │
         ▼
  ┌─────────────┐     "¡Ingreso exitoso!"
  │    LOBBY    │  ← Ver salas · Crear sala · Unirse
  └──────┬──────┘
         │
         ▼
  ┌─────────────────┐
  │ COLOCAR BARCOS  │  ← Posicionar flota en tablero 10×10
  └────────┬────────┘
           │  Ambos jugadores listos
           ▼
  ┌─────────────────┐
  │    BATALLA      │  ← Turnos alternados · HIT / MISS / SUNK
  └────────┬────────┘
           │
           ▼
  ┌─────────────────┐
  │   RESULTADO     │  ← VICTORIA 🏆 / Derrota 💀 · BD actualizada
  └─────────────────┘
```

---

## ⚓ Flota de Barcos

| Barco | Tamaño | Cantidad |
|-------|--------|----------|
| 🛳️ Portaaviones | 5 celdas | 1 |
| ⚓ Acorazado | 4 celdas | 2 |
| 🚢 Crucero | 3 celdas | 1 |
| 🚤 Destructor | 2 celdas | 2 |
| **Total** | **17 celdas** | **6 barcos** |

---

## 🔌 Protocolo TCP (Puerto 9090)

### Cliente → Servidor
```
LOGIN username password
REGISTER user pass nombre apellido avatar
GET_ROOMS
CREATE_ROOM
JOIN_ROOM roomId
PLACE_SHIPS datos_serializados
SHOOT fila columna
LEAVE_ROOM
```

### Servidor → Cliente
```
LOGIN_OK wins losses datos_usuario
LOGIN_FAIL No existe el usuario
ROOM_LIST salas_serializadas
GAME_START nombre_oponente
YOUR_TURN
OPPONENT_TURN
SHOT_RESULT HIT|MISS|SUNK fila columna
OPPONENT_SHOT HIT|MISS|SUNK fila columna
GAME_OVER WIN|LOSE
OPPONENT_LEFT
```

---

## 🗄️ Base de Datos

```sql
CREATE TABLE usuarios (
    id                SERIAL PRIMARY KEY,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    password          VARCHAR(50)  NOT NULL,
    nombre            VARCHAR(100),
    apellido          VARCHAR(100),
    avatar            VARCHAR(100),
    partidas_ganadas  INTEGER DEFAULT 0,
    partidas_perdidas INTEGER DEFAULT 0,
    puntos_totales    INTEGER DEFAULT 0
);
```

> ⚠️ **Solo el servidor accede a la BD.** Los clientes nunca tienen conexión directa a PostgreSQL.

---

## 👥 Usuarios de Prueba

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `jugador1` | `1234` | Jugador |
| `Danna27` | `12345678` | Jugador |
| `luis.almeida` | `pass123` | Jugador |
| `maria.perez` | `pass123` | Jugadora |
| `admin` | `admin123` | 🔐 Monitor |

---

## 🛠️ Stack Tecnológico

| Tecnología | Uso |
|-----------|-----|
| **Java 11** | Lenguaje principal |
| **Java Swing** | Interfaces gráficas (cliente + monitor) |
| **TCP Sockets** | Comunicación en red |
| **Multithreading** | Un hilo por cliente conectado |
| **JPA / Hibernate** | Persistencia ORM |
| **PostgreSQL / Neon** | Base de datos en la nube |
| **Apache Maven** | Gestión de dependencias y build |

---

<div align="center">

```
⚓ ════════════════════════════════════════════════════ ⚓
      ¡Que gane el mejor almirante!
⚓ ════════════════════════════════════════════════════ ⚓
```

**prime-girls · UPS · 2025–2026**

</div>
