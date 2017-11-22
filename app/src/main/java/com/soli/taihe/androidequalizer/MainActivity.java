package com.soli.taihe.androidequalizer;

import android.Manifest;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // 定义播放声音的MediaPlayer
    private MediaPlayer mPlayer;
    // 定义系统的频谱
    private Visualizer mVisualizer;
    // 定义系统的均衡器
    private Equalizer mEqualizer;
    // 定义系统的重低音控制器
    private BassBoost mBass;
    //环绕音
    private Virtualizer mVirtualizer;
    private PresetReverb mPresetReverb;
    private LinearLayout layout;
    private LinearLayout equalizerLayout;
    private List<Short> reverbNames = new ArrayList<Short>();
    private List<String> reverbVals = new ArrayList<String>();

    private RxPermissions rxPermissions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.RECORD_AUDIO)
                .subscribe(aBoolean -> {
                    if (aBoolean)
                        init();
                    else
                        finish();
                });
    }

    /**
     *
     */
    private void init() {
        //设置音频流 - STREAM_MUSIC：音乐回放即媒体音量
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        ScrollView scrollView = new ScrollView(this);
        int padding = (int) (10f * getResources().getDisplayMetrics().density);
        scrollView.setPadding(padding, padding, padding, padding);
        layout = new LinearLayout(this);//代码创建布局
        layout.setOrientation(LinearLayout.VERTICAL);//设置为线性布局-上下排列
        scrollView.addView(layout);
        setContentView(scrollView);//将布局添加到 Activity
        // 创建MediaPlayer对象,并添加音频
        // 音频路径为  res/raw/beautiful.mp3
        mPlayer = MediaPlayer.create(this, R.raw.beautiful);
        // 初始化示波器
        setupVisualizer();
        // 初始化均衡控制器
        layout.addView(setupEqualizer());
        // 初始化重低音控制器
        setupBassBoost();
        //环绕音
        setVirtualizer();
        // 初始化预设音场控制器
        setupPreset();
        //音效混响
        setupReverb();

        // 开发播放音乐
        mPlayer.start();
    }

    /**
     * 初始化频谱
     */
    private void setupVisualizer() {
        // 创建MyVisualizerView组件，用于显示波形图
        TextView textView = new TextView(this);
        textView.setText("wave");
        layout.addView(textView);
        final MyVisualizerView mVisualizerView = new MyVisualizerView(this);
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (100f * getResources().getDisplayMetrics().density)));
        // 将MyVisualizerView组件添加到layout容器中
        layout.addView(mVisualizerView);

        textView = new TextView(this);
        textView.setText("时域波形");
        layout.addView(textView);

        final VisualizerView mWaveView = new VisualizerView(this);
        mWaveView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (100f * getResources().getDisplayMetrics().density)));
        // 将MyVisualizerView组件添加到layout容器中
        layout.addView(mWaveView);

        textView = new TextView(this);
        textView.setText("频域波形");
        layout.addView(textView);

        final VisualizerFFTView mFFtView = new VisualizerFFTView(this);
        mFFtView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (100f * getResources().getDisplayMetrics().density)));
        // 将MyVisualizerView组件添加到layout容器中
        layout.addView(mFFtView);

        textView = new TextView(this);
        textView.setText("音乐频谱");
        layout.addView(textView);

        final VisualizerGoodView mGoodView = new VisualizerGoodView(this);
        mGoodView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (100f * getResources().getDisplayMetrics().density)));
        // 将MyVisualizerView组件添加到layout容器中
        layout.addView(mGoodView);


        // 以MediaPlayer的AudioSessionId创建Visualizer
        // 相当于设置Visualizer负责显示该MediaPlayer的音频数据
        mVisualizer = new Visualizer(mPlayer.getAudioSessionId());
        //设置需要转换的音乐内容长度，专业的说这就是采样，该采样值一般为2的指数倍，如64,128,256,512,1024。
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        // 为mVisualizer设置监听器
        /*
         * Visualizer.setDataCaptureListener(OnDataCaptureListener listener, int rate, boolean waveform, boolean fft
         *
         *      listener，表监听函数，匿名内部类实现该接口，该接口需要实现两个函数
                rate， 表示采样的周期，即隔多久采样一次，联系前文就是隔多久采样128个数据
                iswave，是波形信号
                isfft，是FFT信号，表示是获取波形信号还是频域信号

         */
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    //这个回调应该采集的是快速傅里叶变换有关的数据
                    @Override
                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] fft, int samplingRate) {
                        mFFtView.updateVisualizer(fft);
                        mGoodView.updateVisualizer(fft);
                    }

                    //这个回调应该采集的是波形数据
                    @Override
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] waveform, int samplingRate) {
                        // 用waveform波形数据更新mVisualizerView组件
                        mVisualizerView.updateVisualizer(waveform);
                        mWaveView.updateVisualizer(waveform);
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, true);
        mVisualizer.setEnabled(true);
    }

    /**
     * 初始化均衡控制器
     */
    private LinearLayout setupEqualizer() {

        if (equalizerLayout == null) {
            equalizerLayout = new LinearLayout(this);
            equalizerLayout.setOrientation(LinearLayout.VERTICAL);
        } else {
            equalizerLayout.removeAllViews();
        }

        // 以MediaPlayer的AudioSessionId创建Equalizer
        // 相当于设置Equalizer负责控制该MediaPlayer
        if (mEqualizer == null) {
            mEqualizer = new Equalizer(0, mPlayer.getAudioSessionId());
            // 启用均衡控制效果
            mEqualizer.setEnabled(true);
        }

        TextView eqTitle = new TextView(this);
        eqTitle.setText("均衡器：");
        equalizerLayout.addView(eqTitle);
        // 获取均衡控制器支持最小值和最大值
        final short minEQLevel = mEqualizer.getBandLevelRange()[0];//第一个下标为最低的限度范围
        short maxEQLevel = mEqualizer.getBandLevelRange()[1];  // 第二个下标为最高的限度范围
        // 获取均衡控制器支持的所有频率
        short brands = mEqualizer.getNumberOfBands();
        for (short i = 0; i < brands; i++) {
            TextView eqTextView = new TextView(this);
            // 创建一个TextView，用于显示频率
            eqTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            eqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            // 设置该均衡控制器的频率
            eqTextView.setText((mEqualizer.getCenterFreq(i) / 1000) + " Hz");
            equalizerLayout.addView(eqTextView);
            int[] brandFreqRang = mEqualizer.getBandFreqRange(i);
            Log.e("BandFreqRange：" + i, "min;" + (brandFreqRang[0] / 1000) + "Hz" + "  Max:" + (brandFreqRang[1] / 1000) + "Hz");
            // 创建一个水平排列组件的LinearLayout
            LinearLayout tmpLayout = new LinearLayout(this);
            tmpLayout.setOrientation(LinearLayout.HORIZONTAL);
            // 创建显示均衡控制器最小值的TextView
            TextView minDbTextView = new TextView(this);
            minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            // 显示均衡控制器的最小值
            minDbTextView.setText((minEQLevel / 100) + " dB");
            // 创建显示均衡控制器最大值的TextView
            TextView maxDbTextView = new TextView(this);
            maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            // 显示均衡控制器的最大值
            maxDbTextView.setText((maxEQLevel / 100) + " dB");
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            // 定义SeekBar做为调整工具
            SeekBar bar = new SeekBar(this);
            bar.setLayoutParams(layoutParams);
            bar.setMax(maxEQLevel - minEQLevel);
            int currentLevel = mEqualizer.getBandLevel(i) + maxEQLevel;
            Log.e("currentLevel：" + i, "" + currentLevel);
            bar.setProgress(currentLevel);
            final short brand = i;
            // 为SeekBar的拖动事件设置事件监听器
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // 设置该频率的均衡值
                    Log.e("onProgressChanged", "" + (short) (progress + minEQLevel));
                    mEqualizer.setBandLevel(brand, (short) (progress + minEQLevel));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            // 使用水平排列组件的LinearLayout“盛装”3个组件
            tmpLayout.addView(minDbTextView);
            tmpLayout.addView(bar);
            tmpLayout.addView(maxDbTextView);
            // 将水平排列组件的LinearLayout添加到myLayout容器中
            equalizerLayout.addView(tmpLayout);
        }

        return equalizerLayout;
    }

    /**
     * 初始化重低音控制器
     */
    private void setupBassBoost() {
        mBass = new BassBoost(0, mPlayer.getAudioSessionId());
        if (mBass.getStrengthSupported()) {
            // 设置启用重低音效果
            mBass.setEnabled(true);

            TextView bbTitle = new TextView(this);
            bbTitle.setText("重低音：");
            layout.addView(bbTitle);
            // 使用SeekBar做为重低音的调整工具
            SeekBar bar = new SeekBar(this);
            // 重低音的范围为0～1000
            bar.setMax(1000);
            bar.setProgress(0);
            // 为SeekBar的拖动事件设置事件监听器
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // 设置重低音的强度
                    mBass.setStrength((short) progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            //default
            short strength = mBass.getRoundedStrength();
            mBass.setStrength(strength);

            layout.addView(bar);
        }
    }

    /**
     * 环绕音
     */
    private void setVirtualizer() {
        mVirtualizer = new Virtualizer(0, mPlayer.getAudioSessionId());
        if (mVirtualizer.getStrengthSupported()) {
            // 设置启用重低音效果
            mVirtualizer.setEnabled(true);

            TextView bbTitle = new TextView(this);
            bbTitle.setText("环绕音 ：");
            layout.addView(bbTitle);
            // 使用SeekBar做为重低音的调整工具
            SeekBar bar = new SeekBar(this);
            // 重低音的范围为0～1000
            bar.setMax(1000);
            bar.setProgress(0);
            // 为SeekBar的拖动事件设置事件监听器
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // 设置重低音的强度
                    mVirtualizer.setStrength((short) progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

            });

            //default
            short strength = mVirtualizer.getRoundedStrength();
            mVirtualizer.setStrength(strength);
            layout.addView(bar);
        }
    }

    /**
     * 音效混响
     */
    private void setupReverb() {

        mPresetReverb = new PresetReverb(0, mPlayer.getAudioSessionId());
        mPresetReverb.setEnabled(true);

        TextView prTitle = new TextView(this);
        prTitle.setText("音效混响");
        layout.addView(prTitle);
        // 获取系统支持的所有预设音场
        List<String> list = new ArrayList<>();
        list.add("None");
        list.add("SmallRoom");
        list.add("MediumRoom");
        list.add("LargeRoom");
        list.add("MediumHall");
        list.add("LargeHall");
        list.add("Plate");

        // 使用Spinner做为音场选择工具
        Spinner sp = new Spinner(this);
        sp.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, list));
        // 为Spinner的列表项选中事件设置监听器
        sp.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // 设定音场
                mPresetReverb.setPreset((short) arg2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        layout.addView(sp);
    }

    /**
     * 初始化预设音场控制器
     */
    private void setupPreset() {
        TextView prTitle = new TextView(this);
        prTitle.setText("预设音场");
        layout.addView(prTitle);
        // 获取系统支持的所有预设音场
        for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++) {
            reverbNames.add(i);
            reverbVals.add(mEqualizer.getPresetName(i));
        }
        // 使用Spinner做为音场选择工具
        Spinner sp = new Spinner(this);
        sp.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, reverbVals));
        // 为Spinner的列表项选中事件设置监听器
        sp.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // 设定音场
                mEqualizer.usePreset((short) arg2);
                setupEqualizer();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        layout.addView(sp);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing() && mPlayer != null) {
            // 释放所有对象
            mVisualizer.release();
            mEqualizer.release();
//            mPresetReverb.release();
            mBass.release();
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * 根据Visualizer传来的数据动态绘制波形效果，分别为：
     * 块状波形、柱状波形、曲线波形
     */
    private class MyVisualizerView extends View {
        // bytes数组保存了波形抽样点的值
        private byte[] bytes;
        private float[] points;
        private Paint paint = new Paint();
        private Rect rect = new Rect();
        private byte type = 0;

        public MyVisualizerView(Context context) {
            super(context);
            bytes = null;
            // 设置画笔的属性
            paint.setStrokeWidth(1f);
            paint.setAntiAlias(true);//抗锯齿
            paint.setColor(Color.GREEN);//画笔颜色
            paint.setStyle(Paint.Style.FILL);
        }

        public void updateVisualizer(byte[] ftt) {
            bytes = ftt;
            // 通知该组件重绘自己。
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent me) {
            // 当用户触碰该组件时，切换波形类型
            if (me.getAction() != MotionEvent.ACTION_DOWN) {
                return false;
            }
            type++;
            if (type >= 3) {
                type = 0;
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (bytes == null) {
                return;
            }
            // 绘制白色背景
            canvas.drawColor(Color.WHITE);
            // 使用rect对象记录该组件的宽度和高度
            rect.set(0, 0, getWidth(), getHeight());
            switch (type) {
                // -------绘制块状的波形图-------
                case 0:
                    for (int i = 0; i < bytes.length - 1; i++) {
                        float left = getWidth() * i / (bytes.length - 1);
                        // 根据波形值计算该矩形的高度
                        float top = rect.height() - (byte) (bytes[i + 1] + 128)
                                * rect.height() / 128;
                        float right = left + 1;
                        float bottom = rect.height();
                        canvas.drawRect(left, top, right, bottom, paint);
                    }
                    break;
                // -------绘制柱状的波形图（每隔18个抽样点绘制一个矩形）-------
                case 1:
                    for (int i = 0; i < bytes.length - 1; i += 18) {
                        float left = rect.width() * i / (bytes.length - 1);
                        // 根据波形值计算该矩形的高度
                        float top = rect.height() - (byte) (bytes[i + 1] + 128)
                                * rect.height() / 128;
                        float right = left + 6;
                        float bottom = rect.height();
                        canvas.drawRect(left, top, right, bottom, paint);
                    }
                    break;
                // -------绘制曲线波形图-------
                case 2:
                    // 如果point数组还未初始化
                    if (points == null || points.length < bytes.length * 4) {
                        points = new float[bytes.length * 4];
                    }
                    for (int i = 0; i < bytes.length - 1; i++) {
                        // 计算第i个点的x坐标
                        points[i * 4] = rect.width() * i / (bytes.length - 1);
                        // 根据bytes[i]的值（波形点的值）计算第i个点的y坐标
                        points[i * 4 + 1] = (rect.height() / 2)
                                + ((byte) (bytes[i] + 128)) * 128
                                / (rect.height() / 2);
                        // 计算第i+1个点的x坐标
                        points[i * 4 + 2] = rect.width() * (i + 1)
                                / (bytes.length - 1);
                        // 根据bytes[i+1]的值（波形点的值）计算第i+1个点的y坐标
                        points[i * 4 + 3] = (rect.height() / 2)
                                + ((byte) (bytes[i + 1] + 128)) * 128
                                / (rect.height() / 2);
                    }
                    // 绘制波形曲线
                    canvas.drawLines(points, paint);
                    break;
            }
        }
    }

    /**
     * 显示时域波形
     */
    private class VisualizerView extends View {
        private byte[] mBytes;
        private float[] mPoints;
        private Rect mRect = new Rect();
        private Paint mForePaint = new Paint();

        public VisualizerView(Context context) {
            super(context);
            init();
        }

        /**
         * 初始化
         */
        private void init() {
            mBytes = null;
            mForePaint.setStrokeWidth(1f);
            mForePaint.setAntiAlias(true);
            mForePaint.setColor(Color.GREEN);
        }

        public void updateVisualizer(byte[] waveForm) {
            mBytes = waveForm;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mBytes == null) {
                return;
            }
            if (mPoints == null || mPoints.length < mBytes.length * 4) {
                mPoints = new float[mBytes.length * 4];
            }
            // 绘制白色背景
            canvas.drawColor(Color.WHITE);

            mRect.set(0, 0, getWidth(), getHeight());
            //绘制波形
            for (int i = 0; i < mBytes.length - 1; i++) {
                mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
                mPoints[i * 4 + 1] = mRect.height() / 2
                        + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
                mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
                mPoints[i * 4 + 3] = mRect.height() / 2
                        + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
            }
            canvas.drawLines(mPoints, mForePaint);
        }
    }


    /**
     * 频谱
     */
    private class VisualizerFFTView extends View {
        private byte[] mBytes;
        private float[] mPoints;
        private Rect mRect = new Rect();
        private Paint mForePaint = new Paint();
        private int mSpectrumNum = 48;

        public VisualizerFFTView(Context context) {
            super(context);
            init();
        }

        /**
         * 初始化
         */
        private void init() {
            mBytes = null;
            mForePaint.setStrokeWidth(8f);
            mForePaint.setAntiAlias(true);
            mForePaint.setColor(Color.rgb(0, 128, 255));
        }

        public void updateVisualizer(byte[] fft) {
            byte[] model = new byte[fft.length / 2 + 1];
            model[0] = (byte) Math.abs(fft[0]);
            for (int i = 2, j = 1; j < mSpectrumNum; ) {
                model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
                i += 2;
                j++;
            }
            mBytes = model;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mBytes == null) {
                return;
            }
            if (mPoints == null || mPoints.length < mBytes.length * 4) {
                mPoints = new float[mBytes.length * 4];
            }
            // 绘制白色背景
            canvas.drawColor(Color.WHITE);
            mRect.set(0, 0, getWidth(), getHeight());
            //绘制频谱
            final int baseX = mRect.width() / mSpectrumNum;
            final int height = mRect.height();
            for (int i = 0; i < mSpectrumNum; i++) {
                if (mBytes[i] < 0) {
                    mBytes[i] = 127;
                }
                final int xi = baseX * i + baseX / 2;
                mPoints[i * 4] = xi;
                mPoints[i * 4 + 1] = height;
                mPoints[i * 4 + 2] = xi;
                mPoints[i * 4 + 3] = height - mBytes[i];
            }
            canvas.drawLines(mPoints, mForePaint);
        }
    }

    /**
     * 音乐频谱
     */
    private class VisualizerGoodView extends View {

        private final int DN_W = 470;//view宽度与单个音频块占比 - 正常480 需微调
        private final int DN_H = 360;//view高度与单个音频块占比
        private final int DN_SL = 15;//单个音频块宽度
        private final int DN_SW = 5;//单个音频块高度

        private int hgap = 0;
        private int vgap = 0;
        private int levelStep = 0;
        private float strokeWidth = 0;
        private float strokeLength = 0;

        protected final static int MAX_LEVEL = 30;//音量柱·音频块 - 最大个数

        protected final static int CYLINDER_NUM = 26;//音量柱 - 最大个数

        protected Paint mPaint = null;//画笔

        protected byte[] mData = new byte[CYLINDER_NUM];//音量柱 数组

        boolean mDataEn = true;

        //构造函数初始化画笔
        public VisualizerGoodView(Context context) {
            super(context);

            mPaint = new Paint();//初始化画笔工具
            mPaint.setAntiAlias(true);//抗锯齿
            mPaint.setColor(Color.RED);//画笔颜色

            mPaint.setStrokeJoin(Paint.Join.ROUND); //频块圆角
            mPaint.setStrokeCap(Paint.Cap.ROUND); //频块圆角

            levelStep = 230 / MAX_LEVEL;
        }

        //执行 Layout 操作
        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);

            float w, h, xr, yr;

            w = right - left;
            h = bottom - top;
            xr = w / (float) DN_W;
            yr = h / (float) DN_H;

            strokeWidth = DN_SW * yr;
            strokeLength = DN_SL * xr;
            hgap = (int) ((w - strokeLength * CYLINDER_NUM) / (CYLINDER_NUM + 1));
            vgap = (int) (h / (MAX_LEVEL + 2));//频谱块高度

            mPaint.setStrokeWidth(strokeWidth); //设置频谱块宽度
        }

        //绘制频谱块和倒影
        protected void drawCylinder(Canvas canvas, float x, byte value) {
            if (value == 0) {
                value = 1;
            }//最少有一个频谱块
            for (int i = 0; i < value; i++) { //每个能量柱绘制value个能量块
                float y = (getHeight() / 2 - i * vgap - vgap);//计算y轴坐标
                float y1 = (getHeight() / 2 + i * vgap + vgap);
                //绘制频谱块
                mPaint.setColor(Color.RED);//画笔颜色
                canvas.drawLine(x, y, (x + strokeLength), y, mPaint);//绘制频谱块

                //绘制音量柱倒影
                if (i <= 6 && value > 0) {
                    mPaint.setColor(Color.RED);//画笔颜色
                    mPaint.setAlpha(100 - (100 / 6 * i));//倒影颜色
                    canvas.drawLine(x, y1, (x + strokeLength), y1, mPaint);//绘制频谱块
                }
            }
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.WHITE);

            int j = -4;
            for (int i = 0; i < CYLINDER_NUM / 2 - 4; i++) { //绘制25个能量柱

                drawCylinder(canvas, strokeWidth / 2 + hgap + i * (hgap + strokeLength), mData[i]);
            }
            for (int i = CYLINDER_NUM; i >= CYLINDER_NUM / 2 - 4; i--) {
                j++;
                drawCylinder(canvas, strokeWidth / 2 + hgap + (CYLINDER_NUM / 2 + j - 1) * (hgap + strokeLength), mData[i - 1]);
            }
        }

        /**
         * @param fft
         */
        public void updateVisualizer(byte[] fft) {
            byte[] model = new byte[fft.length / 2 + 1];
            if (mDataEn) {
                model[0] = (byte) Math.abs(fft[1]);
                int j = 1;
                for (int i = 2; i < fft.length; ) {
                    model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
                    i += 2;
                    j++;
                }
            } else {
                for (int i = 0; i < CYLINDER_NUM; i++) {
                    model[i] = 0;
                }
            }
            for (int i = 0; i < CYLINDER_NUM; i++) {
                final byte a = (byte) (Math.abs(model[CYLINDER_NUM - i]) / levelStep);

                final byte b = mData[i];
                if (a > b) {
                    mData[i] = a;
                } else {
                    if (b > 0) {
                        mData[i]--;
                    }
                }
            }
            invalidate();
        }
    }
}
