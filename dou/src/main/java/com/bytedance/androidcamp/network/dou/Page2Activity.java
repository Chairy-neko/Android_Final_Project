package com.bytedance.androidcamp.network.dou;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bytedance.androidcamp.network.dou.database.LikeDao;
import com.bytedance.androidcamp.network.dou.database.LikeDatabase;
import com.bytedance.androidcamp.network.dou.database.LikeEntity;
import com.bytedance.androidcamp.network.dou.model.BaseRecAdapter;
import com.bytedance.androidcamp.network.dou.model.BaseRecViewHolder;
import com.bytedance.androidcamp.network.dou.model.MyVideoList;
import com.bytedance.androidcamp.network.dou.model.MyVideoPlayer;
import com.bytedance.androidcamp.network.dou.model.OnRecyclerItemClickListener;
import com.bytedance.androidcamp.network.dou.model.Video;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class Page2Activity extends AppCompatActivity {

    @BindView(R.id.rv_page2)
    RecyclerView rvPage2;
    private ListVideoAdapter videoAdapter;
    private List<Video> mVideos = new ArrayList<>();
    private PagerSnapHelper snapHelper;
    private LinearLayoutManager layoutManager;
    private int position;
    private int currentPosition;
    private boolean inLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page2);
        ButterKnife.bind(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        initView();
        addListener();
    }

    private void initView() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        MyVideoList myVideoList = (MyVideoList)bundle.getSerializable("myVideoList");
        mVideos = myVideoList.mVideos;
        position = myVideoList.getPosition();

        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvPage2);

        videoAdapter = new ListVideoAdapter(mVideos);

        layoutManager = new LinearLayoutManager(Page2Activity.this, LinearLayoutManager.VERTICAL, false);
        rvPage2.setLayoutManager(layoutManager);
        rvPage2.setAdapter(videoAdapter);
        rvPage2.scrollToPosition(position);
    }

    private void addListener() {
        rvPage2.addOnItemTouchListener(new OnRecyclerItemClickListener(rvPage2){
            @Override
            public void onLongClick(RecyclerView.ViewHolder vh) {
                View view = snapHelper.findSnapView(layoutManager);
                int pos = rvPage2.getChildPosition(view);
                String[] str = new String[]{mVideos.get(pos).videoUrl};
                new Thread() {
                    @Override
                    public void run() {
                        inLike = false;
                        LikeDao dao = LikeDatabase.inst(Page2Activity.this).LikeDao();
                        final List<LikeEntity> entityList = dao.loadAllByIds(str);
                        dao.deleteAll();
                        if(!entityList.isEmpty()) {
                            inLike = true;
                        }
                    }
                }.start();
                if(inLike == true) {
                    ImageView img = view.findViewById(R.id.img_heart);
                    img.setVisibility(View.INVISIBLE);
                }
                Toast.makeText( Page2Activity.this, "Don't like this vedio!!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                View view = snapHelper.findSnapView(layoutManager);
                RecyclerView.ViewHolder viewHolder = rvPage2.getChildViewHolder(view);
                if (viewHolder != null && viewHolder instanceof VideoViewHolder) {
                    ((VideoViewHolder) viewHolder).tv_title.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ImageView img = view.findViewById(R.id.img_heart);
                            img.setVisibility(View.VISIBLE);
                            int pos = rvPage2.getChildPosition(view);
                            new Thread() {
                                @Override
                                public void run() {
                                    LikeDao dao = LikeDatabase.inst(Page2Activity.this).LikeDao();
                                    dao.addTodo(new LikeEntity(mVideos.get(pos).videoUrl));
                                }
                            }.start();
                            Toast.makeText( Page2Activity.this, "Like this video !!!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        rvPage2.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE://停止滚动
                        View view = snapHelper.findSnapView(layoutManager);

                        //当前固定后的item position
                        int pos = recyclerView.getChildAdapterPosition(view);
                        if (currentPosition != pos) {
                            //如果当前position 和 上一次固定后的position 相同, 说明是同一个, 只不过滑动了一点点, 然后又释放了
                            MyVideoPlayer.releaseAllVideos();
                            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
                            if (viewHolder != null && viewHolder instanceof VideoViewHolder) {
                                ((VideoViewHolder) viewHolder).mp_video.startVideo();

                            }
                        }
                        currentPosition = pos;
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING://拖动
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING://惯性滑动
                        break;
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        MyVideoPlayer.releaseAllVideos();
    }

    class ListVideoAdapter extends BaseRecAdapter< Video, VideoViewHolder > {

        public ListVideoAdapter(List<Video> list) {
            super(list);
        }

        @Override
        public void onHolder(VideoViewHolder holder, Video bean, int pos) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

            holder.mp_video.setUp( bean.videoUrl, "第" + pos + "个视频", MyVideoPlayer.STATE_NORMAL);
            holder.mp_video.startVideo();

            Glide.with(context).load(bean.videoUrl).into(holder.mp_video.thumbImageView);
            holder.tv_title.setText("Video from: " + bean.userName);

            String[] str = new String[]{ mVideos.get(pos+1).videoUrl };
            ImageView img = holder.img_heart;
                new Thread() {
                    @Override
                    public void run() {
                        inLike = false;
                        LikeDao dao = LikeDatabase.inst(Page2Activity.this).LikeDao();
                        final List<LikeEntity> entityList = dao.loadAllByIds(str);
                        if(!entityList.isEmpty()) {
                            inLike = true;
                        }
                    }
                }.start();
                if(inLike == true) {
                    img.setVisibility(View.VISIBLE);
                } else {
                    img.setVisibility(View.INVISIBLE);
                }
            }

        @Override
        public VideoViewHolder onCreateHolder() {
            return new VideoViewHolder(getViewByRes(R.layout.item_page_2));
        }
    }

    public class VideoViewHolder extends BaseRecViewHolder {
        public View rootView;
        public MyVideoPlayer mp_video;
        public TextView tv_title;
        public ImageView img_heart;

        public VideoViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.mp_video = rootView.findViewById(R.id.mp_video);
            this.tv_title = rootView.findViewById(R.id.tv_title);
            this.img_heart = rootView.findViewById(R.id.img_heart);
        }
    }
}
