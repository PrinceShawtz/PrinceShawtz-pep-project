package Service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import DAO.AccountDao;
import DAO.DaoException;
import Model.Account;

/**
 * This class is responsible for handling business logic for Account objects.
 */
public class AccountService {
    private AccountDao accountDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    // Default constructor initializing the AccountDao object
    public AccountService() {
        accountDao = new AccountDao();
    }

    // Constructor that allows an external AccountDao to be used, useful for testing
    // purposes.
    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    /**
     * Retrieve an Account by its ID using the AccountDao.
     *
     * @param id The ID of the Account.
     * @return Optional containing the found Account.
     * @throws ServiceException If the Account is not found or there is a DAO exception.
     */
    public Optional<Account> getAccountById(int id) {
        LOGGER.info("Fetching account with ID: {}", id);
        try {
            Optional<Account> account = accountDao.getById(id);
            LOGGER.info("Fetched account: {}", account.orElse(null));
            return account;
        } catch (DaoException e) {
            throw new ServiceException("Exception occurred while fetching account", e);
        }
    }

    /**
     * Retrieve all accounts using the AccountDao.
     *
     * @return List of all Accounts.
     * @throws ServiceException If there is a DAO exception.
     */

    public List<Account> getAllAccounts() {
        LOGGER.info("Fetching all accounts");
        try {
            List<Account> accounts = accountDao.getAll();
            LOGGER.info("Fetched {} accounts", accounts.size());
            return accounts;
        } catch (DaoException e) {
            throw new ServiceException("Exception occurred while fetching accounts", e);
        }
    }

    /**
     * Find an account by its username using the AccountDao.
     *
     * @param username The username of the account to find.
     * @return Optional containing the found account.
     * @throws ServiceException If any exception occurs during finding.
     */
    public Optional<Account> findAccountByUsername(String username) {
        LOGGER.info("Finding account by username: {}", username);
        try {
            Optional<Account> account = accountDao.findAccountByUsername(username);
            LOGGER.info("Found account: {}", account.orElse(null));
            return account;
        } catch (DaoException e) {
            throw new ServiceException("Exception occurred while finding account by username " + username, e);
        }
    }

    /**
     * Validate the login credentials of an account using the AccountDao.
     *
     * @param account The account to validate.
     * @return Optional containing the validated account.
     * @throws ServiceException If any exception occurs during validation.
     */
    public Optional<Account> validateLogin(Account account) {
        LOGGER.info("Validating login");
        try {
            Optional<Account> validatedAccount = accountDao.validateLogin(account.getUsername(),
                    account.getPassword());
            LOGGER.info("Login validation result: {}", validatedAccount.isPresent());
            return validatedAccount;
        } catch (DaoException e) {
            throw new ServiceException("Exception occurred while validating login", e);
        }
    }

    /**
     * Creates a new account in the database using the AccountDao.
     *
     * @param account The account to create.
     * @return The created account.
     * @throws ServiceException If any exception occurs during creation.
     */
    public Account createAccount(Account account) {
        LOGGER.info("Creating account: {}", account);
        try {
            validateAccount(account);
            Optional<Account> searchedAccount = findAccountByUsername(account.getUsername());
            if (searchedAccount.isPresent()) {
                throw new ServiceException("Account already exist");
            }
            Account createdAccount = accountDao.insert(account);
            LOGGER.info("Created account: {}", createdAccount);
            return createdAccount;
        } catch (DaoException e) {
            throw new ServiceException("Exception occurred while creating account", e);
        }
    }

    /**
     * Updates an existing account in the database using the AccountDao.
     *
     * @param account The account to update.
     * @return true if the update was successful, false otherwise.
     * @throws ServiceException If any exception occurs during update.
     */
    public boolean updateAccount(Account account) {
        LOGGER.info("Updating account: {}", account);
        try {
            account.setPassword(account.password);
            boolean updated = accountDao.update(account);
            LOGGER.info("Updated account: {}. Update successful {}", account, updated);
            return updated;
        } catch (DaoException e) {
            throw new ServiceException("Exception occurred while while updating account", e);
        }
    }

    /**
     * Deletes an existing account in the database using the AccountDao.
     *
     * @param account The account to delete.
     * @return true if the deletion was successful, false otherwise.
     * @throws ServiceException If any exception occurs during deletion.
     */
    public boolean deleteAccount(Account account) {
        LOGGER.info("Deleting account: {}", account);
        if (account.getAccount_id() == 0) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        try {
            boolean deleted = accountDao.delete(account);
            LOGGER.info("Deleted account: {} . Deletion successful {}", account, deleted);
            return deleted;
        } catch (DaoException e) {
            throw new ServiceException("Exception occurred while while deleting account", e);
        }
    }

    /**
     * Validates an account to ensure it meets the required criteria.
     *
     * @param account The account to validate.
     * @throws ServiceException If the account is invalid.
     */
    private void validateAccount(Account account) {
        LOGGER.info("Validating account: {}", account);
        try {

            String username = account.getUsername().trim();
            String password = account.getPassword().trim();

            if (username.isEmpty()) {
                throw new ServiceException("Username cannot be blank");
            }
            if (password.isEmpty()) {
                throw new ServiceException("Password cannot be empty");
            }

            if (password.length() < 4) {
                throw new ServiceException("Password must be at least 4 characters long");
            }
            if (accountDao.doesUsernameExist(account.getUsername())) {
                throw new ServiceException("The username must be unique");
            }
        } catch (DaoException e) {
            throw new ServiceException("Exception occurred while validating account", e);
        }
    }

    /**
     * Check if an account exists in the database using the AccountDao.
     *
     * @param accountId The ID of the account to check.
     * @return true if the account exists, false otherwise.
     * @throws ServiceException If any exception occurs during existence check.
     */
    public boolean accountExists(int accountId) {
        LOGGER.info("Checking account existence with ID: {}", accountId);
        try {
            Optional<Account> account = accountDao.getById(accountId);
            boolean exists = account.isPresent();
            LOGGER.info("Account existence: {}", exists);
            return exists;
        } catch (DaoException e) {
            throw new ServiceException("Exception occurred while checking account existence", e);
        }
    }
}
