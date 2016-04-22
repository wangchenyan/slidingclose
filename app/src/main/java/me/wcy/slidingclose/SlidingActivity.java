package me.wcy.slidingclose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SlidingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SlidingLayout rootView = new SlidingLayout(this);
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
