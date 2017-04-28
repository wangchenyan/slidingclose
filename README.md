# SlidingClose
向右滑动关闭界面（仿iOS）

大概效果就是， Activity 向右滑动，滑动超过屏幕的一半，就关闭，否则，恢复原来的状态。

解决了**滑动冲突**。

* 项目地址：https://github.com/wangchenyan/SlidingClose
* 有问题可以提Issues

## 截图
![](https://raw.githubusercontent.com/wangchenyan/SlidingClose/master/art/screenshot.gif)

## 源码解析
### 配置透明主题
要想 Activity 滑出屏幕后不遮挡下层 Activity ，需设置透明主题
```
<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
    <item name="colorPrimary">@color/colorPrimary</item>
    <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
    <item name="colorAccent">@color/colorAccent</item>
</style>

<style name="AppTheme.Slide" parent="@style/AppTheme">
    <!--Required-->
    <item name="android:windowBackground">@android:color/transparent</item>
    <item name="android:windowIsTranslucent">true</item>
    <item name="android:windowAnimationStyle">@style/AppTheme.Slide.Animation</item>
</style>

<style name="AppTheme.Slide.Animation" parent="@android:style/Animation.Activity">
    <item name="android:activityOpenEnterAnimation">@anim/anim_slide_in</item>
    <item name="android:activityOpenExitAnimation">@anim/anim_slide_out</item>
    <item name="android:activityCloseEnterAnimation">@anim/anim_slide_in</item>
    <item name="android:activityCloseExitAnimation">@anim/anim_slide_out</item>
</style>
```
如果需要滑动关闭则指定 Activity 的 theme 为 `AppTheme.Slide` ，否则使用 `AppTheme` 。

这里也添加了 Activity 切换动画，增强体验。

### SlideLayout
继承自 FrameLayout ，主要是处理滑动逻辑和滑动冲突。
```
public class SlidingLayout extends FrameLayout {
    // 页面边缘阴影的宽度默认值
    private static final int SHADOW_WIDTH = 16;
    private Activity mActivity;
    private Scroller mScroller;
    // 页面边缘的阴影图
    private Drawable mLeftShadow;
    // 页面边缘阴影的宽度
    private int mShadowWidth;
    private int mInterceptDownX;
    private int mLastInterceptX;
    private int mLastInterceptY;
    private int mTouchDownX;
    private int mLastTouchX;
    private int mLastTouchY;
    private boolean isConsumed = false;

    public SlidingLayout(Context context) {
        this(context, null);
    }

    public SlidingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mScroller = new Scroller(context);
        mLeftShadow = getResources().getDrawable(R.drawable.left_shadow);
        int density = (int) getResources().getDisplayMetrics().density;
        mShadowWidth = SHADOW_WIDTH * density;
    }

    /**
     * 绑定Activity
     */
    public void bindActivity(Activity activity) {
        mActivity = activity;
        ViewGroup decorView = (ViewGroup) mActivity.getWindow().getDecorView();
        View child = decorView.getChildAt(0);
        decorView.removeView(child);
        addView(child);
        decorView.addView(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercept = false;
                mInterceptDownX = x;
                mLastInterceptX = x;
                mLastInterceptY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastInterceptX;
                int deltaY = y - mLastInterceptY;
                // 手指处于屏幕边缘，且横向滑动距离大于纵向滑动距离时，拦截事件
                if (mInterceptDownX < (getWidth() / 10) && Math.abs(deltaX) > Math.abs(deltaY)) {
                    intercept = true;
                } else {
                    intercept = false;
                }
                mLastInterceptX = x;
                mLastInterceptY = y;
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                mInterceptDownX = mLastInterceptX = mLastInterceptY = 0;
                break;
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = x;
                mLastTouchX = x;
                mLastTouchY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastTouchX;
                int deltaY = y - mLastTouchY;

                if (!isConsumed && mTouchDownX < (getWidth() / 10) && Math.abs(deltaX) > Math.abs(deltaY)) {
                    isConsumed = true;
                }

                if (isConsumed) {
                    int rightMovedX = mLastTouchX - (int) ev.getX();
                    // 左侧即将滑出屏幕
                    if (getScrollX() + rightMovedX >= 0) {
                        scrollTo(0, 0);
                    } else {
                        scrollBy(rightMovedX, 0);
                    }
                }
                mLastTouchX = x;
                mLastTouchY = y;
                break;
            case MotionEvent.ACTION_UP:
                isConsumed = false;
                mTouchDownX = mLastTouchX = mLastTouchY = 0;
                // 根据手指释放时的位置决定回弹还是关闭
                if (-getScrollX() < getWidth() / 2) {
                    scrollBack();
                } else {
                    scrollClose();
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
        int dx = -getScrollX() - getWidth();
        mScroller.startScroll(startX, 0, dx, 0, 300);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        } else if (-getScrollX() >= getWidth()) {
            mActivity.finish();
        }
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
        mLeftShadow.setBounds(0, 0, mShadowWidth, getHeight());
        canvas.save();
        canvas.translate(-mShadowWidth, 0);
        mLeftShadow.draw(canvas);
        canvas.restore();
    }
}
```

重写 `onInterceptTouchEvent` 和 `onTouchEvent` 处理滑动逻辑和滑动冲突。
- 当有子 View 消费 Touch 事件时，事件会经过 SlidingLayout 的 onInterceptTouchEvent 。当手指在屏幕边缘按下（`mTouchDownX < (getWidth() / 10)`），且横向滑动距离大于纵向滑动距离，则拦截事件，交由 onTouchEvent 处理。
- 当没有子 View 消费 Touch 事件时，事件会直接回传到 SlidingLayout 的 onTouchEvent 中，这时需要在 onTouchEvent 中判断是否需要消费该事件，条件同上。
- 滑动过程中如果 View 即将滑出屏幕左侧，则直接把 View 滑动到 (0,0) 位置。
- 手指释放后，如果滑动距离超过屏幕的一半，则关闭 Activity ，否则，恢复原来状态。
- 使用 `Scroller` 来处理手指释放后的滑动操作。
- 在 `dispatchDraw` 中绘制 View 左侧的阴影，增加层次感。

### SlideActivity
继承自 AppCompatActivity ，作为滑动关闭 Activity 的基类，主要是做了绑定操作。
```
public class SlidingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (enableSliding()) {
            SlidingLayout rootView = new SlidingLayout(this);
            rootView.bindActivity(this);
        }
    }

    protected boolean enableSliding() {
        return true;
    }
}
```
如果不需要滑动关闭，则重写 `enableSliding` 并返回 false 。

## 使用
- 下载源码。
- 将 Activity 的基类继承 SlideActivity 。
- 将需要滑动关闭的 Activity 的 theme 指定为 `AppTheme.Slide` 。
- 将不需要滑动关闭的 Activity （如 App 主界面）的 theme 指定为 `AppTheme` ，重写 `enableSliding` 并返回 false 。

## License

    Copyright 2016 wangchenyan

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
