/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author samar
 */
public class Inicio extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Inicio.class.getName());
    
    class PanelFondo extends javax.swing.JPanel {
    // Guardamos la imagen de fondo que se va a dibujar.
    private java.awt.Image imagenFondo;
    // Constructor: recibe la ruta de la imagen y la carga.
     public PanelFondo() {
        // Cargamos la imagen desde la carpeta de recursos.
        imagenFondo = new javax.swing.ImageIcon(getClass().getResource("/Imagenes/fondo.jpeg")).getImage();
        setLayout(null);
     }
     
    // Método que Java llama solo para dibujar el panel.
    @Override
     protected void paintComponent(java.awt.Graphics g) {
        // Primero dejamos que Java dibuje lo normal del panel.
        super.paintComponent(g);
        g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
     }
    }

    
    /**
     * Creates new form Inicio
     */
    public Inicio() {
        // Crea todos los botones, labels y paneles del diseño.
        // Ponemos la imagen de fondo en la ventana.
        // El PanelFondo es solo visual.
        // Volvemos a poner los componentes encima del fondo.
        // Si no hacemos esto, el fondo los taparía.
        setContentPane(new PanelFondo());
        initComponents();
        setExtendedState(Inicio.MAXIMIZED_BOTH); 
        this.setResizable(false); // no se puede hacer chiquita ni agrandar
        this.setDefaultCloseOperation(Inicio.DO_NOTHING_ON_CLOSE); // no se puede cerrar con la X
        // Iniciamos el carrusel de anuncios.
        // Solo cambia fotos, no hace nada del banco.
        iniciarCarrusel(); 
        // Ponemos el logo del banco en forma circular.
        // Solo es estética, no es lógica de negocio.
        jlLogo.setIcon(imagenCircular("/Imagenes/Logo.jpeg",134));
        
    }
    // Lista de las rutas de las imágenes que se van a mostrar
    // en el carrusel de anuncios. Solo son textos, no lógica.
    private String[] imagenes = {
    "/Imagenes/Foto1.jpeg",
    "/Imagenes/Foto2.jpeg",
    "/Imagenes/Foto3.jpeg"
    };
    
    // Posición actual del carrusel. Empieza en 0 (primera imagen).
    private int indice = 0;
    
    
    private void iniciarCarrusel() {
    jlAnuncio.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jlAnuncio.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
    
     // Reloj interno que nos ayuda a cambiar la imagen cada cierto tiempo
     // 2. Creamos un temporizador que se repite cada 4000 ms = 4 segundos..
    javax.swing.Timer timer = new javax.swing.Timer(4000, e -> {
        // 1. Mostramos la primera imagen al abrir la ventana.
        mostrarImagen();
        indice++;
        if (indice >= imagenes.length) {
            indice = 0;
        }
    });
    timer.start();
    mostrarImagen(); // para que muestre la 1 desde el inicio
}
   // FUNCIÓN: Dibujar la imagen de fondo de la ventana.
   // Solo pinta, no decide nada, no calcula nada.
   private javax.swing.ImageIcon imagenCircular(String ruta, int diametro) {
    try {
        // 1. Leemos la imagen del logo desde recursos.
        java.awt.image.BufferedImage original = javax.imageio.ImageIO.read(getClass().getResource(ruta));
        java.awt.image.BufferedImage circular = new java.awt.image.BufferedImage(diametro, diametro, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = circular.createGraphics();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, diametro, diametro));

        // Esto es lo que lo expande para que no se vea lo blanco
        double escala = Math.max((double) diametro / original.getWidth(), (double) diametro / original.getHeight());
        escala = escala * 1.5;

        int nuevoAncho = (int) (original.getWidth() * escala);
        int nuevoAlto = (int) (original.getHeight() * escala);
        int x = (diametro - nuevoAncho) / 2;
        int y = (diametro - nuevoAlto) / 2;
        g2.drawImage(original, x, y, nuevoAncho, nuevoAlto, null);

        g2.dispose();
        return new javax.swing.ImageIcon(circular);
        // Si no encuentra la imagen, no se cae el programa,
        // solo muestra el error en consola para que lo veas.
    } catch (Exception e) {
        return null;
    }
}
   
    private void mostrarImagen() {
        // 2. Cargamos la imagen desde recursos.
        javax.swing.ImageIcon iconoOriginal = new javax.swing.ImageIcon(getClass().getResource(imagenes[indice]));
        
        // 3. La ajustamos al tamaño del label jlAnuncio para que no se deforme.
        java.awt.Image img = iconoOriginal.getImage().getScaledInstance( jlAnuncio.getWidth(), jlAnuncio.getHeight(), java.awt.Image.SCALE_SMOOTH);jlAnuncio.setIcon(new javax.swing.ImageIcon(img));
}
    
        
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jlAnuncio = new javax.swing.JLabel();
        jlLogo = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Baskerville Old Face", 1, 80)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(51, 51, 51));
        jLabel1.setText("AXIOM");

        jLabel2.setFont(new java.awt.Font("Baskerville Old Face", 1, 60)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 51, 51));
        jLabel2.setText("BANK");

        jLabel3.setFont(new java.awt.Font("Baskerville Old Face", 1, 48)); // NOI18N
        jLabel3.setText("Tu futuro financiero comienza aqui");

        jLabel4.setFont(new java.awt.Font("Baskerville Old Face", 1, 48)); // NOI18N
        jLabel4.setText("Una  nueva manera de tener tu dinero seguro");

        jLabel5.setFont(new java.awt.Font("Baskerville Old Face", 1, 36)); // NOI18N
        jLabel5.setText("Preciona cualquier tecla para comenzar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jlAnuncio, javax.swing.GroupLayout.PREFERRED_SIZE, 1456, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 70, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(311, 311, 311)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(410, 410, 410)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 741, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jlLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(106, 106, 106)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jLabel1)))
                        .addGap(488, 488, 488))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 635, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 210, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jlLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addGap(39, 39, 39)
                .addComponent(jlAnuncio, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(63, 63, 63))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        // TODO add your handling code here:
        Login ventana = new Login();
        ventana.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_formMouseClicked

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        // TODO add your handling code here:
        Login ventana = new Login();
        ventana.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_formKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Inicio().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jlAnuncio;
    private javax.swing.JLabel jlLogo;
    // End of variables declaration//GEN-END:variables
}
