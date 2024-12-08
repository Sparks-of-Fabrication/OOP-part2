package com.sparks.of.fabrication.oop2.scenes;

import com.sparks.of.fabrication.oop2.models.Item;
import com.sparks.of.fabrication.oop2.utils.EntityManagerWrapper;
import com.sparks.of.fabrication.oop2.Singleton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class Client_scene {

    @FXML
    private TableView<Item> itemTable;

    @FXML
    private TableColumn<Item, Long> idItemColumn;

    @FXML
    private TableColumn<Item, String> nameColumn;

    @FXML
    private TableColumn<Item, String> categoryColumn;

    @FXML
    private TableColumn<Item, Double> priceColumn;

    @FXML
    private TableColumn<Item, Double> arrivalPriceColumn;

    @FXML
    private TableColumn<Item, Integer> quantityColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button showCartButton;

    private ObservableList<Item> items;

    private final EntityManagerWrapper entityManager = Singleton.getInstance(EntityManagerWrapper.class);

    @FXML
    public void initialize() {
        idItemColumn.setCellValueFactory(new PropertyValueFactory<>("idItem"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        arrivalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalPrice"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));


        loadItems();
    }

    private void loadItems() {
        List<Item> itemList = entityManager.findAllEntities(Item.class);
        if (itemList != null) {
            items = FXCollections.observableArrayList(itemList);
            itemTable.setItems(items);
            System.out.println("Loaded items: " + itemList);
        } else {
            System.out.println("No items found in the database.");
        }
    }



    @FXML
    private void showCart() {
        try {
            SceneLoader sceneLoader = new SceneLoader();
            //Cart_scene to be added later
            sceneLoader.loadScene("scenes/cart_scene.fxml", 500, 500, "Cart", true, new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void search() {
        String searchText = searchField.getText().toLowerCase();

        if (searchText.isEmpty()) {
            itemTable.setItems(items);
        } else {
            ObservableList<Item> filteredItems = FXCollections.observableArrayList();
            for (Item item : items) {
                if (item.getName().toLowerCase().contains(searchText) || item.getCategory().getCategory().toLowerCase().contains(searchText)) {
                    filteredItems.add(item);
                }
            }
            itemTable.setItems(filteredItems);
        }
    }
}
