package sunxt.testsevenzip;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MainActivity extends AppCompatActivity {

    Button mUnzipStandardApk;
    Button mUnzip7ZipedApk;

    private static final String STANDARD_APK_PATH = Environment.getExternalStorageDirectory() + File.separator + "standard.apk";
    private static final String SEVEN_ZIPED_APK_PATH = Environment.getExternalStorageDirectory() + File.separator + "sevenzip.apk";
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 8;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUnzipStandardApk = (Button) findViewById(R.id.unzip_zip);
        mUnzip7ZipedApk = (Button) findViewById(R.id.unzip_7zip);

        mUnzipStandardApk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long totalTime = 0;
                        for (int i = 0; i < 10; i++) {
                            long time1 = System.currentTimeMillis();
                            unCompress(STANDARD_APK_PATH, STANDARD_APK_PATH + "_unzip");
                            long time2 = System.currentTimeMillis() - time1;
                            Log.e("sunxt", "standard unzip cost " + time2 + " ms");
                            totalTime += time2;
                        }
                        Log.e("sunxt", "standard unzip 10 times, average cost " + (totalTime / 10) + " ms");
                    }
                }).start();

            }
        });

        mUnzip7ZipedApk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long totalTime = 0;
                        for (int i = 0; i < 10; i++) {
                            long time1 = System.currentTimeMillis();
                            unCompress(SEVEN_ZIPED_APK_PATH, SEVEN_ZIPED_APK_PATH + "_unzip");
                            long time2 = System.currentTimeMillis() - time1;
                            Log.e("sunxt", "7ziped unzip cost " + time2 + " ms");
                            totalTime += time2;
                        }
                        Log.e("sunxt", "7ziped unzip 10 times, average cost " + (totalTime / 10) + " ms");
                    }
                }).start();

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
        }
    }

    private void unCompress(String zipPath, String unZipPath) {
        if (TextUtils.isEmpty(zipPath) || TextUtils.isEmpty(unZipPath)) {
            return;
        }

        unZipPath = unZipPath.endsWith(File.separator)? unZipPath : unZipPath + File.separator;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int length;
        ZipFile readZipFile = null;

        try {
            readZipFile = new ZipFile(new File(zipPath));
            Enumeration<? extends ZipEntry> zipEntries = readZipFile.entries();
            ZipEntry zipEntry = null;
            boolean isFirst = true;
            String path = null;

            while (zipEntries.hasMoreElements()) {
                zipEntry = zipEntries.nextElement();
                String name = zipEntry.getName();
                if (isFirst) {
                    int index = name.indexOf(File.separator);
                    if (index >= 0) {
                        path = name.substring(index);
                    } else {
                        path = name;
                    }

                    isFirst = false;
                }

                File outPutFile = new File(unZipPath + name);
                if (zipEntry.isDirectory()) {
                    // 当前条目是目录
                    outPutFile.mkdirs();
                } else {
                    // 当前条目是文件
                    if (!outPutFile.getParentFile().exists()) {
                        outPutFile.getParentFile().mkdirs();
                    }

                    BufferedOutputStream bos = null;
                    InputStream is = null;
                    try {
                        bos = new BufferedOutputStream(new FileOutputStream(outPutFile), DEFAULT_BUFFER_SIZE);
                        is = readZipFile.getInputStream(zipEntry);
                        while ((length = is.read(buffer)) > 0) {
                            bos.write(buffer, 0 , length);
                        }
                        bos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                                is = null;
                            } catch (IOException e){

                            }
                        }
                        if (bos != null) {
                            try {
                                bos.close();
                                bos = null;
                            } catch (IOException e){

                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (readZipFile != null) {
                try {
                    readZipFile.close();
                    readZipFile = null;
                } catch (IOException e) {

                }
            }
        }

    }
}
