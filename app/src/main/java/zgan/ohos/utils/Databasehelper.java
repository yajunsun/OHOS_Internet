package zgan.ohos.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Databasehelper extends SQLiteOpenHelper {

	public final static int VERSION = 1;
	public static Context CONTEXT;;

	public Databasehelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		CONTEXT = context;
		// TODO Auto-generated constructor stub
	}

	public Databasehelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	public Databasehelper(Context context, String name) {
		this(context, name, null, VERSION);
	}

	public Databasehelper(Context context) {
		this(context, "myhealth");
	}

	public Databasehelper() {
		this(CONTEXT, "myhealth");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// db.execSQL(sql)
		Log.v("suntest", "��ݿ��ѽ���");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		assert (newVersion == VERSION);
	}

}
