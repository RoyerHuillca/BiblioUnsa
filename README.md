# BiblioUnsa — Sistema de gestión de biblioteca universitaria

Aplicación en **Java** para administrar el catálogo, los usuarios, los préstamos, las reservas y los reportes de una
biblioteca universitaria. Proyecto final de *Desarrollo de Software Orientado a Objetos* — EPIS, UNSA 2026.
Tiene dos formas de usarse sobre la misma lógica:

- **Modo consola** (Sprint 1): `biblioteca.consola.AppConsola`
- **Modo gráfico, Swing** (Sprint 2): `biblioteca.vista.AppGrafica`

El diseño sigue el diagrama UML del equipo (`docs/diagrama_uml.png`); la interfaz gráfica está en `docs/diagrama_vista.png`.

## Requisitos
- JDK 17 o superior (probado con JDK 21). Debe responder `javac -version`.

## Cómo compilar y ejecutar

Ejecutar los comandos en la carpeta raíz del proyecto (la que contiene `biblioteca`, `datos` y `test`).

**PowerShell (Windows)**
```powershell
chcp 65001
mkdir out -Force
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse biblioteca -Filter *.java).FullName
java -Dfile.encoding=UTF-8 -cp out biblioteca.vista.AppGrafica        # modo gráfico
java -Dfile.encoding=UTF-8 -cp out biblioteca.consola.AppConsola      # modo consola
```

**CMD (Windows)**
```bat
chcp 65001
mkdir out
javac -encoding UTF-8 -d out biblioteca\modelo\*.java biblioteca\servicio\*.java biblioteca\persistencia\*.java biblioteca\consola\*.java biblioteca\vista\*.java
java -Dfile.encoding=UTF-8 -cp out biblioteca.vista.AppGrafica
```

**Linux / macOS**
```bash
./compilar.sh
java -cp out biblioteca.vista.AppGrafica
java -cp out biblioteca.consola.AppConsola
```

**VS Code**: abrir la carpeta raíz, abrir `biblioteca/vista/AppGrafica.java` (o `consola/AppConsola.java`) y pulsar *Run* sobre `main`.

La primera vez se cargan datos de ejemplo. Al cerrar la ventana (o salir de la consola) los datos se guardan en `datos/biblioteca.dat`.

## Pruebas
```bash
./probar.sh      # 41 verificaciones de la lógica + 37 de la interfaz gráfica (no abre ventanas)
```
En Windows, después de compilar:
```powershell
mkdir out-test -Force
javac -encoding UTF-8 -cp out -d out-test test\PruebasLogica.java test\biblioteca\vista\PruebasVista.java
java -Dstdout.encoding=UTF-8 -cp "out;out-test" PruebasLogica
java -Djava.awt.headless=true -Dstdout.encoding=UTF-8 -cp "out;out-test" biblioteca.vista.PruebasVista
```

## Modo gráfico
Ventana con cuatro pestañas y un menú **Archivo** (guardar, crear y restaurar copia de seguridad, salir):
1. **Materiales**: tabla, búsqueda mientras se escribe (título, autor o código), registrar (ventana con formulario), editar y dar de baja.
2. **Usuarios**: tabla con límite, plazo y deuda; buscar por código; registrar; pagar multas.
3. **Préstamos y reservas**: elegir usuario y material para registrar un préstamo o reservar; registrar devolución (muestra la multa); cancelar reserva.
4. **Reportes**: libros más prestados, usuarios con deuda y préstamos vencidos; exportar a `.txt`.

Los errores de negocio (por ejemplo, "El usuario tiene multas pendientes") se muestran en cuadros de aviso.

## Menú de consola
1. Materiales  2. Usuarios  3. Préstamos y reservas  4. Reportes  5. Copias de seguridad  0. Salir

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
  persistencia/  Respaldo (copias de seguridad con serialización)
  consola/       AppConsola (menús por texto)
  vista/         AppGrafica, VentanaPrincipal, PanelMateriales, PanelUsuarios, PanelPrestamos, PanelReportes,
                 FormularioMaterial, FormularioUsuario, DialogoMaterial, DialogoUsuario, Mensajes, Sesion, Actualizable, Util
test/            PruebasLogica, biblioteca/vista/PruebasVista y CapturasGrafico
docs/            diagramas UML y capturas
```
