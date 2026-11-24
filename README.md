# Simulador de Sistema Bancario y Créditos (POO - Java)

Proyecto final de la asignatura de Programación Orientada a Objetos. Aplicación de escritorio desarrollada en Java que simula operaciones bancarias básicas y gestión de créditos mediante el sistema de amortización francés.

## Integrantes del Equipo
- **Juan Pablo Díaz Peñaloza**
- **Héctor Leonardo Torres Campos**
- **Samara Mardueño Bernardino**

---

## Arquitectura y Patrones Implementados
- **Arquitectura MVC (Modelo-Vista-Controlador):** Separación estricta entre la interfaz gráfica (`view`), la lógica de flujo (`controller`), las entidades de negocio (`model`) y la capa de persistencia (`data`).
- **Patrón Singleton:** Implementado en la clase `BancoContext` para mantener una única instancia centralizada de los datos en memoria durante toda la ejecución.
- **Modularidad (Librería Independiente):** El módulo `amortizacion-lib` compila como un archivo `.jar` totalmente desacoplado de la interfaz gráfica y del dominio bancario, encargándose exclusivamente del cálculo matemático de las tablas de amortización.
- **Persistencia en Archivos Planos (`.csv`):** Carga automática al iniciar la aplicación (con manejo de excepciones si los archivos no existen) y guardado mediante botón explícito y evento de cierre de ventana (`windowClosing`).

---

## Estructura del Proyecto
```text
Simulador-Bancario-POO/
├── pom.xml                  # Configuración Maven multiproyecto
├── amortizacion-lib/        # Librería independiente de cálculo financiero (.jar)
├── banco-app/               # Aplicación principal (MVC + Persistencia)
├── datos-prueba/            # Archivos .csv con datos iniciales (3 clientes y 1 gerente)
└── docs/                    # Diagramas UML (Clases, Paquetes y Secuencia)
```

---

## Requisitos Previos
1. **Java Development Kit (JDK):** Versión 17 o superior.
2. **Apache Maven:** Versión 3.9 o superior (incluido por defecto en NetBeans/IntelliJ).

---

## Compilar y Ejecutar
> **Paso previo (Datos iniciales):** Antes de ejecutar la aplicación, copia la carpeta `datos-prueba/` dentro de `banco-app/` y renómbrala a `datos/` (si se omite este paso, el programa arrancará sin usuarios precargados).

### Desde NetBeans
Los dos módulos son independientes (cada uno tiene su propio `pom.xml`).

1. Ir a **File > Open Project** y abrir las carpetas `amortizacion-lib` y `banco-app` (ambas tienen el ícono azul de Maven).
2. **Una sola vez:** clic derecho en `amortizacion-lib` > **Clean and Build** (si falla, usar **Build**). Debe terminar en `BUILD SUCCESS` para instalar la librería localmente.
3. Clic derecho en `banco-app` > **Reload POM** (si se ven marcas rojas) y luego **Run**.

### Desde Terminal
En la carpeta raíz del proyecto, ejecutar:
```bash
mvn clean install
```
Para iniciar la aplicación:
```bash
mvn -pl banco-app exec:java -Dexec.mainClass="com.banco.app.Main"
```

### Solución de errores comunes
- **`Could not find artifact com.banco:amortizacion-lib`:** Falta ejecutar el paso 2 (compilar `amortizacion-lib`).
- **`...com.banco:proyecto-banco:pom:1.0.0 (absent)`:** Ejecutar nuevamente **Clean and Build** (o **Build**) sobre `amortizacion-lib` y después sobre `banco-app`.

---

## Credenciales de Prueba
Una vez copiada la carpeta `datos-prueba/` como `datos/`, al iniciar la aplicación se cargarán automáticamente los siguientes usuarios precargados:

| Rol | Usuario | Contraseña |
| :--- | :--- | :--- |
| **Cliente** | `ana` | `1234` |
| **Cliente** | `luis` | `1234` |
| **Cliente** | `maria` | `1234` |
| **Gerente** | `gerente` | `admin` |

---

## Reglas de Negocio y Persistencia
- **Control de sobregiros:** Retirar efectivo o pagar una cuota sin saldo suficiente lanza `SaldoInsuficienteException` sin alterar el saldo.
- **Préstamos:** Tasa fija del 12% anual bajo el sistema francés usando `BigDecimal` (2 decimales, redondeo `HALF_UP`). Al aceptar un préstamo, el monto se suma al saldo, se registra la deuda y se guarda en los archivos `.csv`.
- **Gestión de Gerente:** Permite consultar clientes, revisar sus transacciones/préstamos y registrar pagos de cuotas. La última cuota se ajusta para dejar el saldo en `0.00` y el estado pasa a `PAGADO`.
- **Guardado:** Al iniciar se carga; se guarda con el botón "Guardar", al cerrar la ventana, al aceptar un préstamo y al registrar un pago.
- **Formato de archivos `.csv`** (separador `;`, UTF-8, con encabezado):
```text
usuarios.csv       id;rol;username;password;nombre
cuentas.csv        idCuenta;idCliente;saldo
transacciones.csv  idTransaccion;idCuenta;tipo;monto;fecha;descripcion
prestamos.csv      idPrestamo;idCliente;idCuenta;montoOriginal;tasaAnual;plazoMeses;cuotasPagadas;estado;fechaInicio
```

---

## Pruebas Unitarias y de Integración
El repositorio incluye clases de prueba independientes que imprimen `OK` / `FALLO` por cada verificación sin modificar los datos originales:
- `amortizacion-lib/src/test/java/com/banco/amortizacion/PruebaAmortizacion.java` (15 verificaciones de cálculo financiero).
- `banco-app/src/test/java/com/banco/app/PruebaNegocio.java` (47 verificaciones de login, caja, préstamos, gerente y CSV).

---

## Documentación UML
Los diagramas de **Clases**, **Paquetes** y **Secuencia** se encuentran disponibles en la carpeta `docs/` en formato `.png`, `.svg` y código fuente `.puml`.

---

<!-- BORRAR TODO LO DE ABAJO ANTES DE LA ENTREGA FINAL -->

## Notas Internas del Equipo (Guía para la interfaz)

### Datos de la aplicación (`Main`)
`Main` lee y reescribe la carpeta `datos/` del directorio de trabajo (`banco-app/` en NetBeans). Para empezar con los datos de ejemplo, copiar `datos-prueba/` como `datos/`. Si `datos/` no existe, arranca vacía sin errores.

### Controladores disponibles para `com.banco.app.view`
La vista solo llama al controlador y muestra lo que devuelve; nunca toca el modelo ni los archivos. `MainPrueba` es un ejemplo de cómo se usan los controladores.

| Pantalla | Controlador | Métodos |
| :--- | :--- | :--- |
| **Login** | `LoginController` | `iniciarSesion(user, pass)` -> `boolean`; `getRolActual()` (`CLIENTE` / `GERENTE`); `cerrarSesion()` |
| **Dashboard** | `CuentaController` | `getNombreCliente()`, `getSaldo()` (`BigDecimal`), `getUltimasTransacciones(5)` |
| | `PersistenciaController` | `guardar()` -> `boolean` (si es `false`, avisar que no se pudo guardar) |
| **Caja** | `CuentaController` | `depositar(monto)`, `retirar(monto)` |
| **Préstamos** | `PrestamoController` | `getTasaAnual()`, `simularPrestamo(monto, meses)`, `aceptarPrestamo(monto, meses)` -> `List<CuotaAmortizacion>`, `getPrestamosActivos()` |
| **Gerente** | `GerenteController` | `getClientes()`, `getPrestamosDe(idCliente)`, `getTransaccionesDe(idCliente)`, `registrarPago(idPrestamo)` |

### Excepciones que hay que atrapar (`try-catch`) y mostrar:
- `SaldoInsuficienteException` (checked): en `retirar` y `registrarPago` (sobregiro).
- `IllegalArgumentException`: monto o plazo inválidos (`<= 0`).
- `IllegalStateException`: sin sesión, no es gerente, préstamo ya pagado, o error al guardar.
- `NumberFormatException`: al convertir el texto de un campo a `new BigDecimal(...)` (lo atrapa la vista).

### Pendientes para conectar la interfaz:
- En `Main`, construir la ventana y las pantallas. En `banco-app/pom.xml`, cambiar `exec.mainClass` a `com.banco.app.Main`.
- En el `windowClosing`, llamar a `persistencia.guardarAlCerrar()` (si devuelve `false`, preguntar si desea cerrar de todos modos).
- Botón "Guardar" -> `persistencia.guardar()`. Tras depositar, retirar o aceptar un préstamo, refrescar el saldo.
- Dinero en pantalla: `String.format("$ %,.2f", valor)` funciona con `BigDecimal`.
- Una sola ventana con 5 pantallas (`CardLayout`). En NetBeans: `New > JPanel Form` (no `JFrame`).
