package cs520_term_project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Warehouse implements IWarehouse {

    private static final int max_totes = 100;  // Total totes in warehouse = 100
    private List<Tote> totes = new ArrayList<>(); // Listing all totes (100)
    private Map<String, List<Tote>> upcToTotes = new HashMap<>(); //maps each upc to a list of totes that contain that upc
    private int nextToteId = 0; // tracking next toteid

    public Warehouse() {
        for (int i = 0; i < max_totes; i++) { // loop up to 100 (100 total totes)
            totes.add(null); // prefilling 100 totes in warehouse with null, can add products now
        }
    }

    public void LoadProducts(String filename) {
        System.out.println("Loading products...");
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",", 2); // parsing upc from products file
                String upc = tokens[0].trim(); // extract upc
                Product product = new Product(upc); // creating product object from that upc token

                List<Tote> toteList = upcToTotes.getOrDefault(upc, new ArrayList<>()); // retrieve list of totes for that upc, OR new empty one
                Tote thisTote = null;

                for (int i = 0; i < toteList.size(); i++) {
                    if (!toteList.get(i).toteFull() && thisTote == null) {
                        thisTote = toteList.get(i);
                    }
                }

                if (thisTote == null && nextToteId < max_totes) { // if tote is empty, and still totes available create new tote
                    thisTote = new Tote(upc, nextToteId);
                    totes.set(nextToteId, thisTote); // store new tote in list
                    System.out.println("Used additional tote (" + nextToteId + ") for UPC: " + upc);
                    nextToteId++; 
                    toteList.add(thisTote); 
                    upcToTotes.put(upc, toteList); // update map
                }

                if (thisTote != null) { // If tote matches fully
                    thisTote.addProduct(product); // adding product to tote
                }
            }
        } catch (IOException e) {
        	}
        System.out.println("Loading complete.");
    }
    public void FulfillOrders(String filename) {
    	
        System.out.println("Fulfilling orders...\n");
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(","); // using , for token
                String orderNum = tokens[0].trim(); // first token is order number
                System.out.println("Order fulfillment started: Order " + orderNum); 
                List<String> fulfilled = new ArrayList<>();  // list of fulfilled orders

                for (int i = 1; i < tokens.length; i++) { //Looping over all upcs in the order
                    String upc = tokens[i].trim(); // upc(s) is extracted based on # of ,
                    List<Tote> toteList = upcToTotes.get(upc); // get totes that match upc
                    if (toteList == null || toteList.isEmpty()) continue; // SKIPS if none exist

                    List<Tote> nonEmptyTotes = toteList.stream().filter(t -> !t.toteEmpty()).toList(); // filtering empty totes
                    if (nonEmptyTotes.isEmpty()) continue; // if no totes are usable skip

                    Random rand = new Random(); // using random for random tote
                    Tote selected = nonEmptyTotes.get(rand.nextInt(nonEmptyTotes.size())); // picking random non empty tote
                    int selectedIndex = selected.getToteID(); // retrieving that tote ID for info

                    if (selected != null) { 
                        selected.removeProduct(); // remove product from tote, FIRST IN FIRST OUT 
                        System.out.println("Retrieving product from tote (" + selectedIndex + ") for UPC: " + upc);
                        fulfilled.add(upc); // recording sucessful retrival of product for order
                    }
                }

                System.out.println("Order fulfilled--> Order " + orderNum + ", " + String.join(", ", fulfilled));
                System.out.println();
            }
        } catch (IOException e) {
        	}
        System.out.println("Orders complete.\n");
    }

    public boolean MergeTotes() {
        System.out.println("Merging partially filled totes...");
        boolean merged = false; // boolean flag marking merge

        List<String> sortedUPCs = new ArrayList<>(upcToTotes.keySet()); //list of all upcs currently stored in warehouse
        Collections.sort(sortedUPCs); // sorting upcs in order

        for (String upc : sortedUPCs) { // loop through each upc
            List<Tote> toteList = new ArrayList<>(upcToTotes.get(upc)); // new list for totes of upc
            List<Product> allProducts = new ArrayList<>(); // new list to hold products for upc

            for (Tote t : toteList) { // loop for each tote assigned to current upc
                while (!t.toteEmpty()) {
                    allProducts.add(t.removeProduct()); // removing products to organize into new totes
                }
            }

            for (Tote t : toteList) { // going back into old totes
                int index = t.getToteID(); // retrieve its id
                if (index >= 0 && index < totes.size()) { // valid index = clear it from index
                    totes.set(index, null);
                }
            }

            List<Tote> newTotes = new ArrayList<>(); // initalize for new totes
            Tote current = null;

            for (Product p : allProducts) {
                if (current == null || current.toteFull()) { // if no current tote, or full
                    current = null; // trigger tote creation with reset
                    for (int i = 0; i < totes.size() && current == null; i++) { // loop to find a warehouse slot
                        if (totes.get(i) == null) {
                            current = new Tote(upc, i); // creating new tote for product at that index 
                            totes.set(i, current); //store new tote
                            newTotes.add(current); // track new tote 
                        }
                    }
                }
                if (current != null) {
                    current.addProduct(p); // if tote is available, add product
                }
            }
            
            if (!newTotes.isEmpty()) {
                upcToTotes.put(upc, newTotes); // if tote created, add upcs - tote mapping to new list
            } else {
                upcToTotes.put(upc, toteList); // else old list
            }

            merged = true; // merge occured flag
        }

        System.out.println("Merge complete.\n");
        return merged;
    }

    public void DisplayDetails() {
        int full = 0, partial = 0, empty = 0;

        for (Tote t : totes) { // iterating through every tote slot
            if (t == null) {
                empty++; // if empty, increments empty amount
            } else if (t.toteFull()) { 
                full++; // if tote exists and full, increment full
            } else {
                partial++; // otherwise, its partially full so increment the counter
            }
        }

        System.out.println("Warehouse details:\n");
        System.out.printf("   %d full totes, %d partially filled totes, and %d empty totes\n\n", full, partial, empty);
        System.out.println("   UPC             # Totes");
        System.out.println("   --------------- -------");

        List<String> sortedUPCs = new ArrayList<>(upcToTotes.keySet());
        Collections.sort(sortedUPCs);
        for (String upc : sortedUPCs) {
            System.out.printf("   %-15s %7d\n", upc, upcToTotes.get(upc).size());
        }
        System.out.println();
    }
}
