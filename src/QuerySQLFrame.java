import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuerySQLFrame extends JFrame {
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
        c.gridheight = listeners.length;
        c.fill = GridBagConstraints.BOTH;
        resultTextArea = new JTextArea(20, 50);
        resultTextArea.setEditable(false);
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);

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
            // Listener 1: Add User (Operation 1)
            e -> {
                String[] inputs = showInputDialog("Aggiungi Utente", new String[]{
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

                        resultTextArea.setText("Utente aggiunto con successo!\n" +
                                "ID nuovo utente = " + res);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage());
                    }
                }
            },
            // Listener 2: Select User Data (Operation 2)
            e -> {
                String[] inputs = showInputDialog("Seleziona Dati Utente", new String[]{
                        "ID utente (int)"
                });
                if (inputs != null) {
                    try {
                        int id = Integer.parseInt(inputs[0]);
                        java.sql.ResultSet rs = DatabaseManager.selezionaDatiUtente(id);
                        if (rs.next()) {
                            String dati = "ID: " + rs.getInt("ID")
                                    + "\nNome: " + rs.getString("nome")
                                    + "\nCognome: " + rs.getString("cognome")
                                    + "\nEmail: " + rs.getString("email");
                            resultTextArea.setText(dati);
                        } else {
                            resultTextArea.setText("Utente non trovato.");
                        }
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
                        "Numero capitoli (int)", "ID autore (int)", "Codice lingua"
                });
                if (inputs != null) {
                    try {
                        String titolo = inputs[0];
                        String rating = inputs[1];
                        Timestamp dataPub = Timestamp.valueOf(inputs[2]);
                        int numCap = Integer.parseInt(inputs[3]);
                        int idAutore = Integer.parseInt(inputs[4]);
                        String codiceLingua = inputs[5];

                        int nuovoId = DatabaseManager.aggiungiLavoroPubblico(titolo, rating, dataPub, numCap, idAutore, codiceLingua);
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
                        "Prezzo di partenza (double)", "Data scadenza (yyyy-mm-dd HH:mm:ss)"
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


                        int nuovoId = DatabaseManager.aggiungiLavoroInVendita(titolo, rating, dataPub, numCap, idAutore, codiceLingua, prezzoPartenza, scadenza);
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
            // Listener 8: Add Alias (Operation 8)
            e -> {
                String[] inputs = showInputDialog("Aggiungi Alias", new String[]{
                        "ID utente (int)", "Alias"
                });
                if (inputs != null) {
                    try {
                        int idUtente = Integer.parseInt(inputs[0]);
                        String alias = inputs[1];

                        DatabaseManager.aggiungiAlias(idUtente, alias);
                        resultTextArea.setText("Alias aggiunto con successo!");
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
                            // TODO rivedi formato
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
                try {
                    List<Map<String, Object>> lista = DatabaseManager.elencaLavoriPubblici();
                    if (lista.isEmpty()) {
                        resultTextArea.setText("Nessun lavoro pubblico trovato.");
                    } else {
                        StringBuilder msg = new StringBuilder();
                        lista.forEach(lav -> msg.append("ID: ").append(lav.get("ID"))
                                .append(", Titolo: ").append(lav.get("titolo")).append("\n"));
                        resultTextArea.setText(msg.toString());
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Listener 11: List Works for Sale (Operation 11)
            e -> {
                try {
                    List<Map<String, Object>> lista = DatabaseManager.elencaLavoriInVendita();
                    if (lista.isEmpty()) {
                        resultTextArea.setText("Nessun lavoro in vendita trovato.");
                    } else {
                        StringBuilder msg = new StringBuilder();
                        lista.forEach(lav -> msg.append("ID: ").append(lav.get("ID"))
                                .append(", Titolo: ").append(lav.get("titolo")).append("\n"));
                        resultTextArea.setText(msg.toString());
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Listener 12: List Works Ordered by Chapter Count (Operation 12)
            e -> {
                try {
                    List<Map<String, Object>> lista = DatabaseManager.elencaLavoriOrdinePerCapitoli();
                    if (lista.isEmpty()) {
                        resultTextArea.setText("Nessun lavoro trovato.");
                    } else {
                        StringBuilder msg = new StringBuilder();
                        lista.forEach(lav -> msg.append("ID: ").append(lav.get("ID"))
                                .append(", Titolo: ").append(lav.get("titolo"))
                                .append(", Capitoli: ").append(lav.get("numeroCapitoli")).append("\n"));
                        resultTextArea.setText(msg.toString());
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Listener 13: List Works by Language (Operation 13)
            e -> {
                String[] inputs = showInputDialog("Elenca Lavori per Lingua", new String[]{
                        "Codice lingua"
                });
                if (inputs != null) {
                    try {
                        String codiceLingua = inputs[0];
                        List<Map<String, Object>> lista = DatabaseManager.elencaLavoriPerLingua(codiceLingua);
                        if (lista.isEmpty()) {
                            resultTextArea.setText("Nessun lavoro trovato per la lingua " + codiceLingua);
                        } else {
                            StringBuilder msg = new StringBuilder();
                            lista.forEach(lav -> msg.append("ID: ").append(lav.get("ID"))
                                    .append(", Titolo: ").append(lav.get("titolo")).append("\n"));
                            resultTextArea.setText(msg.toString());
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 14: List Works Ordered by Publication Date (Operation 14)
            e -> {
                try {
                    List<Map<String, Object>> lista = DatabaseManager.elencaLavoriOrdinePerDataPubblicazione();
                    if (lista.isEmpty()) {
                        resultTextArea.setText("Nessun lavoro trovato.");
                    } else {
                        StringBuilder msg = new StringBuilder();
                        lista.forEach(lav -> msg.append("ID: ").append(lav.get("ID"))
                                .append(", Titolo: ").append(lav.get("titolo"))
                                .append(", Data: ").append(lav.get("dataPubblicazione")).append("\n"));
                        resultTextArea.setText(msg.toString());
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Listener 15: List Works by Tag (Operation 15)
            e -> {
                String[] inputs = showInputDialog("Elenca Lavori per Tag", new String[]{
                        "Nome tag"
                });
                if (inputs != null) {
                    try {
                        String nomeTag = inputs[0];
                        List<Map<String, Object>> lista = DatabaseManager.elencaLavoriPerTag(nomeTag);
                        if (lista.isEmpty()) {
                            resultTextArea.setText("Nessun lavoro trovato per il tag " + nomeTag);
                        } else {
                            StringBuilder msg = new StringBuilder();
                            lista.forEach(lav -> msg.append("ID: ").append(lav.get("ID"))
                                    .append(", Titolo: ").append(lav.get("titolo")).append("\n"));
                            resultTextArea.setText(msg.toString());
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 16: List Public Works Ordered by Views (Operation 16)
            e -> {
                try {
                    List<Map<String, Object>> lista = DatabaseManager.elencaLavoriPubbliciPerVisualizzazioni();
                    if (lista.isEmpty()) {
                        resultTextArea.setText("Nessun lavoro pubblico trovato.");
                    } else {
                        StringBuilder msg = new StringBuilder();
                        lista.forEach(lav -> msg.append("ID: ").append(lav.get("ID"))
                                .append(", Titolo: ").append(lav.get("titolo")).append("\n"));
                        resultTextArea.setText(msg.toString());
                    }
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
                        if (contenuto == null) {
                            resultTextArea.setText("Capitolo non trovato.");
                        } else {
                            resultTextArea.setText("Contenuto:\n" + contenuto);
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
                        "ID lavoro (int)", "Data fattura (yyyy-mm-dd HH:mm:ss)", "Modalità pagamento", "Prezzo (double)"
                });
                if (inputs != null) {
                    try {
                        int idLavoro = Integer.parseInt(inputs[0]);
                        Timestamp dataFattura = Timestamp.valueOf(inputs[1]);
                        String modalita = inputs[2];
                        double prezzo = Double.parseDouble(inputs[3]);
                        int fatturaNum = DatabaseManager.acquistaLavoro(idLavoro, dataFattura, modalita, prezzo);
                        resultTextArea.setText("Lavoro acquistato e reso privato con successo!\nNumero Fattura: " + fatturaNum);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            },
            // Listener 22: List Works of French Authors with at Least 10 Chapters (Operation 22)
            e -> {
                try {
                    List<Map<String, Object>> lista = DatabaseManager.selezionaLavoriAutoriFrancesi();
                    if (lista.isEmpty()) {
                        resultTextArea.setText("Nessun lavoro trovato.");
                    } else {
                        StringBuilder msg = new StringBuilder();
                        lista.forEach(lav -> msg.append("ID: ").append(lav.get("ID"))
                                .append(", Titolo: ").append(lav.get("titolo")).append("\n"));
                        resultTextArea.setText(msg.toString());
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Listener 23: List Comments that are Replies in French Works (Operation 23)
            e -> {
                try {
                    List<Map<String, Object>> lista = DatabaseManager.selezionaCommentiRispostaLavoriFrancesi();
                    if (lista.isEmpty()) {
                        resultTextArea.setText("Nessun commento trovato.");
                    } else {
                        StringBuilder msg = new StringBuilder();
                        lista.forEach(com -> msg.append("ID: ").append(com.get("ID"))
                                .append(", Contenuto: ").append(com.get("contenuto")).append("\n"));
                        resultTextArea.setText(msg.toString());
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Listener 24: List Works with at Least 100 Likes (Operation 24)
            e -> {
                try {
                    List<Map<String, Object>> lista = DatabaseManager.selezionaLavoriCon100Like();
                    if (lista.isEmpty()) {
                        resultTextArea.setText("Nessun lavoro trovato.");
                    } else {
                        StringBuilder msg = new StringBuilder();
                        lista.forEach(lav -> msg.append("ID: ").append(lav.get("ID"))
                                .append(", Titolo: ").append(lav.get("titolo")).append("\n"));
                        resultTextArea.setText(msg.toString());
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore: " + ex.getMessage(),
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }


    };

    private void initButtons() {
        operationButtons = new ArrayList<>();
        for (var i = 0; i < listeners.length; i++) {
            var button = new JButton("Operazione " + (i + 1));
            button.addActionListener(listeners[i]);
            operationButtons.add(button);
        }
    }
}
