package com.linfeng.shopowner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linfeng.shopowner.model.UpdateInfoRequest;
import com.linfeng.shopowner.server.ApiServiceManager;
import com.linfeng.shopowner.util.BitmapUtil;
import com.linfeng.shopowner.util.CommonUtil;
import com.linfeng.shopowner.util.FileUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import com.qiniu.android.storage.UploadManager;

import static com.linfeng.shopowner.util.CommonUtil.PARAM_GOOD_ID;
import static com.linfeng.shopowner.util.CommonUtil.RESPONSE_SUCCESS;
import static com.linfeng.shopowner.util.FileUtil.saveBitmapToFile;
import static com.linfeng.shopowner.util.PermissionUtil.checkReadExternalStoragePermission;
import static com.qiniu.android.http.ResponseInfo.RequestSuccess;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private static final String CDN_URL = "http://cdn.yulan.work/";
    private static final int MAX_IMAGE_NUM = 9;
    private static final int PICK_IMAGE_REQUEST = 1;

    private Disposable mFetchInfoDisposable;
    private Disposable mFetchTokenDisposable;
    private Disposable mUpdateDataDisposable;
    private GoodDetailInfo mGoodDetailInfo;
    private  EditText mNameEdit;
    private  EditText mDesEdit;
    private  EditText mTagEdit;
    private  Spinner mTypeEdit;
    private  EditText mNumEdit;
    private  EditText mPriceEdit;
    private RecyclerView mImgRecyclerView;
    private Activity mActivity;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> mImageAdapter;
    private final List<String> mToDeleteImgList = new ArrayList<>();
    private final List<String> mToAddImgList = new ArrayList<>();
    private boolean mIsNewGood = false;
    private String mUploadToken = "";
    private ProgressDialog mLoadingDialog;
    private boolean mIsCheckedShowPrice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Picasso.get().setIndicatorsEnabled(true);
        Log.d(TAG, "onCreate");
        mActivity = this;
        mNameEdit = findViewById(R.id.good_name_text);
        mDesEdit = findViewById(R.id.good_des_text);
        mTagEdit = findViewById(R.id.good_tag_text);
        mTypeEdit = findViewById(R.id.good_type_spinner);
        mNumEdit = findViewById(R.id.good_num_text);
        mPriceEdit = findViewById(R.id.good_price_text);
        mImgRecyclerView = findViewById(R.id.img_grid_list);
        View priceLayout = findViewById(R.id.good_price_layout);
        AppCompatCheckBox checkBox = findViewById(R.id.show_price_checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                priceLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                mIsCheckedShowPrice = isChecked;
            }
        });
        fetchUploadToken();

        Intent intent = getIntent();
        int goodID = intent.getIntExtra(PARAM_GOOD_ID, 0);
        if (goodID > 0) {
            TextView idTv = findViewById(R.id.good_id_text);
            idTv.setText(goodID + "");
            fetchGoodDetailInfo(goodID);
        } else {
            mIsNewGood = true;
            mGoodDetailInfo = new GoodDetailInfo();
            refreshImages();
        }
        View confirmBtn = findViewById(R.id.confirm_btn);
        if (confirmBtn != null) {
            confirmBtn.setOnClickListener(v -> onConfirmBtnClick());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mFetchInfoDisposable != null) {
            mFetchInfoDisposable.dispose();
        }
        if (mUpdateDataDisposable != null) {
            mUpdateDataDisposable.dispose();
        }
    }

    private void fetchGoodDetailInfo(int goodID) {
        if (mFetchInfoDisposable != null && !mFetchInfoDisposable.isDisposed()) {
            mFetchInfoDisposable.dispose();
        }
        mFetchInfoDisposable = ApiServiceManager.getInstance().getAPI().getGoodDetailInfo(goodID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( info -> {
                    Log.d(TAG, "fetchGoodDetailInfo:" + info);
                    mGoodDetailInfo = info;
                    refreshViews();
                }, Throwable::printStackTrace);
    }



    private void fetchUploadToken() {
        if (mFetchTokenDisposable != null && !mFetchTokenDisposable.isDisposed()) {
            mFetchTokenDisposable.dispose();
        }
        mFetchTokenDisposable = ApiServiceManager.getInstance().getAPI().getGoodUploadToken()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.result == RESPONSE_SUCCESS) {
                        mUploadToken = response.mToken;
                    }
                }, Throwable::printStackTrace);
    }


    /**
     * 获取到数据后刷新视图
     */
    private void refreshViews() {
        mNameEdit.setText( mGoodDetailInfo.getName());
        mDesEdit.setText(mGoodDetailInfo.getDes());
        mTagEdit.setText(mGoodDetailInfo.getTag());
        mTypeEdit.setSelection(mGoodDetailInfo.getType());
        mNumEdit.setText(String.valueOf(mGoodDetailInfo.getNum()));
        mPriceEdit.setText(String.valueOf(mGoodDetailInfo.getPrice()));
        refreshImages();
    }

    private void refreshImages() {
        Log.d(TAG, "refreshImages");
        // 图片更新
        mImgRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        initAdapter();
        mImgRecyclerView.setAdapter(mImageAdapter);
    }

    private void initAdapter() {
        int imgNum = getCurrentImgNum();
        mImageAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_image_item_layout, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                // TODO lwx 当少于最大数量时，最后补一个加号项， 点击加号项可以添加（max-n)个图片
                // TODO lwx 长按图片后可以提示是否删除图片，删除后刷新图片视图
                ImageView addBtnView = holder.itemView.findViewById(R.id.add_image_btn);
                if (position < imgNum) {
                    addBtnView.setVisibility(View.GONE);
                    ImageView imageView = holder.itemView.findViewById(R.id.real_image);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setOnLongClickListener(v -> onLongClickImg(position));
                    if (position < mGoodDetailInfo.getImgs().size()) {
                        String url = mGoodDetailInfo.getImgs().get(position);
                        Picasso.get().load(url)
                                .resize(450,600)
                                .centerInside()
                                .into(imageView);
                    } else {
                        imageView.setImageURI(mGoodDetailInfo.getLocaImgs().get(position - mGoodDetailInfo.getImgs().size()));
                    }
                } else if (position == imgNum) {
                    addBtnView.setOnClickListener(v -> {
                        Log.d(TAG, "add image btn click");
                        openAndSelectPhoto();
                    });
                } else {
                    Log.e(TAG, "bind view holder position is over MAX: " + position);
                }
            }

            @Override
            public int getItemCount() {
                return imgNum < MAX_IMAGE_NUM ? imgNum + 1 : MAX_IMAGE_NUM;

            }
        };
    }

    private int getCurrentImgNum() {
        int uploadedNum = mGoodDetailInfo.getImgs().size();
        int localNum = mGoodDetailInfo.getLocaImgs() != null ? mGoodDetailInfo.getLocaImgs().size() : 0;
        return uploadedNum + localNum;
    }

    private void openAndSelectPhoto() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 检查是否为相册选择请求
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // 获取选择的图片的 Uri
            Uri selectedImageUri = data.getData();
            if (mGoodDetailInfo.getLocaImgs() == null) {
                mGoodDetailInfo.setLocaImgs(new ArrayList<Uri>());
            }
            mGoodDetailInfo.getLocaImgs().add(selectedImageUri);
            refreshImages();
        }
    }


    private boolean onLongClickImg(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage("确定要删除该图片吗？")
                .setPositiveButton("确认", (dialog, which) -> {
                    removeImgByPosition(position);
                    refreshImages();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                });
        // 创建并显示弹窗
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    private void removeImgByPosition(int position) {
        if (position >= mGoodDetailInfo.getImgs().size()) {
            mGoodDetailInfo.getLocaImgs().remove(position - mGoodDetailInfo.getImgs().size());
        } else {
            mToDeleteImgList.add(mGoodDetailInfo.getImgs().get(position));
            mGoodDetailInfo.getImgs().remove(position);
        }
    }

    private void onConfirmBtnClick() {
        Log.d(TAG, "onConfirmBtnClick");
        if (checkInfoCompleted()) {
            showLoading();
            List<String> localImgList = mGoodDetailInfo.getLocaImgs().stream().map(uri -> CommonUtil.getRealPathFromUri(uri, getContentResolver())).collect(Collectors.toList());
            if (mGoodDetailInfo.getLocaImgs().size() != 0) {
                uploadLocalImages(localImgList);
            } else {
                uploadInfo();
            }
        }
    }

    private void uploadLocalImages(List<String> localImages) {
        boolean b = checkReadExternalStoragePermission(getApplicationContext());
        Toast.makeText(this, "是否有权限？ ：" + b, Toast.LENGTH_LONG).show();


        UploadManager uploadManager = new UploadManager();
        for (int i =0; i< localImages.size(); i++) {
            String filePath = localImages.get(i);
            String desFilePath = getCroppedImageFile(filePath);
            try {
                String imgName = CommonUtil.md5("good" + mGoodDetailInfo.getId() + "_" + System.currentTimeMillis(), mGoodDetailInfo.getId() + "errorEncode");
                String imgUrl = CDN_URL + imgName;
                Log.d(TAG, "local img path: " + filePath);

                uploadManager.put(desFilePath, imgName, mUploadToken, (key, info, response) -> {
                    Log.d(TAG, "key:" + key + "  info  +" + info.statusCode + "  " + response);
                    if (info.statusCode == RequestSuccess) {
                        mToAddImgList.add(imgUrl);
                        if (mToAddImgList.size() == localImages.size()) {
                            // 等最后一个图片文件上传成功，上传商品信息
                            uploadInfo();
                        }
                    } else {
                        if (mToAddImgList.size() == localImages.size()) {
                            dismissLoading();
                        }
                        Toast.makeText(mActivity, "上传出错了， 错误码为：" + info.statusCode, Toast.LENGTH_LONG).show();
                    }

                }, null);
            } catch (Exception e) {
                Log.e(TAG, "error", e);
                dismissLoading();
                e.printStackTrace();
            }

        }
    }

    private void uploadInfo() {
        // 至少有一张照片的机制可以保证这里取首张的逻辑
        String firstImg = mGoodDetailInfo.getImgs().size() != 0 ? mGoodDetailInfo.getImgs().get(0) : mToAddImgList.get(0);
        mGoodDetailInfo.setImg(firstImg);
        mGoodDetailInfo.setImgs(mToAddImgList);
        if (mUpdateDataDisposable != null && !mUpdateDataDisposable.isDisposed()) {
            mUpdateDataDisposable.dispose();
        }
        UpdateInfoRequest request = new UpdateInfoRequest();
        request.goodDetailInfo = mGoodDetailInfo;
        request.toDeleteImgList = mToDeleteImgList;
        mUpdateDataDisposable = ApiServiceManager.getInstance().getAPI().updateGoodInfo(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( response -> {
                    if (response.result == RESPONSE_SUCCESS) {
                        dismissLoading();
                        mActivity.finish();
                    } else {
                        dismissLoading();
                        Log.e(TAG, "update data response error" + response.result);
                    }
                });
    }

    private boolean checkInfoCompleted() {
        if (mGoodDetailInfo.getImgs().size() == 0 && mGoodDetailInfo.getLocaImgs().size() == 0) {
            Toast.makeText(this, "需要至少有一张照片", Toast.LENGTH_LONG).show();
            return false;
        }
        boolean isPriceInputValid = checkPriceValid();
        if(!CommonUtil.isEditTextEmpty(mNameEdit.getText())
                && !CommonUtil.isEditTextEmpty(mDesEdit.getText())
                && !CommonUtil.isEditTextEmpty(mTagEdit.getText())
                && !CommonUtil.isEditTextEmpty(mNumEdit.getText())
                && CommonUtil.safeParseInt(mNumEdit.getText().toString(), 0) != 0
                && mTypeEdit.getSelectedItemPosition() != 0) {
            List<Uri> localImgs = mGoodDetailInfo.getLocaImgs();

            mGoodDetailInfo = new GoodDetailInfo(
                    mGoodDetailInfo.getId(),
                    mNameEdit.getText().toString(),
                    mDesEdit.getText().toString(),
                    mGoodDetailInfo.getImg(),
                    mGoodDetailInfo.getImgs(),
                    mTypeEdit.getSelectedItemPosition(),
                    mTagEdit.getText().toString(),
                    Integer.parseInt(mNumEdit.getText().toString()),
                    isPriceInputValid ? Float.parseFloat(mPriceEdit.getText().toString()) : -1f);
            mGoodDetailInfo.setLocaImgs(localImgs);
            return true;
        } else {
            Toast.makeText(this, "有信息未完善，请继续填写", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean checkPriceValid() {
        boolean isPriceInputValid = false;
        if (mIsCheckedShowPrice) {
            try {
                isPriceInputValid = Float.parseFloat(mPriceEdit.getText().toString()) != 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isPriceInputValid;
    }


    private String getCroppedImageFile(String originPath) {
        try {
            String desPath = FileUtil.getOutputFile() + "/cache.jpg";
            FileUtil.copyFile(originPath, desPath);
            // 将文件转换为 Bitmap
            Bitmap originalBitmap = BitmapFactory.decodeFile(desPath);
            if (originalBitmap != null) {
                // 计算目标宽度和高度
                int targetWidth = 450;
                int targetHeight = 600;

                // 计算缩放比例
                float scaleWidth = ((float) targetWidth) / originalBitmap.getWidth();
                float scaleHeight = ((float) targetHeight) / originalBitmap.getHeight();

                // 创建一个缩放的 Matrix
                android.graphics.Matrix matrix = new android.graphics.Matrix();
                matrix.postScale(scaleWidth, scaleHeight);

                // 根据 Matrix 对 Bitmap 进行缩放
                Bitmap scaledBitmap = Bitmap.createBitmap(originalBitmap, 0, 0,
                        originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

                Bitmap rotatedBitmap = BitmapUtil.rotateBitmap(scaledBitmap, originPath);

                // 将裁剪后的 Bitmap 保存到文件
                saveBitmapToFile(rotatedBitmap, desPath);

                // 释放资源
                originalBitmap.recycle();
                scaledBitmap.recycle();
            }
            return desPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void showLoading() {
        mLoadingDialog = new ProgressDialog(mActivity);
        mLoadingDialog.setMessage("上传中...");
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.show();
    }

    private void dismissLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

}