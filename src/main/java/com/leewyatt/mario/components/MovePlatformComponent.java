package com.leewyatt.mario.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.time.LocalTimer;
import javafx.util.Duration;

/**
 * 
 */
public class MovePlatformComponent extends Component {
    private PhysicsComponent physic;
    private LocalTimer moveTimer = FXGL.newLocalTimer();
    private Duration seconds;
    private double speed;
    private boolean isHor;

    public MovePlatformComponent(SpawnData data) {
        this(data.<Integer>get("millis"),data.<Integer>get("speed"),data.<Boolean>get("isHor"));
    }


    public MovePlatformComponent(int millis ,int speed,boolean isHor) {
        seconds = Duration.millis(millis);
        this.speed = speed;
        this.isHor = isHor;
    }

    @Override
    public void onAdded() {
        moveTimer.capture();
    }

    @Override
    public void onUpdate(double tpf) {
        if (moveTimer.elapsed(seconds)) {
            speed = speed * -1;
            moveTimer.capture();
        }
        if (isHor) {
            physic.setVelocityX(speed);
        }else {
            physic.setVelocityY(speed);
        }
    }
}
