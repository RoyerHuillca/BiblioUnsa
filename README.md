# BiblioUnsa — Sistema de gestión de biblioteca universitaria

Aplicación en **Java, modo consola**, para administrar el catálogo, los usuarios, los préstamos, las reservas y los reportes
de una biblioteca universitaria. Proyecto final de *Desarrollo de Software Orientado a Objetos* — EPIS, UNSA 2026.
El diseño sigue el diagrama UML del equipo (`docs/diagrama_uml.png`).

## Requisitos
- JDK 17 o superior (probado con JDK 21).

## Cómo compilar y ejecutar

**Linux / macOS**
```bash
./compilar.sh
java -cp out biblioteca.consola.AppConsola
```

**Windows (CMD o PowerShell)**
```bat
chcp 65001
mkdir out
javac -encoding UTF-8 -d out src\biblioteca\modelo\*.java src\biblioteca\servicio\*.java src\biblioteca\persistencia\*.java src\biblioteca\consola\*.java
java -Dfile.encoding=UTF-8 -cp out biblioteca.consola.AppConsola
```

**VS Code**: abrir la carpeta del proyecto, abrir `src/biblioteca/consola/AppConsola.java` y pulsar *Run* sobre `main`.

La primera vez se cargan datos de ejemplo. Al salir (opción 0) los datos se guardan en `datos/biblioteca.dat`.

## Pruebas
```bash
./probar.sh      # 41 verificaciones de la lógica de negocio
```

## Menú
1. **Materiales**: listar, buscar por título/autor/código, registrar, editar, dar de baja.
2. **Usuarios**: listar, buscar, registrar, pagar multas.
3. **Préstamos y reservas**: registrar préstamo, devolución (con multa), reservar, ver préstamos y reservas, cancelar reserva.
4. **Reportes**: libros más prestados, usuarios con deuda, préstamos vencidos, exportar a `.txt`.
5. **Copias de seguridad**: crear y restaurar con serialización de objetos.

## Reglas de negocio
| Usuario | Plazo de préstamo | Límite de préstamos |
|---|---|---|
| Estudiante | 7 días | 3 |
| Docente | 15 días | 5 |
| Administrativo | 10 días | 4 |

- Multa por retraso: `Multa.TARIFA_POR_DIA` = S/ 1.00 por cada día después del vencimiento.
- Un usuario con multas pendientes no puede pedir nuevos préstamos.
- Un material prestado se puede reservar; al devolverse, solo quien reservó primero puede prestarlo (la reserva pasa a ATENDIDA).

## Estructura
```
src/biblioteca/
  modelo/        Material (Libro, Tesis, Revista), Usuario (Estudiante, Docente, Administrativo),
                 Prestamo, Multa, Reserva, EstadoPrestamo, EstadoReserva
  servicio/      Biblioteca, Reporte, Exportable, BibliotecaException, DatosEjemplo
  persistencia/  Respaldo (copias de seguridad con serialización)
  consola/       AppConsola (menús por texto)
test/            PruebasLogica
docs/            diagrama UML y capturas de la consola
```

## Correspondencia con el UML
- `Material` y `Usuario` son abstractas; `obtenerTipo()`, `obtenerPlazoPrestamo()` y `obtenerLimitePrestamos()` se redefinen en cada subclase (polimorfismo).
- `Biblioteca` agrega materiales (`ArrayList`), usuarios (`HashMap<String, Usuario>`), préstamos y reservas (agregación).
- `Prestamo` genera su `Multa` (composición, 0..1).
- `Reporte` implementa la interfaz `Exportable` y consulta a `Biblioteca`.
- En el UML el atributo se llama `año`; en el código es `anio` para evitar problemas de codificación.

## Métodos de apoyo que no aparecen en el UML
`Biblioteca`: `buscarMaterialPorCodigo`, `prestamosActivosDe`, `deudaDe`, `pagarMultas`, `reservasPendientes`, getters de las colecciones, `hoy` y `setReloj` (simula fechas en los datos de ejemplo).
`Reporte`: `cantidadPrestamos` y `generarTexto`. `Prestamo`: `estaDevuelto`. La clase `Respaldo` (copias de seguridad) tampoco está en el UML.
