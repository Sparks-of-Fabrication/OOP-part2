package com.sparks.of.fabrication.oop2.scenes;

import com.sparks.of.fabrication.oop2.Singleton;
import com.sparks.of.fabrication.oop2.models.Employee;
import com.sparks.of.fabrication.oop2.models.RoleModel;
import com.sparks.of.fabrication.oop2.users.Role;
import com.sparks.of.fabrication.oop2.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateEmployee_scene {
    private static final Logger log = LoggerFactory.getLogger(CreateEmployee_scene.class);
    @FXML private Label messages;
    @FXML private PasswordField passwordEmployeeField;
    @FXML private TextField emailEmployeeField;
    @FXML private TextField nameField;
    @FXML private ComboBox<Role> roleComboBox = new ComboBox<>();

    private final EntityManagerWrapper entityManager = Singleton.getInstance(EntityManagerWrapper.class);

    private final Validation userValidation = new UserValidation();

    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        System.out.println(Arrays.toString(Role.values()));
    }

    public void createEmployee() throws NoSuchFieldException, RuntimeException {
        StringBuilder builder = new StringBuilder();

        if(passwordEmployeeField.getText().isEmpty() || emailEmployeeField.getText().isEmpty()
                || nameField.getText().isEmpty() || roleComboBox.getValue() == null) {
            builder.append("field must not be empty!\n");
            messages.setText(builder.toString());
            return;
        }

        List<String> inputs = new ArrayList<>();

        List<ValidationTypes> validationTypes = new ArrayList<>(List.of(new ValidationTypes[]{ValidationTypes.EMAIL, ValidationTypes.PASSWORD}));

        if(entityManager == null) {
            log.error("Entity manager wasn't initialized! {}", CreateEmployee_scene.class);
            throw new RuntimeException("Entity manager wasn't initialized!");
        }

        inputs.add(emailEmployeeField.getText());
        inputs.add(passwordEmployeeField.getText());

        Pair<Boolean, List<String>> response =  userValidation.validate(validationTypes, inputs);

        for(String mes: response.y()) {
            builder.append(mes).append("\n");
        }

        if(response.x()) {
            String salt = BCrypt.gensalt(13);
            String hashedPassword = BCrypt.hashpw(passwordEmployeeField.getText(), salt);
            RoleModel role = entityManager.findEntityByVal(RoleModel.class, RoleModel.class.getDeclaredField("role"), roleComboBox.getValue()).y();

            Employee employee = new Employee();
            employee.setEmail(emailEmployeeField.getText());
            employee.setPassword(hashedPassword);
            employee.setName(nameField.getText());
            employee.setRole(role);

            boolean entityGen = entityManager.genEntity(employee);

            if(entityGen) {
                builder.setLength(0);
                builder.append("Employee created!");
            }
        }

        messages.setText(builder.toString());
    }
}
