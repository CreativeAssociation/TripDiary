package com.creative.tripdiary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.creative.tripdiary.network.TransferController;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity {

    private DrawerLayout layDrawer;
    private ListView lstDrawer;
    private Integer fragmentId;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] drawer_menu;
    private int[] drawer_menu_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
        initDrawer();
        initDrawerList();
        selectItem(-1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private void initActionBar(){
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    private void initDrawer(){
        setContentView(R.layout.drawer);

        layDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        lstDrawer = (ListView) findViewById(R.id.left_drawer);

        layDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mTitle = mDrawerTitle = getTitle();
        drawerToggle = new ActionBarDrawerToggle(
                this, 
                layDrawer,
                R.drawable.ic_drawer, 
                R.string.drawer_open,
                R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
            }
        };
        drawerToggle.syncState();

        layDrawer.setDrawerListener(drawerToggle);
    }

    private void initDrawerList(){
    	drawer_menu = this.getResources().getStringArray(R.array.drawer_menu);
    	//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawer_menu);
    	drawer_menu_icon = new int[] {R.drawable.ic_action_group,R.drawable.ic_action_person,R.drawable.ic_action_favorite};
		List<HashMap<String,String>> lstData = new ArrayList<HashMap<String,String>>();
		for (int i = 0; i < drawer_menu.length; i++) {
			HashMap<String, String> mapValue = new HashMap<String, String>();
			mapValue.put("icon", Integer.toString(drawer_menu_icon[i]));
			mapValue.put("title", drawer_menu[i]);
			lstData.add(mapValue);
		}
		SimpleAdapter adapter = new SimpleAdapter(this, lstData, R.layout.drawer_list_item, new String[]{"icon", "title"}, new int[]{R.id.imgIcon, R.id.txtItem});
		lstDrawer.setAdapter(adapter);
		
		//側選單點選偵聽器
		lstDrawer.setOnItemClickListener(new DrawerItemClickListener());
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	//home
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        
    	switch (item.getItemId()) {     
	        case R.id.action_refresh:
	        	selectItem(0);
	            return true;
	        case R.id.action_upload:
	        	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 0);
	            return true;
	        //case R.id.action_add:
	            //return true;    
	        default:
	            return super.onOptionsItemSelected(item);
	    } 
    }
    
    //================================================================================
  	// 側選單點選事件
  	//================================================================================
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
    	@Override
    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    		selectItem(position);
    	}
    }
  	
  	private void selectItem(int position) {
        
  		Fragment fragment = null;
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        
        if(position == -1){
        	fragment = new FragmentAllPost();
        	position = 0;
        	fragmentTransaction.replace(R.id.content_frame, fragment);
        	fragmentTransaction.addToBackStack("home");
        	fragmentTransaction.commit();
        }else{
        	switch (position) {
		  		case 0:
		  			fragment = new FragmentAllPost();
		  			break;	  			
		  		case 1:
		  			fragment = new FragmentAllPostSummary();
		  			break;
		  			
		  		case 2:
		  			break;
		
		  		default:
		  			//還沒製作的選項，fragment 是 null，直接返回
		  			return;
        	}
  			fragmentTransaction.replace(fragmentId, fragment);
        	fragmentTransaction.commit();
        }
        
    	fragmentId = fragment.getId();
          

  		// 更新被選擇項目，換標題文字，關閉選單
        lstDrawer.setItemChecked(position, true);
        setTitle(drawer_menu[position]);
        layDrawer.closeDrawer(lstDrawer);
  	}
  	
  	@Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
	
	/**
	 * Back 鍵處理
	 * 當最後一個 stack 為 R.id.content_frame, 結束 App
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		FragmentManager fragmentManager = this.getFragmentManager();
		int stackCount = fragmentManager.getBackStackEntryCount();
		if (stackCount == 0) {
			this.finish();
			Intent startMain = new Intent(Intent.ACTION_MAIN);
	        startMain.addCategory(Intent.CATEGORY_HOME);
	        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        startActivity(startMain);
		}
	}
	
	@Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if(resCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if(uri != null) {
                TransferController.upload(this, uri, "public");
            }
        }
    }
}
