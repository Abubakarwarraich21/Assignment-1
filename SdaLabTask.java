package sdaLabTask;

//import static com.sun.javafx.fxml.expression.Expression.add;
import javax.swing.JList;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

// DOMAIN LAYER (Entity)
class Book {
    private String title;
    private String author;
    private String details;

    // CONSTRUCTOR 
    public Book(String title, String author, String details) {
        this.title = title;
        this.author = author;
        this.details = details;
    }

    public String getTitle() {
        return title;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return title + " by " + author;
    }
}

// REPOSITORY LAYER (Data Access)
interface BookRepository {
    List<Book> getBooks();
    Book getBookDetails(String title);
}

class InMemoryBookRepository implements BookRepository {
    private List<Book> books;

    public InMemoryBookRepository() {
        books = new ArrayList<>();
        books.add(new Book("Java Programming", "John Doe", "An introductory book on Java."));
        books.add(new Book("Data Structures", "Jane Doe", "An in-depth guide to data structures."));
        books.add(new Book("Web Development", "Alice Smith", "Basics of web development with HTML, CSS, and JS."));
    }

    @Override
    public List<Book> getBooks() {
        return books;
    }

    @Override
    public Book getBookDetails(String title) {
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                return book;
            }
        }
        return null;
    }
}

// FACTORY (for creating repositories)
class BookRepositoryFactory {
    public static BookRepository createBookRepository(String type) {
        if (type.equalsIgnoreCase("inMemory")) {
            return new InMemoryBookRepository();
        }
        throw new IllegalArgumentException("Unsupported repository type");
    }
}

// SINGLETON (Application Layer Entry Point)
class WebApplication {
    private static WebApplication instance;
    private BookRepository bookRepository;

    private WebApplication(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public static WebApplication getInstance(BookRepository bookRepository) {
        if (instance == null) {
            instance = new WebApplication(bookRepository);
        }
        return instance;
    }

    public List<Book> browseBooks() {
        return bookRepository.getBooks();
    }

    public Book selectBook(String title) {
        return bookRepository.getBookDetails(title);
    }
}

// SERVICE LAYER (Facade Pattern for business logic)
class BookServiceFacade {
    private WebApplication webApp;

    public BookServiceFacade(WebApplication webApp) {
        this.webApp = webApp;
    }

    public List<Book> getAvailableBooks() {
        return webApp.browseBooks();
    }

    public Book getBookDetails(String title) {
        return webApp.selectBook(title);
    }
}

// GUI Class
class BookBrowsingGUI extends JFrame {
    private BookServiceFacade bookService;
    private JList<Book> bookList;
    private JTextArea bookDetailsArea;

    public BookBrowsingGUI(BookServiceFacade bookService) {
        this.bookService = bookService;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Book Browsing Application");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Book List
        bookList = new JList<>(bookService.getAvailableBooks().toArray(new Book[0]));
        bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookList.addListSelectionListener(e -> displayBookDetails());
        JScrollPane scrollPane = new JScrollPane(bookList);
        add(scrollPane, BorderLayout.CENTER);

        // Book Details Area
        bookDetailsArea = new JTextArea();
        bookDetailsArea.setEditable(false);
        add(bookDetailsArea, BorderLayout.SOUTH);

        // Button to refresh the book list
        JButton refreshButton = new JButton("Refresh Book List");
        refreshButton.addActionListener(e -> refreshBookList());
        add(refreshButton, BorderLayout.NORTH);
    }

    private void displayBookDetails() {
        Book selectedBook = bookList.getSelectedValue();
        if (selectedBook != null) {
            bookDetailsArea.setText("Title: " + selectedBook.getTitle() + "\nDetails: " + selectedBook.getDetails());
        } else {
            bookDetailsArea.setText("");
        }
    }

    private void refreshBookList() {
        bookList.setListData(bookService.getAvailableBooks().toArray(new Book[0]));
    }

    public static void main(String[] args) {
        // Repository Factory to choose data source
        BookRepository bookRepository = BookRepositoryFactory.createBookRepository("inMemory");

        // Singleton for WebApplication
        WebApplication webApp = WebApplication.getInstance(bookRepository);

        // Facade for Service Layer
        BookServiceFacade bookService = new BookServiceFacade(webApp);

        // Create and display the GUI
        SwingUtilities.invokeLater(() -> {
            BookBrowsingGUI gui = new BookBrowsingGUI(bookService);
            gui.setVisible(true);
        });
    }
}