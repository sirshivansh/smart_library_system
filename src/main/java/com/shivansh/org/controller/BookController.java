package com.shivansh.org.controller;

import com.shivansh.org.dto.Book;
import com.shivansh.org.exception.BookNotFoundException;
import com.shivansh.org.exception.ValidationException;
import com.shivansh.org.service.BookService;
import com.shivansh.org.service.impl.BookServiceImpl;

import java.util.List;

/**
 * Controller class for Book-related operations.
 * Acts as the intermediary between the View (Dashboard) and the Service layer,
 * handling request delegation, exception translation, and response formatting.
 * 
 * This follows the MVC (Model-View-Controller) architectural pattern where:
 * - Model: Book DTO + BookDao
 * - View: Dashboard (console UI)
 * - Controller: This class
 * 
 * @author Shivansh
 * @version 1.0
 */
public class BookController {

    private final BookService bookService;

    public BookController() {
        this.bookService = new BookServiceImpl();
    }

    /**
     * Adds a new book to the library catalog.
     * 
     * @param title  the book title
     * @param author the book author
     * @param isbn   the book ISBN (optional, can be null)
     * @param genre  the book genre
     * @param copies the total number of copies
     * @return true if book was added successfully
     * @throws ValidationException if input validation fails
     */
    public boolean addBook(String title, String author, String isbn, String genre, int copies) throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Book title cannot be empty.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new ValidationException("Book author cannot be empty.");
        }
        if (copies <= 0) {
            throw new ValidationException("Total copies must be at least 1.");
        }

        Book book = new Book(title.trim(), author.trim(), genre != null ? genre.trim() : "", copies, copies);
        if (isbn != null && !isbn.trim().isEmpty()) {
            book.setIsbn(isbn.trim());
        }
        return bookService.addBook(book);
    }

    /**
     * Updates an existing book's details.
     * 
     * @param bookId    the ID of the book to update
     * @param newTitle  the new title (null or empty to keep existing)
     * @param newAuthor the new author (null or empty to keep existing)
     * @param newIsbn   the new ISBN (null or empty to keep existing)
     * @param newGenre  the new genre (null or empty to keep existing)
     * @param newCopies the new total copies as string (null or empty to keep existing)
     * @return true if updated successfully
     * @throws BookNotFoundException if the book ID doesn't exist
     * @throws ValidationException   if the new total causes negative availability
     */
    public boolean updateBook(int bookId, String newTitle, String newAuthor, String newIsbn, String newGenre, String newCopies)
            throws BookNotFoundException, ValidationException {
        Book existing = bookService.getBookById(bookId);
        if (existing == null) {
            throw new BookNotFoundException(bookId);
        }

        if (newTitle != null && !newTitle.trim().isEmpty()) {
            existing.setTitle(newTitle.trim());
        }
        if (newAuthor != null && !newAuthor.trim().isEmpty()) {
            existing.setAuthor(newAuthor.trim());
        }
        if (newIsbn != null && !newIsbn.trim().isEmpty()) {
            existing.setIsbn(newIsbn.trim());
        }
        if (newGenre != null && !newGenre.trim().isEmpty()) {
            existing.setGenre(newGenre.trim());
        }
        if (newCopies != null && !newCopies.trim().isEmpty()) {
            try {
                int newTotal = Integer.parseInt(newCopies.trim());
                if (newTotal <= 0) {
                    throw new ValidationException("Total copies must be a positive number.");
                }
                int diff = newTotal - existing.getTotalCopies();
                int newAvail = existing.getAvailableCopies() + diff;
                if (newAvail < 0) {
                    throw new ValidationException("Cannot reduce copies below the number currently issued.");
                }
                existing.setTotalCopies(newTotal);
                existing.setAvailableCopies(newAvail);
            } catch (NumberFormatException e) {
                throw new ValidationException("Copies must be a valid number.");
            }
        }

        return bookService.updateBook(existing);
    }

    /**
     * Deletes a book from the catalog permanently.
     * 
     * @param bookId the ID of the book to delete
     * @return true if deleted successfully
     * @throws BookNotFoundException if the book ID doesn't exist
     */
    public boolean deleteBook(int bookId) throws BookNotFoundException {
        Book existing = bookService.getBookById(bookId);
        if (existing == null) {
            throw new BookNotFoundException(bookId);
        }
        return bookService.deleteBook(bookId);
    }

    /**
     * Retrieves all books in the library catalog.
     * @return list of all books
     */
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    /**
     * Searches books by a keyword across title, author, and genre fields.
     * @param keyword the search keyword
     * @return list of matching books
     */
    public List<Book> searchBooks(String keyword) {
        return bookService.searchBooks(keyword);
    }

    /**
     * Retrieves a specific book by its ID.
     * @param bookId the book ID
     * @return the Book object, or null if not found
     */
    public Book getBookById(int bookId) {
        return bookService.getBookById(bookId);
    }
}
