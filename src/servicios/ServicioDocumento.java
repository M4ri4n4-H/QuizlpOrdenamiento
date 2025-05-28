package servicios;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import entidades.Documento;

public class ServicioDocumento {

    private static List<Documento> documentos = new ArrayList<>();
    private static List<Integer> coincidencias = new ArrayList<>();
    private static int indiceCoincidenciaActual = -1;

    public static void cargar(String nombreArchivo) {
        var br = Archivo.abrirArchivo(nombreArchivo);
        if (br != null) {
            try {
                var linea = br.readLine();
                linea = br.readLine();
                while (linea != null) {
                    var textos = linea.split(";");
                    var documento = new Documento(textos[0], textos[1], textos[2], textos[3]);
                    documentos.add(documento);
                    linea = br.readLine();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void mostrar(JTable tbl) {
        String[] encabezados = new String[] { "#", "Primer Apellido", "Segundo Apellido", "Nombres", "Documento" };
        String[][] datos = new String[documentos.size()][encabezados.length];

        int fila = 0;
        for (var documento : documentos) {
            datos[fila][0] = String.valueOf(fila + 1);
            datos[fila][1] = documento.getApellido1();
            datos[fila][2] = documento.getApellido2();
            datos[fila][3] = documento.getNombre();
            datos[fila][4] = documento.getDocumento();
            fila++;
        }

        var dtm = new DefaultTableModel(datos, encabezados);
        tbl.setModel(dtm);
    }

    private static boolean esMayor(Documento d1, Documento d2, int criterio) {
        if (criterio == 0) {
            return d1.getNombreCompleto().compareTo(d2.getNombreCompleto()) > 0 ||
                    (d1.getNombreCompleto().equals(d2.getNombreCompleto()) &&
                            d1.getDocumento().compareTo(d2.getDocumento()) > 0);
        } else {
            return d1.getDocumento().compareTo(d2.getDocumento()) > 0 ||
                    (d1.getDocumento().equals(d2.getDocumento()) &&
                            d1.getNombreCompleto().compareTo(d2.getNombreCompleto()) > 0);
        }
    }

    private static void intercambiar(int origen, int destino) {
        if (0 <= origen && origen < documentos.size() &&
                0 <= destino && destino < documentos.size()) {
            var temporal = documentos.get(origen);
            documentos.set(origen, documentos.get(destino));
            documentos.set(destino, temporal);
        }
    }

    public static void ordenarBurbuja(int criterio) {
        for (int i = 0; i < documentos.size() - 1; i++) {
            for (int j = i + 1; j < documentos.size(); j++) {
                if (esMayor(documentos.get(i), documentos.get(j), criterio)) {
                    intercambiar(i, j);
                }
            }
        }
    }

    private static int getPivote(int inicio, int fin, int criterio) {
        var pivote = inicio;
        var documentoPivote = documentos.get(pivote);

        for (int i = inicio + 1; i <= fin; i++) {
            if (esMayor(documentoPivote, documentos.get(i), criterio)) {
                pivote++;
                if (i != pivote) {
                    intercambiar(i, pivote);
                }
            }
        }
        if (inicio != pivote) {
            intercambiar(inicio, pivote);
        }

        return pivote;
    }

    private static void ordenarRapido(int inicio, int fin, int criterio) {
        if (fin > inicio) {
            var pivote = getPivote(inicio, fin, criterio);
            ordenarRapido(inicio, pivote - 1, criterio);
            ordenarRapido(pivote + 1, fin, criterio);
        }
    }

    public static void ordenarRapido(int criterio) {
        ordenarRapido(0, documentos.size() - 1, criterio);
    }

    public static void ordenarInsercion(int criterio) {
        for (int i = 1; i < documentos.size(); i++) {
            var documentoActual = documentos.get(i);
            int j = i - 1;
            while (j >= 0 && esMayor(documentos.get(j), documentoActual, criterio)) {
                documentos.set(j + 1, documentos.get(j));
                j--;
            }
            documentos.set(j + 1, documentoActual);
        }
    }

    private static void ordenarInsercionRecursivo(int posicion, int criterio) {
        if (posicion == 0) {
            return;
        }
        ordenarInsercionRecursivo(posicion - 1, criterio);

        var documentoActual = documentos.get(posicion);
        int j = posicion - 1;
        while (j >= 0 && esMayor(documentos.get(j), documentoActual, criterio)) {
            documentos.set(j + 1, documentos.get(j));
            j--;
        }
        documentos.set(j + 1, documentoActual);
    }

    public static void ordenarInsercionRecursivo(int criterio) {
        ordenarInsercionRecursivo(documentos.size() - 1, criterio);
    }

    public static void buscarDocumentos(String texto, int criterio) {
        coincidencias.clear();
        indiceCoincidenciaActual = -1;
        texto = texto.toLowerCase();
        
        // Intentar búsqueda binaria recursiva solo para apellido1 cuando el criterio es 0
        if (criterio == 0) {
            int posBinaria = busquedaBinariaRecursiva(texto, 0, documentos.size() - 1);
            if (posBinaria >= 0) {
                // Recoger todas las coincidencias de apellido1
                int temp = posBinaria;
                while (temp >= 0 && documentos.get(temp).getApellido1().equalsIgnoreCase(texto)) {
                    coincidencias.add(temp);
                    temp--;
                }
                
                temp = posBinaria + 1;
                while (temp < documentos.size() && documentos.get(temp).getApellido1().equalsIgnoreCase(texto)) {
                    coincidencias.add(temp);
                    temp++;
                }
            }
        }
        
        // Si no encontramos coincidencias con binaria, hacer secuencial
        if (coincidencias.isEmpty()) {
            for (int i = 0; i < documentos.size(); i++) {
                Documento doc = documentos.get(i);
                if (doc.getApellido1().toLowerCase().contains(texto) ||
                    doc.getApellido2().toLowerCase().contains(texto) ||
                    doc.getNombre().toLowerCase().contains(texto) ||
                    doc.getDocumento().toLowerCase().contains(texto)) {
                    coincidencias.add(i);
                }
            }
        }
    }

    // Búsqueda binaria recursiva para apellido1
    private static int busquedaBinariaRecursiva(String apellido1, int inicio, int fin) {
        if (inicio > fin) {
            return -1; // Caso base: no encontrado
        }
        
        int medio = (inicio + fin) / 2;
        Documento doc = documentos.get(medio);
        int comparacion = doc.getApellido1().compareToIgnoreCase(apellido1);
        
        if (comparacion == 0) {
            return medio; // Encontrado
        } else if (comparacion < 0) {
            return busquedaBinariaRecursiva(apellido1, medio + 1, fin); // Buscar en la mitad derecha
        } else {
            return busquedaBinariaRecursiva(apellido1, inicio, medio - 1); // Buscar en la mitad izquierda
        }
    }

    public static boolean hayCoincidencias() {
        return !coincidencias.isEmpty();
    }

    public static int getPrimeraCoincidencia() {
        if (coincidencias.isEmpty()) return -1;
        indiceCoincidenciaActual = 0;
        return coincidencias.get(indiceCoincidenciaActual);
    }

    public static int getSiguienteCoincidencia() {
        if (coincidencias.isEmpty()) return -1;
        indiceCoincidenciaActual = (indiceCoincidenciaActual + 1) % coincidencias.size();
        return coincidencias.get(indiceCoincidenciaActual);
    }

    public static int getAnteriorCoincidencia() {
        if (coincidencias.isEmpty()) return -1;
        indiceCoincidenciaActual = (indiceCoincidenciaActual - 1 + coincidencias.size()) % coincidencias.size();
        return coincidencias.get(indiceCoincidenciaActual);
    }

    public static int getTotalCoincidencias() {
        return coincidencias.size();
    }

    public static int getCoincidenciaActual() {
        return indiceCoincidenciaActual;
    }
}