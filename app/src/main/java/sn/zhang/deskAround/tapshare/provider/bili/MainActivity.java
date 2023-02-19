package sn.zhang.deskAround.tapshare.provider.bili;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBili();
        findViewById(R.id.restart_button).setOnClickListener(view -> startBili());
    }

    private void startBili() {
        try {
            Runtime.getRuntime().exec("su -c am start -S tv.danmaku.bili/.MainActivityV2");
        } catch (IOException e) {
            new AlertDialog.Builder(this).setMessage(e.toString()).show();
            //throw new RuntimeException(e);
        }
    }
}
