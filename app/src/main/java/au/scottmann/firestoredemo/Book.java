package au.scottmann.firestoredemo;

public class Book {

    private String Author;
    private String Title;
    private int Price;
    private String ISBN;

    public Book(String author, String title, int price, String ISBN) {
        Author = author;
        Title = title;
        Price = price;
        this.ISBN = ISBN;
    }

    public Book() { }  //empty constructor needed

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getPrice() {
        return Price;
    }

    public void setPrice(int price) {
        Price = price;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    @Override
    public String toString() {
        return "Book{" +
                "Author='" + Author + '\'' +
                ", Title='" + Title + '\'' +
                ", Price=" + Price +
                ", ISBN='" + ISBN + '\'' +
                '}';
    }
}
