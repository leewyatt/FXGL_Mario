package com.leewyatt.mario.ui;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * 
 */
public class MarioLoadingScene extends LoadingScene {

    public MarioLoadingScene() {
        //天蓝色的矩形作为此场景的背景
        Rectangle bg  = new Rectangle(getAppWidth(),getAppHeight(), Color.AZURE);
        //大致类似于new Text 然后设置填充色 和字体  , 这样更简洁; 不过注意默认颜色是白色,所以要设置为黑色
        Text text = FXGL.getUIFactoryService().newText("Loading Level", Color.BLACK, 46.0);
        //Text text =  new Text("Loading level");
        //text.setFill(Color.BLACK);
        //text.setFont(Font.font(46));

        //设置文本的中心位置
        FXGL.centerText(text,getAppWidth()/2.0,getAppHeight()/2.0);

        //3 个点 ,明暗相接的动画,表示加载中
        HBox box  = new HBox(5);
        for (int i = 0; i < 3; i++) {
            Text textDot = FXGL.getUIFactoryService().newText(".", Color.BLACK,46.0);
            box.getChildren().add(textDot);

            //指定下SubScene ,否则默认就是找GameScene
            FXGL.animationBuilder(this)
                    //这里可以省略耗时,因为默认时间也是1秒
                    .duration(Duration.seconds(1.0))
                    .autoReverse(true)
                    .delay(Duration.seconds(i*0.5))
                    .repeatInfinitely()
                    .fadeIn(textDot)
                    .buildAndPlay();
        }

        getContentRoot().getChildren().setAll(bg, text, box);

    }
}
