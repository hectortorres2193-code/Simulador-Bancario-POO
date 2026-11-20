\# 🏦 Simulador de Sistema Bancario y Créditos en Java



Proyecto final de la materia \*\*Programación Orientada a Objetos (POO)\*\*. Aplicación de escritorio desarrollada en Java bajo arquitectura \*\*MVC (Modelo-Vista-Controlador)\*\*, patrón \*\*Singleton\*\*, persistencia en archivos planos \*\*`.csv`\*\* y una \*\*librería matemática independiente\*\* para el cálculo de tablas de amortización.



\---



\## 👥 Integrantes del Equipo

\* \*\*Héctor Leonardo Torres Campos\*\* — Capa de Persistencia (`datos`), Singleton (`ContextoBanco`) y archivos `.csv` de prueba.

\* \*\*\[Nombre Compañero 2]\*\* — Librería Matemática de Amortización y Paquete `modelo`.

\* \*\*\[Nombre Compañero 3]\*\* — Interfaz Gráfica (`vista` en Java Swing) y Paquete `controlador`.



\---



\## 🏗️ Estructura del Proyecto



&#x20;   Simulador-Bancario-POO/

&#x20;   ├── docs/                  # Diagramas UML (Clases, Paquetes y Secuencia)

&#x20;   └── BancoApp/              # Proyecto principal en Java (NetBeans)

&#x20;       ├── datos/             # Archivos CSV de persistencia (usuarios, cuentas, prestamos, transacciones)

&#x20;       └── src/

&#x20;           ├── modelo/        # Entidades de negocio (Usuario, Cuenta, Prestamo, Transaccion)

&#x20;           ├── vista/         # Interfaces gráficas en Java Swing

&#x20;           ├── controlador/   # Controladores MVC y manejo de eventos

&#x20;           └── datos/         # RepositorioCSV, RepositorioDatos y ContextoBanco (Singleton)



\---



\## ⚙️ Requisitos y Ejecución

1\. \*\*Requisitos:\*\* Java JDK 17 o superior y Apache NetBeans (o cualquier IDE compatible con proyectos Java).

2\. \*\*Clonar el repositorio:\*\* `git clone \[https://github.com/hectortorres2193-code/Simulador-Bancario-POO.git](https://github.com/hectortorres2193-code/Simulador-Bancario-POO.git)`

3\. \*\*Abrir y ejecutar:\*\*

&#x20;  \* Abrir el IDE y seleccionar \*\*File > Open Project\*\*.

&#x20;  \* Seleccionar la carpeta `BancoApp` (y vincular la librería de amortización en \*Libraries\* si aplica).

&#x20;  \* Ejecutar la clase principal (`Main` / `VistaLogin`).



\---



\## 🔑 Credenciales de Prueba (Precargadas en `datos/usuarios.csv`)



| Rol | Usuario (`nombre`) | Contraseña | Número de Cuenta |

| :--- | :--- | :--- | :--- |

| \*\*Cliente\*\* | `Carlo Torres` | `1234` | `CTA-1001` |

| \*\*Cliente\*\* | `Sofia Torres` | `5678` | `CTA-1002` |

| \*\*Cliente\*\* | `Diego Mendoza` | `4321` | `CTA-1003` |

| \*\*Cliente (Sin préstamo)\*\* | `Valeria Gomez` | `8765` | `CTA-1004` |

| \*\*Gerente (Admin)\*\* | `Admin Banco` | `admin123` | \*N/A\* |

