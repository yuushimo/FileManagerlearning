package com.example.filemanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends ListActivity {
    private static final String ROOT_PATH = "/";
    //文件名称
    private ArrayList<String> mFileName = null;
    //文件路径
    private ArrayList<String> mFilePath = null;
    //dialog
    private View view;
    private EditText editText;

    @Override
    //显示文件
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showFileDir(ROOT_PATH);
    }


    private void showFileDir(String path) {
        mFileName = new ArrayList<String>();
        mFilePath = new ArrayList<String>();
        File file = new File(path);

        File[] files = file.listFiles();
        //如果不是根目录
        if (!ROOT_PATH.equals(path)) {
            mFileName.add("@1");
            mFilePath.add(ROOT_PATH);
            mFileName.add("@2");
            mFilePath.add(file.getParent());
        }
        //添加文件
        for (File f : files) {
            mFileName.add(f.getName());
            mFilePath.add(f.getPath());
        }
        this.setListAdapter(new Adapter(this, mFileName, mFilePath));
    }

    //点击
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String path = mFilePath.get(position);
        File file = new File(path);
        // 文件存在、可读
        if (file.exists() && file.canRead()) {
            if (file.isDirectory()) {
                //显示子目录文件
                showFileDir(path);
            } else {
                //文件处理
                fileHandle(file);
            }
        }
        super.onListItemClick(l, v, position, id);
    }

    //修改文件
    private void fileHandle(final File file) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 打开
                if (which == 0) {
                    openFile(file);
                }
                //重命名
                else if (which == 1) {
                    LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                    view = factory.inflate(R.layout.dialog, null);
                    editText = (EditText) view.findViewById(R.id.editText);
                    editText.setText(file.getName());

                    DialogInterface.OnClickListener listener2 = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String modifyName = editText.getText().toString();
                            final String fpath = file.getParentFile().getPath();
                            final File newFile = new File(fpath + "/" + modifyName);
                            if (newFile.exists()) {
                                //存在重名
                                if (!modifyName.equals(file.getName())) {
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("注意!")
                                            .setMessage("文件名已存在，是否覆盖？")
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (file.renameTo(newFile)) {
                                                        showFileDir(fpath);
                                                        displayToast("重命名成功");
                                                    } else {
                                                        displayToast("重命名失败");
                                                    }
                                                }
                                            })
                                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            })
                                            .show();
                                }
                            } else {
                                if (file.renameTo(newFile)) {
                                    showFileDir(fpath);
                                    displayToast("重命名成功");
                                } else {
                                    displayToast("重命名失败");
                                }
                            }
                        }
                    };
                    AlertDialog renameDialog = new AlertDialog.Builder(MainActivity.this).create();
                    renameDialog.setView(view);
                    renameDialog.setButton("确定", listener2);
                    renameDialog.setButton2("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    renameDialog.show();
                }
                //删除
                else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("CAUTION")
                            .setMessage("确定要删除此文件吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (file.delete()) {
                                        //更新文件列表
                                        showFileDir(file.getParent());
                                        displayToast("删除成功");
                                    } else {
                                        displayToast("删除失败");
                                    }
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }
            }
        };
        //选择文件弹出对话框
        String[] menu = {"打开文件", "重命名", "删除文件"};
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("选择操作")
                .setItems(menu, listener)
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    //打开
    private void openFile(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        String type = getMIMEType(file);
        intent.setDataAndType(Uri.fromFile(file), type);
        startActivity(intent);
    }

    //mimetype识别文件类型？
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
            //不能直接打开的情况
            type = "*";
        }
        type += "/*";
        return type;
    }

    private void displayToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }
    //创建选项菜单
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST + 1, 7, "新建")
                .setIcon(android.R.drawable.ic_menu_send);
        menu.add(Menu.NONE, Menu.FIRST + 2, 3, "取消")
                .setIcon(android.R.drawable.ic_menu_edit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST + 1:{
                final EditText editText = new EditText(MainActivity.this);
                new AlertDialog.Builder(MainActivity.this).setTitle("输入文件名").setView(editText).setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                String mFileName = editText.getText().toString();
                                String IMAGES_PATH = Environment.getExternalStorageDirectory() + "/" + mFileName + "/";       //获取根目录
                                //String IMAGES_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + mFileName + "/";
                                createMkdir(IMAGES_PATH);
                            }
                        }).setNegativeButton("取消", null).show();
            }
            break;
            case Menu.FIRST + 2:
                Toast.makeText(this, "取消", Toast.LENGTH_LONG).show();
                break;
        }
        return false;
    }
    //创建file
        public static void createMkdir(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }
    //按下事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}