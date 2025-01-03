package com.sparks.of.fabrication.oop2.scenes.arrivalGoods;

import com.sparks.of.fabrication.oop2.utils.Singleton;
import com.sparks.of.fabrication.oop2.models.*;
import com.sparks.of.fabrication.oop2.utils.EntityManagerWrapper;
import com.sparks.of.fabrication.oop2.utils.Pair;
import com.sparks.of.fabrication.oop2.utils.LogEmployee;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.lang.reflect.Field;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The controller for managing the Arrival Goods scene, handling the display and saving of items and invoice data.
 */
public class ArrivalGoodsScene {

    private static final Logger log = LogManager.getLogger(ArrivalGoodsScene.class);

    private final LogEmployee logEmployee = Singleton.getInstance(LogEmployee.class);

    @FXML private TableView<Item> arrivalTable;
    @FXML private TableColumn<Item, Long> colCode;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, String> colMeasure;
    @FXML private TableView<AmSData> AmS;
    @FXML private TableColumn<AmSData, Integer> colQuantity;
    @FXML private TableColumn<AmSData, Double> colArrivalPrice;
    @FXML private TableColumn<AmSData, Double> colSellingPrice;
    @FXML private TextField txtDocumentNumber;
    @FXML private ComboBox<String> SupplierBox;
    @FXML private DatePicker lblSystemDate;
    @FXML private ComboBox<ArrivalState> cmbStatus;

    private int currentIndex = -1;
    private List<Nomenclature> nomenclatureList;
    private ObservableList<Item> itemList;
    private ObservableList<AmSData> amsData = FXCollections.observableArrayList();
    private ArrivalGoodsService arrivalGoodsService;
    private final EntityManagerWrapper entityManagerWrapper = Singleton.getInstance(EntityManagerWrapper.class);
    private final Employee loggedInEmployee = Singleton.getInstance(Employee.class);
    private InvoiceStore currentInvoiceStore;

    /**
     * Initializes the ArrivalGoodsScene by setting up the table and date listeners.
     */
    @FXML
    private void initialize() {
        log.info("Initializing ArrivalGoodsScene...");
        arrivalGoodsService = new ArrivalGoodsService();
        nomenclatureList = new ArrayList<>();
        itemList = FXCollections.observableArrayList();

        TableViewSetup.configureTableColumns(arrivalTable, colCode, colName, colMeasure, AmS, colQuantity, colArrivalPrice, colSellingPrice, SupplierBox);

        lblSystemDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                log.info("System date changed to: {}", newValue);
                loadItemsFromDatabase(newValue);
            }
        });

        lblSystemDate.setValue(LocalDate.now());
        cmbStatus.setItems(FXCollections.observableArrayList(ArrivalState.values()));
        cmbStatus.setValue(ArrivalState.Open);
    }

    /**
     * Loads the items from the database for a given date.
     *
     * @param selectedDate the date for which to load items
     */
    private void loadItemsFromDatabase(LocalDate selectedDate) {
        log.info("Loading items from the database for date: {}", selectedDate);
        nomenclatureList = arrivalGoodsService.loadItems(selectedDate);
    }

    /**
     * Loads items and associated data for a specific nomenclature.
     *
     * @param currentIndex the index of the nomenclature
     */
    private void loadItemsForNomenclature(int currentIndex) {
        try {
            itemList.clear();
            amsData.clear();

            Pair<List<Item>, List<AmSData>> itemsAndData = arrivalGoodsService.loadItemsForNomenclature(nomenclatureList.get(currentIndex));
            itemList = FXCollections.observableArrayList(itemsAndData.x());
            amsData = FXCollections.observableArrayList(itemsAndData.y());

            arrivalTable.setItems(itemList);
            AmS.setItems(amsData);
            arrivalTable.refresh();
            AmS.refresh();
            log.info("Loaded items for nomenclature index: {}", currentIndex);
        } catch (Exception e) {
            log.error("Error loading items for nomenclature: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles the action for navigating to the first nomenclature.
     *
     * @param event the action event
     */
    @FXML
    private void handleFirstButtonAction(ActionEvent event) {
        if (!nomenclatureList.isEmpty()) {
            currentIndex = 0;
            log.info("Navigated to the first nomenclature.");
            loadItemsForNomenclature(currentIndex);
        }
    }

    /**
     * Handles the action for navigating to the previous nomenclature.
     *
     * @param event the action event
     */
    @FXML
    private void handlePreviousButtonAction(ActionEvent event) {
        if (currentIndex > 0) {
            currentIndex--;
            log.info("Navigated to the previous nomenclature. Current index: {}", currentIndex);
            loadItemsForNomenclature(currentIndex);
        }
    }

    /**
     * Handles the action for navigating to the next nomenclature.
     *
     * @param event the action event
     */
    @FXML
    private void handleNextButtonAction(ActionEvent event) {
        if (currentIndex < nomenclatureList.size() - 1) {
            currentIndex++;
            log.info("Navigated to the next nomenclature. Current index: {}", currentIndex);
            loadItemsForNomenclature(currentIndex);
        }
    }

    /**
     * Handles the action for navigating to the last nomenclature.
     *
     * @param event the action event
     */
    @FXML
    private void handleLastButtonAction(ActionEvent event) {
        if (!nomenclatureList.isEmpty()) {
            currentIndex = nomenclatureList.size() - 1;
            log.info("Navigated to the last nomenclature.");
            loadItemsForNomenclature(currentIndex);
        }
    }

    /**
     * Handles the action for creating a new nomenclature and setting up necessary data.
     *
     * @param event the action event
     */
    @FXML
    private void handleNewButtonAction(ActionEvent event) {
        currentInvoiceStore = new InvoiceStore();
        currentInvoiceStore.setStatus(false);
        currentInvoiceStore.setEmployee(loggedInEmployee);

        Nomenclature nomenclature = new Nomenclature();
        nomenclature.setEmployee(loggedInEmployee);
        nomenclatureList.add(nomenclature);

        currentIndex = nomenclatureList.size() - 1;
        log.info("Created new nomenclature. Current index: {}", currentIndex);
        logEmployee.createLog("New nomenclature created by employee: " + loggedInEmployee.getId(),"");

        loadItemsForNomenclature(currentIndex);
        arrivalGoodsService.toggleTableEditability(currentInvoiceStore, arrivalTable, AmS);
    }

    /**
     * Handles the action for saving the current invoice store and related data.
     *
     * @param event the action event
     * @throws NoSuchFieldException if a field cannot be found
     */
    @FXML
    private void handleSaveButtonAction(ActionEvent event) throws NoSuchFieldException {
        if (currentInvoiceStore != null) {
            Field field = Suppliers.class.getDeclaredField("name");
            Suppliers supplier = entityManagerWrapper.findEntityByVal(Suppliers.class, field, SupplierBox.getValue()).y();
            log.info("Saving invoice store with supplier: {}", supplier.getName());

            arrivalGoodsService.updateCurrentNomenclature(supplier, nomenclatureList, currentIndex, currentInvoiceStore);
            arrivalGoodsService.saveCurrentInvoiceStore(currentInvoiceStore, Integer.parseInt(txtDocumentNumber.getText()), Date.valueOf(lblSystemDate.getValue()));
            arrivalGoodsService.processArrivalTableItems(currentInvoiceStore, arrivalTable, AmS);
            arrivalGoodsService.finalizeInvoiceStore(currentInvoiceStore);

            logEmployee.createLog("Invoice saved by employee: " + loggedInEmployee.getId(),"");
            log.info("Invoice successfully saved.");
        }
    }

    /**
     * Handles the action for adding a new row to the arrival table.
     *
     * @param event the action event
     */
    @FXML
    private void handleAddRow(ActionEvent event) {
        Item item = new Item();
        arrivalTable.getItems().add(item);

        AmSData newEntry = new AmSData(0, 0.0, 0.0);
        amsData.add(newEntry);

        AmS.setItems(amsData);
        AmS.refresh();
        arrivalTable.refresh();
        txtDocumentNumber.clear();

        log.info("Added a new row to the table.");
    }
}
