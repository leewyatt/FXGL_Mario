package com.leewyatt.mario.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

/**
 * 
 */
public class PlayerComponent extends Component {

    /**
     * 物理组件会被自动注入
     */
    private PhysicsComponent physics;

    //@Override
    //public boolean isComponentInjectionRequired() {
    //    //true 会自动添加其他组件的依赖
    //    return true;
    //}


    private AnimatedTexture texture;

    private AnimationChannel animIdle,animWalk;

    /**
     * 每次落地后能够跳跃的次数
     */
    private static final int MAX_JUMPS=2;
    private int jumps = MAX_JUMPS;

    public PlayerComponent() {
        Image image = FXGL.image("player.png");

        animIdle = new AnimationChannel(image, 4, 32, 42, Duration.seconds(1), 1, 1);
        animWalk = new AnimationChannel(image, 4, 32, 42, Duration.seconds(1), 0, 3);

        texture = new AnimatedTexture(animIdle);
        //循环播放
        texture.loop();
    }

    @Override
    public void onAdded() {
        //设置缩放的中心位置在图片的中间 32/2 =16; 42/2 = 21;为了等下水平镜像翻转图片
        entity.getTransformComponent().setScaleOrigin(new Point2D(16, 21));
        entity.getViewComponent().addChild(texture);
        //如果玩家已经落地.那么恢复蹦跶次数
        physics.onGroundProperty().addListener((ob, ov, nv) ->{
            if (nv) {
                jumps = MAX_JUMPS;
            }
        });
    }

    /**
     * 如果实体存在于世界当中,并且没有暂停, 那么每一帧都会调用到这个方法
     * @param tpf 每一帧的时间
     */
    @Override
    public void onUpdate(double tpf) {
        //如果水平移动了, 那么循环播放走路动画
        if (physics.isMovingX()) {
            if (texture.getAnimationChannel() != animWalk) {
                texture.loopAnimationChannel(animWalk);
            }
        }else {//如果是静止 或者 垂直运行,那么循环播放待机动画
            if (texture.getAnimationChannel() != animIdle) {
                texture.loopAnimationChannel(animIdle);
            }
        }
    }

    public void left() {
        //因为view是向右运动的图片,所以向左的时候,需要水平翻转这个实体
        getEntity().setScaleX(-1);
        //设置水平方向每秒钟的速度是-170像素
        physics.setVelocityX(-170);
    }

    public void right() {
        getEntity().setScaleX(1);
        physics.setVelocityX(170);
    }

    public void stop() {
        physics.setVelocityX(0);
    }

    public void jump() {
        if (jumps == 0) {
            return;
        }
        //设置垂直方向每秒钟的运动-300像素
        physics.setVelocityY(-300);
        jumps-- ;
    }


}
