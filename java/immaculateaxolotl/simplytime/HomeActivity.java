package immaculateaxolotl.simplytime;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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


public class HomeActivity extends AppCompatActivity {

    /*
     * Private fields
     */

    private Realm realm;
    private TaskAdapter adapter;
    private boolean isActive;
    private Task activeTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        /*
         * Hide the Status Bar
         */

        // For Build Versions below 16
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        //For Build Versions above or equal to 16
        else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            ActionBar actionBar = getActionBar();
            if(actionBar != null)
                actionBar.hide();
        }


        //Initialize an instance of the SharedPreferences and Editor
        final SharedPreferences pref = HomeActivity.this.getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor ed = pref.edit();

        //Tutorial Activity
        if(!pref.contains("tutorial") || pref.getBoolean("tutorial", true)){
            ed.putBoolean("tutorial", false);
            ed.apply();
            Intent intent = new Intent(this, Tutorial.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }



        /*
         * Set up the realm
         */

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

        //Set up Realm Controller
        final RealmController controller = RealmController.with(this);

        //Set the realm
        this.realm = controller.getRealm();

        //Refresh realm
        controller.refresh();

        //Create the adapter
        adapter = new TaskAdapter(HomeActivity.this);

        //Set the adapter
        setRealmAdapter(controller.getTasks());


        /*
         * Set content view and initialize variables
         */

        //Setting Content View
        setContentView(R.layout.activity_home);

        //No Task is currently active
        isActive = false;

        ConstraintLayout lay = (ConstraintLayout)findViewById(R.id.background);

        /*
        * Display licenses on long click
        */

        lay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final Notices notices = new Notices();
                notices.addNotice(new Notice("Realm", "https://realm.io/", "Realm Mobile Database", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Android Onboarder", "https://github.com/chyrta/AndroidOnboarder", "Copyright (c) 2017 Dzmitry Chyrta, Daniel Morales",new MITLicense()));

                new LicensesDialog.Builder(HomeActivity.this)
                        .setNotices(notices)
                        .setIncludeOwnLicense(true)
                        .build()
                        .show();
                return false;
            }
        });

        /*
         * Buttons and their OnClickListeners
         */

        //Adding a task
        final Button addTaskBtn = (Button)findViewById(R.id.btn_add_task);

        //Viewing all tasks
        final Button viewTasksBtn = (Button)findViewById(R.id.btn_view_task);

        //Working on tasks
        final Button gotTimeBtn = (Button)findViewById(R.id.btn_got_time);


        //If no task is active, set text
        if (activeTask == null) {
            gotTimeBtn.setText(R.string.got_time);
        }

        //Add Task OnClickListener
        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    /*
                     * Display "Add Task Box" Alert Dialog
                     */

                    //Create a LayoutInflater to inflate the custom layout
                    LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
                    //Inflate custom layout

                    @SuppressWarnings("all")
                    //Inflate custom layout and suppress warning about using null as root, since
                    // no root exists and docs allow use of null
                    final View infl = inflater.inflate(R.layout.add_task_box, null);

                    // Initialize layout elements of the custom layout
                    final EditText title = (EditText) infl.findViewById(R.id.add_task_box_title);
                    final NumberPicker minutesO = (NumberPicker) infl.findViewById(R.id.minutesPickerO);
                    final NumberPicker minutesT = (NumberPicker) infl.findViewById(R.id.minutesPickerT);

                    //Create the AlertDialog
                    final AlertDialog.Builder addTaskDialog = new AlertDialog.Builder(HomeActivity.this);

                    //Set minimum and maximum values of the number pickers giving us a maximum of 59 minutes per task
                    //For the ones
                    minutesO.setMinValue(0);
                    minutesO.setMaxValue(9);
                    //for the tens
                    minutesT.setMinValue(0);
                    minutesT.setMaxValue(5);

                    //Set our custom layout as the view
                    addTaskDialog.setView(infl);

                    //User creates a task
                    addTaskDialog.setPositiveButton(R.string.addTask_create, null);

                    //User cancels the AlertDialog
                    addTaskDialog.setNegativeButton(R.string.addTask_cancel, null);

                    //Create Dialog
                    final AlertDialog addShowDialog = addTaskDialog.create();

                    //Create OnShowListener
                    addShowDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            //Get the "Create" Button
                            Button b = addShowDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            //Set OnClickListener
                            b.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //Check if task name is empty
                                    if(title.getText().toString().trim().equals("")){
                                        Toast.makeText(HomeActivity.this, R.string.valid_name, Toast.LENGTH_SHORT).show();
                                    }
                                    //Else set task
                                    else{
                                        //Create a new Task Object
                                        Task t = new Task();
                                        //Set values of object to input from user
                                        t.setId(String.valueOf(System.currentTimeMillis()));
                                        t.setName(title.getText().toString());
                                        t.setTime(minutesO.getValue() + (10 * minutesT.getValue()));

                                        //Copy item to realm
                                        realm.beginTransaction();
                                        realm.copyToRealm(t);
                                        realm.commitTransaction();

                                        //Make toast, to show that task was successfully added
                                        Toast.makeText(HomeActivity.this, "\"" + title.getText().toString() + "\" " + HomeActivity.this.getString(R.string.task_was_added), Toast.LENGTH_SHORT).show();
                                        addShowDialog.dismiss();
                                    }
                                }
                            });
                        }
                    });


                    //Show Dialog
                    addShowDialog.show();

            }
        });

        //View Task OnClickListener
        viewTasksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
                final View infl = inflater.inflate(R.layout.view_task_box, null);

                RecyclerView recycler = (RecyclerView) infl.findViewById(R.id.recycler);
                TextView noDataText = (TextView) infl.findViewById(R.id.empty_view);
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

                if(!RealmController.with(HomeActivity.this).hasTasks()){
                    noDataText.setVisibility(View.VISIBLE);
                }
                else{
                    noDataText.setVisibility(View.INVISIBLE);
                }

                final AlertDialog.Builder viewTaskDialog = new AlertDialog.Builder(HomeActivity.this, R.style.viewTaskTheme);
                viewTaskDialog.setView(infl);
                viewTaskDialog.setNegativeButton(R.string.back_b, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(activeTask != null && !activeTask.isValid()){
                            activeTask = null;
                            isActive = false;
                            gotTimeBtn.setText(R.string.got_time);
                        }
                        if(activeTask != null && !controller.getTask(activeTask.getId()).getName().equals(gotTimeBtn.getText().toString())){
                            gotTimeBtn.setText(controller.getTask(activeTask.getId()).getName());
                        }
                    }
                });
                AlertDialog dialog = viewTaskDialog.create();

                dialog.getWindow().setLayout((getWindow().getAttributes().width),(getWindow().getAttributes().height));
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.show();

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if(activeTask != null && !activeTask.isValid()){
                            activeTask = null;
                            isActive = false;
                            gotTimeBtn.setText(R.string.got_time);
                        }
                    }
                });

            }
        });

        gotTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
                final View infl = inflater.inflate(R.layout.do_task_box, null);
                final NumberPicker minutesO = (NumberPicker) infl.findViewById(R.id.gotMinutesPickerO);
                final NumberPicker minutesT = (NumberPicker) infl.findViewById(R.id.gotMinutesPickerT);
                final AlertDialog.Builder gotTimeDialog = new AlertDialog.Builder(HomeActivity.this);
                minutesO.setMinValue(0);
                minutesO.setMaxValue(9);
                minutesT.setMinValue(0);
                minutesT.setMaxValue(5);
                gotTimeDialog.setView(infl);

                if(!isActive) {
                    gotTimeDialog.setPositiveButton(R.string.just_do_it, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

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

                            }
                        });
                    gotTimeDialog.setNegativeButton(R.string.addTask_cancel, null);
                    gotTimeDialog.create();
                    gotTimeDialog.show();
            }
                else{
                    AlertDialog doneolog = new AlertDialog.Builder(HomeActivity.this)
                            .setTitle(R.string.did_complete)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    isActive = false;
                                    RealmController.with(HomeActivity.this).deleteItem(activeTask);
                                    activeTask = null;
                                    gotTimeBtn.setText(R.string.got_time);
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AlertDialog noDialog = new AlertDialog.Builder(HomeActivity.this)
                                            .setTitle(R.string.edit_or_delete)
                                            .setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
                                                    final View infl = inflater.inflate(R.layout.add_task_box, null);
                                                    final EditText title = (EditText) infl.findViewById(R.id.add_task_box_title);
                                                    final NumberPicker minutesO = (NumberPicker) infl.findViewById(R.id.minutesPickerO);
                                                    final NumberPicker minutesT = (NumberPicker) infl.findViewById(R.id.minutesPickerT);
                                                    final AlertDialog.Builder updateTaskDialog = new AlertDialog.Builder(HomeActivity.this);
                                                    minutesO.setMinValue(0);
                                                    minutesO.setMaxValue(9);
                                                    minutesT.setMinValue(0);
                                                    minutesT.setMaxValue(5);
                                                    updateTaskDialog.setView(infl);

                                                    Task existingTask = RealmController.with(HomeActivity.this).getTask(activeTask.getId());

                                                    title.setText(existingTask.getName());
                                                    minutesO.setValue(existingTask.getTime() % 10);
                                                    minutesT.setValue(existingTask.getTime()/10);

                                                    /*
                                                     * Set action on "positive" response
                                                     */

                                                    updateTaskDialog.setPositiveButton(R.string.update, null);

                                                    /*
                                                     * Set action on "negative" response
                                                     */

                                                    updateTaskDialog.setNegativeButton(R.string.addTask_cancel, null);

                                                    final AlertDialog updateShowDialog = updateTaskDialog.create();

                                                    updateShowDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                                        @Override
                                                        public void onShow(DialogInterface dialogInterface) {
                                                            Button b = updateShowDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                                            b.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    if(title.getText().toString().trim().equals("")){
                                                                        Toast.makeText(HomeActivity.this, R.string.valid_name, Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    else {
                                                                        Task t = new Task();
                                                                        t.setId(activeTask.getId());
                                                                        t.setName(title.getText().toString());
                                                                        t.setTime(minutesO.getValue() + (10 * minutesT.getValue()));
                                                                        realm.beginTransaction();
                                                                        realm.copyToRealmOrUpdate(t);
                                                                        realm.commitTransaction();

                                                                        isActive = false;
                                                                        activeTask = null;
                                                                        gotTimeBtn.setText(R.string.got_time);

                                                                        if (activeTask == null) {
                                                                            gotTimeBtn.setText(R.string.got_time);
                                                                        }

                                                                        Toast.makeText(HomeActivity.this, "\"" + title.getText().toString() + "\" " +  HomeActivity.this.getString(R.string.was_updated), Toast.LENGTH_SHORT).show();
                                                                        updateShowDialog.dismiss();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });


                                                    updateShowDialog.show();

                                                }
                                            })
                                            .setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    isActive = false;
                                                    RealmController.with(HomeActivity.this).deleteItem(activeTask);
                                                    activeTask = null;
                                                    gotTimeBtn.setText(R.string.got_time);
                                                }
                                            })
                                            .create();

                                    noDialog.show();
                                }
                            })
                            .setNeutralButton(R.string.addTask_cancel, null)
                            .create();

                    doneolog.show();
                }

            }
        });

        /*
         * Initialize Realm adapter
         */

        RealmController.with(this).refresh();
    }

    public void setRealmAdapter(RealmResults<Task> tasks) {
        RealmTaskAdapter realmAdapter = new RealmTaskAdapter(HomeActivity.this, tasks, true);
        // Set the data and tell the RecyclerView to draw
        adapter.setRealmAdapter(realmAdapter);
        adapter.notifyDataSetChanged();
    }
}