package cs520_term_project;

import java.util.LinkedList;


public class Tote {
	private int toteID; 
	private LinkedList<Product> products; 
	private String upc;
	private static final int tote_max = 10; 
	
	public Tote (String upc, int toteID) 
	{
		this.upc = upc;
		this.toteID = toteID;
		this.products = new LinkedList<>();
	}
	public boolean toteFull() {
		return products.size() >= tote_max;
	}
	public boolean toteEmpty() { 
		return products.isEmpty(); // True if tote empty
	}
	
	public boolean addProduct(Product p) {
		if (toteFull()) { 
			return false;
		}
		products.add(p); 
		return true;
	}
	public Product removeProduct() {
		return products.removeFirst(); 
	}
	// getter methods for tote info
	public int getToteTotal() {
		return products.size();
	}
	public int getToteID() {
		return toteID;
	}
	public String getToteUPC() {
		return upc;
	}
	public String toString() {
        return "Tote (UPC: " + upc + ", Size: " + products.size() + ")";
}
}
