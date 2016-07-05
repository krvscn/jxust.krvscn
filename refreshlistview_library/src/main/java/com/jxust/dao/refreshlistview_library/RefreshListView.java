package com.jxust.dao.refreshlistview_library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by DAO on 2016/7/3.
 *
 * @项目名称 view
 * @创建时间 2016/7/3/18:08
 * @修改人： DAO
 * @修改时间： 2016/7/3/18:08
 */
public class RefreshListView extends ListView {

    private LinearLayout ll_refresh_head_root;

    private int ll_refresh_head_root_height;
    private int	ll_refresh_foot_Height;

    private float downY=-1;
    private final int PULL_DOWN=1; //下拉刷新状态
    private final int RELEASE_STATE=2;//松开刷新
    private final int REFRESHING =3;//正在刷新
    private int currentState=PULL_DOWN; //当前状态
    private View lunbotu;
    private int  listViewOnScreanY;  //listview在屏幕中y轴坐标位置
    private TextView tv_state;
    private TextView tv_time;
    private ImageView iv_arrow;
    private ProgressBar pb_loading;
    private RotateAnimation up_ra;
    private RotateAnimation down_ra;
    private OnRefreshDataListener listener; //刷新数据监听回调
    private View foot; //listview尾部加载更多组件
    private LinearLayout head;//listview头部刷新组件
    private boolean isEnablePullRefresh;
    private  boolean isLoadingMore;
    private boolean isEnableLoadingMore;

    public RefreshListView(Context context) {
        this(context,null);
    }

    public RefreshListView(Context context, AttributeSet attrs) {

      this(context,attrs,0);
    }



    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initAnimation();
        initEvent();
    }

    private void initEvent() {

        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //如果isEnablrLoadingMore为false就不使用 加载更多.. 组件
                if(!isEnableLoadingMore){
                    return;
                }

                //状态停止，如果listview显示最后一条 加载更多数据
                //是否最后一条数据
                if(getLastVisiblePosition()==getAdapter().getCount()-1 && ! isLoadingMore){
                    foot.setPadding(0,0,0,0);//显示加载更多
                   setSelection(getAdapter().getCount());
                //加载更多数据
                    isLoadingMore=true;
                    if(listener != null){
                        listener.loadingMore();//实现该接口的组件完成数据的加载
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void initAnimation() {
        up_ra=new RotateAnimation(0,-180, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        up_ra.setDuration(500);
        up_ra.setFillAfter(true);//停留在动画结束的状态
        down_ra=new RotateAnimation(-180,-360,Animation.RELATIVE_TO_SELF,05f,Animation.RELATIVE_TO_SELF,0.5f);
        down_ra.setDuration(500);
        down_ra.setFillAfter(true);//停留在动画结束的状态
    }

    private void initView() {
        initFoot();
        initHead();

    }


    private void initHead(){
        //头部
        head = (LinearLayout) View.inflate(getContext(), R.layout.listview_head_container,null);
        //listview刷新头
        ll_refresh_head_root=(LinearLayout)head.findViewById(R.id.ll_listview_head_root);

        //获取刷新头的子组件
        //刷新状态的文件描述
        tv_state= (TextView) head.findViewById(R.id.tv_listview_head_state_dec);
        //最新的刷新时间
        tv_time= (TextView) head.findViewById(R.id.tv_listview_head_refersh_time);

        //刷新箭头
        iv_arrow= (ImageView) head.findViewById(R.id.iv_listview_head_arrow);
        //刷新进度
        pb_loading= (ProgressBar) head.findViewById(R.id.pb_listview_head_loading);
        
        //隐藏刷行头的根布局
        //获取刷新头组件的高度
        ll_refresh_head_root.measure(0,0);
        //获取测量的高度
        ll_refresh_head_root_height=ll_refresh_head_root.getMeasuredHeight();
         
        ll_refresh_head_root.setPadding(0,-ll_refresh_head_root_height,0,0);

        addHeaderView(head);
        //
        


    }

    @Override
    public void addHeaderView(View v) {
        //判断 如果你使用下拉刷新，把头布局加下拉刷新容器中，否则加载原生ListView
        if (isEnablePullRefresh){
            //启用下拉刷新
            //轮播图组件
            lunbotu=v;
            head.addView(v);

        }else {
            super.addHeaderView(v);
        }
    }

    /**
     * 初始化尾部组件
     */
    private  void initFoot(){
        //listview尾部
        foot = View.inflate(getContext(), R.layout.listview_refresh_foot,null);

        //测量尾部组件的高度
        foot.measure(0,0);
        //listView尾部组件的高度

        ll_refresh_foot_Height=foot.getMeasuredHeight();

        foot.setPadding(0,-ll_refresh_foot_Height,0,0);
        //加载到listview中
        addFooterView(foot);

    }



    /**
     * 覆盖此方法以完成自己的事件处理
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
       // return super.onTouchEvent(ev);

        //我们的功能必须屏蔽掉父类的touch事件
        //下拉拖动（当lsietview显示第一个数据）
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downY=ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isEnablePullRefresh){
                    break;
                }
                if (currentState==REFRESHING){
                    //正在刷新
                    break;
                }
                if(!isLunboFullShow()){
                    //如果轮播没有完全显示
                    break;
                }
                //如果轮播图显示完全了
                //先判断downY有没有抓取
                if(downY==-1){
                    //如果按下的时候没有捕捉到坐标
                    downY=ev.getY();
                }
                //获取移动位置的坐标

                float moveY=ev.getY();
                //移动的位置间距
                float dy=moveY-downY;
                //下拉拖动（当listview显示出第一条数据）处理自己的事件,不让listview原生的拖动事件生效

                if(dy>0 && getFirstVisiblePosition()==0){
                    //当前padding top 参数
                    float scrollYDis=-ll_refresh_head_root_height + dy;
                    if(scrollYDis<0 && currentState!=PULL_DOWN){
                        //刷新头没有完全显示
                        //下拉刷新的状态
                        currentState=PULL_DOWN;
                        refreshState();
                    }else if(scrollYDis>=0 && currentState!=RELEASE_STATE){
                        currentState=RELEASE_STATE;
                        refreshState();
                    }
                    ll_refresh_head_root.setPadding(0, (int) scrollYDis,0,0);
                    return  true;
                }
                break;
            case MotionEvent.ACTION_UP:
                downY=-1;
                //判断状态
                //如果是PULL_DOWN状态，松开恢复原状
                if (currentState==PULL_DOWN){
                ll_refresh_head_root.setPadding(0,-ll_refresh_head_root_height,0,0);

                }else if(currentState==RELEASE_STATE){
                    ll_refresh_head_root.setPadding(0,0,0,0);
                    currentState=REFRESHING;//改变状态标识变为刷新状态
                    refreshState();//刷新界面
                    //真的刷新数据
                    if(listener!=null){
                        listener.refresdData();
                    }
                }
                break;
            default:
                break;
        }
        return  super.onTouchEvent(ev);
    }

    /**
     * @return 轮播图是否完全显示
     */
    private boolean isLunboFullShow() {
        //判断轮播图是否显示
        int[] location=new int[2];
        //如果轮播图没有完全显示则为listview的事件
        //如何判断轮播图是否完全显示
        //取轮播图在屏幕中的坐标和listview在屏幕中坐标 作判断
        
        //1.取listview坐标
        if( listViewOnScreanY==0){
            this.getLocationOnScreen(location);
            listViewOnScreanY=location[1];
        }
        //2.轮播图的坐标位置
        lunbotu.getLocationOnScreen(location);

        //3.做判断
        if(location[1] < listViewOnScreanY){
            //轮播图位置小于listview当前位置
            //继续listview事件

            return false;
        }
        //否则
        return true;

    }

    /**
     * 刷新文字描述及箭头动画设置
     */
    private void refreshState() {
        switch (currentState){
            case PULL_DOWN:
                System.out.println("下拉刷新");
                tv_state.setText("下拉刷新");
                iv_arrow.startAnimation(down_ra);
                break;
            case RELEASE_STATE:
                System.out.println("松开刷新");
                tv_state.setText("松开刷新");
                iv_arrow.startAnimation(up_ra);
                break;
            case REFRESHING:
                iv_arrow.clearAnimation();
                iv_arrow.setVisibility(View.GONE);//隐藏箭头
                pb_loading.setVisibility(View.VISIBLE);//显示进度条
                tv_state.setText("正在刷新数据");

            default:
                break;
        }

    }
    /**
     * 用户自己选择是否启用下拉刷新头的功能
     * @param isPullrefresh
     *            true 启用下拉刷新 false 不用下拉刷新
     *
     */
    public void setIsRefreshHead(boolean isPullrefresh) {
        isEnablePullRefresh = isPullrefresh;
    }

    /**
     * 用户自己选择是否启用下加载更多数据尾组件的功能
     *
     * @param isLoadingMore
     */
    public void setIsRefreshFoot(boolean isLoadingMore) {
        isEnableLoadingMore = isLoadingMore;
    }

    /**
     * 自定义监听事件接口
     * @param listener
     */

    public void setOnRefreshDataListener(OnRefreshDataListener listener){
        this.listener=listener;

    }

    public interface OnRefreshDataListener{
        void refresdData();
        void loadingMore();
    }



    /**
     * 刷新数据成功，处理结果
     */
     public void  refreshStateFinish(){

         //改变文件
         if (isLoadingMore) {
             //加载更多数据，并隐藏组件
             isLoadingMore = false;
             foot.setPadding(0, -ll_refresh_foot_Height, 0, 0);
         }else {
             tv_state.setText("下拉刷新");
             iv_arrow.setVisibility(View.VISIBLE);//显示箭头
             pb_loading.setVisibility(View.INVISIBLE);//隐藏进度条

             //设置刷新时间为当前时间
             tv_time.setText(getCurrentFormatDate());
             ll_refresh_head_root.setPadding(0,-ll_refresh_head_root_height,0,0);
             currentState=PULL_DOWN;
         }


    }

    private String getCurrentFormatDate() {
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return  format.format(new Date());

    }
}
