package CPSC559;

import java.io.BufferedWriter;
import java.io.FileWriter;  
import java.util.ArrayList;
import java.util.List;

import CPSC559.Book;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class BookDB {
    public String filepath;

    public BookDB(String file) {
        this.filepath = file;
    }

    public synchronized void addBook(Book book) throws IOException {
        book.id = this.readHighestId() + 1;
        String toAdd = String.join(",", Integer.toString(book.id), book.title, Integer.toString(book.holder));
        toAdd += "\n";
        BufferedWriter writer = new BufferedWriter(new FileWriter(this.filepath, true));
        writer.write(toAdd);
        writer.close();
    }

    ///
    // Return the first book whose data in the specified column matches the specified key
    ///
    public Book getBook(String key, int col) throws IOException {
        List<String> data = new ArrayList<>(Files.readAllLines(Paths.get(this.filepath), StandardCharsets.UTF_8));
        for (int i = 0; i < data.size(); i++) {
            String[] items = data.get(i).split(",", 0);
            if (String.valueOf(items[col]).equals(key)) {
                Book book = new Book(items[1]);
                book.id = Integer.parseInt(items[0]);
                book.holder = Integer.parseInt(items[2]);
                return book;
            }
        }
        Book nullBook = new Book("null");
        nullBook.id = -1;
        return nullBook;
    }

    ///
    // Updates a book, b, based off the id
    // The id should never be manually changed so this shouldn't be a problem
    // The book b should have both a title and holder specified
    //
    public synchronized void updateBook(Book b) throws IOException {
        List<String> data = new ArrayList<>(Files.readAllLines(Paths.get(this.filepath), StandardCharsets.UTF_8));
        for (int i = 0 ; i < data.size(); i++) {
            String[] items = data.get(i).split(",", 0);
            if (b.id == Integer.parseInt(items[0])) {
                String toUpdate = String.join(",", Integer.toString(b.id), b.title, Integer.toString(b.holder));
                data.set(i, toUpdate);
                break;
            }
        }
        Files.write(Paths.get(this.filepath), data, StandardCharsets.UTF_8);
    }

    public synchronized void deleteBook(int id) throws IOException {
        List<String> data = new ArrayList<>(Files.readAllLines(Paths.get(this.filepath), StandardCharsets.UTF_8));
        for (int i = 0 ; i < data.size(); i++) {
            String[] items = data.get(i).split(",", 0);
            if (id == Integer.parseInt(items[0])) {
                data.remove(i);
                break;
            }
        }
        Files.write(Paths.get(this.filepath), data, StandardCharsets.UTF_8);
    }

    ///
    // Get the md5 hash of the database file
    ///
    public byte[] getHash() throws IOException {
        byte[] hash = null;
        try {
            byte[] b = Files.readAllBytes(Paths.get(this.filepath));
            hash = MessageDigest.getInstance("MD5").digest(b);
        }
        catch (NoSuchAlgorithmException n) {
            System.err.println("Unknown hashnig algorithm specified" + n.getMessage());
        }
        return hash;
    }


    private int readHighestId() throws IOException {
        int maxId = -1;
        List<String> data = new ArrayList<>(Files.readAllLines(Paths.get(this.filepath), StandardCharsets.UTF_8));
        for (int i = 0; i < data.size(); i++) {
            String[] items = data.get(i).split(",", 0);
            if (Integer.parseInt(items[0]) > maxId) {
                maxId = Integer.parseInt(items[0]);
            }
        }
        return maxId;
    }
}
