package com.hello.sandbox.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import top.niunaijun.blackboxa.R;

public class SideBarLayout extends RelativeLayout
    implements SideBarSortView.OnIndexChangedListener {
  private View mLayout;
  private Context mContext;
  private TextView mTvTips;
  private SideBarSortView mSortView;
  private int selectTextColor;
  private int unselectTextColor;
  private float selectTextSize;
  private float unselectTextSize;

  private int wordTextColor;
  private float wordTextSize;
  private Drawable wordBackground;

  public SideBarLayout(Context context) {
    super(context);
  }

  public SideBarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
    initView();
  }

  public SideBarLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
    initView();
  }

  private void init(Context context, AttributeSet attrs) {
    mContext = context;
    // 获取自定义属性
    if (attrs != null) {
      TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.SideBarView);
      unselectTextColor =
          ta.getColor(
              R.styleable.SideBarView_sidebarUnSelectTextColor, Color.parseColor("#1ABDE6"));
      selectTextColor =
          ta.getColor(R.styleable.SideBarView_sidebarSelectTextColor, Color.parseColor("#2E56D7"));
      selectTextSize =
          ta.getDimension(R.styleable.SideBarView_sidebarSelectTextSize, dip2px(mContext, 12));
      unselectTextSize =
          ta.getDimension(R.styleable.SideBarView_sidebarUnSelectTextSize, dip2px(mContext, 10));

      wordTextSize =
          ta.getDimension(R.styleable.SideBarView_sidebarWordTextSize, px2sp(mContext, 45));
      wordTextColor =
          ta.getColor(R.styleable.SideBarView_sidebarWordTextColor, Color.parseColor("#FFFFFF"));
      wordBackground = ta.getDrawable(R.styleable.SideBarView_sidebarWordBackground);
      if (wordBackground == null) {
        wordBackground = context.getResources().getDrawable(R.drawable.sort_text_view_hint_bg);
      }
      ta.recycle();
    }
  }

  private void initView() {
    // 引入布局
    mLayout = LayoutInflater.from(mContext).inflate(R.layout.view_sidebar_layout, null, true);
    mTvTips = (TextView) mLayout.findViewById(R.id.tvTips);
    mSortView = (SideBarSortView) mLayout.findViewById(R.id.sortView);
    mSortView.setIndexChangedListener(this);

    mSortView.setmTextColor(unselectTextColor);
    mSortView.setmTextSize(unselectTextSize);

    mSortView.setmTextColorChoose(selectTextColor);
    mSortView.setmTextSizeChoose(selectTextSize);
    mSortView.invalidate();

    mTvTips.setTextColor(wordTextColor);
    mTvTips.setTextSize(px2sp(mContext, wordTextSize));
    mTvTips.setBackground(wordBackground);
    this.addView(mLayout); // 将子布局添加到父容器,才显示控件
  }

  /** 监听回调：由侧边栏滑动更新Item */
  private OnSideBarLayoutListener mListener;

  public static interface OnSideBarLayoutListener {
    void onSideBarScrollUpdateItem(String word);
  }

  public void setSideBarLayoutListener(OnSideBarLayoutListener listener) {
    this.mListener = listener;
  }

  /**
   * 侧边栏滑动 更新Item
   *
   * @param word 字母
   */
  @Override
  public void onSideBarScrollUpdateItem(String word) {
    mTvTips.setVisibility(View.VISIBLE);
    mTvTips.setText(word);
    if (mListener != null) {
      mListener.onSideBarScrollUpdateItem(word);
    }
  }

  /** 侧边栏滑动结束 隐藏提示 */
  @Override
  public void onSideBarScrollEndHideText() {
    mTvTips.setVisibility(View.GONE);
  }

  /**
   * Item滚动更新 侧边栏
   *
   * @param word
   */
  public void onItemScrollUpdateSideBarText(String word) {
    if (mListener != null) {
      mSortView.onUpdateSideBarText(word);
    }
  }

  public static int dip2px(Context context, float dpValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }

  public static int px2sp(Context context, float pxValue) {
    final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
    return (int) (pxValue / fontScale + 0.5f);
  }
}
