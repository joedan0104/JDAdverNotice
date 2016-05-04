package com.example.jdadvernotice.view;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.jdadvernotice.R;
import com.example.jdadvernotice.adapter.JDViewAdapter;

/**
 * 广告轮播组件
 * <ul>
 * 改版履历:
 * <ul>
 * <li> Created by zengyu on 2016/3/20.
 * <li> 1.1.0 周荣华  2016/4/11 内存泄露修正
 * <li> 1.1.1 周荣华 2016/4/13 广告栏高度根据子控件进行自动调整。如果想设置广告栏高度不变，可以设置定长高度。
 */
@SuppressLint("NewApi")
public class JDAdverView extends LinearLayout {
    protected static final String TAG = JDAdverView.class.getSimpleName();
    /** 动画移动方向: 向上 */
    public static final int DIRECTION_UP = 1;
    /** 动画移动方向: 向下 */
    public static final int DIRECTION_DOWN = -1;

    /** 内存缓存视图队列 */
    private Queue<View> cacheViews = new LinkedBlockingQueue<View>();
    /** 控件高度(默认每隔Item项的高度和广告栏高度一致) */
    private float mAdverHeight = 0f;
    /** 控件宽度 */
    private float mAdverWidth = 0f;
    //视图停留间隔时间
    private final int mGap = 4000;
    //动画间隔时间
    private final int mAnimDuration = 1500;
    //显示文字的尺寸
    @SuppressWarnings("unused")
    private final float TEXTSIZE = 20f;
    /** JD型广告条Adapter */
    private JDViewAdapter mAdapter;
    /** 默认广告栏高度 */
    private final float jdAdverHeight = 50;
    //显示的view
    private View mFirstView;
    private View mSecondView;
    //播放的下标
    private int mPosition;
    //线程的标识
    private boolean isStarted;
    /** 是否向上滚动 */
    private int mAnimDirection = DIRECTION_UP;
    //画笔
    @SuppressWarnings("unused")
	private Paint mPaint;
    /** 刷新动画线程 */
    private AnimRunnable mRunnable;
    /** 动画对象 */
    private AnimatorSet animatorSet;
    /** 子项目个数 */
    private int mItemCount;
    /** 内部控制刷新频度防止重复刷新重合(动画启动/停止频繁切换时需要控制动画播放频率)  */
    private long mLastAnimation = 0;

    public JDAdverView(Context context) {
        super(context, null);
        init(context, null, 0);
    }

    public JDAdverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public JDAdverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * 初始化属性
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    @SuppressWarnings("unused")
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        //设置为垂直方向
        setOrientation(VERTICAL);
        //抗锯齿效果
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //获取自定义属性
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.JDAdverView);
        mAdverHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, jdAdverHeight, getResources().getDisplayMetrics());
        int gap = array.getInteger(R.styleable.JDAdverView_gap, mGap);
        mAnimDirection = array.getInteger(R.styleable.JDAdverView_animDirection, DIRECTION_UP);
        int animDuration = array.getInteger(R.styleable.JDAdverView_animDuration, mAnimDuration);
        if (mGap <= mAnimDuration) {
            gap = mGap;
            animDuration = mAnimDuration;
        }
        //关闭清空TypedArray
        array.recycle();
    }

    @Override
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        super.setOnHierarchyChangeListener(listener);
    }

    /**
     * 设置数据
     */
    public void setAdapter(JDViewAdapter adapter) {
        this.mAdapter = adapter;
        setupAdapter();
    }

    /** 获取Adapter */
    public JDViewAdapter getAdapter() {
		return mAdapter;
	}

    /** 默认延时启动 */
    public void start() {
        start(false, 0);
    }

    /**
     * 延时启动
     * 
     * @param delay: 延时启动毫秒数
     */
    public void start(long delay) {
        start(false, delay);
    }

    /**
     * 即时启动
     * 
     * @param immediately: 是否立即启动
     */
    public void start(boolean immediately) {
        start(immediately, 0);
    }

    /**
     * 开启线程
     * 
     * @param delay: 延时毫秒数
     */
    public void start(boolean immediately, long delay) {
        if (!isStarted && null != mAdapter && mAdapter.getCount() > 1) {
        	isStarted = true;
        	if(null == mRunnable) {
        		mRunnable = new AnimRunnable();
        	}
        	if(immediately) {
        		//即时刷新立即播放动画
        		if(delay > 0) {
        			post(mRunnable);
        		} else {
        			postDelayed(mRunnable, delay);
        		}
        	} else {
        		//间隔mgap刷新一次UI
                postDelayed(mRunnable, mGap + delay);
        	}
        }
    }

    /**
     * 暂停滚动
     */
    public void stop() {
    	if(isStarted) {
            //移除handle更新
            removeCallbacks(mRunnable);
            mRunnable = null;
            if(null != animatorSet) {
                animatorSet.cancel();
                animatorSet = null;
            }
            isStarted = false;
            //内存回收 
            System.gc();
        }
    }

    /** 延时刷新 */
    public void refresh(long delay) {
    	//暂停原动画
    	stop();
    	//数据变化适配器需要重新刷新
		setupAdapter();
		//尝试启动动画
		start(false, delay);
    }

    /**
     * 数据更新后刷新列表
     * 说明:
     * 1)首先停止播放原来的动画防止出现
     * 
     */
    public void refresh() {
    	//暂停原动画
    	stop();
    	//数据变化适配器需要重新刷新
		setupAdapter();
		//尝试启动动画
		start(false);
    }

    /**
     * 设置数据适配
     */
	private void setupAdapter() {
		// 只有一条数据,不滚动
        //原始Child个数
        int childCount = getChildCount();
		if (0 == mAdapter.getCount()) {
            // 移除所有view
            clearAllChildViews();
		} else if (1 == mAdapter.getCount()) {
            // 移除所有view
            if(0 == childCount) {
                //原来的视图没有子视图
                mFirstView = mAdapter.getView(this, getCacheView());
                mAdapter.setItem(mFirstView, mAdapter.getItem(0));
                addView(mFirstView);
            } else if(childCount >= 1) {
                //移除多余的子视图
                if(childCount > 1) {
                    for(int i = childCount -1; i >= 0; i--) {
                        View child = getChildAt(i);
                        addCacheView(child);
                        removeView(child);
                    }
                }
                mFirstView = getChildAt(0);
                mAdapter.setItem(mFirstView, mAdapter.getItem(0));
            }
		} else {
			// 多个数据
			if(DIRECTION_UP == mAnimDirection) {
                if(0 == childCount) {
                    mFirstView = mAdapter.getView(this, getCacheView());
                    mSecondView = mAdapter.getView(this, getCacheView());
                    mAdapter.setItem(mFirstView, mAdapter.getItem(0));
                    mAdapter.setItem(mSecondView, mAdapter.getItem(1));
                    // 把2个添加到此控件里
                    addView(mFirstView);
                    addView(mSecondView);
                } else if(1 == childCount) {
                    mFirstView = getChildAt(0);
                    mSecondView = mAdapter.getView(this, getCacheView());
                    mAdapter.setItem(mFirstView, mAdapter.getItem(0));
                    mAdapter.setItem(mSecondView, mAdapter.getItem(1));
                    addView(mSecondView);
                } else {
                    mFirstView = getChildAt(0);
                    mSecondView = getChildAt(1);
                    mAdapter.setItem(mFirstView, mAdapter.getItem(0));
                    mAdapter.setItem(mSecondView, mAdapter.getItem(1));
                }
			} else {
                if(0 == childCount) {
                    mFirstView = mAdapter.getView(this, getCacheView());
                    mSecondView = mAdapter.getView(this, getCacheView());
                    mAdapter.setItem(mFirstView, mAdapter.getItem(0));
                    mAdapter.setItem(mSecondView, mAdapter.getItem(1));
                    // 把2个添加到此控件里
                    addView(mSecondView);
                    addView(mFirstView);
                } else if(1 == childCount) {
                    mFirstView = getChildAt(0);
                    mSecondView = mAdapter.getView(this, getCacheView());
                    mAdapter.setItem(mFirstView, mAdapter.getItem(0));
                    mAdapter.setItem(mSecondView, mAdapter.getItem(1));
                    addView(mSecondView, 0);
                } else {
                    mFirstView = getChildAt(1);
                    mSecondView = getChildAt(0);
                    mAdapter.setItem(mFirstView, mAdapter.getItem(0));
                    mAdapter.setItem(mSecondView, mAdapter.getItem(1));
                }
			}
			mPosition = 1;
			isStarted = false;
		}
        Log.d(TAG, "setupAdapter");
	}

	/** 清理全部子控件视图(以及缓存的引用) */
	private void clearAllChildViews() {
		// 移除所有view
		removeAllViews();
		//当子控件内容全部清空时需要清理原视图缓存否则会引起视图引用异常
		cacheViews.clear();
	}

    /** 获取一个缓存的视图 */
    private View getCacheView() {
    	if(!cacheViews.isEmpty()) {
    		//从表头获取一个元素，若队列为空，返回null
    		return cacheViews.poll();
    	}
    	return null;
    }

    /** 添加一个缓存视图 */
    @SuppressWarnings("unused")
	private void addCacheView(View view) {
    	if(!cacheViews.contains(view)) {
    		cacheViews.offer(view);
    	}
    }

    /**
     * 设置控件预设高度
     * 
     * @param height: 预设的广告栏高度
     */
    public void setAdverHeight(float height) {
    	if(height > 0) {
    		mAdverHeight = height;
    	}
    }
    
    /**
     * 测量控件的宽高
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int childWidth = 0;
        int childHeight = 0;
        int childState = 0;
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        mItemCount = mAdapter == null ? 0 : mAdapter.getCount();
        if (mItemCount > 0 && (widthMode == MeasureSpec.UNSPECIFIED ||
                heightMode == MeasureSpec.UNSPECIFIED)) {
            final View child = getChildAt(0);

            //测量子控件（子控件自动适配）
            measureScrapChild(child, 0, widthMeasureSpec);

            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            if(Build.VERSION.SDK_INT  >= Build.VERSION_CODES.HONEYCOMB) {
                //3.0以上版本可使用getMeasuredState
                childState = combineMeasuredStates(childState, child.getMeasuredState());
            }
        }

        if (widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = paddingLeft + paddingRight + childWidth +
                    getVerticalScrollbarWidth();
        } else {
            widthSize |= (childState&MEASURED_STATE_MASK);
        }

        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = paddingTop + paddingBottom + childHeight +
                    getVerticalFadingEdgeLength() * 2;
        }
        if(mAdapter.getCount() > 1) {
            if(heightSize > mAdverHeight) {
                mAdverHeight = heightSize;
            }
            if(widthSize > mAdverWidth) {
                mAdverWidth = widthSize;
            }
            if (mFirstView != null) {
                mFirstView.getLayoutParams().width = (int)mAdverWidth;
                mFirstView.getLayoutParams().height = (int) mAdverHeight;
            }
            if (mSecondView != null) {
                mSecondView.getLayoutParams().width = (int) mAdverWidth;
                mSecondView.getLayoutParams().height = (int) mAdverHeight;
            }
            if (DIRECTION_DOWN == mAnimDirection) {
                // 移动到mFirstView
                scrollTo(0, (int)mAdverHeight);
            }
        } else {
            // 移动到恢复到第一个子视图的位置放置出现空白(例如从向下滚动多屏变为一屏控件默认位置为(0, mAdverHeight)出现空白)
            scrollTo(0, 0);
        }
        //设置控件的测量宽度和高度
        setMeasuredDimension(widthSize , heightSize);
    }

    /**
     * 测量子控件的高度
     * 
     * @param child: 子控件
     * @param i: 子控件索引
     * @param widthMeasureSpec: 父控件测量宽度
     * 
     */
    private void measureScrapChild(View child, int i, int widthMeasureSpec) {
        LayoutParams p = (LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (LayoutParams) generateDefaultLayoutParams();
            child.setLayoutParams(p);
        }

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int childWidthSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec,
        		paddingLeft + paddingRight, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * 画布局
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /**
     * 垂直滚动
     */
    private void performSwitch() {
        if(mAdapter.getCount() <= 1) {
            return;
        }
        if(null == animatorSet) {
        	float offsetY = (DIRECTION_UP == mAnimDirection) ? - mAdverHeight : mAdverHeight;
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(mFirstView, "translationY", ViewHelper.getTranslationY(mFirstView) + offsetY);
            ObjectAnimator animator2 = ObjectAnimator.ofFloat(mSecondView, "translationY", ViewHelper.getTranslationY(mSecondView) + offsetY);
            //动画集
            animatorSet = new AnimatorSet();
            animatorSet.playTogether(animator1, animator2);//2个动画一起
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    Log.d(TAG, "onAnimationStart");
                }

                @Override
                public void onAnimationEnd(Animator animation) {//动画结束
                    Log.d(TAG, "onAnimationEnd");
                    if (null != mFirstView) {
                        ViewHelper.setTranslationY(mFirstView, 0);
                    }
                    if (null != mSecondView) {
                        ViewHelper.setTranslationY(mSecondView, 0);
                    }
                    int lastChildIndex = DIRECTION_UP == mAnimDirection ? 0 : 1;
                    View removedView = getChildAt(lastChildIndex);//获得第一个子布局
                    if (null != removedView) {
                        mPosition++;
                        //设置显示的布局
                        mAdapter.setItem(removedView, mAdapter.getItem(mPosition % mAdapter.getCount()));
                        //移除前一个view
                        removeView(removedView);
                        //添加下一个view
                        int secondChildIndex = DIRECTION_UP == mAnimDirection ? 1 : 0;
                        addView(removedView, secondChildIndex);
                    }
                }

            });
            animatorSet.setDuration(mAnimDuration);
        }
        long curTimed = System.currentTimeMillis();
        if(curTimed - mLastAnimation >= mGap) {
            //控制刷新频度(如果与上一次动画播放间距小于动画播放间距不进行播放)
            mLastAnimation = curTimed;
            animatorSet.start();
        }
    }

    /** 动画播放方向 */
    public boolean isAnimationUp() {
        return mAnimDirection > 0;
    }
    
    /**
     * 定时刷新线程任务
     * 
     */
    private class AnimRunnable implements Runnable {

        @Override
        public void run() {
            performSwitch();
            postDelayed(this, mGap);
        }
    }

    /**
     * 屏幕 旋转
     *
     * @param newConfig
     */
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
