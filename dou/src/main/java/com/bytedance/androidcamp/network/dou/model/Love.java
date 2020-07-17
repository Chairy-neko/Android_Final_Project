package com.bytedance.androidcamp.network.dou.model;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bytedance.androidcamp.network.dou.Page2Activity;
import com.bytedance.androidcamp.network.dou.R;

import java.util.Random;
import java.util.jar.Attributes;

public class Love extends RelativeLayout implements View.OnClickListener {

        private OnCallBack onCallBack;
        private Context context;
        private float[] num = new float[]{-35f, -25f, 0f, 25f, 35f};
        private long[] mHits = new long[2];
        private ImageView img_h;

        public Love(Context context) {
            this(context,null);
        }

        public Love(Context context, AttributeSet attrs) {
            this(context, attrs,0);
        }

        public Love(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            this.context = context;
        }

        //监听点击事件
        public void onClick(View v) {
            System.arraycopy(mHits,1, mHits,0,mHits.length - 1);
            mHits[mHits.length-1] = SystemClock.uptimeMillis();

            if (mHits[0] >= (SystemClock.uptimeMillis() - 200)){
                final ImageView imageView = new ImageView(context);
                Toast.makeText( context, "这里是点击爱心的动画，待展示", Toast.LENGTH_SHORT).show();
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                View view1 = layoutInflater.inflate(R.layout.activity_page2, null);
                img_h = view1.findViewById(R.id.img_heart);
                img_h.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.heart_red);
                imageView.bringToFront();

                //为组件添加动画
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(//缩放动画，X轴2倍缩小至0.9倍
                        scaleAni(imageView, "scaleX", 2f, 0.9f, 100L, 0L))
                        //缩放动画，Y轴2倍缩放至0.9倍
                        .with(scaleAni(imageView, "scaleY", 2f, 0.9f, 100l, 0l))
                        //旋转动画，随机旋转角
                        .with(rotation(imageView, 0l, 0l, num[new Random().nextInt(4)]))
                        //渐变透明动画，透明度从0-1
                        .with(alphaAni(imageView, 0F, 1F, 100l, 0L))
                        //缩放动画，X轴0.9倍缩小至
                        .with(scaleAni(imageView, "scaleX", 0.9f, 1F, 50L, 150L))
                        //缩放动画，Y轴0.9倍缩放至
                        .with(scaleAni(imageView, "scaleY", 0.9f, 1F, 50L, 150L))
                        //位移动画，Y轴从0上移至600
                        .with(translationY(imageView, 0F, -600F, 800L, 400L))
                        //透明动画，从1-0
                        .with(alphaAni(imageView, 1F, 0F, 300L, 400L))
                        //缩放动画，X轴1至3倍
                        .with(scaleAni(imageView, "scaleX", 1F, 3f, 700L, 400L))
                        //缩放动画，Y轴1至3倍
                        .with(scaleAni(imageView, "scaleY", 1F, 3f, 700L, 400L));
                animatorSet.start();
                animatorSet.addListener(new AnimatorListenerAdapter(){
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        img_h.setVisibility(View.INVISIBLE);
                    }
                });

                if (onCallBack!=null){
                    onCallBack.callback();
                }
            }
            // return super.onTouchEvent(event);
        }

        private ObjectAnimator scaleAni(View view,String propertyName,Float from,Float to ,Long time,Long delay){
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, propertyName, from, to);
            objectAnimator.setInterpolator(new LinearInterpolator());
            objectAnimator.setStartDelay(delay);
            objectAnimator.setDuration(time);
            return objectAnimator;
        }

        private ObjectAnimator translationX(View view,Float from,Float to,Long time, Long delayTime){
            ObjectAnimator ani = ObjectAnimator.ofFloat(view, "translationX", from, to);
            ani.setInterpolator(new LinearInterpolator());
            ani.setStartDelay(delayTime);
            ani.setDuration(time);
            return ani;
        }

        private ObjectAnimator translationY(View view, Float from, Float to, Long time,Long delayTime){
            ObjectAnimator ani = ObjectAnimator.ofFloat(view, "translationY", from, to);
            ani.setInterpolator(new LinearInterpolator());
            ani.setStartDelay(delayTime);
            ani.setDuration(time);
            return ani;
        }

        private ObjectAnimator alphaAni(View view,Float from,Float to,Long time,Long delayTime){
            ObjectAnimator ani = ObjectAnimator.ofFloat(view, "alpha", from, to);
            ani.setInterpolator(new LinearInterpolator());
            ani.setStartDelay(delayTime);
            ani.setDuration(time);
            return ani;
        }

        private ObjectAnimator rotation(View view,Long time,Long delayTime,Float values){
            ObjectAnimator ani = ObjectAnimator.ofFloat(view, "rotation",values);
            ani.setInterpolator(new TimeInterpolator() {
                @Override
                public float getInterpolation(float input) {
                    return 0;
                }
            });
            ani.setStartDelay(delayTime);
            ani.setDuration(time);
            return ani;
        }

        public void setCallBack(OnCallBack onCallBack){
            this.onCallBack = onCallBack;
        }
        public interface OnCallBack{
            void callback();
        }
}
