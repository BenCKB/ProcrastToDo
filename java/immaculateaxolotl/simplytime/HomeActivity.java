package immaculateaxolotl.simplytime;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

import immaculateaxolotl.simplytime.database.RealmController;
import immaculateaxolotl.simplytime.database.RealmTaskAdapter;
import immaculateaxolotl.simplytime.tasks.Task;
import immaculateaxolotl.simplytime.tasks.TaskAdapter;
import immaculateaxolotl.simplytime.tasks.TaskItemTouchHelperCallback;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public class HomeActivity extends AppCompatActivity
{
	private Realm realm;
	private TaskAdapter adapter;
	private boolean isActive;
	private Task activeTask = null;
	private RealmController controller;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//Initialization of HomeActivity
		CheckFirstTimeUse();
		SetupDatabase();
		SetupListAdapter();

		setContentView(R.layout.activity_home);

		//Get activity ready for use
		SetupLegalNotice();
		isActive = false; //No Task is currently active

		final Button addTaskBtn = (Button) findViewById(R.id.btn_add_task); 		//Adding a task
		final Button viewTasksBtn = (Button) findViewById(R.id.btn_view_task); 		//Viewing all tasks
		final Button gotTimeBtn = (Button) findViewById(R.id.btn_got_time);			//Working on tasks

		DisplayDefaultText(gotTimeBtn);
		SetupAddTaskListener(addTaskBtn);
		SetupViewTaskListener(viewTasksBtn, gotTimeBtn);
		SetupGotTimeListener(gotTimeBtn);
	}

	public void SetupListAdapter()
	{
		//Create the list adapter
		adapter = new TaskAdapter(HomeActivity.this);

		RealmTaskAdapter realmAdapter = new RealmTaskAdapter(HomeActivity.this, controller.getTasks(), true);

		//Set the adapter
		adapter.setRealmAdapter(realmAdapter);
		adapter.notifyDataSetChanged();
	}

	public void CheckFirstTimeUse()
	{
		//Initialize an instance of the SharedPreferences and Editor
		final SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);
		final SharedPreferences.Editor preferencesEditor = preferences.edit();

		//Show Tutorial activity when opening the app for the first time, or when explicitly forcing it.
		if (!preferences.contains("tutorial") || preferences.getBoolean("tutorial", true)) {
			preferencesEditor.putBoolean("tutorial", false);
			preferencesEditor.apply();
			Intent intent = new Intent(this, Tutorial.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Clear to root
			startActivity(intent);
		}
	}

	public void SetupDatabase()
	{
		//Initialize Realm
		Realm.init(this);

		//Create default configurations
		RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
				.name(Realm.DEFAULT_REALM_NAME)
				.schemaVersion(0)
				.deleteRealmIfMigrationNeeded()
				.build();

		//Set configurations
		Realm.setDefaultConfiguration(realmConfiguration);
		controller = RealmController.with(this);
		realm = controller.getRealm();
	}

	public void SetupLegalNotice()
	{
		ConstraintLayout lay = (ConstraintLayout) findViewById(R.id.background);

		lay.setOnLongClickListener(view -> {
			final Notices notices = new Notices();
			notices.addNotice(new Notice("Realm", "https://realm.io/", "Realm Mobile Database", new ApacheSoftwareLicense20()));
			notices.addNotice(new Notice("Android Onboarder", "https://github.com/chyrta/AndroidOnboarder", "Copyright (c) 2017 Dzmitry Chyrta, Daniel Morales", new MITLicense()));

			new LicensesDialog.Builder(HomeActivity.this)
					.setNotices(notices)
					.setIncludeOwnLicense(true)
					.build()
					.show();
			return false;
		});
	}

	public void DisplayDefaultText(Button gotTimeBtn)
	{
		if (activeTask == null) {
			gotTimeBtn.setText(R.string.got_time);
		}
	}

	public void SetupAddTaskListener(Button addTaskBtn)
	{
		addTaskBtn.setOnClickListener((View.OnClickListener) view -> {
			//Create a LayoutInflater to inflate the custom layout
			LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);

			final View viewToInflate = inflater.inflate(R.layout.add_task_box, null);

			// Initialize layout elements of the custom layout
			final EditText title = (EditText) viewToInflate.findViewById(R.id.add_task_box_title);
			final NumberPicker minutesO = (NumberPicker) viewToInflate.findViewById(R.id.minutesPickerO); //Ones: 0X
			final NumberPicker minutesT = (NumberPicker) viewToInflate.findViewById(R.id.minutesPickerT); //Tens: X0

			//Create the AlertDialog
			final AlertDialog.Builder addTaskDialog = new AlertDialog.Builder(HomeActivity.this);

			//Set up minimum and maximum values for time => min: 00, max: 59
			minutesO.setMinValue(0);
			minutesO.setMaxValue(9);
			minutesT.setMinValue(0);
			minutesT.setMaxValue(5);

			//Add Add-Task-Dialog to the view that is being infalted
			addTaskDialog.setView(viewToInflate);

			//User creates a task
			addTaskDialog.setPositiveButton(R.string.addTask_create, null);

			//User cancels the AlertDialog
			addTaskDialog.setNegativeButton(R.string.addTask_cancel, null);

			//Create Dialog
			final AlertDialog addShowDialog = addTaskDialog.create();

			//Create OnShowListener
			addShowDialog.setOnShowListener(dialogInterface -> {

				Button createButton = addShowDialog.getButton(AlertDialog.BUTTON_POSITIVE);

				createButton.setOnClickListener(view1 -> {

					//Check if task name is empty
					if (title.getText().toString().trim().equals("")) {
						Toast.makeText(HomeActivity.this, R.string.valid_name, Toast.LENGTH_SHORT).show();
					}
					else {
						//Create a new task with given name and time
						Task t = new Task();

						t.setId(String.valueOf(System.currentTimeMillis())); //Use current time as id in database
						t.setName(title.getText().toString());
						t.setTime(minutesO.getValue() + (10 * minutesT.getValue()));

						//Copy item to database
						realm.beginTransaction();
						realm.copyToRealm(t);
						realm.commitTransaction();

						//Tell user that the task has been added successfully
						Toast.makeText(HomeActivity.this, "\"" + title.getText().toString() + "\" " + HomeActivity.this.getString(R.string.task_was_added), Toast.LENGTH_SHORT).show();
						addShowDialog.dismiss();
					}
				});
			});

			addShowDialog.show();

		});
	}

	public void SetupViewTaskListener(Button viewTasksBtn, Button gotTimeBtn)
	{
		//View Task OnClickListener
		viewTasksBtn.setOnClickListener((View.OnClickListener) view -> {

			LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
			final View viewToInflate = inflater.inflate(R.layout.view_task_box, null);

			RecyclerView recycler = (RecyclerView) viewToInflate.findViewById(R.id.recycler);
			TextView noDataText = (TextView) viewToInflate.findViewById(R.id.empty_view);
			recycler.setHasFixedSize(true);

			ItemTouchHelper.Callback callback = new TaskItemTouchHelperCallback(adapter);
			ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
			touchHelper.attachToRecyclerView(recycler);

			// use a linear layout manager since the cards are vertically scrollable
			final LinearLayoutManager layoutManager = new LinearLayoutManager(HomeActivity.this);
			layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
			recycler.setLayoutManager(layoutManager);

			recycler.setAdapter(adapter);
			adapter.notifyDataSetChanged();

			if (!RealmController.with(HomeActivity.this).hasTasks()) {
				noDataText.setVisibility(View.VISIBLE);
			} else {
				noDataText.setVisibility(View.INVISIBLE);
			}

			final AlertDialog.Builder viewTaskDialog = new AlertDialog.Builder(HomeActivity.this, R.style.viewTaskTheme);
			viewTaskDialog.setView(viewToInflate);
			viewTaskDialog.setNegativeButton(R.string.back_b, (dialogInterface, i) -> {
				if (activeTask != null && !activeTask.isValid()) {
					activeTask = null;
					isActive = false;
					gotTimeBtn.setText(R.string.got_time);
				}
				if (activeTask != null && !controller.getTask(activeTask.getId()).getName().equals(gotTimeBtn.getText().toString())) {
					gotTimeBtn.setText(controller.getTask(activeTask.getId()).getName());
				}
			});
			AlertDialog dialog = viewTaskDialog.create();

			dialog.getWindow().setLayout((getWindow().getAttributes().width), (getWindow().getAttributes().height));
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.show();

			dialog.setOnDismissListener(dialogInterface -> {
				if (activeTask != null && !activeTask.isValid()) {
					activeTask = null;
					isActive = false;
					gotTimeBtn.setText(R.string.got_time);
				}
			});

		});
	}

	public void SetupGotTimeListener(Button gotTimeBtn)
	{
		gotTimeBtn.setOnClickListener((View.OnClickListener) view -> {

			LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
			final View viewToInflate = inflater.inflate(R.layout.do_task_box, null);
			final AlertDialog.Builder gotTimeDialog = new AlertDialog.Builder(HomeActivity.this);

			final NumberPicker minutesO = (NumberPicker) viewToInflate.findViewById(R.id.gotMinutesPickerO);
			final NumberPicker minutesT = (NumberPicker) viewToInflate.findViewById(R.id.gotMinutesPickerT);

			minutesO.setMinValue(0);
			minutesO.setMaxValue(9);
			minutesT.setMinValue(0);
			minutesT.setMaxValue(5);

			gotTimeDialog.setView(viewToInflate);

			if (!isActive) {
				gotTimeDialog.setPositiveButton(R.string.just_do_it, (dialog, which) -> {

					int time = minutesO.getValue() + (10 * minutesT.getValue());

					RealmResults results = RealmController.with(HomeActivity.this).queryedTasks(time);
					if (!results.isEmpty()) {

						activeTask = (Task) results.sort("time", Sort.ASCENDING).last();
						gotTimeBtn.setText(activeTask.getName());
						isActive = true;
						adapter.notifyDataSetChanged();
					} else {
						gotTimeBtn.setText(R.string.not_tasks);
						isActive = false;
					}

				});
				gotTimeDialog.setNegativeButton(R.string.addTask_cancel, null);
				gotTimeDialog.create();
				gotTimeDialog.show();
			} else {
				AlertDialog doneolog = new AlertDialog.Builder(HomeActivity.this)
						.setTitle(R.string.did_complete)
						.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
							isActive = false;
							RealmController.with(HomeActivity.this).deleteItem(activeTask);
							activeTask = null;
							gotTimeBtn.setText(R.string.got_time);
						})
						.setNegativeButton(R.string.no, (DialogInterface.OnClickListener) (dialogInterface, i) -> {
							AlertDialog noDialog = new AlertDialog.Builder(HomeActivity.this)
									.setTitle(R.string.edit_or_delete)
									.setNeutralButton(R.string.edit, (DialogInterface.OnClickListener) (dialogInterface1, i1) -> {

										LayoutInflater inflater1 = LayoutInflater.from(HomeActivity.this);
										final View infl1 = inflater1.inflate(R.layout.add_task_box, null);
										final EditText title = (EditText) infl1.findViewById(R.id.add_task_box_title);
										final NumberPicker minutesO1 = (NumberPicker) infl1.findViewById(R.id.minutesPickerO);
										final NumberPicker minutesT1 = (NumberPicker) infl1.findViewById(R.id.minutesPickerT);
										final AlertDialog.Builder updateTaskDialog = new AlertDialog.Builder(HomeActivity.this);
										minutesO1.setMinValue(0);
										minutesO1.setMaxValue(9);
										minutesT1.setMinValue(0);
										minutesT1.setMaxValue(5);
										updateTaskDialog.setView(infl1);

										Task existingTask = RealmController.with(HomeActivity.this).getTask(activeTask.getId());

										title.setText(existingTask.getName());
										minutesO1.setValue(existingTask.getTime() % 10);
										minutesT1.setValue(existingTask.getTime() / 10);

										/*
										 * Set action on "positive" response
										 */

										updateTaskDialog.setPositiveButton(R.string.update, null);

										/*
										 * Set action on "negative" response
										 */

										updateTaskDialog.setNegativeButton(R.string.addTask_cancel, null);

										final AlertDialog updateShowDialog = updateTaskDialog.create();

										updateShowDialog.setOnShowListener(dialogInterface11 -> {
											Button b = updateShowDialog.getButton(AlertDialog.BUTTON_POSITIVE);
											b.setOnClickListener(view1 -> {
												if (title.getText().toString().trim().equals("")) {
													Toast.makeText(HomeActivity.this, R.string.valid_name, Toast.LENGTH_SHORT).show();
												} else {
													Task t = new Task();
													t.setId(activeTask.getId());
													t.setName(title.getText().toString());
													t.setTime(minutesO1.getValue() + (10 * minutesT1.getValue()));
													realm.beginTransaction();
													realm.copyToRealmOrUpdate(t);
													realm.commitTransaction();

													isActive = false;
													activeTask = null;
													gotTimeBtn.setText(R.string.got_time);

													if (activeTask == null) {
														gotTimeBtn.setText(R.string.got_time);
													}

													Toast.makeText(HomeActivity.this, "\"" + title.getText().toString() + "\" " + HomeActivity.this.getString(R.string.was_updated), Toast.LENGTH_SHORT).show();
													updateShowDialog.dismiss();
												}
											});
										});


										updateShowDialog.show();

									})
									.setNegativeButton(R.string.delete, (DialogInterface.OnClickListener) (dialogInterface12, i12) -> {
										isActive = false;
										RealmController.with(HomeActivity.this).deleteItem(activeTask);
										activeTask = null;
										gotTimeBtn.setText(R.string.got_time);
									})
									.create();

							noDialog.show();
						})
						.setNeutralButton(R.string.addTask_cancel, null)
						.create();

				doneolog.show();
			}

		});
	}
}