package com.sparks.of.fabrication.oop2.scenes.arrivalGoods;

import com.sparks.of.fabrication.oop2.utils.Singleton;
import com.sparks.of.fabrication.oop2.models.*;
import com.sparks.of.fabrication.oop2.utils.EntityManagerWrapper;
import com.sparks.of.fabrication.oop2.utils.LogEmployee;
import com.sparks.of.fabrication.oop2.utils.Pair;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing the adding goods, including loading items from the database,
 * processing arrival data, updating nomenclature, and finalizing invoices.
 */
public class ArrivalGoodsService {

    private final EntityManagerWrapper entityManagerWrapper = Singleton.getInstance(EntityManagerWrapper.class);
    private static final Logger log = LogManager.getLogger(ArrivalGoodsService.class);
    private final LogEmployee logEmployee = Singleton.getInstance(LogEmployee.class);

    /**
     * Loads the list of nomenclatures for a given date from the database.
     *
     * @param date The date for which to load the nomenclature list.
     * @return A list of Nomenclature objects corresponding to the provided date.
     */
    public List<Nomenclature> loadItems(LocalDate date) {
        List<Nomenclature> nomenclatureList = new ArrayList<>();
        try {
            Field dateField = InvoiceStore.class.getDeclaredField("date");
            Pair<Boolean, List<InvoiceStore>> result = entityManagerWrapper.findEntityByValAll(InvoiceStore.class, dateField, java.sql.Date.valueOf(date));

            if (result.x()) {
                for (InvoiceStore invoice : result.y()) {
                    nomenclatureList.add(invoice.getNomenclatura());
                }
            }
            log.info("Loaded nomenclature list for date: {}", date);
        } catch (NoSuchFieldException e) {
            log.error("Field 'date' not found in InvoiceStore: ", e);
            logEmployee.createLog("Load Items Error", e.getMessage());
        } catch (Exception e) {
            log.error("Error loading nomenclatures for date: ", e);
            logEmployee.createLog("Load Items Error", e.getMessage());
        }
        return nomenclatureList;
    }

    /**
     * Loads the list of items and associated arrival data for a specific nomenclature.
     *
     * @param nomenclature The nomenclature for which to load the items.
     * @return A pair of lists: one containing items and the other containing AmSData.
     */
    public Pair<List<Item>, List<AmSData>> loadItemsForNomenclature(Nomenclature nomenclature) {
        List<Item> items = new ArrayList<>();
        List<AmSData> amsDataList = new ArrayList<>();
        try {
            Field nomenclatureField = NomenclatureDetails.class.getDeclaredField("nomenclature");
            List<String> joinFields = List.of("nomenclature", "item");

            Pair<Boolean, List<NomenclatureDetails>> nomenclatureDetailsPair = entityManagerWrapper.findEntitiesWithJoins(
                    NomenclatureDetails.class, nomenclatureField, nomenclature, joinFields);

            if (nomenclatureDetailsPair.x()) {
                for (NomenclatureDetails details : nomenclatureDetailsPair.y()) {
                    Item item = details.getItem();
                    int quantity = details.getItemQuantity();
                    items.add(item);
                    amsDataList.add(new AmSData(quantity, item.getArrivalPrice(), item.getPrice()));
                }
            }
            log.info("Loaded items for nomenclature: {}", nomenclature.getIdNomenclature());
        } catch (Exception e) {
            log.error("Error loading items for nomenclature: ", e);
            logEmployee.createLog("Load Items for Nomenclature Error", e.getMessage());
        }
        return new Pair<>(items, amsDataList);
    }

    /**
     * Updates the current nomenclature with the supplier and associates it with the given invoice store.
     *
     * @param supplier            The supplier to associate with the nomenclature.
     * @param nomenclatureList    The list of nomenclatures.
     * @param currentIndex        The current index of the nomenclature in the list.
     * @param currentInvoiceStore The invoice store to associate with the nomenclature.
     */
    protected void updateCurrentNomenclature(Suppliers supplier, List<Nomenclature> nomenclatureList, int currentIndex, InvoiceStore currentInvoiceStore) {
        try {
            Nomenclature currentNomenclature = nomenclatureList.get(currentIndex);
            currentNomenclature.setSuppliers(supplier);

            if (currentNomenclature.getIdNomenclature() == null) {
                entityManagerWrapper.genEntity(currentNomenclature);
            }
            currentInvoiceStore.setNomenclatura(currentNomenclature);
            log.info("Updated current nomenclature for supplier: {}", supplier.getName());
        } catch (Exception e) {
            log.error("Error updating current nomenclature: ", e);
            logEmployee.createLog("Update Current Nomenclature Error", e.getMessage());
        }
    }

    /**
     * Saves the current invoice store with the provided document number and date.
     *
     * @param currentInvoiceStore The invoice store to save.
     * @param number              The document number.
     * @param date                The date to associate with the invoice.
     */
    protected void saveCurrentInvoiceStore(InvoiceStore currentInvoiceStore, int number, Date date) {
        try {
            currentInvoiceStore.setNumber(number);
            currentInvoiceStore.setDate(date);
            entityManagerWrapper.genEntity(currentInvoiceStore);

            if (!currentInvoiceStore.getStatus()) {
                currentInvoiceStore.setStatus(true);
                entityManagerWrapper.genEntity(currentInvoiceStore);
            }
            log.info("Saved current invoice store with number: {}", number);
        } catch (Exception e) {
            log.error("Error saving current invoice store: ", e);
            logEmployee.createLog("Save Current Invoice Store Error", e.getMessage());
        }
    }

    /**
     * Processes the arrival table items and calculates the final price for the invoice store.
     *
     * @param currentInvoiceStore The invoice store to update.
     * @param arrivalTable        The table containing the items.
     * @param AmS                 The table containing the AmS data.
     */
    protected void processArrivalTableItems(InvoiceStore currentInvoiceStore, TableView<Item> arrivalTable, TableView<AmSData> AmS) {
        try {
            double finalPrice = 0.0;
            for (Item item : arrivalTable.getItems()) {
                Item dbItem = entityManagerWrapper.findEntityById(Item.class, item.getIdItem().intValue()).y();
                int rowIndex = arrivalTable.getItems().indexOf(item);

                if (rowIndex >= 0 && rowIndex < AmS.getItems().size()) {
                    finalPrice += updateItemAndNomenclatureDetails(dbItem, rowIndex, AmS, currentInvoiceStore.getNomenclatura());
                }
            }
            currentInvoiceStore.setFinalPrice(finalPrice);
            log.info("Processed arrival table items with final price: {}", finalPrice);
        } catch (Exception e) {
            log.error("Error processing arrival table items: ", e);
            logEmployee.createLog("Process Arrival Table Items Error", e.getMessage());
        }
    }

    /**
     * Updates the item and its associated nomenclature details for the given row in the arrival table.
     *
     * @param dbItem            The item to update.
     * @param rowIndex          The row index of the item in the table.
     * @param AmS               The table containing AmS data.
     * @param currentNomenclature The nomenclature to associate with the item.
     * @return The calculated value based on item quantity and price.
     */
    protected double updateItemAndNomenclatureDetails(Item dbItem, int rowIndex, TableView<AmSData> AmS, Nomenclature currentNomenclature) {
        try {
            TableColumn<AmSData, Integer> colQuantity = (TableColumn<AmSData, Integer>) AmS.getColumns().get(0);
            TableColumn<AmSData, Double> colArrivalPrice = (TableColumn<AmSData, Double>) AmS.getColumns().get(1);
            TableColumn<AmSData, Double> colSellingPrice = (TableColumn<AmSData, Double>) AmS.getColumns().get(2);

            Double newArrivalPrice = colArrivalPrice.getCellData(rowIndex);
            Double newSellingPrice = colSellingPrice.getCellData(rowIndex);
            Integer newTableQuantity = colQuantity.getCellData(rowIndex);

            boolean priceChanged = !dbItem.getArrivalPrice().equals(newArrivalPrice) || !dbItem.getPrice().equals(newSellingPrice);

            if (priceChanged) {
                dbItem.setArrivalPrice(newArrivalPrice);
                dbItem.setPrice(newSellingPrice);
            }
            dbItem.setQuantity(dbItem.getQuantity() + (newTableQuantity != null ? newTableQuantity : 0));
            entityManagerWrapper.genEntity(dbItem);

            NomenclatureDetails nomenclatureDetails = new NomenclatureDetails();
            nomenclatureDetails.setItem(dbItem);
            nomenclatureDetails.setItemQuantity(newTableQuantity != null ? newTableQuantity : 0);
            nomenclatureDetails.setItemPrice(newArrivalPrice);
            nomenclatureDetails.setNomenclature(currentNomenclature);
            entityManagerWrapper.genEntity(nomenclatureDetails);

            log.info("Updated item and nomenclature details for item ID: {}", dbItem.getIdItem());
            return dbItem.getArrivalPrice() * nomenclatureDetails.getItemQuantity();
        } catch (Exception e) {
            log.error("Error updating item and nomenclature details: ", e);
            logEmployee.createLog("Update Item Details Error", e.getMessage());
            return 0.0;
        }
    }

    /**
     * Finalizes the invoice store by saving it to the database.
     *
     * @param currentInvoiceStore The invoice store to finalize.
     */
    protected void finalizeInvoiceStore(InvoiceStore currentInvoiceStore) {
        try {
            entityManagerWrapper.genEntity(currentInvoiceStore);
            log.info("Finalized invoice store with ID: {}", currentInvoiceStore.getIdInvoice());
        } catch (Exception e) {
            log.error("Error finalizing invoice store: ", e);
            logEmployee.createLog("Finalize Invoice Store Error", e.getMessage());
        }
    }

    /**
     * Changes the editable state of the tables arrivalTable and AmS table based on the invoice store status.
     *
     * @param currentInvoiceStore The invoice store to check the status of.
     * @param arrivalTable        The table containing the items.
     * @param AmS                 The table containing the AmS data.
     */
    protected void toggleTableEditability(InvoiceStore currentInvoiceStore, TableView<Item> arrivalTable, TableView<AmSData> AmS) {
        boolean editable = currentInvoiceStore == null || !currentInvoiceStore.getStatus();
        arrivalTable.setEditable(editable);
        AmS.setEditable(editable);
        log.info("Toggled table editability: {}", editable);
    }
}
