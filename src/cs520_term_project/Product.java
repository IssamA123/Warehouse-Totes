package cs520_term_project;

public class Product {
	private String upc;
	private String description;
public Product(String upc) {
    this.upc = upc;
}

public String getUpc() {
    return upc;
}
public String getDescription() {
    return description;
}
// Overriding toString to give specific product info
public String toString() {
	return "Product Information [UPC: " + upc + ", Description: " + description + "]";
}

public int hashCode() {
    return upc.hashCode(); // use UPC for hashing
}
	}
