package com.javabaas.sample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.javabaas.JBCloud;
import com.javabaas.JBFile;
import com.javabaas.JBObject;
import com.javabaas.JBQuery;
import com.javabaas.ResponseEntity;
import com.javabaas.callback.CloudCallback;
import com.javabaas.callback.CountCallback;
import com.javabaas.callback.DeleteCallback;
import com.javabaas.callback.FileUploadCallback;
import com.javabaas.callback.FindCallback;
import com.javabaas.callback.RequestCallback;
import com.javabaas.callback.SaveCallback;
import com.javabaas.exception.JBException;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    boolean isSync = false;
    private EditText amountEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((Switch) findViewById(R.id.switch1)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSync = isChecked;
            }
        });
        amountEt = (EditText) findViewById(R.id.amount);
    }

    public void onSave(View view) {
        final JBObject testC = new JBObject("testC");
        testC.put("testA", "测试A");
        testC.put("testB", "测试B");
        if (isSync) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Message msg = Message.obtain();
                    try {
                        testC.save();
                        msg.obj = "同步保存对象成功 id=" + testC.getId();
                    } catch (JBException e) {
                        e.printStackTrace();
                        msg.obj = "同步保存对象失败";
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        } else
            testC.saveInBackground(new SaveCallback() {
                @Override
                public void done(JBObject object) {
                    showToast(MainActivity.this ,"异步保存对象成功 id=" + testC.getId());
                }

                @Override
                public void error(JBException e) {
                    showToast(MainActivity.this ,"异步保存对象失败");
                    e.printStackTrace();
                }
            });
    }

    public void onDelete(View view) {
        if (isSync) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Message msg = Message.obtain();
                    try {
                        JBObject.deleteById("testC", "");
                        msg.obj = "同步删除成功";
                    } catch (JBException e) {
                        e.printStackTrace();
                        msg.obj = "同步删除失败 "+e.getResponseErrorMsg();
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        } else
            JBObject.deleteByIdInBackground("testC", "4c14152e0fbf4aa08bfbfb0dd329a74d", new DeleteCallback() {
                @Override
                public void done() {
                    showToast(MainActivity.this , "异步删除成功");
                }

                @Override
                public void error(JBException e) {
                    showToast(MainActivity.this , "异步删除失败 "+e.getResponseErrorMsg());
                }
            });
    }

    public void onQuery(View view) {
        final JBQuery jbQuery = JBQuery.getInstance("Device");
        jbQuery.whereEqualTo("_id" , "1231254");
        jbQuery.include("user");
        if (isSync) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    List<JBObject> list = null;
                    Message msg = Message.obtain();
                    try {
                        list = jbQuery.find();
                        msg.obj = "同步查询成功,共 " + list.size() + "个结果";
                    } catch (JBException e) {
                        e.printStackTrace();
                        msg.obj = "同步查询失败";
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        } else
            jbQuery.findInBackground(new FindCallback<JBObject>() {
                @Override
                public void done(List<JBObject> result) {
                    Toast.makeText(MainActivity.this, "异步查询成功共 " + result.size() + "个结果", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void error(JBException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "异步查询失败", Toast.LENGTH_SHORT).show();
                }
            });


    }

    public void onCount(View view) {
        final JBQuery jbQuery = JBQuery.getInstance("testC");
        jbQuery.whereEqualTo("testA", "测试A");
        if (isSync) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Message msg = Message.obtain();
                    try {
                        int count = jbQuery.count();
                        msg.obj = "同步查询成功,共 " + count + "个";
                    } catch (JBException e) {
                        e.printStackTrace();
                        msg.obj = "同步查询失败";
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        } else {
            jbQuery.countInBackground(new CountCallback() {
                @Override
                public void done(int count) {
                    Toast.makeText(MainActivity.this, "异步查询成功,共 " + count + "个", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void error(JBException e) {
                    Toast.makeText(MainActivity.this, "异步查询失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void onCloud(View view) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("episodeId", "1a23a933c4d24b04856046bb21b4af93");
        params.put("score", 8);
        JBCloud.callFunctionInBackground("addEpisodeScore", params, new CloudCallback() {
            @Override
            public void done(ResponseEntity responseEntity) {
                showToast(MainActivity.this , "调用成功 " + responseEntity.getMessage());
            }

            @Override
            public void error(JBException e, ResponseEntity responseEntity) {
                showToast(MainActivity.this , "调用失败 " + e.getMessage());
            }
        });
    }


    public void onUpload(View view) {
        showFileChooser();
    }


    public void onDeleteByQuery(View view) {
        final JBQuery jbQuery = JBQuery.getInstance("testC");
        jbQuery.whereEqualTo("testA", "测试A");
        if (isSync) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Message msg = Message.obtain();
                    try {
                        jbQuery.deleteAll();
                        msg.obj = "同步删除成功";
                    } catch (JBException e) {
                        e.printStackTrace();
                        msg.obj = "同步删除失败";
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        } else {
            jbQuery.deleteAllInBackground(new DeleteCallback() {
                @Override
                public void done() {
                    Toast.makeText(MainActivity.this, "异步删除成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void error(JBException e) {
                    Toast.makeText(MainActivity.this, "异步删除失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    public void onIncrement(View view) {
        final JBObject testC = JBObject.createWithoutData("testC", "928ca972d4e04912ab9ef6f1de214a02");
        String s = amountEt.getText().toString();
        final Integer amount ;
        if (TextUtils.isEmpty(s))
            amount = 1;
        else
            amount = Integer.parseInt(s);
        if (isSync){
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    Message msg = Message.obtain();
                    try {
                        testC.incrementKey("testInt" , amount);
                        msg.obj = "同步自增"+amount+"成功";
                    } catch (JBException e) {
                        e.printStackTrace();
                        msg.obj = "同步自增"+amount+"失败";
                    }
                    handler.sendMessage(msg);
                }
            }.start();
        }else {
            testC.incrementKeyInBackground("testInt", amount, new RequestCallback() {
                @Override
                public void done() {
                    showToast(MainActivity.this , "异步自增"+amount+"成功");
                }

                @Override
                public void error(JBException e) {
                    showToast(MainActivity.this ,"异步自增"+amount+"失败");
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
            return false;
        }
    });

    public static Toast toast = null;

    public static void showToast(Context context ,String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }


    public void onSignUp(View view) {
        startActivity(new Intent(this , UserActivity.class));
    }

    public void onRegexQuery(View view) {
        final JBQuery jbQuery = JBQuery.getInstance("testC");
        jbQuery.whereMatches("testA" , "^z");
        //jbQuery.whereStartsWith("testA" , "张");
        //jbQuery.whereEndsWith("testA" , "四");
        //jbQuery.whereContains("testA" , "Z");
        jbQuery.findInBackground(new FindCallback<JBObject>() {
            @Override
            public void done(List<JBObject> result) {
                for (JBObject jbObject : result) {
                    System.out.println(((String) jbObject.get("testA")));
                }
            }

            @Override
            public void error(JBException e) {
                System.out.println(e.getResponseErrorCode());
            }
        });
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult( Intent.createChooser(intent, "Select a File to Upload"), 100);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",  Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
        switch (requestCode) {
            case 100:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = getPath(this, uri);
                    if (TextUtils.isEmpty(path)){
                        showToast(this , "文件错误");
                        return;
                    }
                    final File file = new File(path);

                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.show();
                    progressDialog.setMax((int) file.length());
                    new JBFile(file).saveInBackground(new FileUploadCallback() {
                        @Override
                        public void done(JBFile jbFile) {
                            showToast(MainActivity.this , "上传成功 "+jbFile.getId());
                            progressDialog.dismiss();
                        }

                        @Override
                        public void error(JBException e) {
                            showToast(MainActivity.this , "上传失败");
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onProgress(double percent) {
                            progressDialog.setProgress((int) (percent * file.length()));
                        }
                    });
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getPath(Context context, Uri uri) {

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection,null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }

        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
}
