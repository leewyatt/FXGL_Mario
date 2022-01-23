package com.leewyatt.mario.ui;

import com.almasb.fxgl.animation.AnimationBuilder;
import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.scene.SubScene;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.ui.FontFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;


/**
 * 
 */
public class LevelEndScene extends SubScene {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 250;
    private final BooleanProperty isAnimationDone = new SimpleBooleanProperty(false);

    //默认的填充色就是白色,所以可以省略参数Color.WHITE
    private final Text textUserTime = FXGL.getUIFactoryService().newText("", Color.WHITE, 24.0);
    private final HBox gradeBox = new HBox();

    public LevelEndScene() {
        Rectangle bg = new Rectangle(WIDTH,HEIGHT,Color.color(0, 0, 0, 0.7));
        bg.setStroke(Color.GOLDENROD);
        bg.setStrokeWidth(1.5);
        bg.setEffect(new DropShadow(28, Color.color(0, 0, 0,0.9)));

        VBox.setVgrow(gradeBox, Priority.ALWAYS);

        Text textContinue = FXGL.getUIFactoryService().newText("Tap to continue",11.0);

        textContinue.visibleProperty().bind(isAnimationDone);

        FXGL.animationBuilder(this)
                .repeatInfinitely()
                .autoReverse(true)
                .scale(textContinue)
                .from(new Point2D(1, 1))
                .to(new Point2D(1.25, 1.25))
                .buildAndPlay();


        VBox vbox = new VBox(15, textUserTime, gradeBox,textContinue);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(25));

        StackPane root = new StackPane(bg,vbox);
        root.setTranslateX((FXGL.getAppWidth()-WIDTH)/2.0);
        root.setTranslateY((FXGL.getAppHeight()-HEIGHT)/2.0);

        Text textLevel  = new Text();
        textLevel.textProperty().bind(FXGL.getip("level").asString("Level %d"));
        FontFactory fontFactory = FXGL.getAssetLoader().loadFont("level_font.ttf");
        textLevel.setFont(fontFactory.newFont(52));
        textLevel.setRotate(-20);
        textLevel.setFill(Color.ORANGE);
        textLevel.setStroke(Color.BLACK);
        textLevel.setStrokeWidth(3.5);

        textLevel.setTranslateX(root.getTranslateX() - textLevel.getLayoutBounds().getWidth() / 2);
        textLevel.setTranslateY(root.getTranslateY() + 25);

        getContentRoot().getChildren().setAll(root, textLevel);

        //特定于此场景的输入。 它仅在此场景处于活动状态时接收事件。这里并不是FXGL.getInput();
        getInput().addAction(new UserAction("Close Level End Scene") {
            @Override
            protected void onActionBegin() {
                //如果动画还没有播放完毕, 那么返回
                if (!isAnimationDone.getValue()) {
                    return;
                }
                //关闭当前场景
                FXGL.getSceneService().popSubScene();
            }
        }, MouseButton.PRIMARY);
    }

    public void onLevelFinish() {
        //每次关卡结束,显示这个子场景的时候,先设置动画结束的标记为false
        isAnimationDone.setValue(false);

        //获取当前关卡用时
        Duration useTime = Duration.seconds(FXGL.getd("levelTime"));
        //获取当前关卡,每一个星级的时间标准
        LevelTimeData timeData = FXGL.geto("levelTimeData");

        textUserTime.setText(String.format("Your time %.2f second!", useTime.toSeconds()));

        gradeBox.getChildren().setAll(
                new Grade(Duration.seconds(timeData.star1), useTime),
                new Grade(Duration.seconds(timeData.star2), useTime),
                new Grade(Duration.seconds(timeData.star3), useTime)

        );

        for (int i = 0; i < gradeBox.getChildren().size(); i++) {
            AnimationBuilder builder = FXGL.animationBuilder(this)
                    .delay(Duration.seconds(i*0.25))
                    .duration(Duration.seconds(0.25))
                    .interpolator(Interpolators.EXPONENTIAL.EASE_OUT());
            //如果是最后一个动画
            if (i == gradeBox.getChildren().size()-1) {
                builder = builder.onFinished(()->isAnimationDone.setValue(true));
            }
            builder.translate(gradeBox.getChildren().get(i))
                    .from(new Point2D(0, -500))
                    .to(new Point2D(0, 0))
                    .buildAndPlay();
        }
        //推送当前场景到游戏场景上
        FXGL.getSceneService().pushSubScene(this);

    }

    private static class Grade extends VBox {

        private static final Texture STAR_EMPTY = FXGL.texture("star_empty.png",65,72);
        private static final Texture STAR_FULL = FXGL.texture("star_full.png",65,72);

        public Grade(Duration gradeTime,Duration userTime) {
            super(15);
            HBox.setHgrow(this, Priority.ALWAYS);
            setAlignment(Pos.CENTER);

            //为什么不直接用texture 而要用copy,比如三颗星都是亮的,那么texture就被3次条件到父节点里就会报错
            getChildren().add(userTime.lessThanOrEqualTo(gradeTime)?
                    STAR_FULL.copy():STAR_EMPTY.copy());
            getChildren().add(FXGL.getUIFactoryService().newText(String.format("<%.2f", gradeTime.toSeconds()), 16.0));
        }
    }




    public static class LevelTimeData {

        private final double star1;
        private final double star2;
        private final double star3;

        /**
         * @param star1 in seconds
         * @param star2 in seconds
         * @param star3 in seconds
         */
        public LevelTimeData(double star1, double star2, double star3) {
            this.star1 = star1;
            this.star2 = star2;
            this.star3 = star3;
        }
    }

}
