package com.linfeng.shopowner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.linfeng.shopowner.server.ApiServiceManager;
import com.linfeng.shopowner.util.CommonUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.linfeng.shopowner.util.CommonUtil.PARAM_GOOD_ID;

public class MainActivity extends AppCompatActivity {

    private  static final String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private List<GoodInfo>  mGoodsList;
    private Disposable mDisposable;
    private Disposable mDeleteDisposable;
    private Activity mActivity;
    private boolean mIsRefreshing;
    private SwipeRefreshLayout mRefreshLayout;
    private ProgressDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("lwxdebug", "onCreate");
        Picasso.get().setIndicatorsEnabled(true);
        mActivity = this;
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.home_list_view);
        fetchGoodsInfo();
        FloatingActionButton addBtn = findViewById(R.id.add_button);
        if (addBtn != null) {
            addBtn.setOnClickListener(v -> {
                // 进入新建商品信息页面
                Intent intent = new Intent(mActivity, DetailActivity.class);
                startActivity(intent);
            });
        }
        mRefreshLayout = findViewById(R.id.refresh_layout);
        if (mRefreshLayout != null) {
            mRefreshLayout.setOnRefreshListener(() -> {
                mIsRefreshing = true;
                fetchGoodsInfo();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mDeleteDisposable != null) {
            mDeleteDisposable.dispose();
        }
    }

    private void fetchGoodsInfo() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mDisposable = ApiServiceManager.getInstance().getAPI().getGoodsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( list -> {
                    Log.d("lwxdebug", "list is" + list);
                    mGoodsList = list;
                    initAdapter();
                    mAdapter.notifyDataSetChanged();
                    if (mIsRefreshing) {
                        mIsRefreshing = false;
                        mRefreshLayout.setRefreshing(false);
                    }
                }, e -> {
                    Log.e("MainActivity", "get api service error", e);
                });
    }

    private void initAdapter() {
        if (mGoodsList == null || mGoodsList.isEmpty()) {
            return;
        }
        mAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_good_item_layout, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                View itemView = holder.itemView;
                GoodInfo currentInfo = mGoodsList.get(position);
                if (currentInfo == null) {
                    Log.e("SHOP_OWNER", "error bind good info is null, position:" + position);
                    return;
                }
                itemView.setOnClickListener(v -> {
                    int goodId = currentInfo.getId();
                    Intent intent = new Intent(mActivity, DetailActivity.class);
                    intent.putExtra(PARAM_GOOD_ID, goodId);
                    startActivity(intent);
                });
                ImageView imgView = itemView.findViewById(R.id.good_image);
                TextView txtView = itemView.findViewById(R.id.good_info);
                Log.d("lwxdebug", "" + currentInfo);
                String imgUrl = currentInfo.getImg();
                Picasso.get().load(imgUrl)
                        .resize(400, 400)
                        .centerCrop()
                        .into(imgView);
                int id = currentInfo.getId();
                String name = currentInfo.getName();
                String divider = getResources().getString(R.string.string_divider);
                txtView.setText(id + divider + name);
                TextView deleteBtn = itemView.findViewById(R.id.delete_item_btn);
                deleteBtn.setOnClickListener(v -> onDeleteBtnClick(position, currentInfo.getId()));
            }

            @Override
            public int getItemCount() {
                return mGoodsList.size();
            }
        };
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void onDeleteBtnClick(int position, int goodId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage("确定要删除该商品吗？")
                .setPositiveButton("确认", (dialog, which) -> {
                    showLoading();
                    deleteGoodFromList(position, goodId);
                    
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                });
        // 创建并显示弹窗
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void deleteGoodFromList(int position, int goodId) {
        if (mDeleteDisposable != null && !mDeleteDisposable.isDisposed()) {
            mDeleteDisposable.dispose();
        }
        List<Integer> toDeleteList = new ArrayList<>();
        toDeleteList.add(goodId);
        mDeleteDisposable = ApiServiceManager.getInstance().getAPI().deleteGoodItem(toDeleteList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    if (res.result == CommonUtil.RESPONSE_SUCCESS) {
                        mGoodsList.remove(position);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(mActivity, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                    dismissLoading();

                }, e -> {
                    dismissLoading();
                    Toast.makeText(mActivity, "删除失败 error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void showLoading() {
        mLoadingDialog = new ProgressDialog(mActivity);
        mLoadingDialog.setMessage("删除中...");
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.show();
    }

    private void dismissLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }
}