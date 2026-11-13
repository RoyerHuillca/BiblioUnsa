package biblioteca.servicio;

import biblioteca.modelo.*;

import java.time.LocalDate;

// Datos iniciales para probar el programa la primera vez que se ejecuta.
public class DatosEjemplo {

    public static Biblioteca crear() {
        Biblioteca b = new Biblioteca("BiblioUnsa");

        b.registrarMaterial(new Libro("L001", "Java: Cómo programar", "Paul Deitel", 2016, "978-0134743356", "Pearson"));
        b.registrarMaterial(new Libro("L002", "Clean Code", "Robert C. Martin", 2008, "978-0132350884", "Prentice Hall"));
        b.registrarMaterial(new Libro("L003", "Introduction to Algorithms", "Thomas H. Cormen", 2009, "978-0262033848", "MIT Press"));
        b.registrarMaterial(new Libro("L004", "Los ríos profundos", "José María Arguedas", 1958, "978-9972401145", "Losada"));
        b.registrarMaterial(new Libro("L005", "Cien años de soledad", "Gabriel García Márquez", 1967, "978-0307474728", "Sudamericana"));
        b.registrarMaterial(new Tesis("T001", "Sistema de riego automatizado con IoT", "Carlos Quispe Mamani", 2023, "Dr. Luis Paredes", "Ingeniero de Sistemas"));
        b.registrarMaterial(new Tesis("T002", "Análisis de sentimientos en redes sociales", "Ana Condori Huamán", 2024, "Dra. Rosa Flores", "Ingeniera de Sistemas"));
        b.registrarMaterial(new Revista("R001", "Revista de Investigación UNSA", "Editorial UNSA", 2025, "1234-5678", 12, 3));
        b.registrarMaterial(new Revista("R002", "Ciencia y Tecnología Andina", "Fondo Editorial", 2025, "8765-4321", 5, 1));

        b.registrarUsuario(new Estudiante("E001", "Royer Huillca Maqquera", "rhuillca@unsa.edu.pe", "Ingeniería de Sistemas"));
        b.registrarUsuario(new Estudiante("E002", "María Torres Valdivia", "mtorres@unsa.edu.pe", "Ingeniería de Sistemas"));
        b.registrarUsuario(new Docente("D001", "Marco Aedo López", "maedo@unsa.edu.pe", "Ingeniería de Sistemas"));
        b.registrarUsuario(new Administrativo("A001", "Julio Ramos Salas", "jramos@unsa.edu.pe", "Secretaría Académica"));

        // Historial simulado con un reloj controlado, para tener un vencido y una multa de ejemplo.
        LocalDate hoy = LocalDate.now();
        b.setReloj(() -> hoy.minusDays(30));
        b.registrarPrestamo("E002", "L001");     // plazo 7 días -> debía devolver hace 23 días
        b.setReloj(() -> hoy.minusDays(10));
        b.registrarDevolucion("L001");           // devuelto con 13 días de retraso -> multa S/ 13.00
        b.setReloj(() -> hoy.minusDays(20));
        b.registrarPrestamo("A001", "L003");     // plazo 10 días -> sigue prestado y vencido
        b.setReloj(() -> hoy.minusDays(2));
        b.registrarPrestamo("D001", "L002");     // vigente
        b.setReloj(() -> hoy.minusDays(1));
        b.registrarPrestamo("E001", "R001");     // vigente
        b.setReloj(null);
        return b;
    }
}
