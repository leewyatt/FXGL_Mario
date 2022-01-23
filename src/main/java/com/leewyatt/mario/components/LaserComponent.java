package com.leewyatt.mario.components;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.animation.*;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/**
 * 
 */
public class LaserComponent extends Component {

    private Region rect;
    private boolean isFirstLevel = true;

    private int pauseTime;
    private float range;
    private String strColor;
    private String dir;
    private int initLen  = 15;

    @Override
    public void onAdded() {
        rect = new Region();
        pauseTime = entity.getProperties().getInt("pauseTime");
        range = entity.getProperties().getValue("range");
        strColor = entity.getProperties().getString("color");
        dir = entity.getProperties().getString("dir").toLowerCase();

        if (dir.equals("left") || dir.equals("right")) {
            horShootLaser();
        } else {
            verShootLaser();
        }

    }

    private void horShootLaser() {
        rect.setPrefSize(0, 15);
        rect.setStyle("-fx-background-color: white;-fx-border-width: 3 0 3 0;-fx-border-color:" + strColor + ";");

        entity.getViewComponent().addChild(rect);
        double initX = entity.getX();
        rect.prefWidthProperty().addListener((observableValue, oldWidth, newWidth) -> {
            if (entity.isActive()) {
                if (isFirstLevel) {
                    if (dir.equals("left")) {
                        entity.setX(initX - newWidth.doubleValue());
                    }
                }
                entity.getBoundingBoxComponent().clearHitBoxes();
                entity.getBoundingBoxComponent().addHitBox(new HitBox(BoundingShape.box(rect.getWidth(), rect.getHeight())));
            }
        });
        Timeline shootLaser = new Timeline(new KeyFrame(Duration.seconds(0.3), event -> isFirstLevel = false, new KeyValue(rect.prefWidthProperty(), range)));

        PauseTransition pause1 = new PauseTransition(Duration.seconds(1.5));
        Timeline clearLaser = new Timeline(new KeyFrame(Duration.seconds(0.3), event -> {
            if (entity.isActive()) {
                isFirstLevel = true;
                entity.setX(initX);
            }
        }, new KeyValue(rect.prefWidthProperty(), 0)));

        PauseTransition pause2 = new PauseTransition(Duration.millis(pauseTime));

        Timeline readyAnim =  new Timeline(
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(rect.prefWidthProperty(), initLen))
        );


        SequentialTransition laserAnim = new SequentialTransition(readyAnim,shootLaser, pause1, clearLaser, pause2);
        laserAnim.setCycleCount(Animation.INDEFINITE);
        laserAnim.play();
        entity.setOnNotActive(laserAnim::stop);
    }

    private void verShootLaser() {
        rect.setPrefSize(15, 0);
        rect.setStyle("-fx-background-color: white;-fx-border-width: 0 3 0 3;-fx-border-color:" + strColor + ";");
        entity.getViewComponent().addChild(rect);
        double initY = entity.getY();
        rect.prefHeightProperty().addListener((observableValue, ov, nv) -> {
            if (entity.isActive()) {
                if (isFirstLevel) {
                    if (dir.equals("up")) {
                        entity.setY(initY - nv.doubleValue());
                    }
                }
                entity.getBoundingBoxComponent().clearHitBoxes();
                entity.getBoundingBoxComponent().addHitBox(new HitBox(BoundingShape.box(rect.getWidth(), rect.getHeight())));
            }
        });
        Timeline shootLaser = new Timeline(new KeyFrame(Duration.seconds(0.3), event -> isFirstLevel = false, new KeyValue(rect.prefHeightProperty(), range)));
        PauseTransition pause1 = new PauseTransition(Duration.seconds(1.5));
        Timeline clearLaser = new Timeline(new KeyFrame(Duration.seconds(0.3), event -> {
            if (entity.isActive()) {
                isFirstLevel = true;
                entity.setY(initY);
            }
        }, new KeyValue(rect.prefHeightProperty(), 0)));
        PauseTransition pause2 = new PauseTransition(Duration.millis(pauseTime));

        Timeline readyAnim =  new Timeline(
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(rect.prefHeightProperty(), initLen))
        );

        SequentialTransition laserAnim = new SequentialTransition(readyAnim,shootLaser, pause1, clearLaser, pause2);
        laserAnim.setCycleCount(Animation.INDEFINITE);
        laserAnim.play();
        entity.setOnNotActive(laserAnim::stop);
    }

}
