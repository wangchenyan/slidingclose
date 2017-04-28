package me.wcy.slidingclose;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ViewPagerActivity extends SlidingActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new Adapter());
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, ListViewActivity.class);
        startActivity(intent);
    }

    private static class Adapter extends PagerAdapter {

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            View item = LayoutInflater.from(container.getContext()).inflate(R.layout.view_pager_item, container, false);
            TextView textView = (TextView) item.findViewById(R.id.item_text);
            Button button = (Button) item.findViewById(R.id.item_button);
            item.setBackgroundColor(Color.rgb(255 / (position + 1), 255 / (position + 1), 255));
            textView.setText("第" + position + "页");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    container.getContext().startActivity(new Intent(container.getContext(), ListViewActivity.class));
                }
            });
            container.addView(item);
            return item;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }
    }
}
