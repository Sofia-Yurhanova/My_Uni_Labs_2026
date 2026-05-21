package org.example.lab6;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private static final String DB_URL = "jdbc:sqlite:orders.db";

    public OrderRepository() {
        createTableIfNotExists();
    }

    // Створення таблиці, якщо її ще немає
    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS orders (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " product_name TEXT NOT NULL,\n"
                + " price REAL NOT NULL,\n"
                + " quantity INTEGER NOT NULL,\n"
                + " customer_name TEXT NOT NULL,\n"
                + " city TEXT NOT NULL,\n"
                + " delivery_method TEXT NOT NULL,\n"
                + " is_paid BOOLEAN NOT NULL\n"
                + ");";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Помилка створення таблиці: " + e.getMessage());
        }
    }

    // CRUD: Додавання замовлення (Create)
    public void addOrder(Order order) {
        String sql = "INSERT INTO orders(product_name, price, quantity, customer_name, city, delivery_method, is_paid) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, order.getProductName());
            pstmt.setDouble(2, order.getPrice());
            pstmt.setInt(3, order.getQuantity());
            pstmt.setString(4, order.getCustomerName());
            pstmt.setString(5, order.getCity());
            pstmt.setString(6, order.getDeliveryMethod());
            pstmt.setBoolean(7, order.isPaid());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Помилка додавання: " + e.getMessage());
        }
    }

    // CRUD: Читання всіх (Read)
    public List<Order> getAllOrders() {
        return fetchOrdersByQuery("SELECT * FROM orders");
    }

    // Фільтр: оплачені за містом
    public List<Order> getPaidOrdersByCity(String city) {
        String safeCity = city.replace("'", "''");
        return fetchOrdersByQuery("SELECT * FROM orders WHERE is_paid = 1 AND city = '" + city + "'");
    }

    // Фільтр: неоплачені за доставкою
    public List<Order> getUnpaidOrdersByDelivery(String deliveryMethod) {
        String safeDelivery = deliveryMethod.replace("'", "''");
        return fetchOrdersByQuery("SELECT * FROM orders WHERE is_paid = 0 AND delivery_method = '" + deliveryMethod + "'");
    }

    // CRUD: Видалення (Delete)
    public void deleteOrder(int id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Помилка видалення: " + e.getMessage());
        }
    }

    // Допоміжний метод для виконання SQL-запитів на вибірку
    private List<Order> fetchOrdersByQuery(String sql) {
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(new Order(
                        rs.getInt("id"),
                        rs.getString("product_name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("customer_name"),
                        rs.getString("city"),
                        rs.getString("delivery_method"),
                        rs.getBoolean("is_paid")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Помилка вибірки: " + e.getMessage());
        }
        return orders;
    }
}
