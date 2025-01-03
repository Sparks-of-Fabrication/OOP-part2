package com.sparks.of.fabrication.oop2.scenes;

import com.sparks.of.fabrication.oop2.utils.Singleton;
import com.sparks.of.fabrication.oop2.models.Employee;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.LogManager;

import com.sparks.of.fabrication.oop2.utils.*;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class LoginController {

    private final Logger log = LogManager.getLogger(LoginController.class);
    private final SceneLoader loader = Singleton.getInstance(SceneLoader.class);
    private final EntityManagerWrapper entityManagerWrapper = Singleton.getInstance(EntityManagerWrapper.class);
    private static Employee loggedInEmployee = Singleton.getInstance(Employee.class);

    @FXML
    private TextField username;

    @FXML
    private PasswordField passwordField;

    @FXML
    protected void checkCredentials() {
        String user = username.getText();
        String password = passwordField.getText();

        if (user.isEmpty() || password.isEmpty()) {
            showAlert("Enter both user and password");
        }

        try {
            Pair<Boolean, Employee> employeeResult = entityManagerWrapper.findEntityByVal(Employee.class,
                    Employee.class.getDeclaredField("email"), user);

            if (employeeResult.x()) {
                loggedInEmployee = employeeResult.y();
                LogEmployee.initializeLogEmployee();
                Singleton.getInstance(Employee.class, loggedInEmployee);
                if (BCrypt.checkpw(password, loggedInEmployee.getPassword())) {
                    loader.loadScene("scenes/manager_scene.fxml", 500, 300, "Main", false, new Stage());
                    username.getScene().getWindow().hide();
                } else {
                    showAlert("Invalid password");
                }
            } else {
                showAlert("User not found");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            showAlert("An error occurred during login");
        }
    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
