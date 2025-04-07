package edu.pitt.lersais.mhealth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.pitt.lersais.mhealth.adapter.ExpandableListAdapter;
import edu.pitt.lersais.mhealth.adapter.GridListAdapter;
import edu.pitt.lersais.mhealth.model.ExpandedMenu;
import edu.pitt.lersais.mhealth.model.GridItem;
import edu.pitt.lersais.mhealth.util.DownloadImageTask;

/**
 * The MainActivity.
 *
 * @author Haobing Huang and Runhua Xu.
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private List<ExpandedMenu> mMenuList;
    private HashMap<ExpandedMenu, List<ExpandedMenu>> mMenuListChild;
    private ExpandableListAdapter mExpandableListAdaptor;
    private ExpandableListView expandableListView;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            initializeToolbar();
            initializeDrawerMenu();

            // initialize the grid view in main window
            GridView gridView = findViewById(R.id.content_grid_view);
            ArrayList<GridItem> mData = new ArrayList<GridItem>();
            mData.add(new GridItem(R.drawable.icon_medical_48, "Medical Record"));

            BaseAdapter adapter = new GridListAdapter<GridItem>(mData, R.layout.grid_view_item) {
                @Override
                public void bindView(ViewHolder holder, GridItem obj) {
                    holder.setImageResource(R.id.img_icon, obj.getiId());
                    holder.setText(R.id.txt_icon, obj.getiName());
                }
            };
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    if (id == 0) {
                        moveToMedicalRecord();
                    }
                }
            });
        }
    }

    private void moveToMedicalRecord() {
        // TODO: implement the function to move to your designed medical record activity view
        // An instance here
        // Intent intent = new Intent(MainActivity.this, MedicalRecordActivity.class);
        // startActivity(intent);
        // overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

        // BEGIN your code here

    }

    private void signOut() {
        // TODO: Task 2.4 implement sign out function.
        // Tips:
        // 1) call the sign out method provided by Firebase Authentication
        mAuth.signOut();
        // 2) jump to the login view
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

    }

    private void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initializeDrawerMenu() {
        // the drawer left menu
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View navHeaderView = navigationView.getHeaderView(0);
        CircleImageView profilePhotoImageView = navHeaderView.findViewById(R.id.status_profile_photo);
        if (currentUser.getPhotoUrl() != null) {
            Uri photoUrl = currentUser.getPhotoUrl();
            new DownloadImageTask(profilePhotoImageView).execute(photoUrl.toString());
        }
        TextView navHeaderUidTextView = navHeaderView.findViewById(R.id.status_uid);
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            navHeaderUidTextView.setText(currentUser.getDisplayName());
        }
        TextView navHeaderEmailTextView = navHeaderView.findViewById(R.id.status_email);
        navHeaderEmailTextView.setText(currentUser.getEmail());

        // prepare the expandable menu list
        prepareExpandableMenuData();

        expandableListView = (ExpandableListView) findViewById(R.id.navigation_expandable_menu);
        expandableListView.setGroupIndicator(null);
        mExpandableListAdaptor = new ExpandableListAdapter(this, mMenuList, mMenuListChild, expandableListView);
        expandableListView.setAdapter(mExpandableListAdaptor);
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                ExpandedMenu selectModel = (ExpandedMenu) expandableListView.getExpandableListAdapter().getGroup(groupPosition);
                if (selectModel.getMenuName() == R.string.expandable_menu_account) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    return true;
                } else if (selectModel.getMenuName() == R.string.expandable_menu_setting) {
                    Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    return true;
                } else if (selectModel.getMenuName() == R.string.expandable_menu_logout) {
                    MainActivity.this.signOut();
                    return true;
                }

                return false;
            }
        });
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                ExpandedMenu selectModel = (ExpandedMenu) expandableListView.getExpandableListAdapter().getChild(groupPosition, childPosition);
                if (selectModel.getMenuName() == R.string.expandable_menu_medical_item1) {
                    moveToMedicalRecord();
                    return true;
                }
                return false;
            }
        });
    }

    private void prepareExpandableMenuData() {
        mMenuList = new ArrayList<>();
        mMenuListChild = new HashMap<>();
        mMenuList.add(new ExpandedMenu(R.string.expandable_menu_account, R.drawable.menu_account_24));
        mMenuList.add(new ExpandedMenu(R.string.expandable_menu_medical, R.drawable.menu_medical_24));
        mMenuList.add(new ExpandedMenu(R.string.expandable_menu_setting, R.drawable.ic_menu_manage));
        mMenuList.add(new ExpandedMenu(R.string.expandable_menu_logout, R.drawable.menu_logout_24));

        List<ExpandedMenu> medicalMenuList = new ArrayList<>();
        medicalMenuList.add(new ExpandedMenu(R.string.expandable_menu_medical_item1, R.drawable.menu_medical_24));
        medicalMenuList.add(new ExpandedMenu(R.string.expandable_menu_medical_item2, R.drawable.menu_medical_24));
        mMenuListChild.put(mMenuList.get(1), medicalMenuList);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this, "Action clicked", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
