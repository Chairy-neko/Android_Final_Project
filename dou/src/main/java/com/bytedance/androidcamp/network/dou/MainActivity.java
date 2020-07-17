package com.bytedance.androidcamp.network.dou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.model.GetVideosResponse;
import com.bytedance.androidcamp.network.dou.model.MyVideoList;
import com.bytedance.androidcamp.network.dou.model.Video;
import com.bytedance.androidcamp.network.lib.util.ImageHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRv;
    public List<Video> mVideos = new ArrayList<>();

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(IMiniDouyinService.BASE_URL)//设置网络请求的url地址
            .addConverterFactory(GsonConverterFactory.create())//设置数据解析器
            .build();
    private IMiniDouyinService miniDouyinService = retrofit.create(IMiniDouyinService.class);

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_refresh:
                    refreshAll();
                    return true;
                case R.id.item_add:
                    startActivity(new Intent(MainActivity.this, AddActivity.class));
                    return true;
                case R.id.item_my:
                    refreshMy();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.navigation_home);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        initRecyclerView();
        refreshAll();
    }

    private void refreshAll(){
        miniDouyinService.getVideos().enqueue(new Callback<GetVideosResponse>() {
            @Override
            public void onResponse(Call<GetVideosResponse> call, Response<GetVideosResponse> response) {
                if (response.body() != null && response.body().videos != null) {
                    mVideos.clear();
                    for (int i = 0; i < response.body().videos.size(); i++) {
                        mVideos = response.body().videos;
                    }
                    mRv.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<GetVideosResponse> call, Throwable throwable) {
                Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshMy(){
        miniDouyinService.getVideos().enqueue(new Callback<GetVideosResponse>() {
            @Override
            public void onResponse(Call<GetVideosResponse> call, Response<GetVideosResponse> response) {
                if (response.body() != null && response.body().videos != null) {
                    mVideos.clear();
                    for (int i = 0; i < response.body().videos.size(); i++) {
                        Video v = response.body().videos.get(i);
                        if (v.studentId.equals("17396876307") && v.userName.equals("neko"))
                            mVideos.add(v);
                    }
                    mRv.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<GetVideosResponse> call, Throwable throwable) {
                Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;
        public View itemView;
        public TextView tv_title;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.img = itemView.findViewById(R.id.img);
            this.tv_title = itemView.findViewById(R.id.tv_title);
        }

        public void bind(final Activity activity, final Video video, int position) {
            ImageHelper.displayWebImage(video.imageUrl, img);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyVideoList myVideoList = new MyVideoList();
                    myVideoList.setmVideos(mVideos);
                    myVideoList.setPosition(position);

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("myVideoList", myVideoList);

                    Intent intent = new Intent( activity, Page2Activity.class );
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }
    }

    private void initRecyclerView() {
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(new RecyclerView.Adapter<MyViewHolder>() {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new MyViewHolder(
                        LayoutInflater.from(MainActivity.this)
                                .inflate(R.layout.video_item_view, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
                final Video video = mVideos.get(i);
                viewHolder.tv_title.setText("第" + (i+1) + "个视频");
                viewHolder.bind(MainActivity.this, video, i);
            }

            @Override
            public int getItemCount() {
                return mVideos.size();
            }
        });
    }

}
