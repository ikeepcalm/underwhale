module dev.ua.ikeepcalm.underwhale {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires kotlin.stdlib;

    opens dev.ua.ikeepcalm.underwhale;

    exports dev.ua.ikeepcalm.underwhale;
    exports dev.ua.ikeepcalm.underwhale.game;
    opens dev.ua.ikeepcalm.underwhale.game;
}