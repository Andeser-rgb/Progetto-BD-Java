import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuerySQLFrame extends JFrame {
    private ArrayList<JButton> operationButtons;
    private JTextArea resultTextArea;
    private JScrollPane resultScrollPane;

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
        c.gridheight = listeners.length;
        c.fill = GridBagConstraints.BOTH;
        resultTextArea = new JTextArea(20, 50);
        resultTextArea.setEditable(false);
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);

        resultScrollPane = new JScrollPane(resultTextArea);
        panel.add(resultScrollPane, c);

        Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        panel.setBorder(border);

        add(panel);
        pack();
    }

    public static String formatMap(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        map.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
        return sb.toString();
    }

    public static String formatList(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> map : list) {
            sb.append(formatMap(map)).append("\n");
        }
        return sb.toString();
    }

    public static String[] showInputDialog(String title, String[] labels) {
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 5, 5));
        JTextField[] fields = new JTextField[labels.length];
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        var label = new JLabel(title);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        outerPanel.add(label);
        outerPanel.add(panel);

        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i] + ":"));
            fields[i] = new JTextField(22);
            panel.add(fields[i]);
        }
        int result = JOptionPane.showConfirmDialog(null, outerPanel, title,
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
            // Listener 1: Add User (Operation 1)
            e -> {
                String[] inputs = showInputDialog("Aggiungi Utente", new String[]{
                        "Nome",
                        "Cognome",
                        "Email",
                        "Username",
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

                        resultTextArea.setText("Utente aggiunto con successo!\n" +
                                "ID nuovo utente = " + res);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage());
                    }
                }
            },
            // Listener 2: Select User Data (Operation 2)
            e -> {
                var opName = "Seleziona Dati Utente";
                String[] inputs = showInputDialog(opName, new String[]{
                        "ID utente (int)"
                });
                if (inputs != null) {
                    try {
                        int id = Integer.parseInt(inputs[0]);
                        java.sql.ResultSet rs = DatabaseManager.selezionaDatiUtente(id);
                        StringBuilder sb = new StringBuilder(opName + ":\n");
                        // Use ResultSetMetaData to display all columns
                        ResultSetMetaData md = rs.getMetaData();
                        int columnCount = md.getColumnCount();
                        if (rs.next()) {
                            for (int i = 1; i <= columnCount; i++) {
                                sb.append(md.getColumnLabel(i)).append(": ").append(rs.getObject(i)).append("\n");
                            }
                        } else {
                            sb.append("Utente non trovato.");
                        }
                        resultTextArea.setText(sb.toString());
                        rs.getStatement().getConnection().close();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 3: Add Public Work (Operation 3)
            e -> {
                String[] inputs = showInputDialog("Aggiungi Lavoro Pubblico", new String[]{
                        "Titolo", "Rating (G, T, M, E)", "Data pubblicazione (yyyy-mm-dd HH:mm:ss)",
                        "Numero capitoli (int)", "ID autore (int)", "Codice lingua",
                        "Contenuto primo capitolo"
                });
                if (inputs != null) {
                    try {
                        String titolo = inputs[0];
                        String rating = inputs[1];
                        Timestamp dataPub = Timestamp.valueOf(inputs[2]);
                        int numCap = Integer.parseInt(inputs[3]);
                        int idAutore = Integer.parseInt(inputs[4]);
                        String codiceLingua = inputs[5];
                        String contenuto = inputs[6];

                        int nuovoId = DatabaseManager.aggiungiLavoroPubblico(titolo, rating, dataPub, numCap, idAutore, codiceLingua, contenuto);
                        resultTextArea.setText("Lavoro pubblico aggiunto con successo!\nID generato: " + nuovoId);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 4: Add Work for Sale (Operation 4)
            e -> {
                String[] inputs = showInputDialog("Aggiungi Lavoro in Vendita", new String[]{
                        "Titolo", "Rating (G, T, M, E)", "Data pubblicazione (yyyy-mm-dd HH:mm:ss)",
                        "Numero capitoli (int)", "ID autore (int)", "Codice lingua",
                        "Prezzo di partenza (double)", "Data scadenza (yyyy-mm-dd HH:mm:ss)",
                        "Contenuto primo capitolo"
                });
                if (inputs != null) {
                    try {
                        String titolo = inputs[0];
                        String rating = inputs[1];
                        Timestamp dataPub = Timestamp.valueOf(inputs[2]);
                        int numCap = Integer.parseInt(inputs[3]);
                        int idAutore = Integer.parseInt(inputs[4]);
                        String codiceLingua = inputs[5];
                        double prezzoPartenza = Double.parseDouble(inputs[6]);
                        Timestamp scadenza = Timestamp.valueOf(inputs[7]);
                        String contenuto = inputs[7];


                        int nuovoId = DatabaseManager.aggiungiLavoroInVendita(titolo, rating, dataPub, numCap, idAutore, codiceLingua, prezzoPartenza, scadenza, contenuto);
                        resultTextArea.setText("Lavoro in vendita aggiunto con successo!\nID generato: " + nuovoId);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 5: Add Chapter (Operation 5)
            e -> {
                String[] inputs = showInputDialog("Aggiungi Capitolo", new String[]{
                        "ID lavoro (int)", "Numero capitolo (int)", "Data aggiornamento (yyyy-mm-dd HH:mm:ss)", "Contenuto"
                });
                if (inputs != null) {
                    try {
                        int idLavoro = Integer.parseInt(inputs[0]);
                        int numCapitolo = Integer.parseInt(inputs[1]);
                        Timestamp dataAgg = Timestamp.valueOf(inputs[2]);
                        String contenuto = inputs[3];

                        DatabaseManager.aggiungiCapitolo(idLavoro, numCapitolo, dataAgg, contenuto);
                        resultTextArea.setText("Capitolo aggiunto con successo!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 6: Modify Chapter (Operation 6)
            e -> {
                String[] inputs = showInputDialog("Modifica Capitolo", new String[]{
                        "ID lavoro (int)", "Numero capitolo (int)", "Nuovo contenuto"
                });
                if (inputs != null) {
                    try {
                        int idLavoro = Integer.parseInt(inputs[0]);
                        int numCapitolo = Integer.parseInt(inputs[1]);
                        String nuovoContenuto = inputs[2];

                        DatabaseManager.modificaCapitolo(idLavoro, numCapitolo, nuovoContenuto);
                        resultTextArea.setText("Capitolo modificato con successo!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 7: Add Tag to Work (Operation 7)
            e -> {
                String[] inputs = showInputDialog("Aggiungi Tag a Lavoro", new String[]{
                        "ID lavoro (int)", "ID tag (int)"
                });
                if (inputs != null) {
                    try {
                        int idLavoro = Integer.parseInt(inputs[0]);
                        int idTag = Integer.parseInt(inputs[1]);

                        DatabaseManager.aggiungiTagLavoro(idLavoro, idTag);
                        resultTextArea.setText("Tag aggiunto al lavoro con successo!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 8: Modify username (Operation 8)
            e -> {
                String[] inputs = showInputDialog("Modifica Username", new String[]{
                        "ID utente (int)", "Nuovo username"
                });
                if (inputs != null) {
                    try {
                        int idUtente = Integer.parseInt(inputs[0]);
                        String username = inputs[1];

                        DatabaseManager.modificaUsername(idUtente, username);
                        resultTextArea.setText("Username modificato con successo!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 9: Select Work and Increment Views (Operation 9)
            e -> {
                String[] inputs = showInputDialog("Seleziona Lavoro e Incrementa Visualizzazioni", new String[]{
                        "ID lavoro (int)"
                });
                if (inputs != null) {
                    try {
                        int idLavoro = Integer.parseInt(inputs[0]);
                        Map<String, Object> lavoro = DatabaseManager.selezionaLavoro(idLavoro);
                        if (lavoro.isEmpty()) {
                            resultTextArea.setText("Lavoro non trovato.");
                        } else {
                            StringBuilder msg = new StringBuilder();
                            lavoro.forEach((k, v) -> msg.append(k).append(": ").append(v).append("\n"));
                            resultTextArea.setText(msg.toString());
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 10: List Public Works (Operation 10)
            e -> {
                var opName = "Elenca lavori pubblici";
                try {
                    List<Map<String, Object>> lista = DatabaseManager.elencaLavoriPubblici();
                    StringBuilder msg = new StringBuilder(opName + ":\n");
                    if (lista.isEmpty()) {
                        msg.append("Nessun lavoro pubblico trovato.");
                    } else {
                        msg.append(formatList(lista));
                    }
                    resultTextArea.setText(msg.toString());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Listener 11: List Works for Sale (Operation 11)
            e -> {
                var opName = "Elenca lavori in vendita";
                try {
                    List<Map<String, Object>> lista = DatabaseManager.elencaLavoriInVendita();
                    StringBuilder msg = new StringBuilder(opName + ":\n");
                    if (lista.isEmpty()) {
                        msg.append("Nessun lavoro in vendita trovato.");
                    } else {
                        msg.append(formatList(lista));
                    }
                    resultTextArea.setText(msg.toString());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Listener 12: List Works Ordered by Chapter Count (Operation 12)
            e -> {
                var opName = "Elenca lavori ordinati per numero di capitoli";
                try {
                    List<Map<String, Object>> lista = DatabaseManager.elencaLavoriOrdinePerCapitoli();
                    StringBuilder msg = new StringBuilder(opName + ":\n");
                    if (lista.isEmpty()) {
                        msg.append("Nessun lavoro trovato.");
                    } else {
                        msg.append(formatList(lista));
                    }
                    resultTextArea.setText(msg.toString());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Listener 13: List Works by Language (Operation 13)
            e -> {
                var opName = "Elenca lavori in base alla lingua";
                String[] inputs = showInputDialog(opName, new String[]{
                        "Codice lingua"
                });
                if (inputs != null) {
                    try {
                        String codiceLingua = inputs[0];
                        List<Map<String, Object>> lista = DatabaseManager.elencaLavoriPerLingua(codiceLingua);
                        StringBuilder msg = new StringBuilder(opName + ":\n");
                        if (lista.isEmpty()) {
                            msg.append("Nessun lavoro trovato per la lingua " + codiceLingua);
                        } else {
                            msg.append(formatList(lista));
                        }
                        resultTextArea.setText(msg.toString());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 14: List Works Ordered by Publication Date (Operation 14)
            e -> {
                var opName = "Elenca lavori ordinati per data di pubblicazione";
                try {
                    List<Map<String, Object>> lista = DatabaseManager.elencaLavoriOrdinePerDataPubblicazione();
                    StringBuilder msg = new StringBuilder(opName + ":\n");
                    if (lista.isEmpty()) {
                        msg.append("Nessun lavoro trovato.");
                    } else {
                        msg.append(formatList(lista));
                    }
                    resultTextArea.setText(msg.toString());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Listener 15: List Works by Tag (Operation 15)
            e -> {
                var opName = "Elenca lavori in base al tag";
                String[] inputs = showInputDialog("Elenca Lavori per Tag", new String[]{
                        "Nome tag"
                });
                if (inputs != null) {
                    try {
                        String nomeTag = inputs[0];
                        List<Map<String, Object>> lista = DatabaseManager.elencaLavoriPerTag(nomeTag);
                        StringBuilder msg = new StringBuilder(opName + ":\n");
                        if (lista.isEmpty()) {
                            msg.append("Nessun lavoro trovato per il tag " + nomeTag);
                        } else {
                            msg.append(formatList(lista));
                        }
                        resultTextArea.setText(msg.toString());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 16: List Public Works Ordered by Views (Operation 16)
            e -> {
                var opName = "Elenca lavori ordinati in base alle visualizzazioni";
                try {
                    List<Map<String, Object>> lista = DatabaseManager.elencaLavoriPubbliciPerVisualizzazioni();
                    StringBuilder msg = new StringBuilder(opName + ":\n");
                    if (lista.isEmpty()) {
                        msg.append("Nessun lavoro pubblico trovato.");
                    } else {
                        msg.append(formatList(lista));
                    }
                    resultTextArea.setText(msg.toString());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Listener 17: Select Chapter Content (Operation 17)
            e -> {
                String[] inputs = showInputDialog("Seleziona Contenuto Capitolo", new String[]{
                        "ID lavoro (int)", "Numero capitolo (int)"
                });
                if (inputs != null) {
                    try {
                        int idLavoro = Integer.parseInt(inputs[0]);
                        int numCapitolo = Integer.parseInt(inputs[1]);
                        String contenuto = DatabaseManager.selezionaContenutoCapitolo(idLavoro, numCapitolo);
                        resultTextArea.setText("Selelziona Contenuto Capitolo:\n");
                        if (contenuto == null) {
                            resultTextArea.append("Capitolo non trovato.");
                        } else {
                            resultTextArea.append("Contenuto:\n" + contenuto);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 18: Add Comment or Reply (Operation 18)
            e -> {
                String[] inputs = showInputDialog("Aggiungi Commento", new String[]{
                        "Contenuto", "Data (yyyy-mm-dd HH:mm:ss)", "ID utente (int)", "ID lavoro (int)", "ID commentato (lascia vuoto se non risposta)"
                });
                if (inputs != null) {
                    try {
                        String contenuto = inputs[0];
                        Timestamp data = Timestamp.valueOf(inputs[1]);
                        int idUtente = Integer.parseInt(inputs[2]);
                        int idLavoro = Integer.parseInt(inputs[3]);
                        String idCommentatoStr = inputs[4];
                        if (idCommentatoStr == null || idCommentatoStr.trim().isEmpty()) {
                            int newCommentId = DatabaseManager.aggiungiCommento(contenuto, data, idUtente, idLavoro);
                            resultTextArea.setText("Commento aggiunto con successo!\nID generato: " + newCommentId);
                        } else {
                            int commentatoId = Integer.parseInt(idCommentatoStr);
                            int newCommentId = DatabaseManager.aggiungiCommentoRisposta(contenuto, data, idUtente, idLavoro, commentatoId);
                            resultTextArea.setText("Commento risposta aggiunto con successo!\nID generato: " + newCommentId);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 19: Add Like (Operation 19)
            e -> {
                String[] inputs = showInputDialog("Aggiungi Like", new String[]{
                        "ID utente (int)", "ID lavoro (int)"
                });
                if (inputs != null) {
                    try {
                        int idUtente = Integer.parseInt(inputs[0]);
                        int idLavoro = Integer.parseInt(inputs[1]);
                        DatabaseManager.aggiungiLike(idUtente, idLavoro);
                        resultTextArea.setText("Like aggiunto con successo!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 20: Make an Offer (Operation 20)
            e -> {
                String[] inputs = showInputDialog("Fai Offerta", new String[]{
                        "ID lavoro (int)", "ID offerta (int)", "Data (yyyy-mm-dd HH:mm:ss)", "Somma (double)", "ID utente (int)"
                });
                if (inputs != null) {
                    try {
                        int idLavoro = Integer.parseInt(inputs[0]);
                        int idOfferta = Integer.parseInt(inputs[1]);
                        Timestamp data = Timestamp.valueOf(inputs[2]);
                        double somma = Double.parseDouble(inputs[3]);
                        int idUtente = Integer.parseInt(inputs[4]);
                        DatabaseManager.faiOfferta(idLavoro, idOfferta, data, somma, idUtente);
                        resultTextArea.setText("Offerta effettuata con successo!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 21: Purchase Work (Make it Private) (Operation 21)
            e -> {
                String[] inputs = showInputDialog("Acquista Lavoro", new String[]{
                        "ID lavoro (int)", "Data fattura (yyyy-mm-dd HH:mm:ss)", "Prezzo (double)"
                });
                if (inputs != null) {
                    try {
                        int idLavoro = Integer.parseInt(inputs[0]);
                        Timestamp dataFattura = Timestamp.valueOf(inputs[1]);
                        double prezzo = Double.parseDouble(inputs[2]);
                        int fatturaNum = DatabaseManager.acquistaLavoro(idLavoro, dataFattura, prezzo);
                        resultTextArea.setText("Lavoro acquistato e reso privato con successo!\nNumero Fattura: " + fatturaNum);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 22: List Works of French Authors with at Least 10 Chapters (Operation 22)
            e -> {
                var opName = "Elenca tutti i lavori di autori francesi con almeno 10 capitoli";
                try {
                    List<Map<String, Object>> lista = DatabaseManager.selezionaLavoriAutoriFrancesi();
                    StringBuilder msg = new StringBuilder(opName + ":\n");
                    if (lista.isEmpty()) {
                        msg.append("Nessun lavoro trovato.");
                    } else {
                        msg.append(formatList(lista));
                    }
                    resultTextArea.setText(msg.toString());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Listener 23: List Comments that are Replies in French Works (Operation 23)
            e -> {
                var opName = "Elenca tutti i commenti in risposta ad un commento di tutti i lavori in francese";
                try {
                    List<Map<String, Object>> lista = DatabaseManager.selezionaCommentiRispostaLavoriFrancesi();
                    StringBuilder msg = new StringBuilder(opName + ":\n");
                    if (lista.isEmpty()) {
                        msg.append("Nessun commento trovato.");
                    } else {
                        msg.append(formatList(lista));
                    }
                    resultTextArea.setText(msg.toString());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Listener 24: List Works with at Least 100 Likes (Operation 24)
            e -> {
                var opName = "Elenca tutti i lavori che hanno almeno 100 like";
                try {
                    List<Map<String, Object>> lista = DatabaseManager.selezionaLavoriCon100Like();
                    StringBuilder msg = new StringBuilder(opName + ":\n");
                    if (lista.isEmpty()) {
                        msg.append("Nessun lavoro trovato.");
                    } else {
                        msg.append(formatList(lista));
                    }
                    resultTextArea.setText(msg.toString());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }


    };

    static private String[] tooltips = new String[]{
            "Aggiungere utente",
            "Selezionare dati utente",
            "Aggiungere lavoro pubblico",
            "Aggiungere lavoro in vendita",
            "Aggiungere capitolo",
            "Modificare capitolo",
            "Aggiungere tag ad un lavoro",
            "Modificare username utente",
            "Selezionare dati lavoro compreso il # capitoli",
            "Elencare lavori pubblici",
            "Elencare lavori in vendita",
            "Elencare lavori in base al # capitoli",
            "Elencare lavori in base alla lingua",
            "Elencare lavori in base alla data di pubblicazione",
            "Elencare lavori in base ad un tag",
            "Elencare lavori pubblici in base alle visualizzazioni",
            "Selezionare contenuto capitolo",
            "Aggiungere commento",
            "Aggiungere like",
            "Fare offerta",
            "Acquistare lavoro (rendere lavoro privato)",
            "Selezionare tutti i lavori di autori francesi con almeno 10 capitoli",
            "Selezionare tutti i commenti in risposta ad un commento di tutti i lavori in francese",
            "Selezionare tutti i lavori che hanno almeno 100 like"
    };

    private void initButtons() {
        operationButtons = new ArrayList<>();
        for (var i = 0; i < listeners.length; i++) {
            var button = new JButton("Operazione " + (i + 1));
            button.setToolTipText(tooltips[i]);
            var tempListener = listeners[i];
            button.addActionListener(e -> {
                tempListener.actionPerformed(e);
                resultTextArea.setCaretPosition(0);
            });
            operationButtons.add(button);
        }
    }
}
