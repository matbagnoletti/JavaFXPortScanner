module tpsit.javaportscanner.javafxportscanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires atlantafx.base;

    opens tpsit.javaportscanner.javafxportscanner to javafx.fxml;
    exports tpsit.javaportscanner.javafxportscanner;
}