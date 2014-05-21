/* 
 * Copyright (C) 2014 Peter Cai
 *
 * This file is part of BlackLight
 *
 * BlackLight is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlackLight is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlackLight.  If not, see <http://www.gnu.org/licenses/>.
 */

package us.shandian.blacklight.ui.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.TextView;
import android.os.Bundle;
import android.util.Log;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.ui.main.MainActivity;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Utility;
import static us.shandian.blacklight.BuildConfig.DEBUG;

/* BlackMagic Login Activity */
public class LoginActivity extends Activity
{
	private static final String TAG = LoginActivity.class.getSimpleName();
	
	private TextView mAppId;
	private TextView mAppSecret;
	private TextView mUsername;
	private TextView mPasswd;
	
	private MenuItem mMenuItem;
	
	private LoginApiCache mLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.login);
		
		// Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Create login instance
		mLogin = new LoginApiCache(this);
		
		// Get views
		mAppId = (TextView) findViewById(R.id.app_id);
		mAppSecret = (TextView) findViewById(R.id.app_secret);
		mUsername = (TextView) findViewById(R.id.username);
		mPasswd = (TextView) findViewById(R.id.passwd);
		
		if (mLogin.getAppId() != null && mLogin.getAppSecret() != null) {
			mAppId.setText(mLogin.getAppId());
			mAppSecret.setText(mLogin.getAppSecret());
		} else {
			mAppId.setText("211160679");
			mAppSecret.setText("63b64d531b98c2dbff2443816f274dd3");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		mMenuItem = menu.add(R.string.login);
		mMenuItem.setShowAsAction(1);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item == mMenuItem) {
			new LoginTask().execute(new String[]{
				mAppId.getText().toString(),
				mAppSecret.getText().toString(),
				mUsername.getText().toString(),
				mPasswd.getText().toString()
			});
			return true;
		} else if (item.getItemId() == android.R.id.home) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	private class LoginTask extends AsyncTask<String, Void, Void>
	{
		private ProgressDialog progDialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progDialog = new ProgressDialog(LoginActivity.this);
			progDialog.setMessage(getResources().getString(R.string.plz_wait));
			progDialog.setCancelable(false);
			progDialog.show();
		}
		
		@Override
		protected Void doInBackground(String[] params) {
			if (DEBUG) {
				Log.d(TAG, "doInBackground...");
			}
			mLogin.login(params[0], params[1], params[2], params[3]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progDialog.dismiss();
			
			if (mLogin.getAccessToken() != null) {
				if (DEBUG) {
					Log.d(TAG, "Access Token:" + mLogin.getAccessToken());
					Log.d(TAG, "Expires in:" + mLogin.getExpireDate());
				}
				mLogin.cache();
				BaseApi.setAccessToken(mLogin.getAccessToken());
				
				// Expire date
				String msg = String.format(getResources().getString(R.string.expires_in), Utility.expireTimeInDays(mLogin.getExpireDate()));
				new AlertDialog.Builder(LoginActivity.this)
								.setMessage(msg)
								.setCancelable(false)
								.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										dialog.dismiss();
										Intent i = new Intent();
										i.setAction(Intent.ACTION_MAIN);
										i.setClass(LoginActivity.this, MainActivity.class);
										startActivity(i);
										finish();
									}
								})
								.create()
								.show();
			} else {
				// Wrong username or password
				new AlertDialog.Builder(LoginActivity.this)
								.setMessage(R.string.login_fail)
								.setCancelable(true)
								.create()
								.show();
			}
		}
		
	}
}
