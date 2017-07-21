package com.kupaworld.androidtv.activity;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kupaworld.androidtv.R;
import com.kupaworld.androidtv.adapter.WenjianAdapter;
import com.kupaworld.androidtv.entity.Music;
import com.kupaworld.androidtv.entity.Video;
import com.kupaworld.androidtv.util.AbstructProvider;
import com.kupaworld.androidtv.util.VideoProvider;
import com.kupaworld.androidtv.view.ListDialog;
import com.kupaworld.androidtv.view.TipDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WenjianActivity extends ListActivity implements View.OnFocusChangeListener {
    private static final String ROOT_PATH = "/";
    //存储文件名称
    private ArrayList<String> names = null;
    //存储文件路径
    private ArrayList<String> paths = null;
    private TextView mTvAll, mTvNoFile;
    private LinearLayout mTvVideo, mTvMusic, mTvPicture;

    private View[] views;
    private int CURRENT_FOCUS = 0;
    private boolean PRESS_LEFT;

    private List<Video> listVideos;
    private List<String> vname = new ArrayList<>();
    private List<String> vpath = new ArrayList<>();

    private List<Music> listMusic;
    private List<String> mname = new ArrayList<>();
    private List<String> mpath = new ArrayList<>();

    private List<Music> listImage;
    private List<String> iname = new ArrayList<>();
    private List<String> ipath = new ArrayList<>();

    private WenjianAdapter adapter;

    private String location = "所有文件";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kupatv_wenjian);
        getViews();
        views = new View[]{mTvAll, mTvVideo, mTvMusic, mTvPicture};
        //显示文件列表
        showFileDir(ROOT_PATH);
        initMedias();
        registerReceiver();
    }

    private void initMedias() {
        //显示视频
        AbstructProvider provider = new VideoProvider(this);
        if (listVideos != null) {
            listVideos.clear();
            vname.clear();
            vpath.clear();
        }
        listVideos = provider.getList();
        int size = listVideos.size();
        for (int i = 0; i < size; i++) {
            vname.set(i, listVideos.get(i).getDisplayName());
            vpath.set(i, listVideos.get(i).getPath());
        }

        if (listMusic != null) {
            listMusic.clear();
            mname.clear();
            mpath.clear();
        }
        listMusic = VideoProvider.getAllMediaList(this);
        size = listMusic.size();
        for (int i = 0; i < size; i++) {
            mname.add(i, listMusic.get(i).display_name);
            mpath.add(i, listMusic.get(i).path);
        }

        if (iname.size() > 0) {
            listImage.clear();
            iname.clear();
            ipath.clear();
        }
        listImage = VideoProvider.getAllImageList(this);
        size = listImage.size();
        for (int i = 0; i < size; i++) {
            iname.add(i, listImage.get(i).display_name);
            ipath.add(i, listImage.get(i).path);
        }
    }

    private void showFileDir(String path) {
        names = new ArrayList<>();
        paths = new ArrayList<>();
        File file = new File(path);

        File[] files = file.listFiles();

        //如果当前目录不是根目录
        if (!ROOT_PATH.equals(path)) {
            names.add("@1");
            paths.add(ROOT_PATH);

            names.add("@2");
            paths.add(file.getParent());
        }
        //添加所有文件
        for (File f : files) {
            names.add(f.getName());
            paths.add(f.getPath());
        }
        adapter = new WenjianAdapter(this, names, paths);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String path = null;
        switch (CURRENT_FOCUS) {
            case 0:
                path = paths.get(position);
                break;

            case 1:
                path = vpath.get(position);
                break;

            case 2:
                path = mpath.get(position);
                break;

            case 3:
                path = ipath.get(position);
                break;
        }

        mTvAll.setText(location + " > " + path);
        File file = new File(path);
        // 文件存在并可读
        if (file.exists() && file.canRead()) {
            if (file.isDirectory()) {
                //显示子目录及文件
                showFileDir(path);
            } else {
                //处理文件
                fileHandle(file);
            }
        }
        //没有权限
        else {
            final TipDialog dialog = new TipDialog(this, R.style.MyDialog);
            dialog.setTitle("提示");
            dialog.setMessage("没有操作权限！");
            dialog.setYesClickListener(new TipDialog.OnYesClickListener() {
                @Override
                public void onYesClick() {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
        super.onListItemClick(l, v, position, id);
    }

    //对文件进行增删改
    private void fileHandle(final File file) {
        //选择文件时，弹出增删该操作选项对话框
        String[] menu = {"打开文件", "重命名", "删除文件"};
        final ListDialog dialog = new ListDialog(this, R.style.MyDialog, menu);
        dialog.setOnCancelClickLisnter(new ListDialog.onCancelListener() {
            @Override
            public void onCancelClick() {
                dialog.dismiss();
            }
        });
        dialog.setOnOptionClickListener(new ListDialog.onOptionClickListener() {
                                            @Override
                                            public void onOptionClick(AdapterView<?> parent, View view, int position, long id) {
                                                switch (position) {
                                                    case 0:
                                                        openFile(file);
                                                        break;

                                                    case 1:
                                                        renameFile(file);
                                                        break;

                                                    case 2:
                                                        deleteFile(file);
                                                        break;
                                                }
                                                dialog.dismiss();
                                            }
                                        }
        );
        dialog.show();
    }

    //打开文件
    private void openFile(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);

        String type = getMIMEType(file);
        intent.setDataAndType(Uri.fromFile(file), type);
        startActivity(intent);
    }

    private void updateSysFileData(File file) {
        Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
        sendBroadcast(media);
    }

    private void registerReceiver(){
        receiver = new UpdateSysFileReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        filter.addDataScheme("file");
        registerReceiver(receiver,filter);
    }

    UpdateSysFileReceiver receiver;

    private class UpdateSysFileReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            handler.sendEmptyMessageDelayed(0,1000);
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            notifyListUpdate();
        }
    };

    /**
     * 重命名文件
     *
     * @param file
     */
    private void renameFile(final File file) {
        final TipDialog dialog = new TipDialog(this, R.style.MyDialog);
        dialog.setEdit(true);
        dialog.setTitle("重命名");
        dialog.setNoClickListener(new TipDialog.OnNoClickListener() {
            @Override
            public void onNoClick() {
                dialog.dismiss();
            }
        });
        dialog.setYesClickListener(new TipDialog.OnYesClickListener() {
            @Override
            public void onYesClick() {
                String modifyName = dialog.getEditName();
                final String fpath = file.getParentFile().getPath();
                final File newFile = new File(fpath + "/" + modifyName);
                if (newFile.exists()) {
                    //排除没有修改情况
                    if (!modifyName.equals(file.getName())) {

                        final TipDialog tipDialog = new TipDialog(WenjianActivity.this, R.style.MyDialog);
                        tipDialog.setMessage("文件名已存在，是否覆盖？");
                        tipDialog.setTitle("提示");
                        tipDialog.setNoClickListener(new TipDialog.OnNoClickListener() {
                            @Override
                            public void onNoClick() {
                                tipDialog.dismiss();
                            }
                        });
                        tipDialog.setYesClickListener(new TipDialog.OnYesClickListener() {
                            @Override
                            public void onYesClick() {
                                if (file.renameTo(newFile)) {
                                    displayToast("重命名成功！");
                                    updateSysFileData(newFile);
                                    updateSysFileData(file);
                                } else {
                                    displayToast("重命名失败！");
                                }
                                tipDialog.dismiss();
                            }
                        });
                        tipDialog.show();
                    }
                } else {
                    if (file.renameTo(newFile)) {
                        updateSysFileData(newFile);
                        updateSysFileData(file);
                        displayToast("重命名成功！");
                    } else {
                        displayToast("重命名失败！");
                    }
                }
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.setEditName(file.getName());
    }

    /**
     * 删除文件
     *
     * @param file
     */
    private void deleteFile(final File file) {
        final TipDialog dialog = new TipDialog(this, R.style.MyDialog);
        dialog.setTitle("注意");
        dialog.setMessage("确定要删除此文件吗？");
        dialog.setNoClickListener(new TipDialog.OnNoClickListener() {
            @Override
            public void onNoClick() {
                dialog.dismiss();
            }
        });
        dialog.setYesClickListener(new TipDialog.OnYesClickListener() {
            @Override
            public void onYesClick() {
                if (file.delete()) {
                    //更新文件列表
                    displayToast("删除成功！");
                    updateSysFileData(file);
                } else {
                    displayToast("删除失败！");
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void notifyListUpdate() {
        initMedias();
        switch (CURRENT_FOCUS) {
            case 0:
                adapter = new WenjianAdapter(this, names, paths);
                break;

            case 1:
                adapter = new WenjianAdapter(this, mname, mpath);
                break;

            case 2:
                adapter = new WenjianAdapter(this, iname, ipath);
                break;
        }
        adapter.notifyDataSetChanged();
    }

    //获取文件mimetype
    private String getMIMEType(File file) {
        String type = "";
        String name = file.getName();
        //文件扩展名
        String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        if (end.equals("m4a") || end.equals("mp3") || end.equals("wav")) {
            type = "audio";
        } else if (end.equals("mp4") || end.equals("3gp")) {
            type = "video";
        } else if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp") || end.equals("gif")) {
            type = "image";
        } else {
            //如果无法直接打开，跳出列表由用户选择
            type = "*";
        }
        type += "/*";
        return type;
    }

    private void displayToast(String message) {
        Toast.makeText(WenjianActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                setViewFocus(false);
                PRESS_LEFT = false;
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                PRESS_LEFT = true;
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getViews() {
        mTvAll = (TextView) findViewById(R.id.file_all);
        mTvMusic = (LinearLayout) findViewById(R.id.file_music);
        mTvPicture = (LinearLayout) findViewById(R.id.file_picture);
        mTvVideo = (LinearLayout) findViewById(R.id.file_video);
        mTvNoFile = (TextView) findViewById(R.id.noFileData);

        getListView().setEmptyView(mTvNoFile);

        mTvAll.setOnFocusChangeListener(this);
        mTvMusic.setOnFocusChangeListener(this);
        mTvPicture.setOnFocusChangeListener(this);
        mTvVideo.setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int id = v.getId();
        switch (id) {
            case R.id.file_all:
                mTvAll.setText(location);
                CURRENT_FOCUS = 0;
                adapter = new WenjianAdapter(this, names, paths);
                this.setListAdapter(adapter);
                break;

            case R.id.file_video:
                mTvAll.setText(location + " > 视频");
                CURRENT_FOCUS = 1;
                adapter = new WenjianAdapter(this, vname, vpath);
                this.setListAdapter(adapter);
                break;

            case R.id.file_music:
                mTvAll.setText(location + " > 音乐");
                CURRENT_FOCUS = 2;

                adapter = new WenjianAdapter(this, mname, mpath);
                this.setListAdapter(adapter);
                break;

            case R.id.file_picture:
                mTvAll.setText(location + " > 照片");
                CURRENT_FOCUS = 3;
                adapter = new WenjianAdapter(this, iname, ipath);
                this.setListAdapter(adapter);
                break;
        }
        if (PRESS_LEFT)
            setViewFocus(true);
    }

    private void setViewFocus(boolean isFocus) {
        int len = views.length;
        for (int i = 0; i < len; i++) {
            if (i != CURRENT_FOCUS) {
                views[i].setFocusable(isFocus);
                if (isFocus) {
                    PRESS_LEFT = false;
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        if (receiver!=null)
            unregisterReceiver(receiver);
        super.onDestroy();
    }
}
