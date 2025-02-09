import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

import java.sql.Date;

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

    // ****************************
    // Operazione 2: Selezionare dati utente
    // ****************************
    public static ResultSet selezionaDatiUtente(int id) throws SQLException {
        String query = "SELECT * FROM Utente WHERE ID = ?;";
        // NOTA: il caller dovrà chiudere il ResultSet, lo Statement e la Connection
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, id);
        return stmt.executeQuery();
    }

    // ****************************
    // Operazione 3: Aggiungere lavoro pubblico
    // ****************************
    public static void aggiungiLavoroPubblico(int id, String titolo, double rating, Date dataPubblicazione,
                                              int numeroCapitoli, int utenteId, String codiceLingua)
            throws SQLException {
        String queryLavoro = "INSERT INTO Lavoro(ID, titolo, rating, dataPubblicazione, numeroCapitoli, utente_ID, codiceLingua) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";
        String queryPubblico = "INSERT INTO Pubblico(lavoro_ID, visualizzazioni) VALUES (?, 0);";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtLavoro = conn.prepareStatement(queryLavoro);
                 PreparedStatement stmtPubblico = conn.prepareStatement(queryPubblico)) {
                stmtLavoro.setInt(1, id);
                stmtLavoro.setString(2, titolo);
                stmtLavoro.setDouble(3, rating);
                stmtLavoro.setDate(4, dataPubblicazione);
                stmtLavoro.setInt(5, numeroCapitoli);
                stmtLavoro.setInt(6, utenteId);
                stmtLavoro.setString(7, codiceLingua);
                stmtLavoro.executeUpdate();

                stmtPubblico.setInt(1, id);
                stmtPubblico.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ****************************
    // Operazione 4: Aggiungere lavoro in vendita
    // ****************************
    public static void aggiungiLavoroInVendita(int id, String titolo, double rating, Date dataPubblicazione,
                                               int numeroCapitoli, int utenteId, String codiceLingua,
                                               double prezzoDiPartenza, Date scadenza) throws SQLException {
        String queryLavoro = "INSERT INTO Lavoro(ID, titolo, rating, dataPubblicazione, numeroCapitoli, utente_ID, codiceLingua) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";
        String queryInVendita = "INSERT INTO InVendita(lavoro_ID, prezzoDiPartenza, scadenza) VALUES (?, ?, ?);";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtLavoro = conn.prepareStatement(queryLavoro);
                 PreparedStatement stmtInVendita = conn.prepareStatement(queryInVendita)) {
                stmtLavoro.setInt(1, id);
                stmtLavoro.setString(2, titolo);
                stmtLavoro.setDouble(3, rating);
                stmtLavoro.setDate(4, dataPubblicazione);
                stmtLavoro.setInt(5, numeroCapitoli);
                stmtLavoro.setInt(6, utenteId);
                stmtLavoro.setString(7, codiceLingua);
                stmtLavoro.executeUpdate();

                stmtInVendita.setInt(1, id);
                stmtInVendita.setDouble(2, prezzoDiPartenza);
                stmtInVendita.setDate(3, scadenza);
                stmtInVendita.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ****************************
    // Operazione 5: Aggiungere capitolo
    // ****************************
    public static void aggiungiCapitolo(int lavoroId, int numeroCapitolo, Date dataAggiornamento, String contenuto)
            throws SQLException {
        String query = "INSERT INTO Capitolo(lavoro_ID, numeroCapitolo, dataAggiornamento, contenuto) VALUES (?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, lavoroId);
            stmt.setInt(2, numeroCapitolo);
            stmt.setDate(3, dataAggiornamento);
            stmt.setString(4, contenuto);
            stmt.executeUpdate();
        }
    }

    // ****************************
    // Operazione 6: Modificare capitolo
    // ****************************
    public static void modificaCapitolo(int lavoroId, int numeroCapitolo, Date dataAggiornamento, String contenuto)
            throws SQLException {
        String query = "UPDATE Capitolo SET dataAggiornamento = ?, contenuto = ? WHERE numeroCapitolo = ? AND lavoro_ID = ?;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, dataAggiornamento);
            stmt.setString(2, contenuto);
            stmt.setInt(3, numeroCapitolo);
            stmt.setInt(4, lavoroId);
            stmt.executeUpdate();
        }
    }

    // ****************************
    // Operazione 7: Aggiungere tag ad un lavoro
    // ****************************
    public static void aggiungiTagLavoro(int lavoroId, int tagId) throws SQLException {
        String query = "INSERT INTO ClassificatoDa(lavoro_ID, tag_ID) VALUES (?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, lavoroId);
            stmt.setInt(2, tagId);
            stmt.executeUpdate();
        }
    }

    // ****************************
    // Operazione 8: Aggiungere alias
    // ****************************
    public static void aggiungiAlias(int utenteId, String alias) throws SQLException {
        String query = "INSERT INTO Alias(utente_ID, nome) VALUES (?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, utenteId);
            stmt.setString(2, alias);
            stmt.executeUpdate();
        }
    }

    // ****************************
    // Operazione 9: Selezionare dati lavoro (con # capitoli) e incrementare visualizzazioni
    // ****************************
    public static Map<String, Object> selezionaLavoro(int id) throws SQLException {
        Map<String, Object> lavoro = new HashMap<>();
        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM Lavoro WHERE ID = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        lavoro.put("ID", rs.getInt("ID"));
                        lavoro.put("titolo", rs.getString("titolo"));
                        lavoro.put("rating", rs.getDouble("rating"));
                        lavoro.put("dataPubblicazione", rs.getDate("dataPubblicazione"));
                        lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                        lavoro.put("utente_ID", rs.getInt("utente_ID"));
                        lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                    }
                }
            }
            // Incrementa le visualizzazioni (assumendo che il lavoro sia pubblico)
            String updateQuery = "UPDATE Lavoro SET visualizzazioni = visualizzazioni + 1 WHERE ID = ?;";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, id);
                updateStmt.executeUpdate();
            }
        }
        return lavoro;
    }

    // ****************************
    // Operazione 10: Elencare lavori pubblici
    // ****************************
    public static List<Map<String, Object>> elencaLavoriPubblici() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT L.* FROM Lavoro L INNER JOIN Pubblico P ON P.lavoro_ID = L.ID;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> lavoro = new HashMap<>();
                lavoro.put("ID", rs.getInt("ID"));
                lavoro.put("titolo", rs.getString("titolo"));
                lavoro.put("rating", rs.getDouble("rating"));
                lavoro.put("dataPubblicazione", rs.getDate("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lista.add(lavoro);
            }
        }
        return lista;
    }

    // ****************************
    // Operazione 11: Elencare lavori in vendita
    // ****************************
    public static List<Map<String, Object>> elencaLavoriInVendita() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT L.* FROM Lavoro L INNER JOIN InVendita V ON V.lavoro_ID = L.ID;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> lavoro = new HashMap<>();
                lavoro.put("ID", rs.getInt("ID"));
                lavoro.put("titolo", rs.getString("titolo"));
                lavoro.put("rating", rs.getDouble("rating"));
                lavoro.put("dataPubblicazione", rs.getDate("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lista.add(lavoro);
            }
        }
        return lista;
    }

    // ****************************
    // Operazione 12: Elencare lavori in base al numero di capitoli
    // ****************************
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
                lavoro.put("rating", rs.getDouble("rating"));
                lavoro.put("dataPubblicazione", rs.getDate("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lista.add(lavoro);
            }
        }
        return lista;
    }

    // ****************************
    // Operazione 13: Elencare lavori in base alla lingua
    // ****************************
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
                    lavoro.put("rating", rs.getDouble("rating"));
                    lavoro.put("dataPubblicazione", rs.getDate("dataPubblicazione"));
                    lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                    lavoro.put("utente_ID", rs.getInt("utente_ID"));
                    lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                    lista.add(lavoro);
                }
            }
        }
        return lista;
    }

    // ****************************
    // Operazione 14: Elencare lavori in base alla data di pubblicazione
    // ****************************
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
                lavoro.put("rating", rs.getDouble("rating"));
                lavoro.put("dataPubblicazione", rs.getDate("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lista.add(lavoro);
            }
        }
        return lista;
    }

    // ****************************
    // Operazione 15: Elencare lavori in base ad un tag
    // ****************************
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
                    lavoro.put("rating", rs.getDouble("rating"));
                    lavoro.put("dataPubblicazione", rs.getDate("dataPubblicazione"));
                    lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                    lavoro.put("utente_ID", rs.getInt("utente_ID"));
                    lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                    lista.add(lavoro);
                }
            }
        }
        return lista;
    }

    // ****************************
    // Operazione 16: Elencare lavori pubblici in base alle visualizzazioni
    // ****************************
    public static List<Map<String, Object>> elencaLavoriPubbliciPerVisualizzazioni() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT L.* FROM Lavoro L " +
                "INNER JOIN Pubblico P ON P.lavoro_ID = L.ID " +
                "ORDER BY P.visualizzazioni;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> lavoro = new HashMap<>();
                lavoro.put("ID", rs.getInt("ID"));
                lavoro.put("titolo", rs.getString("titolo"));
                lavoro.put("rating", rs.getDouble("rating"));
                lavoro.put("dataPubblicazione", rs.getDate("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lista.add(lavoro);
            }
        }
        return lista;
    }

    // ****************************
    // Operazione 17: Selezionare contenuto capitolo
    // ****************************
    public static String selezionaContenutoCapitolo(int lavoroId, int numeroCapitolo) throws SQLException {
        String contenuto = null;
        String query = "SELECT contenuto FROM Capitolo WHERE lavoro_ID = ? AND numeroCapitolo = ?;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
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

    // ****************************
    // Operazione 18: Aggiungere commento
    // (Versione 1: rispondere ad un lavoro)
    // ****************************
    public static void aggiungiCommento(int id, String contenuto, Timestamp data, int utenteId, int lavoroId)
            throws SQLException {
        String query = "INSERT INTO Commento(ID, contenuto, data, utente_ID, lavoro_ID) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.setString(2, contenuto);
            stmt.setTimestamp(3, data);
            stmt.setInt(4, utenteId);
            stmt.setInt(5, lavoroId);
            stmt.executeUpdate();
        }
    }

    // ****************************
    // Operazione 18 (variant): Aggiungere commento in risposta a un altro commento
    // ****************************
    public static void aggiungiCommentoRisposta(int id, String contenuto, Timestamp data, int utenteId,
                                                int lavoroId, int commentatoId) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtCommento = conn.prepareStatement(
                    "INSERT INTO Commento(ID, contenuto, data, utente_ID, lavoro_ID) VALUES (?, ?, ?, ?, ?);");
                 PreparedStatement stmtRisponde = conn.prepareStatement(
                         "INSERT INTO Risponde(commentatore_ID, commentato_ID) VALUES (?, ?);")) {
                stmtCommento.setInt(1, id);
                stmtCommento.setString(2, contenuto);
                stmtCommento.setTimestamp(3, data);
                stmtCommento.setInt(4, utenteId);
                stmtCommento.setInt(5, lavoroId);
                stmtCommento.executeUpdate();

                stmtRisponde.setInt(1, id);
                stmtRisponde.setInt(2, commentatoId);
                stmtRisponde.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ****************************
    // Operazione 19: Aggiungere like
    // ****************************
    public static void aggiungiLike(int utenteId, int lavoroId) throws SQLException {
        String query = "INSERT INTO MiPiace(utente_ID, lavoro_ID) VALUES (?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, utenteId);
            stmt.setInt(2, lavoroId);
            stmt.executeUpdate();
        }
    }

    // ****************************
    // Operazione 20: Fare offerta
    // ****************************
    public static void faiOfferta(int lavoroId, int offertaId, Timestamp data, double somma, int utenteId)
            throws SQLException {
        String query = "INSERT INTO Offerta(lavoro_ID, ID, data, somma, utente_ID) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, lavoroId);
            stmt.setInt(2, offertaId);
            stmt.setTimestamp(3, data);
            stmt.setDouble(4, somma);
            stmt.setInt(5, utenteId);
            stmt.executeUpdate();
        }
    }

    // ****************************
    // Operazione 21: Acquistare lavoro (rendere lavoro privato)
    // ****************************
    public static void acquistaLavoro(int lavoroId, int numeroFattura, Date data, String modalitaPagamento, double prezzo)
            throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Eliminare il lavoro dalla tabella InVendita
                String deleteInVendita = "DELETE FROM InVendita WHERE lavoro_ID = ?;";
                try (PreparedStatement stmt = conn.prepareStatement(deleteInVendita)) {
                    stmt.setInt(1, lavoroId);
                    stmt.executeUpdate();
                }

                // Inserire una fattura
                String insertFattura = "INSERT INTO Fattura(numeroFattura, data, modalitàPagamento, prezzo) VALUES (?, ?, ?, ?);";
                try (PreparedStatement stmt = conn.prepareStatement(insertFattura)) {
                    stmt.setInt(1, numeroFattura);
                    stmt.setDate(2, data);
                    stmt.setString(3, modalitaPagamento);
                    stmt.setDouble(4, prezzo);
                    stmt.executeUpdate();
                }

                // Eliminare tutte le offerte relative a questo lavoro
                String deleteOfferta = "DELETE FROM Offerta WHERE lavoro_ID = ?;";
                try (PreparedStatement stmt = conn.prepareStatement(deleteOfferta)) {
                    stmt.setInt(1, lavoroId);
                    stmt.executeUpdate();
                }

                // Rendere il lavoro privato inserendo nella tabella Privato
                String insertPrivato = "INSERT INTO Privato(lavoro_ID, numeroFattura) VALUES (?, ?);";
                try (PreparedStatement stmt = conn.prepareStatement(insertPrivato)) {
                    stmt.setInt(1, lavoroId);
                    stmt.setInt(2, numeroFattura);
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ****************************
    // Operazione 22: Selezionare tutti i lavori di autori francesi con almeno 10 capitoli
    // ****************************
    public static List<Map<String, Object>> selezionaLavoriAutoriFrancesi() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT L.ID, L.titolo, L.rating, L.dataPubblicazione, L.numeroCapitoli, L.utente_ID, L.codiceLingua " +
                "FROM Lavoro L INNER JOIN Utente U ON L.utente_ID = U.ID " +
                "WHERE L.numeroCapitoli >= 10 AND U.paese = 'France';";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> lavoro = new HashMap<>();
                lavoro.put("ID", rs.getInt("ID"));
                lavoro.put("titolo", rs.getString("titolo"));
                lavoro.put("rating", rs.getDouble("rating"));
                lavoro.put("dataPubblicazione", rs.getDate("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lista.add(lavoro);
            }
        }
        return lista;
    }

    // ****************************
    // Operazione 23: Selezionare tutti i commenti in risposta ad un commento di tutti i lavori in francese
    // ****************************
    public static List<Map<String, Object>> selezionaCommentiRispostaLavoriFrancesi() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT C.ID, C.contenuto, C.data, C.utente_ID, C.lavoro_ID " +
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
                commento.put("data", rs.getTimestamp("data"));
                commento.put("utente_ID", rs.getInt("utente_ID"));
                commento.put("lavoro_ID", rs.getInt("lavoro_ID"));
                lista.add(commento);
            }
        }
        return lista;
    }

    // ****************************
    // Operazione 24: Selezionare tutti i lavori che hanno almeno 100 like
    // ****************************
    public static List<Map<String, Object>> selezionaLavoriCon100Like() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String query = "SELECT L.ID, L.titolo, L.rating, L.dataPubblicazione, L.numeroCapitoli, L.utente_ID, L.codiceLingua " +
                "FROM Lavoro L INNER JOIN MiPiace M ON L.ID = M.lavoro_ID " +
                "GROUP BY L.ID HAVING COUNT(*) >= 100;";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> lavoro = new HashMap<>();
                lavoro.put("ID", rs.getInt("ID"));
                lavoro.put("titolo", rs.getString("titolo"));
                lavoro.put("rating", rs.getDouble("rating"));
                lavoro.put("dataPubblicazione", rs.getDate("dataPubblicazione"));
                lavoro.put("numeroCapitoli", rs.getInt("numeroCapitoli"));
                lavoro.put("utente_ID", rs.getInt("utente_ID"));
                lavoro.put("codiceLingua", rs.getString("codiceLingua"));
                lista.add(lavoro);
            }
        }
        return lista;
    }
}
