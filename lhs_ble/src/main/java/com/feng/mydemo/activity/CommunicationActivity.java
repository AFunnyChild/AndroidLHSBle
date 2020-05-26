package com.feng.mydemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.feng.mydemo.R;
import com.feng.mydemo.adapter.ListviewAdapter;
import com.feng.mydemo.bean.MsgInfo;


/**
 * @author 刘松汉
 * @time 2016/12/20  14:59
 * @desc ${TODD}
 */
public class CommunicationActivity extends AppCompatActivity implements View.OnClickListener {

    private ListviewAdapter adapter = null;

    private ListView listview;
    private EditText et_meg;
    private Button btn_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);
        initView();

        adapter = new ListviewAdapter(this);
        listview.setAdapter(adapter);

    }
    private void initView() {
        listview = (ListView) findViewById(R.id.listview);
        et_meg = (EditText) findViewById(R.id.et_meg);
        btn_right = (Button) findViewById(R.id.btn_right);
        btn_right.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        String msg = et_meg.getText().toString().trim();

                adapter.addDataToAdapter(new MsgInfo(null, msg));
                adapter.notifyDataSetChanged();

        listview.smoothScrollToPosition(listview.getCount() - 1);
        et_meg.setText("");

    }

}
