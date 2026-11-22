package id.ac.usk.dompetdeadline.model;

public record Category(long id, String name, String type) {
    @Override public String toString() { return name; }
}
