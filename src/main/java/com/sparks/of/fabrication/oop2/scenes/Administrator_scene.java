package com.sparks.of.fabrication.oop2.scenes;

import com.sparks.of.fabrication.oop2.Singleton;
import com.sparks.of.fabrication.oop2.utils.EntityManagerWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class Administrator_scene {
    @FXML
    private Button btnInventory;

    @FXML
    private Button btnCreateEmployee;

    @FXML
    private Button btnCheckout;

    private EntityManagerWrapper entityManagerWrapper = Singleton.getInstance(EntityManagerWrapper.class);
    private SceneLoader sceneLoader = Singleton.getInstance(SceneLoader.class);

    @FXML
    private void handleInventory() {
        System.out.println("Managing Inventory...");
        try {
            sceneLoader.loadScene("scenes/inventory_scene.fxml", 500, 500, "Inventory", true, new Stage());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateEmployee() {
        System.out.println("Creating Employee...");
        try {
            sceneLoader.loadScene("scenes/createEmployee_scene.fxml", 500, 500, "Create Employee", true, new Stage());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCheckout() {
        System.out.println("Processing Checkout...");
        //load scene
    }
}
