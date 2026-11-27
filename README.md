# BiblioUnsa — Sistema de gestión de biblioteca universitaria

Aplicación de escritorio en **Java (Swing)** para administrar el catálogo, los usuarios, los préstamos, las reservas y los
reportes de una biblioteca universitaria. Proyecto final de *Desarrollo de Software Orientado a Objetos* — EPIS, UNSA 2026.
El diseño sigue el diagrama UML del equipo (`docs/diagrama_uml.png`). Los datos se pueden guardar en un archivo, en copias de
seguridad o en una **base de datos SQLite con JDBC**.

## Requisitos
- JDK 17 o superior (probado con JDK 21). Debe responder `javac -version`.
- Para la base de datos: el driver `sqlite-jdbc-3.36.0.3.jar` en la carpeta `lib` (ver `lib/LEEME.txt`). Sin el jar el programa funciona igual y solo las opciones de base de datos avisan que falta el driver.

## Cómo compilar y ejecutar
Ejecutar los comandos en la carpeta raíz del proyecto (la que contiene `biblioteca`, `datos`, `lib` y `test`).

**PowerShell (Windows)**
```powershell
chcp 65001
mkdir out -Force
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse biblioteca -Filter *.java).FullName
java "-Dfile.encoding=UTF-8" -cp "out;lib/sqlite-jdbc-3.36.0.3.jar" biblioteca.vista.AppGrafica
```

**Linux / macOS**
```bash
./compilar.sh
java -cp "out:lib/sqlite-jdbc-3.36.0.3.jar" biblioteca.vista.AppGrafica
```

**VS Code**: abrir la carpeta raíz, abrir `biblioteca/vista/AppGrafica.java` y pulsar *Run* sobre `main`. VS Code toma solo los `.jar` de la carpeta `lib`.

La primera vez se cargan datos de ejemplo. Al cerrar la ventana los datos se guardan en `datos/biblioteca.dat`.

## Base de datos (JDBC + SQLite)
Menú **Archivo**:
- **Guardar en base de datos (SQLite)**: crea `datos/biblioteca.db` con las tablas `material`, `usuario`, `prestamo`, `multa` y `reserva` y guarda todo en una sola transacción. Si se guarda otra vez, reemplaza el contenido sin duplicar filas.
- **Cargar desde base de datos (SQLite)**: reemplaza los datos actuales por los de la base (pide confirmación). Si la base está vacía o falta el driver, avisa y no cambia nada.

Clases: `BaseDatos` (conexión y transacciones JDBC), `Sql` (las sentencias), `Mapeo` (convierte objetos en filas y filas en objetos, sin JDBC) y `AccionesBD` / `BarraMenu` (menú de la ventana).

## Modo gráfico
Ventana con cuatro pestañas:
1. **Materiales**: tabla, búsqueda mientras se escribe, registrar (ventana con formulario), editar y dar de baja.
2. **Usuarios**: tabla con límite, plazo y deuda; buscar por código; registrar; pagar multas.
3. **Préstamos y reservas**: registrar préstamo, devolución (muestra la multa), reservar y cancelar reserva.
4. **Reportes**: libros más prestados, usuarios con deuda y préstamos vencidos; exportar a `.txt`.

## Pruebas (no abren ventanas)
```bash
./probar.sh
```
Ejecuta: lógica de negocio (41), conversión a tablas (16), interfaz gráfica (44) y, si `lib` tiene el jar, la base de datos real (`PruebasBaseDatos`).

**PowerShell (Windows)**, después de compilar:
```powershell
mkdir out-test -Force
javac -encoding UTF-8 -cp "out;lib/sqlite-jdbc-3.36.0.3.jar" -d out-test test\Comparador.java test\PruebasLogica.java test\PruebasMapeo.java test\PruebasBaseDatos.java test\biblioteca\vista\PruebasVista.java
java "-Dstdout.encoding=UTF-8" -cp "out;out-test" PruebasLogica
java "-Dstdout.encoding=UTF-8" -cp "out;out-test" PruebasMapeo
java "-Djava.awt.headless=true" "-Dstdout.encoding=UTF-8" -cp "out;out-test" biblioteca.vista.PruebasVista
java "-Dstdout.encoding=UTF-8" -cp "out;out-test;lib/sqlite-jdbc-3.36.0.3.jar" PruebasBaseDatos
```
`PruebasBaseDatos` guarda y carga una base de datos real; sin el jar dice "PRUEBA OMITIDA".

## Reglas de negocio
| Usuario | Plazo de préstamo | Límite de préstamos |
|---|---|---|
| Estudiante | 7 días | 3 |
| Docente | 15 días | 5 |
| Administrativo | 10 días | 4 |

- Multa por retraso: S/ 1.00 por cada día después del vencimiento (`Multa.TARIFA_POR_DIA`).
- Un usuario con multas pendientes no puede pedir nuevos préstamos.
- Un material prestado se puede reservar; al devolverse, solo quien reservó primero puede prestarlo.

## Estructura
```
biblioteca/
  modelo/        Material (Libro, Tesis, Revista), Usuario (Estudiante, Docente, Administrativo),
                 Prestamo, Multa, Reserva, EstadoPrestamo, EstadoReserva
  servicio/      Biblioteca, Reporte, Exportable, BibliotecaException, DatosEjemplo
  persistencia/  BaseDatos, Sql, Mapeo (JDBC/SQLite) y Respaldo (copias de seguridad por serialización)
  vista/         AppGrafica, VentanaPrincipal, BarraMenu, AccionesBD, paneles, formularios, diálogos, Mensajes
lib/             driver sqlite-jdbc (el .jar se descarga aparte)
test/            PruebasLogica, PruebasMapeo, PruebasBaseDatos, Comparador, ValidarConSqlite (+ validar_sqlite.py),
                 biblioteca/vista/PruebasVista y programas que generan las capturas
docs/            diagramas UML y capturas
```

## Correspondencia con el UML
- `Material` y `Usuario` son abstractas y sus subclases redefinen `obtenerTipo()`, `obtenerPlazoPrestamo()` y `obtenerLimitePrestamos()` (polimorfismo).
- `Biblioteca` agrega materiales (`ArrayList`), usuarios (`HashMap<String, Usuario>`), préstamos y reservas (agregación).
- `Prestamo` genera su `Multa` (composición). `Reporte` implementa `Exportable`.
- En el UML el atributo se llama `año`; en el código es `anio` para evitar problemas de codificación.

## Métodos de apoyo que no aparecen en el UML original
`Biblioteca`: `buscarMaterialPorCodigo`, `prestamosActivosDe`, `deudaDe`, `pagarMultas`, `reservasPendientes`, getters, `hoy` y `setReloj`.
`Reporte`: `cantidadPrestamos` y `generarTexto`. `Prestamo`: `estaDevuelto`. Clases de persistencia: `Respaldo`, `BaseDatos`, `Sql` y `Mapeo`.
