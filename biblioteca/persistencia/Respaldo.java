package biblioteca.persistencia;

import biblioteca.servicio.Biblioteca;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Copias de seguridad: guarda y restaura toda la biblioteca con serialización de objetos. */
public class Respaldo {

    public static void guardar(Biblioteca biblioteca, String ruta) throws IOException {
        Path destino = Paths.get(ruta);
        if (destino.getParent() != null) Files.createDirectories(destino.getParent());
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ruta))) {
            out.writeObject(biblioteca);
        }
    }

    public static Biblioteca cargar(String ruta) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(ruta))) {
            return (Biblioteca) in.readObject();
        }
    }

    public static boolean existe(String ruta) { return new File(ruta).exists(); }
}
