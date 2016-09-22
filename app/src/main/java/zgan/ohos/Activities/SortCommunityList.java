package zgan.ohos.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import zgan.ohos.ConstomControls.SortView.CharacterParser;
import zgan.ohos.ConstomControls.SortView.ClearEditText;
import zgan.ohos.ConstomControls.SortView.PinyinComparator;
import zgan.ohos.ConstomControls.SortView.SideBar;
import zgan.ohos.ConstomControls.SortView.SortAdapter;
import zgan.ohos.ConstomControls.SortView.SortModel;
import zgan.ohos.Dals.NewUserCommDal;
import zgan.ohos.Models.NewUserComm;
import zgan.ohos.R;
import zgan.ohos.services.login.ZganLoginService;
import zgan.ohos.utils.Frame;
import zgan.ohos.utils.generalhelper;
import zgan.ohos.utils.resultCodes;

public class SortCommunityList extends myBaseActivity implements Serializable {
    private ListView sortListView;
    private SideBar sideBar;
    private TextView dialog;
    private SortAdapter adapter;
    private ClearEditText mClearEditText;

    private CharacterParser characterParser;
    private List<SortModel> SourceDateList;
    private List<NewUserComm> list;

    private PinyinComparator pinyinComparator;

    private String Phone;

    @Override
    protected void initView() {
        setContentView(R.layout.sortlistview);
        Phone = getIntent().getStringExtra("username");
        initViews();
        loadData();
    }

    private void loadData() {
        //list = new String[]{"金易伯爵世家", "尚阳康城", "云满庭C"};
        ZganLoginService.toGetServerData(9, 0, Phone, handler);
    }

    private void bindData() {
        //数据填充
        SourceDateList = filledData(list);
        Collections.sort(SourceDateList, pinyinComparator);
        adapter = new SortAdapter(this, SourceDateList);
        sortListView.setAdapter(adapter);
    }

    @Override
    public void ViewClick(View v) {
    }

    private void initViews() {
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialog);

        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    sortListView.setSelection(position);
                }

            }
        });

        sortListView = (ListView) findViewById(R.id.country_lvcountry);
        sortListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Toast.makeText(getApplication(), ((SortModel)adapter.getItem(position)).getName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                Bundle bundle=new Bundle();
                bundle.putSerializable("UserComm",list.get(SourceDateList.get(position).getSourceIndex()));
                intent.putExtras(bundle);
                setResult(resultCodes.BINDDEVICE, intent);
                finish();
            }
        });
        mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);

        mClearEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    private List<SortModel> filledData(List<NewUserComm> date) {
        List<SortModel> mSortList = new ArrayList<SortModel>();

        for (int i = 0; i < date.size(); i++) {
            SortModel sortModel = new SortModel();
            sortModel.setSourceIndex(i);
            sortModel.setName(date.get(i).getCommName());
            String pinyin = characterParser.getSelling(date.get(i).getCommName());
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    private void filterData(String filterStr) {
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = SourceDateList;
        } else {
            filterDateList.clear();
            for (SortModel sortModel : SourceDateList) {
                String name = sortModel.getName();
                if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
                    filterDateList.add(sortModel);
                }
            }
        }

        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == resultCodes.COMMSELECTED) {
            if (data != null && data.hasExtra("commid")) {
                setResult(resultCodes.COMMSELECTED, data);
                finish();
            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    generalhelper.ToastShow(SortCommunityList.this,"获取数据失败，请重试~");
                    break;
                case 1:
                    Frame f = (Frame) msg.obj;
                    //String result = generalhelper.getSocketeStringResult(f.strData);
                    String[] results = f.strData.split("\t");
                    System.out.print(f.strData);
                    if (f.subCmd == 9) {
                        if (results[0].equals("0")) {
                            if (results[3].length() > 0) {
                                list = new NewUserCommDal().getCommListfromString(results[3]);
                                if (list != null)
                                    bindData();
                            }
                        }
                    }
                    break;
            }
        }
    };
}
