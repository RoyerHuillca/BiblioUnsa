# BiblioUnsa — Sistema de gestión de biblioteca universitaria

Aplicación en **Java, modo consola**, para administrar el catálogo, los usuarios, los préstamos, las reservas y los reportes
de una biblioteca universitaria. 
Proyecto final de *Desarrollo de Software Orientado a Objetos*

## Requisitos
- JDK 17 o superior.

## ejecutar

**Visual Studio Code**: abrir la carpeta del proyecto, abrir `biblioteca/consola/AppConsola.java` y pulsar *Run* sobre `main`.

La primera vez se cargan datos de ejemplo. Al salir (opción 0) los datos se guardan en `datos/biblioteca.dat`.


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

## Estructura
```
biblioteca/
  modelo/        Material (Libro, Tesis, Revista), 
                 Usuario (Estudiante, Docente, Administrativo),
                 Prestamo, Multa, Reserva, EstadoPrestamo, EstadoReserva
  servicio/      Biblioteca, Reporte, Exportable, BibliotecaException, DatosEjemplo
  persistencia/  Respaldo (copias de seguridad con serialización)
  consola/       AppConsola (menús por texto)
test/            PruebasLogica
```