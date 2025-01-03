package com.sparks.of.fabrication.oop2.scenes.inventory;

import com.sparks.of.fabrication.oop2.utils.Singleton;
import com.sparks.of.fabrication.oop2.models.*;
import com.sparks.of.fabrication.oop2.utils.EntityManagerWrapper;
import com.sparks.of.fabrication.oop2.utils.LogEmployee;
import com.sparks.of.fabrication.oop2.utils.Pair;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

/**
 * Controller for managing the inventory UI, allowing for the display, creation, updating, and deletion of inventory items.
 */
public class InventoryController {

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

    @FXML
    @Getter
    private TextField nameField, priceField, arrivalPriceField, quantityField, searchField, idField, catCliSupText;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> catCliSupComboBox;

    @FXML
    @Getter
    @Setter
    private Button saveButton;

    @FXML
    private Button catCliSupCreate, catCliSupDelete;

    private final EntityManagerWrapper entityManagerWrapper = Singleton.getInstance(EntityManagerWrapper.class);
    private final InventoryServices inventoryServices = new InventoryServices();
    private static final Logger log = LogManager.getLogger(InventoryController.class);
    private final LogEmployee logEmployee = Singleton.getInstance(LogEmployee.class);

    @Getter
    @Setter
    private boolean isEditMode = false;

    private int catCliSup;

    @Getter
    @Setter
    private Item currentItem;

    /**
     * Initializes the controller, setting up the table columns and loading categories.`
     */
    @FXML
    public void initialize() {
        try {
            log.info("Initializing InventoryController.");
            TableViewSetup.configureTableColumns(inventoryTable, idItemColumn, nameColumn, categoryColumn, priceColumn, arrivalPriceColumn, quantityColumn, this);
            inventoryServices.loadCategories(categoryComboBox);
            catCliSup = 0;
            log.info("InventoryController initialized successfully.");
        } catch (Exception e) {
            log.error("Error during InventoryController initialization: ", e);
            logEmployee.createLog("Initialization Error", "Failed to initialize InventoryController: " + e.getMessage());
        }
    }

    /**
     * Loads the items into the inventory table based on the provided search text.
     *
     * @param text The search text to filter items.
     * @throws NoSuchFieldException if the specified field cannot be found during the loading process.
     */
    protected void loadItems(String text) throws NoSuchFieldException {
        try {
            log.info("Loading items with search text: {}", text);
            inventoryTable.setItems(inventoryServices.loadItems(text));
            log.info("Items loaded successfully.");
        } catch (Exception e) {
            log.error("Error loading items: ", e);
            logEmployee.createLog("Load Items Error", "Failed to load items with text: " + text + " - " + e.getMessage());
        }
    }

    /**
     * Populates the fields with the data from the provided item.
     *
     * @param item The item whose data will populate the fields.
     */
    protected void populateFields(Item item) {
        try {
            log.info("Populating fields for item: {}", item.getName());
            idField.setText(item.getIdItem().toString());
            nameField.setText(item.getName());
            priceField.setText(String.valueOf(item.getPrice()));
            arrivalPriceField.setText(String.valueOf(item.getArrivalPrice()));
            quantityField.setText(String.valueOf(item.getQuantity()));
            categoryComboBox.setValue(item.getCategory().getCategory());
            log.info("Fields populated successfully for item: {}", item.getName());
        } catch (Exception e) {
            log.error("Error populating fields for item: {}", item, e);
        }
    }

    /**
     * Clears all input fields in the inventory form.
     */
    protected void clearFields() {
        log.info("Clearing all input fields.");
        idField.clear();
        nameField.clear();
        priceField.clear();
        arrivalPriceField.clear();
        quantityField.clear();
        categoryComboBox.setValue(null);
    }

    /**
     * Saves or updates the item based on the current mode.
     *
     * @throws NoSuchFieldException if an error occurs while accessing fields during the save or update.
     */
    @FXML
    protected void saveOrUpdateItem() throws NoSuchFieldException {
        if (isEditMode) {
            updateItem();
        } else {
            createNewItem();
        }
    }

    /**
     * Updates an existing item with the data from the input fields.
     *
     * @throws NoSuchFieldException if an error occurs while accessing fields during the update.
     */
    private void updateItem() throws NoSuchFieldException {
        try {
            log.info("Updating item: {}", currentItem.getName());
            Field dateField = Category.class.getDeclaredField("category");
            Pair<Boolean, Category> result = entityManagerWrapper.findEntityByVal(Category.class, dateField, categoryComboBox.getValue());
            boolean success = inventoryServices.updateItem(currentItem, nameField.getText(), Double.parseDouble(priceField.getText()),
                    Double.parseDouble(arrivalPriceField.getText()), Integer.parseInt(quantityField.getText()), result.y());
            if (success) {
                log.info("Item updated successfully: {}", currentItem.getName());
                inventoryTable.refresh();
                clearFields();
                isEditMode = false;
            } else {
                log.error("Failed to update item: {}", currentItem.getName());
                logEmployee.createLog("Update Item Error", "Failed to update item: " + currentItem.getName());
            }
        } catch (Exception e) {
            log.error("Error updating item: {}", currentItem, e);
            logEmployee.createLog("Update Item Error", "Error updating item: " + currentItem + " - " + e.getMessage());
        }
    }

    /**
     * Creates a new item with the data from the input fields.
     *
     * @throws NoSuchFieldException if an error occurs while accessing fields during the creation.
     */
    private void createNewItem() throws NoSuchFieldException {
        try {
            log.info("Creating a new item: {}", nameField.getText());
            Field dateField = Category.class.getDeclaredField("category");
            Pair<Boolean, Category> result = entityManagerWrapper.findEntityByVal(Category.class, dateField, categoryComboBox.getValue());
            boolean success = inventoryServices.createNewItem(nameField.getText(), Double.parseDouble(priceField.getText()),
                    Double.parseDouble(arrivalPriceField.getText()), Integer.parseInt(quantityField.getText()), result.y());
            if (success) {
                log.info("New item created successfully: {}", nameField.getText());
                searchField.setText("");
                clearFields();
            } else {
                log.error("Failed to create new item: {}", nameField.getText());
                logEmployee.createLog("Create Item Error", "Failed to create item: " + nameField.getText());
            }
        } catch (Exception e) {
            log.error("Error creating a new item: ", e);
            logEmployee.createLog("Create Item Error", "Error creating new item: " + e.getMessage());
        }
    }

    /**
     * Deletes an item from the inventory based on its ID.
     */
    @FXML
    private void deleteItem() {
        try {
            log.info("Deleting item with ID: {}", idField.getText());
            Field field1 = NomenclatureDetails.class.getDeclaredField("item");
            Field field2 = TransactionDetail.class.getDeclaredField("item");
            inventoryServices.CascadeConnections(entityManagerWrapper.findEntityById(Item.class,Integer.parseInt(idField.getText())).y(),field1,field2);
            boolean success = entityManagerWrapper.deleteEntityById(Item.class, Integer.parseInt(idField.getText()));
            if (success) {
                log.info("Item deleted successfully with ID: {}", idField.getText());
                searchField.setText("");
                clearFields();
            } else {
                log.error("Failed to delete item with ID: {}", idField.getText());
                logEmployee.createLog("Delete Item Error", "Failed to delete item with ID: " + idField.getText());
            }
        } catch (Exception e) {
            log.error("Error deleting item: ", e);
            logEmployee.createLog("Delete Item Error", "Error deleting item: " + e.getMessage());
        }
    }

    /**
     * Loads categories into the ComboBox for category selection.
     */
    @FXML
    private void BCat() {
        try {
            log.info("Loading categories.");
            catCliSup = 1;
            inventoryServices.BCatSupCli(catCliSupComboBox, catCliSup);
        } catch (Exception e) {
            log.error("Error loading categories: ", e);
        }
    }

    /**
     * Loads suppliers into the ComboBox for supplier selection.
     */
    @FXML
    private void BSup() {
        try {
            log.info("Loading suppliers.");
            catCliSup = 2;
            inventoryServices.BCatSupCli(catCliSupComboBox, catCliSup);
        } catch (Exception e) {
            log.error("Error loading suppliers: ", e);
        }
    }

    /**
     * Loads clients into the ComboBox for client selection.
     */
    @FXML
    private void BCli() {
        try {
            log.info("Loading clients.");
            catCliSup = 3;
            inventoryServices.BCatSupCli(catCliSupComboBox, catCliSup);
        } catch (Exception e) {
            log.error("Error loading clients: ", e);
        }
    }

    /**
     * Creates a new category, supplier, or client based on the input in the text field.
     */
    @FXML
    private void catCliSupCreate() {
        try {
            if (!catCliSupText.getText().isEmpty() && catCliSup != 0) {
                log.info("Creating category/supplier/client: {}", catCliSupText.getText());
                inventoryServices.catCliSupCreate(catCliSup, catCliSupText);
                catCliSupText.clear();
            }
        } catch (Exception e) {
            log.error("Error creating category/supplier/client: ", e);
        }
    }

    /**
     * Deletes a category, supplier, or client based on the selected value in the ComboBox.
     *
     * @throws NoSuchFieldException if an error occurs while accessing fields during the deletion.
     */
    @FXML
    private void catCliSupDelete() throws NoSuchFieldException {
        try {
            if (catCliSupComboBox.getValue() != null) {
                Field field = switch (catCliSup) {
                    case 1 -> Category.class.getDeclaredField("idCategory");
                    case 2 -> Suppliers.class.getDeclaredField("idSupplier");
                    case 3 -> Client.class.getDeclaredField("id");
                    default -> throw new IllegalStateException("Unexpected value: " + catCliSup);
                };
                log.info("Deleting category/supplier/client: {}", catCliSupComboBox.getValue());
                inventoryServices.catCliSupDelete(catCliSupComboBox, catCliSup, field);
            }
        } catch (Exception e) {
            log.error("Error deleting category/supplier/client: ", e);
        }
    }
}
