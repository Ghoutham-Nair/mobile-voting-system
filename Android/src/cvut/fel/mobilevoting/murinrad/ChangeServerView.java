package cvut.fel.mobilevoting.murinrad;

import cvut.fel.mobilevoting.murinrad.storage.DatabaseStorage;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;

public class ChangeServerView extends Activity {
	ServerData server = null;
	TextView friendlyName;
	TextView ipAdd;
	TextView userName;
	TextView pass;
	TextView pNumber;
	DatabaseStorage storage;
	int id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.asd);
		storage = new DatabaseStorage(this);
		friendlyName = (TextView) findViewById(R.id.idFname);
		ipAdd = (TextView) findViewById(R.id.idIPadd);
		userName = (TextView) findViewById(R.id.idUserName);
		pass = (TextView) findViewById(R.id.idPass);
		pNumber = (TextView) findViewById(R.id.idPortNumber);
		id = (Integer) getIntent().getSerializableExtra("id");
		// server = (ServerData) getIntent().getSerializableExtra("ServerData");
		if (id != -1) {
			server = storage.getServer(id);
		}

		if (server != null) {
			friendlyName.setText(server.getFriendlyName());
			ipAdd.setText(server.getAddress());
			pNumber.setText(server.getPort() + "");
			userName.setText(server.getLogin());
			pass.setText(server.getPassword());
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		storage.closeDB();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.serverchangemenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.connect:
			connect();
			return true;
		case R.id.save:
			try {
				back();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void back() throws Exception {
		save();
		//finish();

	}

	private void save() {
		int port = parsePort(pNumber.getText().toString());
		if (port != -1) {
			ServerData s = new ServerData(userName.getText().toString(), pass
					.getText().toString(), id, ipAdd.getText().toString(),
					port, friendlyName.getText().toString());
			storage.addServer(s);
			finish();
		}

	}

	private void connect() {
		int port = parsePort(pNumber.getText().toString());
		if (port == -1)
			return;
		ServerData s = new ServerData(userName.getText().toString(), pass
				.getText().toString(), -1, ipAdd.getText().toString(), port,
				getString(R.string.temporaryserverTag));
		Intent i = new Intent();
		i.setClassName("cvut.fel.mobilevoting.murinrad",
				"cvut.fel.mobilevoting.murinrad.QuestionsView");
		i.putExtra("ServerData", s);
		startActivity(i);
	}

	private int parsePort(String port) {
		int i = -1;
		try {
			i = Integer.parseInt(port);
		} catch (NumberFormatException ex) {
			Toast.makeText(this, getString(R.string.badPortFormatError),
					Toast.LENGTH_LONG).show();
		}
		if(i<1 || i>65535) {
			Toast.makeText(this, getString(R.string.badPortFormatError),
					Toast.LENGTH_LONG).show();
			return -1;
		}

		return i;

	}

}