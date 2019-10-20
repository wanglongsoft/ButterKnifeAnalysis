package soft.znmd.butterknifeanalysis;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import soft.znmd.annotation.OnClick;
import soft.znmd.bind.MyButterKnifeBind;

public class SecondActivity extends AppCompatActivity {

    private static final String TAG = "SecondActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        MyButterKnifeBind.bind(this);
    }

    @OnClick(R.id.button2)
    public void testButtonClick() {
        Log.d(TAG, "testButtonClick: ");
    }
}
