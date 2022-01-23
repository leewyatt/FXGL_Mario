package com.leewyatt.mario;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.dsl.components.LiftComponent;
import com.almasb.fxgl.dsl.views.ScrollingBackgroundView;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import com.almasb.fxgl.input.view.KeyView;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.leewyatt.mario.components.*;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * 
 */
public class MarioFactory implements EntityFactory {

    @Spawns("background")
    public Entity newBackground(SpawnData data) {

        Image bgImageName = new Image(getAssetLoader().getStream("/assets/textures/"+"background/" + data.get("bgImageName")));
        ScrollingBackgroundView bgView = new ScrollingBackgroundView(bgImageName, getAppWidth(), getAppHeight());
        return entityBuilder(data)
                .view(bgView)
                .zIndex(-1)
                //.with(new IrremovableComponent())
                .build();
    }

    @Spawns("platform")
    public Entity newPlatform(SpawnData data) {

        PhysicsComponent physicsComponent = new PhysicsComponent();
        HitBox hitBox = getHitBox(data);
        return entityBuilder(data)
                .type(MarioType.PLATFORM)
                .bbox(hitBox)
                .with(physicsComponent)
                .build();
    }

    private HitBox getHitBox(SpawnData data) {
        HitBox hitBox;
        if (data.getData().containsKey("polygon")) {
            Polygon polygon1 = data.get("polygon");
            ObservableList<Double> points = polygon1.getPoints();
            double[] ps = new double[points.size()];
            for (int i = 0; i < ps.length; i++) {
                ps[i] = points.get(i);
            }
            hitBox = new HitBox(BoundingShape.polygon(ps));
        } else {
            hitBox = new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height")));
        }
        return hitBox;
    }

    @Spawns("exitTrigger")
    public Entity newExitTrigger(SpawnData data) {
        return entityBuilder(data)
                .type(MarioType.EXIT_TRIGGER)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("doorTop")
    public Entity newDoorTop(SpawnData data) {
        return entityBuilder(data)
                .type(MarioType.DOOR_TOP)
                .opacity(0)
                .build();
    }

    @Spawns("doorBot")
    public Entity newDoorBot(SpawnData data) {
        return entityBuilder(data)
                .type(MarioType.DOOR_BOT)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .opacity(0)
                .with(new CollidableComponent(false))
                .build();
    }

    @Spawns("keyPrompt")
    public Entity newKeyPrompt(SpawnData data) {
        return entityBuilder(data)
                .type(MarioType.KEY_PROMPT)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("exitSign")
    public Entity newExitSign(SpawnData data) {
        return entityBuilder(data)
                .type(MarioType.EXIT_SIGN)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("keyCode")
    public Entity newKeyCode(SpawnData data) {
        String key = data.get("key");
        KeyCode keyCode = KeyCode.getKeyCode(key);
        //电梯组件, 上下或者左右移动实体
        LiftComponent lift = new LiftComponent();
        lift.setGoingUp(true);
        lift.yAxisSpeedDuration(6, Duration.millis(0.75));
        //按键视图, 比如按键  E 等 ,主要用于场景中提示玩家的操作
        KeyView keyView = new KeyView(keyCode, Color.YELLOW, 24);
        keyView.setCache(true);
        keyView.setCacheHint(CacheHint.SCALE);

        return entityBuilder(data)
                .view(keyView)
                .with(lift)
                .zIndex(100)
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        //BodyType.DYNAMIC 正质量，由力确定的非零速度，由求解器移动。
        physics.setBodyType(BodyType.DYNAMIC);
        //添加地面传感器; 
        physics.addGroundSensor(new HitBox("GROUND_SENSOR", new Point2D(16, 38), BoundingShape.box(6, 8)));
        //Friction is 0, preventing the player from sticking to the wall
        //But I think a bit of friction is needed so the player can lie on the wall and it's fun too;
        physics.setFixtureDef(new FixtureDef().friction(0.5f));

        return entityBuilder(data)
                .type(MarioType.PLAYER)
                //player头 圆形半径12
                .bbox(new HitBox(new Point2D(5, 5), BoundingShape.circle(12)))
                //player身体 矩形
                .bbox(new HitBox(new Point2D(10, 25), BoundingShape.box(10, 17)))
                .with(new CollidableComponent(true))
                .with(new IrremovableComponent())
                .with(physics)//在玩家组件的上面添加一个物理组件, 这样玩家组件里才能获取到这个物理组件
                .with(new PlayerComponent())
                .build();
    }

    @Spawns("button")
    public Entity newButton(SpawnData data) {
        Entity keyEntity = FXGL.getGameWorld().create("keyCode",
                new SpawnData(data.getX(), data.getY() - 50).put("key", "E")
        );
        keyEntity.setOpacity(0);

        return entityBuilder(data)
                .type(MarioType.BUTTON)
                .viewWithBBox(texture("button.png", 20, 18))
                .with(new CollidableComponent(true))
                .with("keyEntity", keyEntity)
                .build();
    }

    @Spawns("spike")
    public Entity newSpikes(SpawnData data) {
        return entityBuilder(data)
                .type(MarioType.SPIKE)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .bbox(getHitBox(data))
                .collidable()
                .build();
    }

    /**
     * TODO 背景图片/ move平台 /temp 的平台 ,因为每个地图风格不同, 那么应该使用不同的素材, 所以,需要把图片也传递进来
     * @param data
     * @return
     */
    @Spawns("movePlatform")
    public Entity newMovePlatform(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.KINEMATIC);
        physics.setFixtureDef(new FixtureDef().friction(1F));

        return entityBuilder(data)
                .type(MarioType.MOVE_PLATFORM)
                .collidable()
                .viewWithBBox(texture("ice/tundra_140x40.png"))
                .with(physics)
                .with(new MovePlatformComponent(data))
                .build();
    }

    @Spawns("tempPlatform")
    public Entity newTempPlatform(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.KINEMATIC);
        physics.setFixtureDef(new FixtureDef().friction(1F));
        return entityBuilder(data)
                .type(MarioType.TEMP_PLATFORM)
                //.viewWithBBox(texture("ice/iceBlock.png"))
                .bbox(new HitBox(BoundingShape.box(70,70)))
                .collidable()
                .with(physics)
                .with(new TempPlatformComponent())
                .build();
    }

    AnimationChannel acIceDisappear = new AnimationChannel(FXGL.image("ice/ice_disappear.png",1273/2.0,179/2.0), Duration.seconds(0.3), 7);

    @Spawns("iceDisappear")
    public Entity newIceDisappear(SpawnData data) {
        AnimatedTexture animatedTexture = new AnimatedTexture(acIceDisappear);
        animatedTexture.loop();
        return entityBuilder(data)
                .at(data.getX()-10, data.getY()-30)
                .view(animatedTexture)
                .with(new ExpireCleanComponent(Duration.seconds(0.3)))
                .build();
    }



    @Spawns("coin")
    public Entity newCoin(SpawnData data) {
        return entityBuilder(data)
                .type(MarioType.COIN)
                .viewWithBBox(texture("coinGold.png"))
                .collidable()
                .build();
    }

    AnimationChannel acIceWater = new AnimationChannel(FXGL.image("ice/iceWater.png"), Duration.seconds(1), 2);
    @Spawns("iceWater")
    public Entity newIceWater(SpawnData data) {
        AnimatedTexture animatedTexture = new AnimatedTexture(acIceWater);
        animatedTexture.loop();
        return entityBuilder(data)
                .type(MarioType.ICE_WATER)
                .view(animatedTexture)
                .build();
    }


    AnimationChannel acCollideCoin = new AnimationChannel(FXGL.image("ice/collideCoin.png"),7,128,128,Duration.seconds(0.45),0,13);
    @Spawns("collideCoin")
    public Entity newCollideCoin(SpawnData data) {
        AnimatedTexture animatedTexture = new AnimatedTexture(acCollideCoin);
        animatedTexture.loop();
        return entityBuilder(data)
                .view(animatedTexture)
                .with(new ExpireCleanComponent(Duration.seconds(0.45)))
                .build();
    }

    @Spawns("laser")
    public Entity newLaser(SpawnData data) {
        return entityBuilder(data)
                .type(MarioType.LASER)
                .collidable()
                .with(new LaserComponent())
                .zIndex(100)
                .build();
    }

    @Spawns("springboard")
    public Entity newSpringboard(SpawnData data) {
        return entityBuilder(data)
                .type(MarioType.SPRINGBOARD)
                .with(new PhysicsComponent())
                .collidable()
                .bbox(new HitBox(new Point2D(1,20),BoundingShape.box(68,6)))
                .with(new SpringboardComponent())
                .build();
    }

}
