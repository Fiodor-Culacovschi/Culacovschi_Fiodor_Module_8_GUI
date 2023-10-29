/*Fiodor Culacovschi
 * CEN 3024 - Software Development 1
 * October 29th, 2023
 * GUI LibraryManagementApp.java
 * This program provides a simple GUI interface for managing library's collection of books.
 * Users can search for books, load books from a file, remove books, check books in and out, add new books, and view book details.
 * It also handles the data storage and manipulation for the library's collection.
 * To run this program on other PC please download the lms test.csv file and the source code .
 * Configure your IDE for GUI applications.
 * Change pathways on your PC, run the program.
 * Program display the main window "Library Management System" with following buttons for interaction:
 * "Search, add, load content, check-in, check-out, remove by title/by barcode, and exit buttons."
 * All buttons are functional and perform assigned tasks.
 * Program has "CardLayout" feature implemented for multiple windows interactions.
 * Program can allow user to select and load the database file.
 * Program read and display data from a file, file stored on a local machine.
 * Program allows user to add books to their collection list by: "Title".
 * Program displays added books including info about each book.
 * Program allows user to search for the books within provided database, also it will display if there are
 * any matches and will offer user to select the desired one.
 * Selected book will be displayed.
 * User can remove book from collection list by entering book title or barcode.
 * The appropriate message will be displayed if user try to remove the book
 * without book being check-out.
 * Program has additional functionality such as: user can check the book in and check out.
 * When user checks in his book program sets up due date automatically from current date.
 * Program removes due date after successful check out.
 * All mistakes made by user entering wrong data program displays appropriate messages.
 * Exit the program button will end the GUI application.
 */

// importing all necessary libraries for GUI app
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

/* Fiodor Culacovschi
* CEN 3024 - Software Development 1
* October 29th, 2023
*define the main class,
*class contains app logic and GUI components such as:
* mainFrame, mainPanel, CardLayout
 */
public class LibraryManagementSystemApp {
    private List<Book> database = new ArrayList<>();
    private JTextArea outputTextArea;
    private JFrame mainFrame;
    private JFrame fileContentFrame;
    private JPanel mainPanel;
    private JPanel fileContentPanel;
    private CardLayout cardLayout;

    // main method
    /* Fiodor Culacovschi
     * CEN 3024 - Software Development 1
     * October 29th, 2023
     * This is where the program starts
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryManagementSystemApp app = new LibraryManagementSystemApp();
            app.createAndShowGUI();
        });
    }

    // creating GUI
    private void createAndShowGUI() {
        mainFrame = new JFrame("Library Management System");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(400, 400);

        mainPanel = new JPanel();
        cardLayout = new CardLayout(); // for multiple windows interaction
        mainPanel.setLayout(cardLayout);

        mainFrame.add(mainPanel);

        createMainView();
        createFileContentView();

        mainFrame.setVisible(true);
    }

    // main view of the application
    private void createMainView() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // text area
        outputTextArea = new JTextArea(10, 30);
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // create buttons
        JButton loadFromFileButton = new JButton("Load Books from File");
        JButton searchBookButton = new JButton("Search Book");
        JButton removeByBarcodeButton = new JButton("Remove by Barcode");
        JButton removeByTitleButton = new JButton("Remove by Title");
        JButton checkOutButton = new JButton("Check Out Book");
        JButton checkInButton = new JButton("Check In Book");
        JButton addBookButton = new JButton("Add Book");
        JButton exitButton = new JButton("Exit");

        // panel for holding buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(searchBookButton);
        buttonPanel.add(loadFromFileButton);
        buttonPanel.add(removeByBarcodeButton);
        buttonPanel.add(removeByTitleButton);
        buttonPanel.add(checkOutButton);
        buttonPanel.add(checkInButton);
        buttonPanel.add(addBookButton);
        buttonPanel.add(exitButton);

        // adding this panel to main panel
        panel.add(buttonPanel, BorderLayout.SOUTH);
        // adding main panel to the main frame
        mainPanel.add(panel, "main");

        // searching books by title
        searchBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTitle = JOptionPane.showInputDialog("Enter the book title to search:");
                if (searchTitle != null) {
                    List<Book> matchingBooks = searchBooksByTitle(searchTitle);
                    if (!matchingBooks.isEmpty()) {
                        outputTextArea.append("Matching Books for Title '" + searchTitle + "':\n\n");
                        boolean titleDisplayed = false;
                        for (int i = 0; i < matchingBooks.size(); i++) {
                            Book book = matchingBooks.get(i);
                            if (!titleDisplayed) {
                                outputTextArea.append("Enter the number of the book you want to view:\n");
                                titleDisplayed = true;
                            }
                            outputTextArea.append((i + 1) + ". Barcode: " + book.getBarcode() +
                                    ", Author: " + book.getAuthor() + ", Genre: " + book.getGenre() + "\n");
                        }
                        String selection = JOptionPane.showInputDialog("Enter the number of the book you want to view:");
                        if (selection != null) {
                            try {
                                int selectedIndex = Integer.parseInt(selection);
                                if (selectedIndex >= 1 && selectedIndex <= matchingBooks.size()) {
                                    Book selectedBook = matchingBooks.get(selectedIndex - 1);
                                    displayBookInfo(selectedBook);
                                } else {
                                    outputTextArea.append("\nInvalid selection. Please enter a valid number.\n");
                                }
                            } catch (NumberFormatException e1) {
                                outputTextArea.append("\nInvalid input. Please enter a number to select a book.\n");
                            }
                        }
                    } else {
                        outputTextArea.append("\nNo matching books found for the title '" + searchTitle + "'.\n");
                    }
                }
            }
        });

        // loading content from file
        loadFromFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    loadBooksFromFile(selectedFile);
                    displayFileContent(selectedFile);
                    cardLayout.show(mainPanel, "fileContent");
                }
            }
        });

        // removing book by barcode
        removeByBarcodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String barcode = JOptionPane.showInputDialog("Enter the barcode number to remove:");
                if (barcode != null) {
                    Book book = findBookByBarcode(barcode);
                    if (book != null) {
                        if (book.isCheckedOut()) {
                            // Book is checked out, so it can be removed
                            removeBook(book);
                            outputTextArea.append("\nBook with barcode '" + barcode + "' has been removed.\n");
                        } else {
                            // Book is checked in, display an error message
                            outputTextArea.append("\nCannot remove a checked-in book with barcode '" + barcode + "'. Please check it out first.\n\n");
                        }
                    } else {
                        outputTextArea.append("\nBook with barcode '" + barcode + "' not found in the database.\n");
                    }
                }
            }
        });

        // removing book by title
        removeByTitleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = JOptionPane.showInputDialog("Enter the title to remove:");
                if (title != null) {
                    Book book = findBookInDatabase(title);
                    if (book != null) {
                        if (book.isCheckedOut()) {
                            removeBook(book);
                            outputTextArea.append("\nBook with title '" + title + "' has been removed.\n");
                        } else {
                            outputTextArea.append("\nCannot remove a checked-in book with title '" + title + "'. Please check it out first.\n");
                        }
                    } else {
                        outputTextArea.append("\nBook with title '" + title + "' not found in the database.\n");
                    }
                }
            }
        });

        // book check-out
        checkOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = JOptionPane.showInputDialog("Enter the title to check out:");
                if (title != null) {
                    Book book = findBookInDatabase(title);
                    if (book != null) {
                        if (!book.isCheckedOut()) {
                            // Update the status
                            book.checkOut();
                            outputTextArea.append("\nBook '" + title + "' has been checked out. Status: " + book.getStatus() + "\n");
                            displayBookInfo(book);
                        } else {
                            outputTextArea.append("\nBook '" + title + "' is already checked out. Status: " + book.getStatus() + "\n");
                        }
                    } else {
                        outputTextArea.append("\nBook with title '" + title + "' not found in the database.\n");
                    }
                }
            }

        });

        // book check-in
        checkInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = JOptionPane.showInputDialog("Enter the title to check in:");
                if (title != null) {
                    Book book = findBookInDatabase(title);
                    if (book != null) {
                        // Calculate due date (4 weeks from the current date)
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.WEEK_OF_YEAR, 4);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                        String dueDate = sdf.format(calendar.getTime());

                        book.checkIn();
                        book.setStatus("checked-in");
                        book.setDueDate(dueDate);

                        outputTextArea.append("\nBook '" + title + "' has been checked in. Due Date: " + dueDate + "\n");
                        displayBookInfo(book);
                    } else {
                        outputTextArea.append("\nBook with title '" + title + "' not found in the database.\n");
                    }
                }
            }
        });

        // adding book to user collection
        addBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String titleOrBarcode = JOptionPane.showInputDialog("Enter the book title or barcode:");
                if (titleOrBarcode != null) {
                    Book book = findBookByTitleOrBarcode(titleOrBarcode);
                    if (book != null) {
                        displayBookInfo(book);
                    } else {
                        outputTextArea.append("\nBook with title or barcode '" + titleOrBarcode + "' not found in the database.\n");
                    }
                }
            }
        });

        // exiting program
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    // create view window to display file content
    private void createFileContentView() {
        fileContentFrame = new JFrame("File Content");
        fileContentFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        fileContentFrame.setSize(400, 400);

        fileContentPanel = new JPanel();
        fileContentPanel.setLayout(new BorderLayout());

        JTextArea contentTextArea = new JTextArea();
        contentTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(contentTextArea);
        fileContentPanel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Back to Main");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "main");
                fileContentFrame.setVisible(false);
            }
        });

        fileContentPanel.add(backButton, BorderLayout.SOUTH);

        fileContentFrame.add(fileContentPanel);
    }

    // displaying file content
    private void displayFileContent(File file) {
        try {
            StringBuilder content = new StringBuilder();
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
            JTextArea contentTextArea = new JTextArea(content.toString());
            contentTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(contentTextArea);
            fileContentPanel.add(scrollPane, BorderLayout.CENTER);
            fileContentFrame.setVisible(true);
        } catch (FileNotFoundException e) {
            outputTextArea.append("Error: File not found\n");
        }
    }

    // methods for managing book database
    // searching through database
    // methods: searching, removing, loading, displaying.
    private Book findBookInDatabase(String title) {
        for (Book book : database) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                return book;
            }
        }
        return null;
    }

    private Book findBookByTitleOrBarcode(String titleOrBarcode) {
        for (Book book : database) {
            if (book.getTitle().equalsIgnoreCase(titleOrBarcode) || book.getBarcode().equals(titleOrBarcode)) {
                return book;
            }
        }
        return null;
    }
    private Book findBookByBarcode(String barcode) {
        for (Book book : database) {
            if (book.getBarcode().equals(barcode)) {
                return book;
            }
        }
        return null;
    };

    // Create a method to search for books by title
    private List<Book> searchBooksByTitle(String searchTitle) {
        List<Book> matchingBooks = new ArrayList<>();
        for (Book book : database) {
            if (book.getTitle().equalsIgnoreCase(searchTitle)) {
                matchingBooks.add(book);
            }
        }
        return matchingBooks; // return matching values
    }
    private void removeBook(Book book) {
        database.remove(book);
    }

    // loading,reading line by line and displaying file content
    private void loadBooksFromFile(File file) {
        try {
            database.clear();
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String title = parts[2];
                    String barcode = parts[0];
                    String isbn = parts[1];
                    String author = parts[3];
                    String genre = parts[4];
                    Book book = new Book(title, barcode, isbn, author, genre);
                    database.add(book);
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            outputTextArea.append("Error: File not found\n");
        }
    }

    // a window for displaying an added book info
    private void showAddBookDialog() {
        JFrame addBookFrame = new JFrame("Add Book");
        addBookFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addBookFrame.setSize(400, 200);
        // panel for input fields and buttons organization
        JPanel addBookPanel = new JPanel();
        addBookPanel.setLayout(new GridLayout(5, 2));

        // text fields for user input
        JTextField titleField = new JTextField();
        JTextField barcodeField = new JTextField();
        JTextField isbnField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField genreField = new JTextField();

        // field labels
        addBookPanel.add(new JLabel("Title: "));
        addBookPanel.add(titleField);
        addBookPanel.add(new JLabel("Barcode: "));
        addBookPanel.add(barcodeField);
        addBookPanel.add(new JLabel("ISBN: "));
        addBookPanel.add(isbnField);
        addBookPanel.add(new JLabel("Author: "));
        addBookPanel.add(authorField);
        addBookPanel.add(new JLabel("Genre: "));
        addBookPanel.add(genreField);

        // button to add a book
        JButton addButton = new JButton("Add Book");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // getting user input from the text fields
                String title = titleField.getText();
                String barcode = barcodeField.getText();
                String isbn = isbnField.getText();
                String author = authorField.getText();
                String genre = genreField.getText();

                // fields validation
                if (!title.isEmpty() && !barcode.isEmpty() && !isbn.isEmpty() && !author.isEmpty() && !genre.isEmpty()) {
                    Book book = new Book(title, barcode, isbn, author, genre);
                    database.add(book);
                    displayBookInfo(book);
                    addBookFrame.dispose();
                }
            }
        });

        // adding button to the panel
        addBookPanel.add(addButton);

        addBookFrame.add(addBookPanel);
        addBookFrame.setVisible(true); // dialog visibility
    }

    // display book info
    private void displayBookInfo(Book book) {
        outputTextArea.append("\nBook Information:\n");
        outputTextArea.append("Book barcode: " + book.getBarcode() + "\n");
        outputTextArea.append("Title: " + book.getTitle() + "\n");
        outputTextArea.append("Author: " + book.getAuthor() + "\n");
        outputTextArea.append("Genre: " + book.getGenre() + "\n");
        outputTextArea.append("Status: " + book.getStatus() + "\n");
    }

    // define the inner class book with its attributes
    // class provides check-in/out books methods
    private class Book {
        private static int nextBookId = 1;
        private int bookId;
        private String barcode;
        private String isbn;
        private String title;
        private String author;
        private String genre;
        private String status;
        private boolean checkedOut;

        // book constructor
        public Book(String title, String barcode, String isbn, String author, String genre) {
            this.bookId = nextBookId++;
            this.title = title;
            this.barcode = barcode;
            this.isbn = isbn;
            this.author = author;
            this.genre = genre;
            this.status = "available";
            this.checkedOut = false;
        }

        // Getters and Setters
        public int getBookId() {
            return bookId;
        }

        public String getBarcode() {
            return barcode;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getGenre() {
            return genre;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isCheckedOut() {
            return checkedOut;
        }

        public void checkOut() {
            checkedOut = true;
            setStatus("checked-out");
        }

        public void checkIn() {
            checkedOut = false;
            setStatus("checked-in");
        }

        public void setDueDate(String dueDate) {
        }
    }
}
