package com.leewyatt.mario.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.leewyatt.mario.MarioType;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.List;

/**
 * 
 */
public class SpringboardComponent extends Component {
    Image up = FXGL.image("springboardUp.png");
    Image down = FXGL.image("springboardDown.png");

    AnimationChannel ac = new AnimationChannel(List.of(up, down, up), Duration.seconds(0.3));
    AnimatedTexture animatedTexture = new AnimatedTexture(ac);
    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(animatedTexture);

        FXGL.onCollisionBegin(MarioType.PLAYER, MarioType.SPRINGBOARD, (player, springboard)->{
            player.getComponent(PhysicsComponent.class).setVelocityY(-800);
            animatedTexture.play();
            FXGL.play("jump.wav");
        });

    }
}
