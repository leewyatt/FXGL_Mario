package com.leewyatt.mario;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.GameView;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.view.KeyView;
import com.almasb.fxgl.input.virtual.VirtualButton;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.Texture;
import com.leewyatt.mario.collisions.PlayerButtonHandler;
import com.leewyatt.mario.components.PlayerComponent;
import com.leewyatt.mario.ui.LevelEndScene;
import com.leewyatt.mario.ui.MarioLoadingScene;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Map;

/**
 *  FXGL is very interesting;
 *  I studied the teacher's course;Teacher: @Almas Baimagambetov
 *  add 2 new levels to the Mario game;(Level6 and Level7)
 *  New elements: laser,springboard, spike, move_platform,temp_platform,ice_water
 */
public class MairoApp extends GameApplication {

    private static final int MAX_LEVEL = 7;
    private static final int STARTING_LEVEL = 5;

    /**
     * 每局结束都需要这个关卡显示,懒加载;
     * 仅当调用get时才初始化惰性值。 对get 的后续调用返回相同的值（实例）。
     */
    private LazyValue<LevelEndScene> levelEndSceneValue =
            new LazyValue<>(LevelEndScene::new);

    public Entity player;

    @Override
    protected void initSettings(GameSettings settings) {
        //设置窗口的宽高
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public LoadingScene newLoadingScene() {
                return new MarioLoadingScene();
            }
        });
        /*
        DEBUG
            所有日志记录级别和完整的异常追踪。
        DEVELOPER
            输出日志的级别: Info / Warning / Fatal 和 完整的异常追踪
         RELEASE
            输出日志级别: Fatal和异常消息。在这种模式下性能会有所提高。
         */
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
    }

    @Override
    protected void initInput() {
        //向左
        FXGL.getInput().addAction(new UserAction("left") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).left();
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.A, VirtualButton.LEFT);

        //向右
        FXGL.getInput().addAction(new UserAction("right") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).right();
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.D, VirtualButton.RIGHT);

        //跳跃
        FXGL.getInput().addAction(new UserAction("jump") {
            @Override
            protected void onActionBegin() {//注意这里不是onAction
                player.getComponent(PlayerComponent.class).jump();
            }
        }, KeyCode.W, VirtualButton.A);

        //打开出口
        FXGL.getInput().addAction(new UserAction("Use") {
            @Override
            protected void onActionBegin() {
                FXGL.getGameWorld().getEntitiesByType(MarioType.BUTTON)
                        .stream()
                        //感觉这个逻辑有点啰嗦,碰撞完成后直接设置btn的可碰撞组件为false就可以了啊
                        .filter(btn -> btn.hasComponent(CollidableComponent.class) && player.isColliding(btn))
                        .forEach(btn -> {
                            //移除按钮的可碰撞组件
                            btn.removeComponent(CollidableComponent.class);
                            Entity keyEntity = btn.getObject("keyEntity");
                            keyEntity.setProperty("activated", true);

                            KeyView view = (KeyView) keyEntity.getViewComponent().getChildren().get(0);
                            view.setKeyColor(Color.RED);

                            makeExitDoor();
                        });
            }
        }, KeyCode.E, VirtualButton.B);
    }

    private void makeExitDoor() {
        //获取单例;
        Entity doorTop = FXGL.getGameWorld().getSingleton(MarioType.DOOR_TOP);
        Entity doorBot = FXGL.getGameWorld().getSingleton(MarioType.DOOR_BOT);
        //让门的可碰撞属性为true
        doorBot.getComponent(CollidableComponent.class).setValue(true);
        doorTop.setOpacity(1);
        doorBot.setOpacity(1);
    }

    /**
     * 游戏的全局变量,可以通过FXGL.geti();
     * FXGL.getWorldProperties().getInt()
     * FXGL.getGameWorld().getProperties().getInt()
     * 等多种方式访问
     *
     * @param vars
     */
    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("level", STARTING_LEVEL);
        vars.put("levelTime", 0.0);
        vars.put("score", 0);
        vars.put("lives", 3);
    }

    /**
     * 每个应用程序生命周期调用一次，就在 initGame() 之前。
     */
    @Override
    protected void onPreInit() {
        FXGL.getSettings().setGlobalMusicVolume(0.25);
        FXGL.loopBGM("BGM_dash_runner.wav");
    }

    @Override
    protected void initPhysics() {
        FXGL.getPhysicsWorld().setGravity(0, 760);
        FXGL.getPhysicsWorld().addCollisionHandler(new PlayerButtonHandler());

        FXGL.onCollisionBegin(MarioType.PLAYER, MarioType.COIN, (player, coin) -> {
            FXGL.getGameWorld().spawn("collideCoin", coin.getX() - 60, coin.getY() - 60);
            FXGL.play("coin.wav");
            coin.removeFromWorld();
        });

        //这个碰撞处理也会自动添加到上面的物理世界中
        // 玩家和出口标志只碰撞1次
        FXGL.onCollisionOneTimeOnly(MarioType.PLAYER, MarioType.EXIT_SIGN, (player, sign) -> {
            Texture texture = FXGL.texture("exit_sign.png").brighter();
            texture.setTranslateX(sign.getX() + 9);
            texture.setTranslateY(sign.getY() + 13);

            GameView gameView = new GameView(texture, 150);
            FXGL.getGameScene().addGameView(gameView);
            FXGL.runOnce(() -> {
                FXGL.getGameScene().removeGameView(gameView);
            }, Duration.seconds(1.6));

            //下面的代码效果类似,并且失败的时候关卡重置的时候也能被移出,而不需要等1.6秒
            //Entity entity =FXGL.entityBuilder(new SpawnData(sign.getX()+9, sign.getY()+13))
            //                .view(texture)
            //        .build();
            //FXGL.getGameWorld().addEntity(entity);
            //runOnce(()-> {
            //      //删除前先判断先是否还存在
            //    if (entity.isActive()) {
            //        FXGL.getGameWorld().removeEntity(entity);
            //    }
            //}, Duration.seconds(1.6));

        });

        //当玩家碰到触发器,出现退出的大门
        FXGL.onCollisionOneTimeOnly(MarioType.PLAYER, MarioType.EXIT_TRIGGER, (player, trigger) -> {
            makeExitDoor();
        });

        //玩家和门进行碰撞,显示游戏结果
        FXGL.onCollisionOneTimeOnly(MarioType.PLAYER, MarioType.DOOR_BOT, (player, doorBot) -> {
            System.out.println("fdasfasfdsa");
            //显示游戏结果,弹出游戏结果的窗口; UI暂停在这里了
            levelEndSceneValue.get().onLevelFinish();
            FXGL.getGameScene().getViewport().fade(this::nextLevel);
        });

        FXGL.onCollisionBegin(MarioType.PLAYER, MarioType.KEY_PROMPT, (player, keyPrompt) -> {
            String key = keyPrompt.getString("key");

            Entity entity = FXGL.getGameWorld().create("keyCode", new SpawnData(keyPrompt.getX(), keyPrompt.getY()).put("key", key));
            FXGL.spawnWithScale(entity, Duration.seconds(1), Interpolators.ELASTIC.EASE_OUT());

            FXGL.runOnce(() -> {
                if (entity.isActive()) {
                    FXGL.despawnWithScale(entity, Duration.seconds(1), Interpolators.ELASTIC.EASE_IN());
                }
            }, Duration.seconds(2.5));
        });

        FXGL.onCollisionBegin(MarioType.PLAYER, MarioType.SPIKE, (player, spikes) -> {
            playerDead();
        });


        FXGL.onCollision(MarioType.PLAYER, MarioType.LASER, (player,laser)->{
            playerDead();
        });




    }

    private void playerDead() {
        if (FXGL.geti("lives") > 1) {
            FXGL.inc("lives", -1);
            setLevel(FXGL.geti("level"));
        } else {
            FXGL.showMessage("游戏失败", () -> {
                FXGL.showConfirm("是否重新开始", result -> {
                    if (result) {
                        FXGL.getGameController().startNewGame();
                    } else {
                        FXGL.getGameController().exit();
                    }
                });
            });
        }
    }

    @Override
    protected void initUI() {
        Text text = FXGL.addText("", 50, 50);
        text.textProperty().bind(FXGL.getip("lives").asString("Lives :%d"));
    }

    private void nextLevel() {
        if (FXGL.geti("level") == MAX_LEVEL) {
            //FXGL.showMessage("You finished the demo!");
            //
            //return;
            FXGL.set("level", 1);
        }
        FXGL.inc("level", 1);
        setLevel(FXGL.geti("level"));

    }

    private void setLevel(int levelNum) {
        if (player != null) {
            player.getComponent(PhysicsComponent.class).overwritePosition(new Point2D(50, 50));
            player.setZIndex(Integer.MAX_VALUE);
        }
        FXGL.set("levelTime", 0.0);
        Level level = FXGL.setLevelFromMap("tmx/level" + levelNum + ".tmx");
        boolean bgExists = level.getProperties().exists("bgImageName");
        String bgImageName ;
        if (bgExists) {
             bgImageName = level.getProperties().getString("bgImageName");
        }else {
            bgImageName = "forest.png";
        }
        FXGL.spawn("background", new SpawnData().put("bgImageName", bgImageName));

        Double shortTime = level.getProperties().getDouble("star1time");
        LevelEndScene.LevelTimeData timeData = new LevelEndScene.LevelTimeData(shortTime * 2.4, shortTime * 1.3, shortTime);
        FXGL.set("levelTimeData", timeData);
    }

    @Override
    protected void onUpdate(double tpf) {
        //累加时间
        FXGL.inc("levelTime", tpf);
        if (player.getY() > FXGL.getAppHeight()) {
            if (player.getY() > FXGL.getAppHeight()) {
                playerDead();
            }
        }
    }

    @Override
    protected void initGame() {
        FXGL.getGameWorld().addEntityFactory(new MarioFactory());
        player = null;
        nextLevel();
        player = FXGL.spawn("player", 50, 50);
        FXGL.set("player", player);

        Viewport viewport = FXGL.getGameScene().getViewport();
        viewport.setBounds(-1500, 0, 250 * 70, FXGL.getAppHeight());
        //将视口绑定到实体，使其跟随给定的实体 .  distX 和 distY 表示实体和视口原点之间的绑定距离。
        viewport.bindToEntity(player, FXGL.getAppWidth() / 2.0, FXGL.getAppHeight() / 2.0);
        viewport.setLazy(true);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
