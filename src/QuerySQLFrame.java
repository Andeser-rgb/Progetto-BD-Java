import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class QuerySQLFrame extends JFrame {
    private static final int NUM_OPERAZIONI = 23;
    private ArrayList<JButton> operationButtons;
    private JTextArea resultTextArea;

    public QuerySQLFrame() {
        add(new JLabel("Scegli un operazione: ", JLabel.CENTER), BorderLayout.NORTH);
        var panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 100;
        initButtons();

        for (var button : operationButtons) {
            panel.add(button, c);
            c.gridy++;
        }

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 100;
        c.weighty = 100;
        c.gridheight = NUM_OPERAZIONI;
        c.fill = GridBagConstraints.BOTH;
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);
        resultTextArea.setPreferredSize(new Dimension(1000, 1000));

        panel.add(new JScrollPane(resultTextArea), c);

        Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        panel.setBorder(border);

        add(panel);
        pack();
    }
    public static String[] showInputDialog(String title, String[] labels) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 5, 5));
        JTextField[] fields = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i] + ":"));
            fields[i] = new JTextField(22);
            panel.add(fields[i]);
        }
        int result = JOptionPane.showConfirmDialog(null, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String[] responses = new String[labels.length];
            for (int i = 0; i < labels.length; i++) {
                responses[i] = fields[i].getText();
            }
            return responses;
        } else {
            return null;
        }
    }

    private final ActionListener[] listeners = new ActionListener[]{
            e -> {
                String[] inputs =showInputDialog("Aggiungi Utente", new String[] {
                        "Nome",
                        "Cognome",
                        "Email",
                        "Password",
                        "Via",
                        "Numero",
                        "Città",
                        "Paese",
                        "Codice Postale"
                });
                if (inputs != null) {
                    try {
                        String nome = inputs[0];
                        String cognome = inputs[1];
                        String email = inputs[2];
                        String password = inputs[3];
                        String via = inputs[4];
                        String numero = inputs[5];
                        String citta = inputs[6];
                        String paese = inputs[7];
                        String codicePostale = inputs[8];

                        var res = DatabaseManager.aggiungiUtente(nome, cognome, email, password, via, numero, citta, paese, codicePostale);

                        resultTextArea.setText("Utente aggiunto con successo\n" +
                                "ID nuovo utente = " + res);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage());
                    }
                }
            },
            e -> {
                //Aggiunta Utente
                JTextField firstName = new JTextField();
                JTextField lastName = new JTextField();
                JTextField email = new JTextField();
                JTextField password = new JTextField();
                JTextField via = new JTextField();
                JTextField numeroCivico = new JTextField();
                JTextField citta = new JTextField();
                JTextField paese = new JTextField();
                JTextField codicePostale = new JTextField();
                final JComponent[] inputs = new JComponent[]{
                        new JLabel("Nome"),
                        firstName,
                        new JLabel("Cognome"),
                        lastName,
                        new JLabel("Email"),
                        email,
                        new JLabel("Password"),
                        password,
                        new JLabel("Via"),
                        via,
                        new JLabel("Numero"),
                        numeroCivico,
                        new JLabel("Citta"),
                        citta,
                        new JLabel("Paese"),
                        paese,
                        new JLabel("Codice postale"),
                        codicePostale,
                };

                int result = JOptionPane.showConfirmDialog(null, inputs, e.getActionCommand(), JOptionPane.OK_CANCEL_OPTION);

            },

    };
    private void initButtons() {
        operationButtons = new ArrayList<>();
        for (var i = 0; i < NUM_OPERAZIONI; i++) {
            var button = new JButton("Operazione " + (i + 1));
            button.addActionListener(listeners[0]);
            operationButtons.add(button);
        }
    }
}
