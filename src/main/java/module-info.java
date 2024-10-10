module com.sparks.of.fabrication.oop2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires java.dotenv;
    requires org.apache.logging.log4j;
    requires jakarta.persistence;
    requires org.slf4j;
    requires annotations;

    opens com.sparks.of.fabrication.oop2 to javafx.fxml;
    exports com.sparks.of.fabrication.oop2;
    exports com.sparks.of.fabrication.oop2.utils;
    opens com.sparks.of.fabrication.oop2.utils to javafx.fxml;
}