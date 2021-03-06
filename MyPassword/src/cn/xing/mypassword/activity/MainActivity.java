package cn.xing.mypassword.activity;

import java.util.HashMap;

import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import cn.xing.mypassword.R;
import cn.xing.mypassword.app.BaseActivity;
import cn.xing.mypassword.dialog.ExportDialog;
import cn.xing.mypassword.dialog.ImportDialog;
import cn.xing.mypassword.model.SettingKey;
import cn.xing.mypassword.service.Mainbinder;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

/**
 * 主界面
 * 
 * @author zengdexing
 * 
 */
public class MainActivity extends BaseActivity
{
	/** 数据源 */
	private Mainbinder mainbinder;
	private long lastBackKeyTime;

	private ServiceConnection serviceConnection = new ServiceConnection()
	{
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			mainbinder = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			mainbinder = (Mainbinder) service;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Intent intent = new Intent("cn.xing.mypassword");
		this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

		// 友盟自动升级
		UmengUpdateAgent.update(this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unbindService(serviceConnection);
	}

	private boolean isExistSDCard()
	{
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			return true;
		}
		else
			return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		switch (id)
		{
			case R.id.action_add_password:
				startActivity(new Intent(this, EditPasswordActivity.class));
				break;

			case R.id.action_import:
				// 密码导入
				if (mainbinder == null)
					break;
				ImportDialog importDialog = new ImportDialog(getActivity(), mainbinder);
				importDialog.show();
				break;

			case R.id.action_export:
				// 密码导出
				if (mainbinder == null)
					break;
				if (!isExistSDCard())
				{
					showToast(R.string.export_no_sdcard);
					break;
				}
				ExportDialog exportDialog = new ExportDialog(this, mainbinder);
				exportDialog.show();
				break;

			case R.id.action_set_lock_pattern:
				// 软件锁
				startActivity(new Intent(this, SetLockpatternActivity.class));
				break;
			case R.id.action_set_effect:
				// 列表特效
				onEffectClick();
				break;
			case R.id.action_about:
				// 关于
				onAboutClick();
				break;
			case R.id.action_feedback:
				// 意见反馈
				onFeedbackClick();
				break;
			case R.id.action_exit:
				// 退出
				finish();
				break;

			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 意见反馈
	 */
	private void onFeedbackClick()
	{
		startActivity(new Intent(this, FeedbackActivity.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				long delay = Math.abs(System.currentTimeMillis() - lastBackKeyTime);
				if (delay > 4000)
				{
					// 双击退出程序
					showToast(R.string.toast_key_back);
					lastBackKeyTime = System.currentTimeMillis();
					return true;
				}
				break;

			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void onEffectClick()
	{
		if (getSetting(SettingKey.JAZZY_EFFECT_INTRODUCTION, "false").equals("false"))
		{
			putSetting(SettingKey.JAZZY_EFFECT_INTRODUCTION, "true");
			Builder builder = new Builder(this);
			builder.setMessage(R.string.action_jazzy_effect_introduction);
			builder.setNeutralButton(R.string.i_known, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					onEffectClick();
				}
			});
			builder.show();
		}
		else
		{
			Builder builder = new Builder(this);
			builder.setTitle(R.string.action_jazzy_effect);

			final String[] effectArray = getResources().getStringArray(R.array.jazzy_effects);
			builder.setItems(effectArray, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					getActivity().putSetting(SettingKey.JAZZY_EFFECT, which + "");
					onEventEffect(effectArray[which]);
				}
			});
			builder.show();
		}
	}

	/**
	 * 友盟的事件统计“effect”
	 * 
	 * @param effect
	 */
	private void onEventEffect(String effect)
	{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("effect", effect);
		MobclickAgent.onEvent(getActivity(), "effect", map);
	}

	/**
	 * 关于对话框
	 */
	private void onAboutClick()
	{
		Builder builder = new Builder(getActivity());
		builder.setTitle(R.string.action_about_us);
		builder.setNeutralButton(R.string.common_sure, null);
		String message = getString(R.string.drawer_about_detail, getMyApplication().getVersionName());
		TextView textView = new TextView(getActivity());
		textView.setGravity(Gravity.CENTER);
		textView.setText(message);
		textView.setTextSize(18);
		builder.setView(textView);
		builder.show();
	}
}
