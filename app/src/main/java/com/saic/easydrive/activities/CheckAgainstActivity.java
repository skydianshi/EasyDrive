package com.saic.easydrive.activities;

import android.view.View;

import com.saic.easydrive.R;
import com.saic.easydrive.ui.MyToolbar;

public class CheckAgainstActivity extends BaseActivity {

    MyToolbar tb;
    @Override
    protected View getContentView() {
        return inflateView(R.layout.activity_check_against);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        tb = (MyToolbar)findViewById(R.id.toolbar);
        tb.setTitle("违章查询");
        tb.setTitleTextColor(getResources().getColor(R.color.white));
    }

}
