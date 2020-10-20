package com.kongzue.dialogx.util.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import androidx.core.view.ViewCompat;

import com.kongzue.dialogx.R;
import com.kongzue.dialogx.interfaces.BaseDialog;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.kongzue.dialogx.interfaces.OnSafeInsetsChangeListener;

import java.util.Arrays;
import java.util.List;

/**
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2020/9/22 13:53
 */
public class DialogXBaseRelativeLayout extends RelativeLayout {
    
    private OnSafeInsetsChangeListener onSafeInsetsChangeListener;
    private boolean autoUnsafePlacePadding = true;
    
    private OnLifecycleCallBack onLifecycleCallBack;
    private OnBackPressedListener onBackPressedListener;
    
    public DialogXBaseRelativeLayout(Context context) {
        super(context);
        init();
    }
    
    public DialogXBaseRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public DialogXBaseRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    public DialogXBaseRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    
    private boolean isInited = false;
    
    private void init() {
        if (!isInited) {
            setFocusableInTouchMode(true);
            requestFocus();
        }
    }
    
    @Override
    protected boolean fitSystemWindows(Rect insets) {
        paddingView(insets.left, insets.top, insets.right, insets.bottom);
        return super.fitSystemWindows(insets);
    }
    
    @Override
    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            paddingView(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
        }
        return super.dispatchApplyWindowInsets(insets);
    }
    
    public void paddingView(WindowInsets insets) {
        if (insets == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            paddingView(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
        }
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (onBackPressedListener != null) {
                onBackPressedListener.onBackPressed();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        return super.onTouchEvent(event);
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final ViewParent parent = getParent();
        
        ViewCompat.setFitsSystemWindows(this, ViewCompat.getFitsSystemWindows((View) parent));
        ViewCompat.requestApplyInsets(this);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isInEditMode()) {
            ((Activity) BaseDialog.getContext()).getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(decorViewLayoutListener);
        }
        
        if (onLifecycleCallBack != null) {
            onLifecycleCallBack.onShow();
        }
    }
    
    private ViewTreeObserver.OnGlobalLayoutListener decorViewLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                paddingView(getRootWindowInsets());
            }
        }
    };
    
    @Override
    protected void onDetachedFromWindow() {
        if (decorViewLayoutListener != null) {
            ((Activity) BaseDialog.getContext()).getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(decorViewLayoutListener);
        }
        if (onLifecycleCallBack != null) {
            onLifecycleCallBack.onDismiss();
        }
        super.onDetachedFromWindow();
    }
    
    @Override
    public boolean performClick() {
        return super.performClick();
    }
    
    public DialogXBaseRelativeLayout setOnLifecycleCallBack(OnLifecycleCallBack onLifecycleCallBack) {
        this.onLifecycleCallBack = onLifecycleCallBack;
        return this;
    }
    
    public float getSafeHeight() {
        return getMeasuredHeight() - unsafePlace.bottom - unsafePlace.top;
    }
    
    public abstract static class OnLifecycleCallBack {
        public void onShow() {
        }
        
        public abstract void onDismiss();
    }
    
    protected Rect unsafePlace;
    
    private void paddingView(int left, int top, int right, int bottom) {
        unsafePlace = new Rect(left, top, right, bottom);
        if (onSafeInsetsChangeListener != null) onSafeInsetsChangeListener.onChange(unsafePlace);
        MaxRelativeLayout bkgView = findViewById(R.id.bkg);
        if (bkgView != null && bkgView.getLayoutParams() instanceof LayoutParams) {
            LayoutParams bkgLp = (LayoutParams) bkgView.getLayoutParams();
            if (bkgLp.getRules()[ALIGN_PARENT_BOTTOM] == RelativeLayout.TRUE && isAutoUnsafePlacePadding()) {
                bkgView.setPadding(0, 0, 0, bottom);
                setPadding(left, top, right, 0);
                return;
            }
        }
        if (isAutoUnsafePlacePadding()) setPadding(left, top, right, bottom);
    }
    
    public DialogXBaseRelativeLayout setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
        return this;
    }
    
    public OnSafeInsetsChangeListener getOnSafeInsetsChangeListener() {
        return onSafeInsetsChangeListener;
    }
    
    public DialogXBaseRelativeLayout setOnSafeInsetsChangeListener(OnSafeInsetsChangeListener onSafeInsetsChangeListener) {
        this.onSafeInsetsChangeListener = onSafeInsetsChangeListener;
        return this;
    }
    
    public boolean isAutoUnsafePlacePadding() {
        return autoUnsafePlacePadding;
    }
    
    public DialogXBaseRelativeLayout setAutoUnsafePlacePadding(boolean autoUnsafePlacePadding) {
        this.autoUnsafePlacePadding = autoUnsafePlacePadding;
        return this;
    }
}
