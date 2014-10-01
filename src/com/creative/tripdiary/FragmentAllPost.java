package com.creative.tripdiary;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.creative.tripdiary.Constants;
import com.creative.tripdiary.Util;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;


public class FragmentAllPost extends Fragment{
	private static GridView mGrid;
    private AmazonS3Client mClient;
    private ObjectAdapter mAdapter;
    //keeps track of the objects the user has selected
    private HashSet<S3ObjectSummary> mSelectedObjects = 
        new HashSet<S3ObjectSummary>();
	private String member;
	private boolean checkboxFlag;
	protected ProgressDialog mLoadingProgressDialog;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        member = "public";
        checkboxFlag = false;
        mAdapter = new ObjectAdapter(getActivity());
        mLoadingProgressDialog = new ProgressDialog(getActivity());
    }
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return initView(inflater, container);
    }
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		new RefreshTask().execute();
	}
	
    private View initView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mGrid = (GridView) view.findViewById(android.R.id.list);
        mGrid.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        mGrid.setMultiChoiceModeListener(new MutipleChoiceModel());
        
        mGrid.setAdapter(mAdapter);
        
        return view;
    }
    /* 
     * This lets the user click on anywhere in the row instead of just the checkbox
     * to select the files to download
     */
    
    
    private class ItemLongClickListener implements OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
        		int pos, long id) {
        	// TODO Auto-generated method stub
        	Log.d("ItemLongClickListener", "333");
            return true;
        }
    }
    
    //menu
    private class MutipleChoiceModel implements MultiChoiceModeListener{
    	@Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            // Here you can do something when items are selected/de-selected,
            // such as update the title in the CAB
    		if (checked) {
    			mAdapter.setNewSelection(position, checked);
    		} else {
    			mAdapter.removeSelection(position);               
    		}
    		
    		int selectCount = mGrid.getCheckedItemCount();

            switch (selectCount) {
            case 1:
                mode.setSubtitle("One item selected");
                break;
            default:
                mode.setSubtitle("" + selectCount + " items selected");
                break;
            }
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
            	case R.id.action_group:
                //deleteSelectedItems();
            		
            		for (Integer s : mAdapter.getCurrentCheckedPosition()) {
            		    Log.v("Position :",s.toString());
            		}
            		
	                mode.finish(); // Action picked, so close the CAB
	                return true;
            	case R.id.action_discard:
            		return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate the menu for the CAB
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.image_actionbar, menu);
            checkboxFlag = !checkboxFlag;
            
            mode.setTitle("Select Items");
            mode.setSubtitle("One item selected");
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Here you can make any necessary updates to the activity when
            // the CAB is removed. By default, selected items are deselected/unchecked.
        	checkboxFlag = !checkboxFlag;
        	mAdapter.clearSelection();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here you can perform updates to the CAB due to
            // an invalidate() request
            return false;
        }
    }
    
    private class RefreshTask extends AsyncTask<Void, Void, List<Bitmap>> {
        @Override
        protected void onPreExecute() {
        	// TODO Auto-generated method stub
        	mLoadingProgressDialog.setMessage("Loading...");
        	mLoadingProgressDialog.show();
        }
        
    	@Override
        protected List<Bitmap> doInBackground(Void... params) {
        	mClient = Util.getS3Client(getActivity());
        	
        	List<S3ObjectSummary> objectSummaries = mClient.listObjects(Constants.BUCKET_NAME, member).getObjectSummaries();
        	List<Bitmap> imageList = new ArrayList<Bitmap> ();
        	String key = null;
        	S3ObjectInputStream content = null;
        	byte[] bytes = null;
        	Bitmap bitmap = null;
        	
        	
        	//get all the objects in bucket
        	for (S3ObjectSummary summary : objectSummaries) {
        	    key = summary.getKey();
        	    bitmap = null;
        	    content = mClient.getObject(Constants.BUCKET_NAME, key).getObjectContent();
        	    
    			try {
    				bytes = IOUtils.toByteArray(content);
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                mSelectedObjects.add(summary);
                
        	    imageList.add(Bitmap.createScaledBitmap(bitmap, 120, 120, true));     	    
        	}
        	
            return imageList;
        }

        @Override
        protected void onPostExecute(List<Bitmap> objects) {
            //now that we have all the keys, add them all to the adapter
            mAdapter.clear();
            mAdapter.addAll(objects);
            if (mLoadingProgressDialog.isShowing()) {
            	mLoadingProgressDialog.dismiss();
    		}
        }
    }


    /* Adapter for all the S3 objects */
    private class ObjectAdapter extends ArrayAdapter<Bitmap> {
    	private Context context;
    	private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();
    	
    	public ObjectAdapter(Context context) {
            super(context, R.layout.photo_item);
            this.context = context;
        }
    	
    	private class ViewHolder {
            ImageView photoImageView;
            CheckBox checkbox;
        }
    	
		@Override
        public View getView(final int pos, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            View viewToUse = null;
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        
            if(convertView == null) {
            	holder = new ViewHolder();
                viewToUse = mInflater.inflate(R.layout.photo_item, null);
                holder.photoImageView = (ImageView) viewToUse.findViewById(R.id.imageView);
                
                viewToUse.setTag(holder);
            } else {
            	viewToUse = convertView;
                holder = (ViewHolder) viewToUse.getTag();
            }
            
            holder.checkbox = (CheckBox) viewToUse.findViewById(R.id.itemCheckBox); 
            holder.checkbox.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (((CheckBox) v).isChecked()) {
                        mGrid.setItemChecked(pos, true);
                        mSelection.put(pos, true);
					}else{
						mGrid.setItemChecked(pos, false);
						mSelection.remove(pos);
					}
				}
			});
            
            if(checkboxFlag)holder.checkbox.setVisibility(View.VISIBLE);
            else holder.checkbox .setVisibility(View.GONE);
                      
            if (mSelection.get(pos) != null){
            	holder.checkbox.setChecked(true);
            }
            else {
            	holder.checkbox.setChecked(false);
            }
            
            
            holder.photoImageView.setImageBitmap(getItem(pos));
            
            return viewToUse;
        }
		
		public void addAll(Collection<? extends Bitmap> collection) {
            for(Bitmap obj : collection) {               
                add(obj);
            }
        }
		
		public void setNewSelection(int position, boolean value) {
            mSelection.put(position, value);
            notifyDataSetChanged();
        }
		
		public Set<Integer> getCurrentCheckedPosition() {
			return mSelection.keySet();
		}
		
        public void removeSelection(int position) {
            mSelection.remove(position);
            notifyDataSetChanged();
        }
 
        public void clearSelection() {
            mSelection = new HashMap<Integer, Boolean>();
            notifyDataSetChanged();
        }

    }
}
