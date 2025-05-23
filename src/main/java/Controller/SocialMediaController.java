package Controller;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import Service.ServiceException;

import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * TODO: You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */
public class SocialMediaController {
    /**
     * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */

    private final AccountService accountService;
    private final MessageService messageService;

    public SocialMediaController() {
        // Initialize the accountService and messageService instances
        this.accountService = new AccountService();
        this.messageService = new MessageService();
    }

    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.get("example-endpoint", this::exampleHandler);
        app.post("/register", this::registerAccount);
        app.post("/login", this::loginAccount);
        app.post("/messages", this::createMessage);
        app.get("/messages", this::getAllMessages);
        app.get("/messages/{message_id}", this::getMessageById);
        app.delete("/messages/{message_id}", this::deleteMessageById);
        app.patch("/messages/{message_id}", this::updateMessageById);
        app.get("/accounts/{account_id}/messages",
                this::getMessagesByAccountId);

        return app;
    }

    /**
     * This is an example handler for an example endpoint.
     * @param context The Javalin Context object manages information about both the HTTP request and response.
     */
    private void exampleHandler(Context context) {
        context.json("sample text");
    }

    /**
     * This method handles the registration of new accounts.
     * It expects a POST request to "/register" with the account details in the
     * request body.
     *
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response
     * @throws JsonProcessingException if an error occurs during JSON parsing or
     *                                 serialization
     */
    private void registerAccount(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = mapper.readValue(ctx.body(), Account.class);
        try {
            Account registeredAccount = accountService.createAccount(account);

            // Send the registered account as a JSON response
            ctx.json(mapper.writeValueAsString(registeredAccount));
        } catch (ServiceException e) {
            // Set the response status to 400 (Bad Request) in case of exception
            ctx.status(400);
        }
    }

    /**
     * This method handles the login of an account.
     * It expects a POST request to "/login" with the account details in the request
     * body.
     *
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response
     * @throws JsonProcessingException if an error occurs during JSON parsing or
     *                                 serialization
     */
    private void loginAccount(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(); // it calls a default no-arg constructor from Model.Account - REQUIRED
                                                  // for Jackson ObjectMapper
        Account account = mapper.readValue(ctx.body(), Account.class);

        try {
            Optional<Account> loggedInAccount = accountService
                    .validateLogin(account);
            if (loggedInAccount.isPresent()) {
                // Send the logged-in account as a JSON response
                ctx.json(mapper.writeValueAsString(loggedInAccount));
                ctx.sessionAttribute("logged_in_account",
                        loggedInAccount.get());
                ctx.json(loggedInAccount.get());
            } else {
                // Set the response status to 401 (Unauthorized) if the account is not found
                ctx.status(401);
            }
        } catch (ServiceException e) {
            // Set the response status to 401 (Unauthorized) in case of exception
            ctx.status(401);
        }
    }

    /**
     * This method handles the creation of a new message.
     * It expects a POST request to "/messages" with the message details in the
     * request body.
     *
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response
     * @throws JsonProcessingException if an error occurs during JSON parsing or
     *                                 serialization
     */
    private void createMessage(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message mappedMessage = mapper.readValue(ctx.body(), Message.class);
        try {
            Optional<Account> account = accountService
                    .getAccountById(mappedMessage.getPosted_by());
            Message message = messageService.createMessage(mappedMessage,
                    account);
            ctx.json(message);
        } catch (ServiceException e) {
            // Set the response status to 400 (Bad Request) in case of exception
            ctx.status(400);
        }
    }

    /**
     * This method handles the retrieval of all messages.
     * It expects a GET request to "/messages".
     *
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response
     */
    private void getAllMessages(Context ctx) {

        List<Message> messages = messageService.getAllMessages();
        ctx.json(messages);
    }

    /**
     * This method retrieves a specific message by its ID.
     * It expects a GET request to "/messages/{message_id}".
     *
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response
     */

    private void getMessageById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
                ctx.json(message.get()); // Respond with the message as a JSON object.
            } else {
                ctx.status(200); // Respond with a '200' status even if the message was not found.
                ctx.result(""); // Response body is empty if the message was not found.
            }
            // Handle NumberFormatException for invalid 'message_id'.
        } catch (NumberFormatException e) {
            ctx.status(400); // Respond with a '400' status for a bad request.
        } catch (ServiceException e) {
            ctx.status(200); // Respond with a '200' status even if there was a service error.
            ctx.result(""); // Response body is empty if there was a service error.
        }
    }

    /**
     * This method handles the deletion of a specific message by its ID.
     * It expects a DELETE request to "/messages/{message_id}".
     *
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response
     */
    private void deleteMessageById(Context ctx) {
        try {
            // Retrieve the message ID from the path parameter
            int id = Integer.parseInt(ctx.pathParam("message_id"));

            // Attempt to retrieve the message by its ID
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
                // The message exists, so delete it
                messageService.deleteMessage(message.get());
                ctx.status(200);
                // Include the deleted message in the response body
                ctx.json(message.get());
            } else {
                // The message does not exist
                // Set the response status to 200 (OK) to indicate successful deletion
                ctx.status(200);
            }
        } catch (ServiceException e) {
            // An exception occurred during the deletion process
            // Set the response status to 200 (OK) to handle the exception gracefully
            ctx.status(200);
        }
    }

    /**
     * This method updates a message with a specific ID.
     * It expects a PATCH request to "/messages/{message_id}" with the updated
     * message details in the request body.
     *
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response
     * @throws JsonProcessingException if an error occurs during JSON parsing or
     *                                 serialization
     */
    private void updateMessageById(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message mappedMessage = mapper.readValue(ctx.body(), Message.class);
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            mappedMessage.setMessage_id(id);

            // Update the message with the new content
            Message messageUpdated = messageService
                    .updateMessage(mappedMessage);

            // Set the response status to 200 (OK) and include the updated message in the
            // response body
            ctx.json(messageUpdated);

        } catch (ServiceException e) {
            // An exception occurred during the update process
            // Set the response status to 400 (Bad Request) to indicate a failure in the
            // request
            ctx.status(400);
        }
    }

    /**
     * This method retrieves all messages posted by a specific account.
     * It expects a GET request to "/accounts/{account_id}/messages".
     *
     * @param ctx the Javalin context object representing the current HTTP request
     *            and response
     */
    private void getMessagesByAccountId(Context ctx) {
        try {
            int accountId = Integer.parseInt(ctx.pathParam("account_id"));

            // Call the messageService to retrieve messages by account ID
            List<Message> messages = messageService
                    .getMessagesByAccountId(accountId);
            if (!messages.isEmpty()) {
                // If messages are found, send them as a JSON response
                ctx.json(messages);
            } else {
                // If no messages are found, send an empty JSON response
                ctx.json(messages);
                ctx.status(200);
            }
        } catch (ServiceException e) {
            // Handle ServiceException and set the status code to 400 (Bad Request)
            ctx.status(400);
        }
    }


}