import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.WindowConstants;

import entidades.Documento;
import servicios.ServicioDocumento;
import servicios.Util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

public class FrmOrdenamiento extends JFrame {

    private JButton btnOrdenarBurbuja;
    private JButton btnOrdenarRapido;
    private JButton btnOrdenarInsercion;
    private JToolBar tbOrdenamiento;
    private JComboBox<String> cmbCriterio;
    private JTextField txtTiempo;
    private JButton btnBuscar;
    private JTextField txtBuscar;
    private JButton btnSiguiente;
    private JButton btnAnterior;

    private JTable tblDocumentos;

    public FrmOrdenamiento() {

        tbOrdenamiento = new JToolBar();
        btnOrdenarBurbuja = new JButton();
        btnOrdenarInsercion = new JButton();
        btnOrdenarRapido = new JButton();
        cmbCriterio = new JComboBox<>();
        txtTiempo = new JTextField(10);

        btnBuscar = new JButton();
        txtBuscar = new JTextField(20);

        tblDocumentos = new JTable();

        setSize(700, 500);
        setTitle("Ordenamiento Documentos");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        btnOrdenarBurbuja.setIcon(new ImageIcon(getClass().getResource("/iconos/Ordenar.png")));
        btnOrdenarBurbuja.setToolTipText("Ordenar Burbuja");
        btnOrdenarBurbuja.addActionListener(this::btnOrdenarBurbujaClick);
        
        btnAnterior = new JButton(new ImageIcon(getClass().getResource("/iconos/Anterior.png")));
        btnAnterior.setToolTipText("Anterior");
        btnAnterior.addActionListener(e -> moverA(-1));
        tbOrdenamiento.add(btnAnterior);

        btnSiguiente = new JButton(new ImageIcon(getClass().getResource("/iconos/Siguiente.png")));
        btnSiguiente.setToolTipText("Siguiente");
        btnSiguiente.addActionListener(e -> moverA(1));
        tbOrdenamiento.add(btnSiguiente);

        tbOrdenamiento.add(btnOrdenarBurbuja);

        btnOrdenarRapido.setIcon(new ImageIcon(getClass().getResource("/iconos/OrdenarRapido.png")));
        btnOrdenarRapido.setToolTipText("Ordenar Rapido");
        btnOrdenarRapido.addActionListener(this::btnOrdenarRapidoClick);
        tbOrdenamiento.add(btnOrdenarRapido);

        btnOrdenarInsercion.setIcon(new ImageIcon(getClass().getResource("/iconos/OrdenarInsercion.png")));
        btnOrdenarInsercion.setToolTipText("Ordenar Inserción");
        btnOrdenarInsercion.addActionListener(this::btnOrdenarInsercionClick);
        tbOrdenamiento.add(btnOrdenarInsercion);

        cmbCriterio.setModel(new DefaultComboBoxModel<>(
                new String[] { "Nombre Completo, Tipo de Documento", "Tipo de Documento, Nombre Completo" }));
        tbOrdenamiento.add(cmbCriterio);
        txtTiempo.setEditable(false);
        tbOrdenamiento.add(txtTiempo);

        btnBuscar.setIcon(new ImageIcon(getClass().getResource("/iconos/Buscar.png")));
        btnBuscar.setToolTipText("Buscar");
        btnBuscar.addActionListener(this::btnBuscar);
        tbOrdenamiento.add(btnBuscar);
        tbOrdenamiento.add(txtBuscar);

        var spDocumentos = new JScrollPane(tblDocumentos);

        getContentPane().add(tbOrdenamiento, BorderLayout.NORTH);
        getContentPane().add(spDocumentos, BorderLayout.CENTER);

        String nombreArchivo = System.getProperty("user.dir") + "/src/datos/Datos.csv";
        ServicioDocumento.cargar(nombreArchivo);
        ServicioDocumento.mostrar(tblDocumentos);
    }

    private void btnOrdenarBurbujaClick(ActionEvent evt) {
        if (cmbCriterio.getSelectedIndex() >= 0) {
            Util.iniciarCronometro();
            ServicioDocumento.ordenarBurbuja(cmbCriterio.getSelectedIndex());
            txtTiempo.setText(Util.getTextoTiempoCronometro());
            ServicioDocumento.mostrar(tblDocumentos);
        }
    }

    private void btnOrdenarRapidoClick(ActionEvent evt) {
        if (cmbCriterio.getSelectedIndex() >= 0) {
            Util.iniciarCronometro();
            ServicioDocumento.ordenarRapido(cmbCriterio.getSelectedIndex());
            txtTiempo.setText(Util.getTextoTiempoCronometro());
            ServicioDocumento.mostrar(tblDocumentos);
        }
    }

    private void btnOrdenarInsercionClick(ActionEvent evt) {
        if (cmbCriterio.getSelectedIndex() >= 0) {
            Util.iniciarCronometro();
            ServicioDocumento.ordenarInsercionRecursivo(cmbCriterio.getSelectedIndex());
            txtTiempo.setText(Util.getTextoTiempoCronometro());
            ServicioDocumento.mostrar(tblDocumentos);
        }
    }

    private void btnBuscar(ActionEvent evt) {
        String texto = txtBuscar.getText().trim();
        if (!texto.isEmpty()) {
            Util.iniciarCronometro();
            int criterio = cmbCriterio.getSelectedIndex();
            ServicioDocumento.buscarDocumentos(texto, criterio);
            txtTiempo.setText(Util.getTextoTiempoCronometro());

            if (ServicioDocumento.hayCoincidencias()) {
                int pos = ServicioDocumento.getPrimeraCoincidencia();
                actualizarSeleccion(pos);
                actualizarTituloBusqueda();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontraron coincidencias");
            }
        }
    }

    private void moverA(int desplazamiento) {
        if (ServicioDocumento.hayCoincidencias()) {
            int nuevaPos = -1;
            if (desplazamiento < 0) {
                nuevaPos = ServicioDocumento.getAnteriorCoincidencia();
            } else {
                nuevaPos = ServicioDocumento.getSiguienteCoincidencia();
            }
            
            if (nuevaPos >= 0) {
                actualizarSeleccion(nuevaPos);
                actualizarTituloBusqueda();
            }
        }
    }

    private void actualizarSeleccion(int posicion) {
        tblDocumentos.setRowSelectionInterval(posicion, posicion);
        tblDocumentos.scrollRectToVisible(tblDocumentos.getCellRect(posicion, 0, true));
    }

    private void actualizarTituloBusqueda() {
        int total = ServicioDocumento.getTotalCoincidencias();
        int actual = ServicioDocumento.getCoincidenciaActual() + 1; // Mostrar base 1

        if (total > 0) {
            setTitle("Ordenamiento Documentos - Coincidencia " + actual + " de " + total);
        } else {
            setTitle("Ordenamiento Documentos");
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new FrmOrdenamiento().setVisible(true));
    }
}