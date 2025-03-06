package DAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import Model.Message;
import Util.ConnectionUtil;

/**
 * This class is responsible for handling database operations for Message objects.
 */

public class MessageDao implements BaseDao<Message> {

    // Create a Logger instance for this class.
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDao.class);

    /**
     * Handle SQLExceptions by logging the error details and throwing a DaoException
     *
     * @param e            The SQLException that was caught.
     * @param sql          The SQL query that was being executed when the exception
     *                     occurred.
     * @param errorMessage The error message to include in the DaoException.
     */
    private void handleSQLException(SQLException e, String sql, String errorMessage) {
        LOGGER.error("SQLException Details: {}", e.getMessage());
        LOGGER.error("SQL State: {}", e.getSQLState());
        LOGGER.error("Error Code: {}", e.getErrorCode());
        LOGGER.error("SQL: {}", sql);
        throw new DaoException(errorMessage, e);
    }

    /**
     * Retrieve a message by its ID from the database
     *
     * @param id The ID of the message to retrieve.
     * @return An Optional containing the retrieved message, or an empty Optional if
     *         the message was not found.
     */
    @Override
    public Optional<Message> getById(int id) {
        // The SQL string is outside the try block as it doesn't require closure like
        // Connection, PreparedStatement, or ResultSet.
        String sql = "SELECT * FROM message WHERE message_id = ?";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            // ResultSet is in a separate try block to ensure it gets closed after use,
            // even if an exception is thrown during data processing.
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMessage(rs));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while retrieving the message with id: " + id);
        }
        return Optional.empty();
    }

    /**
     * Retrieve all messages from the database
     *
     * @return A List of all messages in the database.
     */
    @Override
    public List<Message> getAll() {
        String sql = "SELECT * FROM message";
        Connection conn = ConnectionUtil.getConnection();
        List<Message> messages = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultSetToMessage(rs));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while retrieving all messages");
        }
        return messages;
    }

    /**
     * Retrieve all messages posted by a specific account
     *
     * @param accountId The ID of the account
     * @return List of Messages posted by the account
     */
    public List<Message> getMessagesByAccountId(int accountId) {
        String sql = "SELECT * FROM message WHERE posted_by = ?";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSetToList(rs);
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while retrieving a message by account ID: " + accountId);
        }
        return new ArrayList<>();
    }

    /**
     * Insert a new message into the database
     *
     * @param message The message to insert.
     * @return The inserted message with its ID set.
     */
    @Override
    public Message insert(Message message) {
        String sql = "INSERT INTO message(posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
        Connection conn = ConnectionUtil.getConnection();

        // The try-with-resources block ensures that the PreparedStatement is closed
        // after the block is executed
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, message.getPosted_by());
            ps.setString(2, message.getMessage_text());
            ps.setLong(3, message.getTime_posted_epoch());

            ps.executeUpdate();

            // After executing the query, we can retrieve the generated keys using
            // getGeneratedKeys() method of the PreparedStatement object.
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {

                // The ResultSet generatedKeys contains the auto-generated key(s) returned by the
                // database. In this case, we expect only one key (the ID of the inserted
                // message).
                if (generatedKeys.next()) {

                    // Retrieve the generated ID
                    int generatedId = generatedKeys.getInt(1);

                    // Create a new Message object with the generated ID and other attributes
                    return new Message(generatedId, message.getPosted_by(), message.getMessage_text(),
                            message.getTime_posted_epoch());
                } else {
                    throw new DaoException("Failed to insert message, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while inserting a message");
        }
        throw new DaoException("Failed to insert message");
    }

    /**
     * Update a message in the database
     *
     * @param message The message to update.
     * @return true if the update was successful; false if the message was not found
     *         in the database.
     */
    @Override
    public boolean update(Message message) {
        String sql = "UPDATE message SET posted_by = ?, message_text = ?, time_posted_epoch = ? WHERE message_id = ?";
        int rowsUpdated = 0;
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getPosted_by());
            ps.setString(2, message.getMessage_text());
            ps.setLong(3, message.getTime_posted_epoch());
            ps.setInt(4, message.getMessage_id());
            rowsUpdated = ps.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while updating the message with id: " + message.getMessage_id());
        }
        return rowsUpdated > 0;
    }

    /**
     * Delete a message from the database
     *
     * @param message The message to delete.
     * @return true if the deletion was successful; false if the message was not
     *         found in the database.
     */
    @Override
    public boolean delete(Message message) {
        String sql = "DELETE FROM message WHERE message_id = ?";
        int rowsUpdated = 0;
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getMessage_id());
            rowsUpdated = ps.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while deleting the message with id: " + message.getMessage_id());
        }
        return rowsUpdated > 0;
    }

    /**
     * Transforms a ResultSet into a Message object.
     * This helper method allows for the convenient transformation of data returned
     * from a SQL query into
     * a Java-friendly Message object. It assumes that the ResultSet contains a row
     * where each column represents a Message attribute.
     *
     * @param rs The ResultSet from a SQL query that needs to be transformed into a
     *           Message object.
     * @return A Message object that represents the data from the ResultSet.
     * @throws SQLException If a database access error occurs or this method is
     *                      called on a closed ResultSet.
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        int messageId = rs.getInt("message_id");
        int postedBy = rs.getInt("posted_by");
        String messageText = rs.getString("message_text");
        long timePostedEpoch = rs.getLong("time_posted_epoch");
        return new Message(messageId, postedBy, messageText, timePostedEpoch);
    }

    /**
     * Transforms a ResultSet into a List of Message objects.
     * This helper method allows for the convenient transformation of data returned
     * from a SQL query into
     * a Java-friendly List of Message objects. It assumes that the ResultSet
     * contains multiple rows where each column represents a Message attribute.
     *
     * @param rs The ResultSet from a SQL query that needs to be transformed into a
     *           List of Message objects.
     * @return A List of Message objects that represents the data from the ResultSet.
     * @throws SQLException If a database access error occurs or this method is
     *                      called on a closed ResultSet.
     */
    private List<Message> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Message> messages = new ArrayList<>();
        while (rs.next()) {
            messages.add(mapResultSetToMessage(rs));
        }
        return messages;
    }
}