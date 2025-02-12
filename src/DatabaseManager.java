import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;


    static {
        // Carica le proprietà dal file db.properties
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("Impossibile trovare il file db.properties");
            }
            Properties prop = new Properties();
            prop.load(input);
            URL = prop.getProperty("db.url");
            USER = prop.getProperty("db.user");
            PASSWORD = prop.getProperty("db.password");
        } catch (IOException ex) {
            throw new RuntimeException("Errore nel caricamento del file db.properties", ex);
        }

        // Carica il driver JDBC
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Errore nel caricamento del driver JDBC", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ****************************
    // Operazione 1: Aggiungere utente
    // ****************************
    public static Long aggiungiUtente(String nome, String cognome, String email, String password,
                                      String via, String numero, String citta, String paese, String codicePostale)
            throws SQLException {
        String query = "INSERT INTO Utente(nome, cognome, email, passwordH, via, numero, citta, paese, codice_postale) " +
                "VALUES (?, ?, ?, ? , ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            stmt.setString(1, nome);
            stmt.setString(2, cognome);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.setString(5, via);
            stmt.setString(6, numero);
            stmt.setString(7, citta);
            stmt.setString(8, paese);
            stmt.setString(9, codicePostale);
            stmt.executeUpdate();

            var genKeys = stmt.getGeneratedKeys();
            genKeys.next();
            return genKeys.getLong(1);
        }
    }

    // ============================
    // Operazione 2: Selezionare dati utente
    // ============================
    public static ResultSet selezionaDatiUtente(int id) throws SQLException {
        String query = "SELECT * FROM Utente WHERE ID = ?;";
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, id);
        return stmt.executeQuery();
    }

    // ============================
    // Operazione 3: Aggiungere lavoro pubblico
    // Inserisce in Lavoro e poi in Pubblico; restituisce l'ID del nuovo lavoro.
    // ============================
    public static int aggiungiLavoroPubblico(String titolo, String rating, Timestamp dataPubblicazione,
                                             int numeroCapitoli, int utenteId, String codiceLingua, String contenuto)
            throws SQLException {
        String queryLavoro = "INSERT INTO Lavoro(titolo, rating, dataPubblicazione, numeroCapitoli, utente_ID, codiceLingua) " +
                "VALUES (?, ?, ?, ?, ?, ?);";
        String queryPubblico = "INSERT INTO Pubblico(lavoro_ID, visualizzazioni) VALUES (?, 0);";
        String queryCapitolo = "INSERT INTO Capitolo(lavoro_ID, numeroCapitolo, dataAggiornamento, contenuto) VALUES (?, ?, ?, ?);";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            int idLavoro;
            try (PreparedStatement stmtLavoro = conn.prepareStatement(queryLavoro, Statement.RETURN_GENERATED_KEYS)) {
                stmtLavoro.setString(1, titolo);
                stmtLavoro.setString(2, rating); // rating atteso: 'G','T','M','E'
                stmtLavoro.setTimestamp(3, dataPubblicazione);
                stmtLavoro.setInt(4, numeroCapitoli);
                stmtLavoro.setInt(5, utenteId);
                stmtLavoro.setString(6, codiceLingua);
                stmtLavoro.executeUpdate();
                try (ResultSet rs = stmtLavoro.getGeneratedKeys()) {
                    if (rs.next()) {
                        idLavoro = rs.getInt(1);
                    } else {
                        throw new SQLException("Impossibile recuperare l'ID del lavoro.");
                    }
                }
            }
            try (PreparedStatement stmtPub = conn.prepareStatement(queryPubblico)) {
                stmtPub.setInt(1, idLavoro);
                stmtPub.executeUpdate();
            }
            try (PreparedStatement stmtCap = conn.prepareStatement(queryCapitolo)) {
                stmtCap.setInt(1, idLavoro);
                stmtCap.setInt(2, 1);
                stmtCap.setTimestamp(3, dataPubblicazione);
                stmtCap.setString(4, contenuto);
                stmtCap.executeUpdate();
            }
            conn.commit();
            conn.setAutoCommit(true);
            return idLavoro;
        }
    }

    // ============================
    // Operazione 4: Aggiungere lavoro in vendita
    // Inserisce in Lavoro e poi in InVendita; restituisce l'ID del nuovo lavoro.
    // ============================
    public static int aggiungiLavoroInVendita(String titolo, String rating, Timestamp dataPubblicazione,
                                              int numeroCapitoli, int utenteId, String codiceLingua,
                                              double prezzoDiPartenza, Timestamp scadenza, String contenuto)
            throws SQLException {
        String queryLavoro = "INSERT INTO Lavoro(titolo, rating, dataPubblicazione, numeroCapitoli, utente_ID, codiceLingua) " +
                "VALUES (?, ?, ?, ?, ?, ?);";
        String queryInVendita = "INSERT INTO InVendita(lavoro_ID, prezzoDiPartenza, scadenza) VALUES (?, ?, ?);";
        String queryCapitolo = "INSERT INTO Capitolo(lavoro_ID, numeroCapitolo, dataAggiornamento, contenuto) VALUES (?, ?, ?, ?);";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            int idLavoro;
            try (PreparedStatement stmtLavoro = conn.prepareStatement(queryLavoro, Statement.RETURN_GENERATED_KEYS)) {
                stmtLavoro.setString(1, titolo);
                stmtLavoro.setString(2, rating);
                stmtLavoro.setTimestamp(3, dataPubblicazione);
                stmtLavoro.setInt(4, numeroCapitoli);
                stmtLavoro.setInt(5, utenteId);
                stmtLavoro.setString(6, codiceLingua);
                stmtLavoro.executeUpdate();
                try (ResultSet rs = stmtLavoro.getGeneratedKeys()) {
                    if (rs.next()) {
                        idLavoro = rs.getInt(1);
                    } else {
                        throw new SQLException("Impossibile recuperare l'ID del lavoro.");
                    }
                }
            }
            try (PreparedStatement stmtVendita = conn.prepareStatement(queryInVendita)) {
                stmtVendita.setInt(1, idLavoro);
                stmtVendita.setDouble(2, prezzoDiPartenza);
                stmtVendita.setTimestamp(3, scadenza);
                stmtVendita.executeUpdate();
            }
            try (PreparedStatement stmtCap = conn.prepareStatement(queryCapitolo)) {
                stmtCap.setInt(1, idLavoro);
                stmtCap.setInt(2, 1);
                stmtCap.setTimestamp(3, dataPubblicazione);
                stmtCap.setString(4, contenuto);
                stmtCap.executeUpdate();
            }
            conn.commit();
            conn.setAutoCommit(true);
            return idLavoro;
        }
    }

    // ============================
    // Operazione 5: Aggiungere capitolo
    // ============================
    public static void aggiungiCapitolo(int lavoroId, int numeroCapitolo, Timestamp dataAggiornamento, String contenuto)
            throws SQLException {
        String query = "INSERT INTO Capitolo(lavoro_ID, numeroCapitolo, dataAggiornamento, contenuto) VALUES (?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, lavoroId);
            stmt.setInt(2, numeroCapitolo);
            stmt.setTimestamp(3, dataAggiornamento);
            stmt.setString(4, contenuto);
            stmt.executeUpdate();
        }
    }

    // ============================
    // Operazione 6: Modificare capitolo
    // ============================
    public static void modificaCapitolo(int lavoroId, int numeroCapitolo, String contenuto)
            throws SQLException {
        String query = "UPDATE Capitolo SET dataAggiornamento = CURRENT_TIMESTAMP(), contenuto = ? WHERE lavoro_ID = ? AND numeroCapitolo = ?;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, contenuto);
            stmt.setInt(2, lavoroId);
            stmt.setInt(3, numeroCapitolo);
            stmt.executeUpdate();
        }
    }

    // ============================
    // Operazione 7: Aggiungere tag ad un lavoro
    // ============================
    public static void aggiungiTagLavoro(int lavoroId, int tagId) throws SQLException {
        String query = "INSERT INTO ClassificatoDa(lavoro_ID, tag_ID) VALUES (?, ?);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, lavoroId);
            stmt.setInt(2, tagId);
            stmt.executeUpdate();
        }
    }

    // ============================
    // Operazione 8: Modifica username
    // ============================
    public static void modificaUsername(int utenteId, String username) throws SQLException {
        String query = "UPDATE Utente SET username = ? WHERE ID = ?;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, utenteId);
            stmt.executeUpdate();
        }
    }

    // ============================
    // Operazione 9: Selezionare dati lavoro e incrementare visualizzazioni (nella tabella Pubblico)
    // ============================
    public static Map<String, Object> selezionaLavoro(int idLavoro) throws SQLException {
        Map<String, Object> lavoro = new HashMap<>();
        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM Lavoro WHERE ID = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, idLavoro);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        lavoro.put("ID", rs.getInt("ID"));
                        lavoro.put("titolo", rs.getString("titolo"));
                        lavoro.put("rating", rs.getString("rating"));
                        lavoro.put("dataPubblicazione", rs.getTimestamp("dataPubblicazione"));
                        lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                        lavoro.put("utente_ID", rs.getInt("utente_ID"));
                        lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                    }
                }
            }
            String updateQuery = "UPDATE Pubblico SET visualizzazioni = visualizzazioni + 1 WHERE lavoro_ID = ?;";
            try (PreparedStatement stmtUpdate = conn.prepareStatement(updateQuery)) {
                stmtUpdate.setInt(1, idLavoro);
                stmtUpdate.executeUpdate();
            }
        }
        return lavoro;
    }

    // ============================
    // Operazione 10: Elencare lavori pubblici
    // ============================
    public static List<Map<String, Object>> elencaLavoriPubblici() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT * FROM Lavoro L INNER JOIN Pubblico P ON P.lavoro_ID = L.ID;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> lavoro = new HashMap<>();
                lavoro.put("ID", rs.getInt("ID"));
                lavoro.put("titolo", rs.getString("titolo"));
                lavoro.put("rating", rs.getString("rating"));
                lavoro.put("dataPubblicazione", rs.getTimestamp("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lavoro.put("visualizzazioni", rs.getString("visualizzazioni"));
                lista.add(lavoro);
            }
        }
        return lista;
    }

    // ============================
    // Operazione 11: Elencare lavori in vendita
    // ============================
    public static List<Map<String, Object>> elencaLavoriInVendita() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT * FROM Lavoro L INNER JOIN InVendita V ON V.lavoro_ID = L.ID;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> lavoro = new HashMap<>();
                lavoro.put("ID", rs.getInt("ID"));
                lavoro.put("titolo", rs.getString("titolo"));
                lavoro.put("rating", rs.getString("rating"));
                lavoro.put("dataPubblicazione", rs.getTimestamp("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lavoro.put("prezzoDiPartenza", rs.getDouble("prezzoDiPartenza"));
                lavoro.put("scadenza", rs.getTimestamp("scadenza"));
                lista.add(lavoro);
            }
        }
        return lista;
    }

    // ============================
    // Operazione 12: Elencare lavori in base al numero di capitoli
    // ============================
    public static List<Map<String, Object>> elencaLavoriOrdinePerCapitoli() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT * FROM Lavoro ORDER BY numeroCapitoli;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> lavoro = new HashMap<>();
                lavoro.put("ID", rs.getInt("ID"));
                lavoro.put("titolo", rs.getString("titolo"));
                lavoro.put("rating", rs.getString("rating"));
                lavoro.put("dataPubblicazione", rs.getTimestamp("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lista.add(lavoro);
            }
        }
        return lista;
    }

    // ============================
    // Operazione 13: Elencare lavori in base alla lingua
    // ============================
    public static List<Map<String, Object>> elencaLavoriPerLingua(String codiceLingua) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT * FROM Lavoro WHERE codiceLingua = ?;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, codiceLingua);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> lavoro = new HashMap<>();
                    lavoro.put("ID", rs.getInt("ID"));
                    lavoro.put("titolo", rs.getString("titolo"));
                    lavoro.put("rating", rs.getString("rating"));
                    lavoro.put("dataPubblicazione", rs.getTimestamp("dataPubblicazione"));
                    lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                    lavoro.put("utente_ID", rs.getInt("utente_ID"));
                    lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                    lista.add(lavoro);
                }
            }
        }
        return lista;
    }

    // ============================
    // Operazione 14: Elencare lavori in base alla data di pubblicazione
    // ============================
    public static List<Map<String, Object>> elencaLavoriOrdinePerDataPubblicazione() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT * FROM Lavoro ORDER BY dataPubblicazione;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> lavoro = new HashMap<>();
                lavoro.put("ID", rs.getInt("ID"));
                lavoro.put("titolo", rs.getString("titolo"));
                lavoro.put("rating", rs.getString("rating"));
                lavoro.put("dataPubblicazione", rs.getTimestamp("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lista.add(lavoro);
            }
        }
        return lista;
    }

    // ============================
    // Operazione 15: Elencare lavori in base ad un tag
    // ============================
    public static List<Map<String, Object>> elencaLavoriPerTag(String tagNome) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT L.* FROM Lavoro L " +
                "INNER JOIN ClassificatoDa C ON C.lavoro_ID = L.ID " +
                "INNER JOIN Tag T ON C.tag_ID = T.ID " +
                "WHERE T.nome = ?;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tagNome);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> lavoro = new HashMap<>();
                    lavoro.put("ID", rs.getInt("ID"));
                    lavoro.put("titolo", rs.getString("titolo"));
                    lavoro.put("rating", rs.getString("rating"));
                    lavoro.put("dataPubblicazione", rs.getTimestamp("dataPubblicazione"));
                    lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                    lavoro.put("utente_ID", rs.getInt("utente_ID"));
                    lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                    lista.add(lavoro);
                }
            }
        }
        return lista;
    }

    // ============================
    // Operazione 16: Elencare lavori pubblici in base alle visualizzazioni
    // ============================
    public static List<Map<String, Object>> elencaLavoriPubbliciPerVisualizzazioni() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT * FROM Lavoro L " +
                "INNER JOIN Pubblico P ON P.lavoro_ID = L.ID " +
                "ORDER BY P.visualizzazioni;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> lavoro = new HashMap<>();
                lavoro.put("ID", rs.getInt("ID"));
                lavoro.put("titolo", rs.getString("titolo"));
                lavoro.put("rating", rs.getString("rating"));
                lavoro.put("dataPubblicazione", rs.getTimestamp("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lavoro.put("visualizzazioni", rs.getInt("visualizzazioni"));
                lista.add(lavoro);
            }
        }
        return lista;
    }

    // ============================
    // Operazione 17: Selezionare contenuto capitolo
    // ============================
    public static String selezionaContenutoCapitolo(int lavoroId, int numeroCapitolo) throws SQLException {
        String contenuto = null;
        String query = "SELECT contenuto FROM Capitolo WHERE lavoro_ID = ? AND numeroCapitolo = ?;";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, lavoroId);
            stmt.setInt(2, numeroCapitolo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    contenuto = rs.getString("contenuto");
                }
            }
        }
        return contenuto;
    }

    // ============================
    // Operazione 18: Aggiungere commento
    // Se non si tratta di una risposta, restituisce l'ID generato.
    // ============================
    public static int aggiungiCommento(String contenuto, Timestamp dataCommento, int utenteId, int lavoroId)
            throws SQLException {
        String query = "INSERT INTO Commento(contenuto, dataCommento, utente_ID, lavoro_ID) VALUES (?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, contenuto);
            stmt.setTimestamp(2, dataCommento);
            stmt.setInt(3, utenteId);
            stmt.setInt(4, lavoroId);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Impossibile recuperare l'ID del commento.");
                }
            }
        }
    }

    // ============================
    // Operazione 18 (variante): Aggiungere commento risposta
    // Inserisce il commento e poi in Risponde; restituisce l'ID del commento generato.
    // ============================
    public static int aggiungiCommentoRisposta(String contenuto, Timestamp dataCommento, int utenteId, int lavoroId, int commentatoId)
            throws SQLException {
        String queryCommento = "INSERT INTO Commento(contenuto, dataCommento, utente_ID, lavoro_ID) VALUES (?, ?, ?, ?);";
        String queryRisponde = "INSERT INTO Risponde(commentatore_ID, commentato_ID) VALUES (?, ?);";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            int generatedCommentoId;
            try (PreparedStatement stmtCommento = conn.prepareStatement(queryCommento, Statement.RETURN_GENERATED_KEYS)) {
                stmtCommento.setString(1, contenuto);
                stmtCommento.setTimestamp(2, dataCommento);
                stmtCommento.setInt(3, utenteId);
                stmtCommento.setInt(4, lavoroId);
                stmtCommento.executeUpdate();
                try (ResultSet rs = stmtCommento.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedCommentoId = rs.getInt(1);
                    } else {
                        throw new SQLException("Impossibile recuperare l'ID del commento.");
                    }
                }
            }
            try (PreparedStatement stmtRisponde = conn.prepareStatement(queryRisponde)) {
                stmtRisponde.setInt(1, generatedCommentoId);
                stmtRisponde.setInt(2, commentatoId);
                stmtRisponde.executeUpdate();
            }
            conn.commit();
            conn.setAutoCommit(true);
            return generatedCommentoId;
        }
    }

    // ============================
    // Operazione 19: Aggiungere like
    // ============================
    public static void aggiungiLike(int utenteId, int lavoroId) throws SQLException {
        String query = "INSERT INTO MiPiace(utente_ID, lavoro_ID) VALUES (?, ?);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, utenteId);
            stmt.setInt(2, lavoroId);
            stmt.executeUpdate();
        }
    }

    // ============================
    // Operazione 20: Fare offerta
    // La tabella Offerta non utilizza AUTO_INCREMENT per l'ID dell'offerta, quindi non restituiamo un valore.
    // ============================
    public static void faiOfferta(int lavoroId, int offertaId, Timestamp dataOfferta, double somma, int utenteId)
            throws SQLException {
        String query = "INSERT INTO Offerta(lavoro_ID, ID, dataOfferta, somma, utente_ID) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, lavoroId);
            stmt.setInt(2, offertaId);
            stmt.setTimestamp(3, dataOfferta);
            stmt.setDouble(4, somma);
            stmt.setInt(5, utenteId);
            stmt.executeUpdate();
        }
    }

    // ============================
    // Operazione 21: Acquistare lavoro (rendere lavoro privato)
    // - Elimina da InVendita, inserisce in Fattura e Privato; restituisce il numero della fattura generato.
    // ============================
    public static int acquistaLavoro(int lavoroId, Timestamp dataFattura, double prezzo, int utenteId)
            throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            int numeroFattura;
            // Elimina da InVendita
            String deleteInVendita = "DELETE FROM InVendita WHERE lavoro_ID = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(deleteInVendita)) {
                stmt.setInt(1, lavoroId);
                stmt.executeUpdate();
            }
            // Inserisce in Fattura
            String insertFattura = "INSERT INTO Fattura(dataFattura, prezzo, utente_ID) VALUES (?, ?, ?);";
            try (PreparedStatement stmt = conn.prepareStatement(insertFattura, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setTimestamp(1, dataFattura);
                stmt.setDouble(2, prezzo);
                stmt.setInt(3, utenteId);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        numeroFattura = rs.getInt(1);
                    } else {
                        throw new SQLException("Impossibile recuperare il numero della fattura.");
                    }
                }
            }
            // Elimina eventuali offerte per il lavoro
            String deleteOfferta = "DELETE FROM Offerta WHERE lavoro_ID = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOfferta)) {
                stmt.setInt(1, lavoroId);
                stmt.executeUpdate();
            }
            // Inserisce in Privato
            String insertPrivato = "INSERT INTO Privato(lavoro_ID, numeroFattura) VALUES (?, ?);";
            try (PreparedStatement stmt = conn.prepareStatement(insertPrivato)) {
                stmt.setInt(1, lavoroId);
                stmt.setInt(2, numeroFattura);
                stmt.executeUpdate();
            }
            conn.commit();
            conn.setAutoCommit(true);
            return numeroFattura;
        }
    }

    // ============================
    // Operazione 22: Selezionare tutti i lavori di autori francesi con almeno 10 capitoli
    // ============================
    public static List<Map<String, Object>> selezionaLavoriAutoriFrancesi() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT L.* " +
                "FROM Lavoro L INNER JOIN Utente U ON L.utente_ID = U.ID " +
                "WHERE L.numeroCapitoli >= 10 AND U.paese = 'France';";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> lavoro = new HashMap<>();
                lavoro.put("ID", rs.getInt("ID"));
                lavoro.put("titolo", rs.getString("titolo"));
                lavoro.put("rating", rs.getString("rating"));
                lavoro.put("dataPubblicazione", rs.getTimestamp("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lista.add(lavoro);
            }
        }
        return lista;
    }

    // ============================
    // Operazione 23: Selezionare tutti i commenti in risposta ad un commento di tutti i lavori in francese
    // ============================
    public static List<Map<String, Object>> selezionaCommentiRispostaLavoriFrancesi() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT C.* " +
                "FROM Commento C INNER JOIN Risponde R ON C.ID = R.commentatore_ID " +
                "INNER JOIN Lavoro L ON C.lavoro_ID = L.ID " +
                "WHERE L.codiceLingua = 'FR';";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> commento = new HashMap<>();
                commento.put("ID", rs.getInt("ID"));
                commento.put("contenuto", rs.getString("contenuto"));
                commento.put("dataCommento", rs.getTimestamp("dataCommento"));
                commento.put("utente_ID", rs.getInt("utente_ID"));
                commento.put("lavoro_ID", rs.getInt("lavoro_ID"));
                lista.add(commento);
            }
        }
        return lista;
    }

    // ============================
    // Operazione 24: Selezionare tutti i lavori che hanno almeno 100 like
    // ============================
    public static List<Map<String, Object>> selezionaLavoriCon100Like() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT L.*, COUNT(*) as miPiace " +
                "FROM Lavoro L INNER JOIN MiPiace M ON L.ID = M.lavoro_ID " +
                "GROUP BY L.ID HAVING COUNT(*) >= 100;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> lavoro = new HashMap<>();
                lavoro.put("ID", rs.getInt("ID"));
                lavoro.put("titolo", rs.getString("titolo"));
                lavoro.put("rating", rs.getString("rating"));
                lavoro.put("dataPubblicazione", rs.getTimestamp("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lavoro.put("miPiace", rs.getInt("miPiace"));
                lista.add(lavoro);
            }
        }
        return lista;
    }

}
