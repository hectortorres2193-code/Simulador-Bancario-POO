# Simulador de Sistema Bancario y Créditos (POO - Java)

Proyecto final de Programación Orientada a Objetos. Arquitectura MVC, patrón Singleton,
persistencia en .csv y una librería propia de amortización (`.jar` independiente).

Este repositorio tiene toda la lógica y los datos a partir de la union del trabajo en la parte de datos por parte de Leonardo y la parte de la logica realizada por JP. Sin interfaz gráfica por ahora.
 La interfaz (paquete `view`) se
agrega aparte y solo necesita llamar a los controladores.

# Integrantes
- _Juan Pablo_ (lógica de negocio y librería)
- _Hector Leonardo_ (capa de datos)
- _Samara_ (interfaz)

# Estado
| Parte | Estado |
|---|---|
| Librería `amortizacion-lib` |  Terminada y probada |
| `model` (Cuenta, Prestamo, Usuario...) |  Terminado y probado |
| `data` (BancoContext + CsvRepositorio) |  Terminado y probado |
| `controller` (los 5 controladores) |  Terminados y probados |
| `view` (pantallas) |  no incluidas por ahora |

# Estructura
```
proyecto-banco/
├── pom.xml                  Agrupa los dos módulos (mvn clean install compila ambos)
├── amortizacion-lib/        LIBRERÍA independiente -> amortizacion-lib-1.0.0.jar
├── banco-app/               APLICACIÓN: Main, MainPrueba + controller / model / data
├── datos-prueba/            CSV de ejemplo (3 clientes + 1 gerente)
└── docs/                    UML (clases, paquetes, secuencia) y visor interactivo
```

# Compilar y ejecutar (NetBeans)
Los dos módulos son independientes (cada uno tiene su propio `pom.xml`, sin proyecto padre del que dependan).

1. File > Open Project y abrir las carpetas `amortizacion-lib` y `banco-app` (ambas tienen el ícono azul de Maven).
   Opcional: abrir también la carpeta raíz, que solo agrupa los dos módulos.
2. Una sola vez: clic derecho en `amortizacion-lib` > Clean and Build. Debe terminar en `BUILD SUCCESS`.
   Esto instala la librería en tu equipo para que `banco-app` la encuentre. Se repite solo si cambia la librería.
   Si por algo da error, en vez de clean and build presione solo build.
3. Clic derecho en `banco-app` > Reload POM (si se ven marcas rojas) y luego Run.

Por terminal, en la carpeta raíz: `mvn clean install` (compila los dos módulos en orden). Requiere JDK 17+ y Maven 3.9+.

# Errores comunes
- `Could not find artifact com.banco:amortizacion-lib` -> falta el paso 2.
- `...com.banco:proyecto-banco:pom:1.0.0 (absent)` -> quedó instalada una versión vieja de la librería: hacer de nuevo
  Clean and Build sobre `amortizacion-lib` (o build) y luego sobre `banco-app`.

# Ver los datos de ejemplo (main de prueba)
Ejecutar `MainPrueba` (clic derecho en `MainPrueba.java` > Run File; también es lo que ejecuta Run en `banco-app`).
Carga `datos-prueba/` y muestra, para cada cliente: saldo, últimas transacciones, préstamos y su tabla de
amortización; y la vista del gerente. Solo lee: no modifica ningún archivo.

# Probar la lógica
Ejecutar el `main` de estas clases (imprimen OK / FALLO por cada verificación):
- `amortizacion-lib/src/test/.../PruebaAmortizacion.java` (15 verificaciones)
- `banco-app/src/test/.../PruebaNegocio.java` (47 verificaciones: login, caja, préstamos, gerente, CSV).
  Trabaja sobre una copia temporal de `datos-prueba/`; no la modifica.

# Datos de la aplicación (`Main`)
`Main` lee y reescribe la carpeta `datos/` del directorio de trabajo (`banco-app/` en NetBeans). Para empezar con
los datos de ejemplo, copiar `datos-prueba/` como `datos/`. Si `datos/` no existe, arranca vacía sin errores.
`datos/` está en el `.gitignore`.

Usuarios de ejemplo: `ana`/`1234`, `luis`/`1234`, `maria`/`1234` (clientes) y `gerente`/`admin`.

# Guía para la interfaz (OPCIONAL)
Crear el paquete `com.banco.app.view` y construir las pantallas. La vista solo llama al controlador y muestra lo
que devuelve; nunca toca el modelo ni los archivos. Los métodos que fallan lanzan excepciones con un mensaje listo
para mostrar (`e.getMessage()`). `MainPrueba` es un ejemplo de cómo se usan los controladores.

| Pantalla | Controlador | Métodos |
|---|---|---|
| Login | `LoginController` | `iniciarSesion(user, pass)` -> `boolean`; luego `getRolActual()`: `CLIENTE` -> dashboard, `GERENTE` -> vista del gerente; `cerrarSesion()` |
| Dashboard | `CuentaController` | `getNombreCliente()`, `getSaldo()` (`BigDecimal`), `getUltimasTransacciones(5)` |
| | `PersistenciaController` | `guardar()` -> `boolean` (si es `false`, avisar que no se pudo guardar) |
| Caja | `CuentaController` | `depositar(monto)`, `retirar(monto)` |
| Préstamos | `PrestamoController` | `getTasaAnual()`, `simularPrestamo(monto, meses)` y `aceptarPrestamo(monto, meses)` -> `List<CuotaAmortizacion>`; `getPrestamosActivos()` |
| Gerente | `GerenteController` | `getClientes()`, `getPrestamosDe(idCliente)`, `getTransaccionesDe(idCliente)`, `registrarPago(idPrestamo)` |

# Excepciones que hay que atrapar (`try-catch`) y mostrar:
- `SaldoInsuficienteException` (checked): en `retirar` y `registrarPago` (sobregiro).
- `IllegalArgumentException`: monto o plazo inválidos (`<= 0`).
- `IllegalStateException`: sin sesión, no es gerente, préstamo ya pagado, o "se registró pero no se pudo guardar".
- `NumberFormatException`: al convertir el texto de un campo a `new BigDecimal(...)` (lo atrapa la vista).

# Pendiente de la interfaz:
- En `Main` (ya crea los controladores y carga los CSV) construir la ventana y las pantallas. En el `pom.xml` de
  `banco-app`, cambiar `exec.mainClass` a `com.banco.app.Main`.
- Guardar al cerrar la ventana: en el `windowClosing` llamar a `persistencia.guardarAlCerrar()` (devuelve
  `false` si falló: preguntar si se quiere cerrar de todos modos).
- Botón "Guardar" -> `persistencia.guardar()`. Tras depositar, retirar o aceptar un préstamo, refrescar el saldo.
- Dinero en pantalla: `String.format("$ %,.2f", valor)` funciona con `BigDecimal`.
- Una sola ventana con 5 pantallas (CardLayout). En NetBeans: New > JPanel Form (no JFrame).

# Cómo se integró la capa de datos
La capa de datos del compañero Leonardo (`ContextoBanco`, `RepositorioCSV`, `RepositorioDatos`) se adaptó al modelo del UML:

| Original | Ahora | Cambio |
|---|---|---|
| `ContextoBanco` | `BancoContext` | Mismo Singleton (`synchronized`, `limpiar()`, búsqueda sin distinguir mayúsculas). Las relaciones son por id, ya no por mapas con `hashCode()` |
| `RepositorioCSV` | `CsvRepositorio` | Se conservan `leerArchivo`, `escribirArchivo`, `avisoRenglon`, tolerancia a archivos faltantes y `guardar()` con `boolean`. Ahora usa UTF-8 y `BigDecimal` |
| `RepositorioDatos` | igual | Misma interfaz: `cargar()` y `boolean guardar()` |

Formato de los CSV (separador `;`, con encabezado, UTF-8):
```
usuarios.csv       id;rol;username;password;nombre
cuentas.csv        idCuenta;idCliente;saldo
transacciones.csv  idTransaccion;idCuenta;tipo;monto;fecha;descripcion
prestamos.csv      idPrestamo;idCliente;idCuenta;montoOriginal;tasaAnual;plazoMeses;cuotasPagadas;estado;fechaInicio
```
Si el equipo quiere conservar `telefono` y `tarjeta` del usuario, se agregan como atributos de `Usuario` y una columna al CSV.

# Reglas de negocio implementadas
- Sin sobregiros: retirar o pagar una cuota sin saldo lanza `SaldoInsuficienteException` y no cambia nada.
- Préstamo: tasa fija 12 % anual, sistema francés, `BigDecimal` con 2 decimales (HALF_UP). Al aceptar: se suma al
  saldo, se registra la deuda, se guarda en los CSV.
- Pago de cuota (gerente): se debita la cuota N de la cuenta del cliente. La última cuota se ajusta para dejar el
  saldo en 0.00. Al completar las cuotas el préstamo pasa a `PAGADO`.
- Guardado: al iniciar se carga; se guarda con el botón "Guardar", al cerrar la ventana, al aceptar un préstamo y
  al registrar un pago.

 




