# SlidingClose
向右滑动关闭界面（仿iOS）<br>
大概效果就是，Activity向右滑动，滑动超过屏幕的一半，就关闭，否则，恢复原来的状态。<br>
* 项目地址：https://github.com/ChanWong21/SlidingClose
* 有问题可以提Issues

## 截图
![](https://raw.githubusercontent.com/ChanWong21/SlidingClose/master/art/screenshot.gif)

## 源码解析
### 配置透明主题
要想Activity滑出屏幕后不遮挡下层Activity，需设置透明主题
```
<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
    <item name="colorPrimary">@color/colorPrimary</item>
    <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
    <item name="colorAccent">@color/colorAccent</item>
    <!--Required-->
    <item name="android:windowBackground">@android:color/transparent</item>
    <item name="android:windowIsTranslucent">true</item>
    <item name="android:windowAnimationStyle">@android:style/Animation</item>
</style>
```
添加后3条即可，当然直接用Android自带透明主题也是可以的。

### SlideLayout
重写了FrameLayout，主要是处理滑动时的逻辑。
```java
package me.wcy.slideanim;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by chenyan.wang on 2015/10/29.
 */
public class SlideLayout extends FrameLayout {
    private Activity mActivity;
    private Scroller mScroller;
    /**
     * 上次ACTION_MOVE时的X坐标
     */
    private int mLastMotionX;
    /**
     * 屏幕宽度
     */
    private int mWidth;
    /**
     * 可滑动的最小X坐标，小于该坐标的滑动不处理
     */
    private int mMinX;
    /**
     * 页面边缘的阴影图
     */
    private Drawable mLeftShadow;
    /**
     * 页面边缘阴影的宽度默认值
     */
    private static final int SHADOW_WIDTH = 16;
    /**
     * 页面边缘阴影的宽度
     */
    private int mShadowWidth;
    /**
     * Activity finish标识符
     */
    private boolean mIsFinish;

    public SlideLayout(Activity activity) {
        this(activity, null);
    }

    public SlideLayout(Activity activity, AttributeSet attrs) {
        this(activity, attrs, 0);
    }

    public SlideLayout(Activity activity, AttributeSet attrs, int defStyleAttr) {
        super(activity, attrs, defStyleAttr);
        initView(activity);
    }

    private void initView(Activity activity) {
        mActivity = activity;
        mScroller = new Scroller(mActivity);
        mLeftShadow = getResources().getDrawable(R.drawable.left_shadow);
        int density = (int) activity.getResources().getDisplayMetrics().density;
        mShadowWidth = SHADOW_WIDTH * density;
    }

    /**
     * 绑定Activity
     */
    public void bindActivity(Activity activity) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        View child = decorView.getChildAt(0);
        decorView.removeView(child);
        addView(child);
        decorView.addView(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = (int) event.getX();
                mWidth = getWidth();
                mMinX = mWidth / 10;
                break;
            case MotionEvent.ACTION_MOVE:
                int rightMovedX = mLastMotionX - (int) event.getX();
                if (getScrollX() + rightMovedX >= 0) {// 左侧即将滑出屏幕
                    scrollTo(0, 0);
                } else if ((int) event.getX() > mMinX) {// 手指处于屏幕边缘时不处理滑动
                    scrollBy(rightMovedX, 0);
                }
                mLastMotionX = (int) event.getX();
                break;
            case MotionEvent.ACTION_UP:
                if (-getScrollX() < mWidth / 2) {
                    scrollBack();
                    mIsFinish = false;
                } else {
                    scrollClose();
                    mIsFinish = true;
                }
                break;
        }
        return true;
    }

    /**
     * 滑动返回
     */
    private void scrollBack() {
        int startX = getScrollX();
        int dx = -getScrollX();
        mScroller.startScroll(startX, 0, dx, 0, 300);
        invalidate();
    }

    /**
     * 滑动关闭
     */
    private void scrollClose() {
        int startX = getScrollX();
        int dx = -getScrollX() - mWidth;
        mScroller.startScroll(startX, 0, dx, 0, 300);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        } else if (mIsFinish) {
            mActivity.finish();
        }
        super.computeScroll();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawShadow(canvas);
    }

    /**
     * 绘制边缘的阴影
     */
    private void drawShadow(Canvas canvas) {
        // 保存画布当前的状态
        canvas.save();
        // 设置drawable的大小范围
        mLeftShadow.setBounds(0, 0, mShadowWidth, getHeight());
        // 让画布平移一定距离
        canvas.translate(-mShadowWidth, 0);
        // 绘制Drawable
        mLeftShadow.draw(canvas);
        // 恢复画布的状态
        canvas.restore();
    }
}
```

* bindActivity：绑定Activity的界面，这段代码很简单。
* 通过重写onTouchEvent处理滑动逻辑。（注意，为什么不是重写dispatchTouchEvent或interceptTouchEvnet？）

> ACTION_DOWN：主要是记录了屏幕的宽度<br>
ACTION_MOVE：分两种情况，①view的x坐标为0，即初始状态，这时只能向右滑动，禁止向左滑动。②view的坐标大于0，即view的一部分已经划出屏幕（当然是向右滑）。这时，如果继续向右滑则不用多考虑；如果向左滑，就要假设view向左滑动了x后，如果view左边缘还在屏幕内，则可以继续滑动，否则，view左边缘可能已经滑出屏幕，这是我们不想看到的，因此我们直接把view滑动到(0,0)位置。<br>
ACTION_UP：手指释放后，如果滑动距离超过屏幕的一半，就关闭Activity，否则，恢复原来状态。

* 这里用Scroller来处理手指释放后的滑动操作，本文中Scroller不是重点，因此不过多介绍。
* 出于交互友好考虑，这里保留了屏幕最左边的一块区域不能滑动，即mMinX = mWidth / 10;
* drawShadow：页面滑出屏幕后左侧添加阴影区域，增加层次感。在dispatchDraw中调用。

### SlideActivity
继承自AppCompatActivity，作为滑动关闭Activity的基类，主要是做了绑定操作。
```java
package me.wcy.slideanim;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SlideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SlideLayout rootView = new SlideLayout(this);
        rootView.bindActivity(this);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        overridePendingTransition(R.anim.anim_enter, R.anim.anim_exit);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_enter, R.anim.anim_exit);
    }
}
```
startActivity和finish添加了滑动动画。

> 我在这里遇到一个问题，设置透明主题后，在style中设置Activity切换动画无效，因此这里只好在代码中添加动画。知道原因的朋友还请不吝赐教。

主要代码就这么多，这里考虑到了扩展性，因此使用是非常简单的。如果想要滑动关闭Activity，就设置透明主题，继承SlideActivity就可以了。

## License

    Copyright 2016 Chay Wong

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
