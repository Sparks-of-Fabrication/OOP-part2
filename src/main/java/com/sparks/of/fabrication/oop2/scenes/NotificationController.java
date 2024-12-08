package com.sparks.of.fabrication.oop2.scenes;

import com.sparks.of.fabrication.oop2.models.Notification;
import com.sparks.of.fabrication.oop2.utils.EntityManagerWrapper;
import com.sparks.of.fabrication.oop2.Singleton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class NotificationController {

    @FXML
    private TableView<Notification> notificationTable;

    @FXML
    private TableColumn<Notification, Long> idNotificationColumn;

    @FXML
    private TableColumn<Notification, Long> idEmployeeColumn;

    @FXML
    private TableColumn<Notification, String> messageColumn;

    @FXML
    private TableColumn<Notification, String> statusColumn;

    @FXML
    private TableColumn<Notification, String> dateSentColumn;

    private final EntityManagerWrapper entityManager = Singleton.getInstance(EntityManagerWrapper.class);

    @FXML
    public void initialize() {
        idNotificationColumn.setCellValueFactory(new PropertyValueFactory<>("idNotification"));
        idEmployeeColumn.setCellValueFactory(new PropertyValueFactory<>("idEmployee"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateSentColumn.setCellValueFactory(new PropertyValueFactory<>("dateSent"));

        idNotificationColumn.prefWidthProperty().bind(notificationTable.widthProperty().multiply(0.1));
        idEmployeeColumn.prefWidthProperty().bind(notificationTable.widthProperty().multiply(0.1));
        messageColumn.prefWidthProperty().bind(notificationTable.widthProperty().multiply(0.4));
        statusColumn.prefWidthProperty().bind(notificationTable.widthProperty().multiply(0.2));
        dateSentColumn.prefWidthProperty().bind(notificationTable.widthProperty().multiply(0.2));

        loadNotifications();
    }

    private void loadNotifications() {
        ObservableList<Notification> notifications;

        List<Notification> notificationList = entityManager.findAllEntities(Notification.class);

        if (notificationList == null || notificationList.isEmpty()) {
            notifications = FXCollections.observableArrayList();
        } else {
            notifications = FXCollections.observableArrayList(notificationList);
        }

        notificationTable.setItems(notifications);
    }
}
