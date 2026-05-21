package org.example.lab6;

public class Order {
    private int id;
    private String productName;
    private double price;
    private int quantity;
    private String customerName;
    private String city;
    private String deliveryMethod;
    private boolean isPaid;

    public Order(int id, String productName, double price, int quantity, String customerName, String city, String deliveryMethod, boolean isPaid) {
        this.id = id;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.customerName = customerName;
        this.city = city;
        this.deliveryMethod = deliveryMethod;
        this.isPaid = isPaid;
    }

    // Гетери потрібні для JavaFX TableView
    public int getId() { return id; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getCustomerName() { return customerName; }
    public String getCity() { return city; }
    public String getDeliveryMethod() { return deliveryMethod; }
    public boolean isPaid() { return isPaid; }
    public String getPaidStatus() { return isPaid ? "Так" : "Ні"; } // Для зручного відображення
}
