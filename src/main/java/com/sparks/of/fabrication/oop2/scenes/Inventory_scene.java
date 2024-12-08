package com.sparks.of.fabrication.oop2.scenes;

import com.sparks.of.fabrication.oop2.Singleton;
import com.sparks.of.fabrication.oop2.models.Item;
import com.sparks.of.fabrication.oop2.utils.EntityManagerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Setter;

import java.util.List;

public class Inventory_scene {

    @FXML
    private TableView<Item> inventoryTable;
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

    @Setter
    private EntityManagerWrapper entityManager = Singleton.getInstance(EntityManagerWrapper.class);

    @FXML
    public void initialize() {
        idItemColumn.setCellValueFactory(new PropertyValueFactory<>("idItem"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        arrivalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalPrice"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        idItemColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.1));
        nameColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.2));
        categoryColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.2));
        priceColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.2));
        arrivalPriceColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.2));
        quantityColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.1));

        loadItems();
    }

    private void loadItems() {
        List<Item> itemList = entityManager.findAllEntities(Item.class);

        if (itemList != null && !itemList.isEmpty()) {
            ObservableList<Item> items = FXCollections.observableArrayList(itemList);
            inventoryTable.setItems(items);
        } else {
            System.out.println("No items found in the database.");
        }
    }
}
