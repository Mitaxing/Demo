package com.kupaworld.androidtv.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.kupaworld.androidtv.R;
import com.kupaworld.androidtvwidget.bridge.EffectNoDrawBridge;
import com.kupaworld.androidtvwidget.bridge.OpenEffectBridge;
import com.kupaworld.androidtvwidget.utils.OPENLOG;
import com.kupaworld.androidtvwidget.view.FrameMainLayout;
import com.kupaworld.androidtvwidget.view.MainUpView;
import com.kupaworld.androidtvwidget.view.SmoothHorizontalScrollView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DEMO测试.
 * xml布局中 clipChildren clipToPadding 不要忘记了，不然移动的边框无法显示出来的. (强烈注意)
 */
public class MainActivity extends Activity implements OnClickListener, Runnable {
    public View gridview_lay;
    MainUpView mainUpView1;
    View test_top_iv;
    OpenEffectBridge mOpenEffectBridge;
    View mOldFocus; // 4.3以下版本需要自己保存.
    private long exitTime = 0;
    private Handler handler;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OPENLOG.initTag("hailongqiu", true); // 开启log输出.
//         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//         WindowManager.LayoutParams.FLAG_FULLSCREEN);
//         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.kupatv_main);
        UpdateCurrentTime();
        SmoothHorizontalScrollView hscroll_view = (SmoothHorizontalScrollView) findViewById(R.id.hscroll_view);
        hscroll_view.setFadingEdge((int) getDimension(R.dimen.w_100)); // 滚动窗口也需要适配.
//        test_top_iv = findViewById(R.id.test_top_iv);
        /* MainUpView 设置. */
        mainUpView1 = (MainUpView) findViewById(R.id.mainUpView1);
//         mainUpView1 = new MainUpView(this); // 手动添加(test)
//         mainUpView1.attach2Window(this); // 手动添加(test)
        mOpenEffectBridge = (OpenEffectBridge) mainUpView1.getEffectBridge();
        // 4.2 绘制有问题，所以不使用绘制边框.
        // 也不支持倒影效果，绘制有问题.
        // 请大家不要按照我这样写.
        // 如果你不想放大小人超出边框(demo，张靓颖的小人)，可以不使用OpenEffectBridge.
        // 我只是测试----DEMO.(建议大家使用 NoDrawBridge)
//        if (Utils.getSDKVersion() == 17) { // 测试 android 4.2版本.
        switchNoDrawBridgeVersion();
//        } else { // 其它版本（android 4.3以上）.
        mainUpView1.setUpRectResource(R.drawable.white_light_10); // 设置移动边框的图片.
        mainUpView1.setShadowResource(R.drawable.shadow_bg); // 设置移动边框的阴影.
//        }
//         mainUpView1.setUpRectResource(R.drawable.item_highlight); //
//         设置移动边框的图片.(test)
//         mainUpView1.setDrawUpRectPadding(new Rect(0, 0, 0, -26)); //
//         设置移动边框的距离.
        mainUpView1.setDrawShadowPadding(-2); // 阴影图片设置距离.
        mOpenEffectBridge.setTranDurAnimTime(500); // 动画时间.

        FrameMainLayout main_lay11 = (FrameMainLayout) findViewById(R.id.main_lay);
        main_lay11.getViewTreeObserver().addOnGlobalFocusChangeListener(new OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(final View oldFocus, final View newFocus) {
                if (newFocus != null)
                    newFocus.bringToFront(); // 防止放大的view被压在下面. (建议使用MainLayout)
                //设置获得焦点图片放大的倍数
                float scale = 1.05f;
                mainUpView1.setFocusView(newFocus, mOldFocus, scale);
                mOldFocus = newFocus; // 4.3以下需要自己保存.
                // 测试是否让边框绘制在下面，还是上面. (建议不要使用此函数)
//                if (newFocus != null) {
//                    testTopDemo(newFocus, scale);
//                }
            }
        });
        // test demo.
        gridview_lay = findViewById(R.id.guanggao);
        gridview_lay.setOnClickListener(this);
        findViewById(R.id.wodeyingyong).setOnClickListener(this);
        findViewById(R.id.shoujitongping).setOnClickListener(this);
        findViewById(R.id.yingshi).setOnClickListener(this);
        findViewById(R.id.yinyue).setOnClickListener(this);
        findViewById(R.id.zhibo).setOnClickListener(this);
        findViewById(R.id.yingyongshangdian).setOnClickListener(this);
        findViewById(R.id.shezhi).setOnClickListener(this);
        findViewById(R.id.wenjian).setOnClickListener(this);
        /**
         * 尽量不要使用鼠标. !!!! 如果使用鼠标，自己要处理好焦点问题.(警告)
         */
//		main_lay11.setOnHoverListener(new OnHoverListener() {
//			@Override
//			public boolean onHover(View v, MotionEvent event) {
//				mainUpView1.setVisibility(View.INVISIBLE);
//				return true;
//			}
//		});
        //
        for (int i = 0; i < main_lay11.getChildCount(); i++) {
            main_lay11.getChildAt(i).setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        //performClick 是使用代码主动去调用控件的点击事件（模拟人手去触摸控件）
//						v.performClick();
                        v.requestFocus();
                    }
                    return false;
                }
            });
        }
    }

    private void UpdateCurrentTime() {
        textView = (TextView) findViewById(R.id.mainPage_time);
        handler = new Handler() {
            public void handleMessage(Message msg) {
                textView.setText((String) msg.obj);
            }
        };
        new Thread(this).start();
    }

    /**
     * 这是一个测试DEMO，希望对API了解下再使用. 这种DEMO是为了实现这个效果:
     * https://raw.githubusercontent.com/FrozenFreeFall/ImageSaveHttp/master/
     * chaochupingm%20.jpg
     * <p>
     * public void testTopDemo(View newView, float scale) {
     * // 测试第一个小人放大的效果.
     * if (newView.getId() == R.id.mainpage_advertisement) { // 小人在外面的测试.
     * RectF rectf = new RectF(getDimension(R.dimen.w_7), -getDimension(R.dimen.h_63), getDimension(R.dimen.w_7),
     * getDimension(R.dimen.h_30));
     * mOpenEffectBridge.setDrawUpRectPadding(rectf); // 设置移动边框间距，不要被挡住了。
     * mOpenEffectBridge.setDrawShadowRectPadding(rectf); // 设置阴影边框间距，不要被挡住了。
     * mOpenEffectBridge.setDrawUpRectEnabled(false); // 让移动边框绘制在小人的下面.
     * test_top_iv.animate().scaleX(scale).scaleY(scale).setDuration(100).start(); // 让小人超出控件.
     * } else { // 其它的还原.
     * mOpenEffectBridge.setDrawUpRectPadding(0);
     * mOpenEffectBridge.setDrawShadowPadding(0);
     * mOpenEffectBridge.setDrawUpRectEnabled(true);
     * test_top_iv.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start(); // 让小人超出控件.
     * }
     * //    }
     */
    public float getDimension(int id) {
        return getResources().getDimension(id);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.guanggao:
//                startActivity(new Intent(getApplicationContext(), MyAppActivity.class));
//                startActivity(new Intent(getApplicationContext(), AdvertActivity.class));
                Toast.makeText(getApplicationContext(), "HELLO KUPA", Toast.LENGTH_SHORT).show();
                break;
            case R.id.wodeyingyong:
                startActivity(new Intent(getApplicationContext(), MyAppActivity.class));
                break;
            case R.id.shoujitongping:
                openOtherApp("com.hpplay.happyplay.aw");
                break;
            case R.id.yingshi:
                //爱奇艺
                openOtherApp("com.gitvdemo.video");
                break;
            case R.id.yinyue: // viewpager页面切换测试.
                //多米
                openOtherApp("com.duomi.androidtv");
                break;
            case R.id.zhibo: // viewpager页面切换测试.
                //斗鱼
                openOtherApp("com.douyu.xl.douyutv");
                break;
            case R.id.yingyongshangdian:
                //当贝市场
                openOtherApp("com.dangbeimarket");
                break;
            //设置
            case R.id.shezhi:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
            //文件管理
            case R.id.wenjian:
                startActivity(new Intent(getApplicationContext(), WenjianActivity.class));
            default:
                break;
        }
    }

    /**
     * 打开第三方应用
     *
     * @param packName
     */
    private void openOtherApp(String packName) {
        try {
            startActivity(new Intent(getApplication().getPackageManager().getLaunchIntentForPackage(packName)));
        } catch (Exception e) {
            e.printStackTrace();
//            Utils.Toast(getApplicationContext(), "请稍后再试~~~");
        }
    }

    private void switchNoDrawBridgeVersion() {
        float density = getResources().getDisplayMetrics().density;
        RectF rectf = new RectF(getDimension(R.dimen.w_11) * density, getDimension(R.dimen.h_9) * density,
                getDimension(R.dimen.w_9) * density, getDimension(R.dimen.h_7) * density);
        EffectNoDrawBridge effectNoDrawBridge = new EffectNoDrawBridge();
        effectNoDrawBridge.setTranDurAnimTime(200);
//        effectNoDrawBridge.setDrawUpRectPadding(rectf);
        mainUpView1.setEffectBridge(effectNoDrawBridge); // 4.3以下版本边框移动.
        mainUpView1.setUpRectResource(R.drawable.white_light_10); // 设置移动边框的图片.
        mainUpView1.setDrawUpRectPadding(rectf); // 边框图片设置间距.
    }

    @Override
    public void run() {
        try {
            while (true) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String str = sdf.format(new Date());
                handler.sendMessage(handler.obtainMessage(100, str));
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
