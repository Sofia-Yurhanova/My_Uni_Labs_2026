package org.example.lab6;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class OrderController {
    @FXML private TableView<Order> table;
    @FXML private TextField fProduct, fPrice, fQty, fName, fCity, fSearchCity;
    @FXML private ComboBox<String> fDelivery, fSearchDelivery;
    @FXML private CheckBox fPaid;

    private final OrderRepository repository = new OrderRepository();

    @FXML
    public void initialize() {
        // Налаштування ComboBox (випадаючі списки)
        fDelivery.setItems(FXCollections.observableArrayList("Нова Пошта", "Укрпошта", "Кур'єр"));
        fSearchDelivery.setItems(FXCollections.observableArrayList("Нова Пошта", "Укрпошта", "Кур'єр"));

        // Завантажуємо дані при старті
        loadData();
    }

    private void loadData() {
        table.setItems(FXCollections.observableArrayList(repository.getAllOrders()));
    }

    @FXML
    protected void handleAdd() {
        try {
            String product = capitalizeWords(fProduct.getText());
            double price = Double.parseDouble(fPrice.getText());
            int qty = Integer.parseInt(fQty.getText());
            String name = capitalizeWords(fName.getText());
            String city = capitalizeWords(fCity.getText());
            String delivery = fDelivery.getValue();
            boolean isPaid = fPaid.isSelected();

            if (product.isEmpty() || name.isEmpty() || city.isEmpty() || delivery == null) {
                showAlert("Помилка", "Заповніть усі текстові поля та оберіть доставку.");
                return;
            }

            // Передаємо 0 як ID, бо база даних сама призначить правильний ID (AUTOINCREMENT)
            Order newOrder = new Order(0, product, price, qty, name, city, delivery, isPaid);
            repository.addOrder(newOrder);

            // Очищення полів після додавання
            fProduct.clear(); fPrice.clear(); fQty.clear(); fName.clear(); fCity.clear();
            fDelivery.getSelectionModel().clearSelection(); fPaid.setSelected(false);

            loadData(); // Оновлюємо таблицю
        } catch (NumberFormatException e) {
            showAlert("Помилка вводу", "Перевірте правильність вводу ціни та кількості (мають бути числа).");
        }
    }

    @FXML
    protected void handleDelete() {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            repository.deleteOrder(selected.getId());
            loadData();
        } else {
            showAlert("Увага", "Оберіть запис у таблиці для видалення.");
        }
    }

    @FXML
    protected void handleShowAll() {
        loadData();
    }

    @FXML
    protected void handleFilterCity() {
        String city = capitalizeWords(fSearchCity.getText());
        if (!city.isEmpty()) {
            table.setItems(FXCollections.observableArrayList(repository.getPaidOrdersByCity(city)));
        }
    }

    @FXML
    protected void handleFilterDelivery() {
        String delivery = fSearchDelivery.getValue();
        if (delivery != null) {
            table.setItems(FXCollections.observableArrayList(repository.getUnpaidOrdersByDelivery(delivery)));
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    // Метод для автоматичної великої літери
    private String capitalizeWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        String[] words = text.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                // Першу літеру робимо великою, решту залишаємо як є
                result.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim(); // trim() забирає зайвий пробіл в кінці
    }
}
