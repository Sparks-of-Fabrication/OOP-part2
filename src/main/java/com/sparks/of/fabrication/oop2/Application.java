package com.sparks.of.fabrication.oop2;

import com.sparks.of.fabrication.oop2.models.RoleModel;
import com.sparks.of.fabrication.oop2.scenes.SceneLoader;
import com.sparks.of.fabrication.oop2.users.Role;
import com.sparks.of.fabrication.oop2.utils.EntityManagerWrapper;
import com.sparks.of.fabrication.oop2.utils.Env;
import jakarta.persistence.EntityManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

public class Application extends javafx.application.Application {
    private static final Env env = Singleton.getInstance(Env.class, new Env()).y();
    private static final EntityManagerWrapper entityManager =
            Singleton.getInstance(EntityManagerWrapper.class, new EntityManagerWrapper(env)).y();
    private static final Logger log = LogManager.getLogger(Application.class);

    @Override
    public void start(Stage stage) throws IOException {
        SceneLoader sceneLoader = new SceneLoader();
        sceneLoader.loadScene("scenes/main_scene.fxml",450, 240,"Main",false,stage);
    }

    private static void setupDB() throws NoSuchFieldException {
        for (Role role : Role.values()) {
            if(!entityManager.findEntityByVal(RoleModel.class, RoleModel.class.getDeclaredField("role"), role).x()) {
                RoleModel roleModel = new RoleModel();
                roleModel.setRole(role);
                if(!entityManager.genEntity(roleModel)) {
                    log.error("Couldn't save to database! {}", roleModel.getId());
                    throw new RuntimeException("Couldn't save to database!");
                }
            }
        }
    }

    private static void exit() {
        boolean exit = entityManager.cleanUp();

        if(exit) {
            log.info("Application exited successfully");
            System.exit(0);
        }
        else {
            log.info("Application exited unsuccessfully");
            System.exit(1);
        }
    }

    public static void main(String[] args) throws NoSuchFieldException {
        setupDB();
        launch();
        exit();
    }
}