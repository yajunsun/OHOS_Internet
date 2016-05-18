package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import zgan.ohos.R;

public class RegisterStep1 extends myBaseActivity {
    Toolbar toolbar;
    String []communityNames=new String []{"金易▪伯爵世家"};//,"测试小区"
    int[]communityIds=new int[]{1};//,4
    ListView lst_community;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void initView() {
        setContentView(R.layout.lo_register_step1);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        lst_community=(ListView)findViewById(R.id.lst_community);
        lst_community.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,android.R.id.text1,communityNames));
        lst_community.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(RegisterStep1.this,Register.class);
                intent.putExtra("communityid",communityIds[position]);
                startActivityWithAnim(intent);
                //finish();
            }
        });
    }

    @Override
    public void ViewClick(View v) {
    }
}
