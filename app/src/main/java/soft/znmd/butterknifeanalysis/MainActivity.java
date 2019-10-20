package soft.znmd.butterknifeanalysis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import soft.znmd.annotation.BindView;
import soft.znmd.annotation.OnClick;
import soft.znmd.bind.MyButterKnifeBind;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    
    @BindView(R.id.button)
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyButterKnifeBind.bind(this);
    }

    @OnClick(R.id.button)
    void doClick() {
        Log.d(TAG, "doClick: mButton");
    }

    @OnClick(R.id.button_view)
    void doButtonClick() {
        Log.d(TAG, "doButtonClick: ");
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SecondActivity.class);
        startActivity(intent);
    }
}
