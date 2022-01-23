package com.leewyatt.mario.collisions;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.leewyatt.mario.MarioType;

/**
 * 
 */
public class PlayerButtonHandler extends CollisionHandler {
    /**
     * The order of types determines the order of entities in callbacks.
     */
    public PlayerButtonHandler() {
        super(MarioType.PLAYER, MarioType.BUTTON);
    }

    @Override
    protected void onCollisionBegin(Entity player, Entity btn) {
        Entity keyEntity = btn.getObject("keyEntity");
        if (!keyEntity.isActive()) {
            keyEntity.setProperty("activated", false);
            FXGL.getGameWorld().addEntity(keyEntity);
        }
        keyEntity.setOpacity(1);
    }

    @Override
    protected void onCollisionEnd(Entity player, Entity btn) {
        Entity keyEntity = btn.getObject("keyEntity");
        if (!keyEntity.getBoolean("activated")) {
            keyEntity.setOpacity(0);
        }
    }
}
