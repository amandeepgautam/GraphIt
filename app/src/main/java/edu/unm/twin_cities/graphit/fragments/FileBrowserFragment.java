package edu.unm.twin_cities.graphit.fragments;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import edu.umn.twin_cities.FileAdapter;
import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.activity.DrawerActivity;
import edu.unm.twin_cities.graphit.application.GraphItApplication;
import edu.unm.twin_cities.graphit.util.ConnectionResourceBundle;
import edu.unm.twin_cities.graphit.util.ImageLoader;
import edu.unm.twin_cities.graphit.util.RemoteConnectionResourceManager;
import edu.unm.twin_cities.graphit.util.ServerActionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

public class FileBrowserFragment extends Fragment {

    public static final String PARAM_FILE_PATH = "file_path";

    private static final String PARAM_CURR_FILE_PATH = "curr_file_path";
    private static final String PARAM_FOLDER_NAVIGATION = "folder_navigation";
    private static final String PARAM_FOLDER_NAVIGATION_SELETION = "folder_navigation_selection";
    private static final String PARAM_CACHE = "cache";


    private final String TAG = this.getClass().getSimpleName();
    private final int CACHE_SIZE = 10;

    private View fragmentView;

    private DrawerActivity parentActivity;

    private ServerActionUtil serverActionUtil = null;

    private FileArrayAdapter fileArrayAdapter;

    private LoadingCache<String, List<FileInfo>> browserCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .build(
                    new CacheLoader<String, List<FileInfo>>() {
                        @Override
                        public List<FileInfo> load(String key) throws Exception {
                            return serverActionUtil.listFiles(key);
                        }
                    }
            );

    private HorizontalRecyclerViewAdapter horizontalRecyclerViewAdapter;
    private RecyclerView recyclerView;

    private String currPath;

    public FileBrowserFragment() {
        setArguments(new Bundle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_file_browser, container, false);

        parentActivity = (DrawerActivity) getActivity();
        parentActivity.setTitle(R.string.file_browser_fragment_title);

        Bundle bundle = getArguments();
        final BluetoothDevice bluetoothDevice = (BluetoothDevice) bundle.getParcelable(AddSensorFragment.PARAM_BLUETOOTH_DEVICE);

        try {
            GraphItApplication application = ((GraphItApplication)parentActivity.getApplicationContext());
            RemoteConnectionResourceManager connectionManager = application.getConnectionManager();

            ConnectionResourceBundle connectionResourceBundle = connectionManager.getConnectionResource(bluetoothDevice, parentActivity);

            serverActionUtil = new ServerActionUtil(connectionResourceBundle);

            ListView listView = (ListView) fragmentView.findViewById(R.id.files);
            fileArrayAdapter = new FileArrayAdapter(parentActivity, R.layout.resource_info, new ArrayList<FileInfo>());
            fileArrayAdapter.setNotifyOnChange(true);
            listView.setAdapter(fileArrayAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View item,
                                        int position, long id) {
                    try {
                        FileInfo file = (FileInfo) parent.getItemAtPosition(position);
                        if (file.getResourceType() == FileAdapter.ResourceType.DIRECTORY) {
                            String path = file.getPath();
                            currPath = path;
                            List<FileInfo> files = browserCache.get(path);
                            fileArrayAdapter.clear();
                            fileArrayAdapter.addAll(files);

                            boolean isItemAdded = horizontalRecyclerViewAdapter.addItem(path);
                            if (isItemAdded) {
                                horizontalRecyclerViewAdapter.notifyItemInserted(
                                        horizontalRecyclerViewAdapter.getItemCount() - 1);
                            }

                            recyclerView.smoothScrollToPosition(
                                    horizontalRecyclerViewAdapter.getSelectedPos());

                        } else if (file.getResourceType() == FileAdapter.ResourceType.FILE) {
                            String path = file.getPath();
                            Bundle bundle = new Bundle();
                            bundle.putString(PARAM_FILE_PATH, path);
                            parentActivity.loadFragment(DrawerActivity.Fragments.ADD_SENSOR, bundle, true);
                        } else if (file.getResourceType() == FileAdapter.ResourceType.UNKNOWN) {
                            //TODO: DO error handling. Send some notifications etc.
                            Log.e(TAG, "File type unknown. Possibly accessing non accessible parts of the system.");
                        }
                    } catch (ExecutionException ee) {
                        Log.e(TAG, "Exception while accessing cache.", ee);
                    } catch (Exception e) { //java.io.IOException
                        //TODO: DO error handling.
                        Log.e(TAG, "We go an exception", e);
                    }
                }
            });

            LinearLayoutManager layoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(parentActivity, LinearLayoutManager.HORIZONTAL, false);
            recyclerView = (RecyclerView) fragmentView.findViewById(R.id.selectable_folder_path);
            recyclerView.setLayoutManager(layoutManager);

            recyclerView.addItemDecoration(new DividerItemDecoration(parentActivity, 0));

            horizontalRecyclerViewAdapter =
                    new HorizontalRecyclerViewAdapter(new ArrayList<String>());

            recyclerView.setAdapter(horizontalRecyclerViewAdapter);
        } catch (Exception e) {
            //TODO: handle exception properly.
            Log.e(TAG, "We go an exception", e);
        }

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(PARAM_CURR_FILE_PATH, currPath);

        List<String> folderNavigation = horizontalRecyclerViewAdapter.getItemList();
        outState.putStringArray(PARAM_FOLDER_NAVIGATION, folderNavigation.toArray(new String[folderNavigation.size()]));
        outState.putInt(PARAM_FOLDER_NAVIGATION_SELETION, horizontalRecyclerViewAdapter.getSelectedPos());

        List<FileInfo> files = fileArrayAdapter.getRows();
        Map<String, List<FileInfo>> cache = browserCache.asMap();

        Bundle cacheBundle = new Bundle();
        for (Map.Entry<String, List<FileInfo>> entry : cache.entrySet()) {
            List<FileInfo> list = entry.getValue();
            cacheBundle.putParcelableArray(entry.getKey(), list.toArray(new FileInfo[list.size()]));
        }

        outState.putBundle(PARAM_CACHE, cacheBundle);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Send a empty currPath. Server should return the default accessible location.
        if (savedInstanceState == null) {
            currPath = "/";
            horizontalRecyclerViewAdapter.addItem(currPath);
            horizontalRecyclerViewAdapter.notifyItemInserted(
                    horizontalRecyclerViewAdapter.getItemCount() - 1);
            try {
                List<FileInfo> files = browserCache.get(currPath);
                fileArrayAdapter.addAll(files);
            } catch(ExecutionException ee) {
                Log.e(TAG, "Exception while accessing cache.", ee);
            }
        } else {
            currPath = savedInstanceState.getString(PARAM_CURR_FILE_PATH);
            String[] folderNavigation = savedInstanceState.getStringArray(PARAM_FOLDER_NAVIGATION);
            int selectedPosition = savedInstanceState.getInt(PARAM_FOLDER_NAVIGATION_SELETION);

            //folderNavigation can never be null. ignore warning.
            horizontalRecyclerViewAdapter.setItemList(Arrays.asList(folderNavigation));
            horizontalRecyclerViewAdapter.setSelectedPos(selectedPosition);

            Bundle cacheBundle = savedInstanceState.getBundle(PARAM_CACHE);
            Map<String, List<FileInfo>> fileNavigationCache = Maps.newHashMap();
            for(String key : cacheBundle.keySet()) {    //ignore warning, would never be null.
                FileInfo[] value = (FileInfo[]) cacheBundle.getParcelableArray(key);
                fileNavigationCache.put(key, Arrays.asList(value));
            }
            browserCache.putAll(fileNavigationCache);

            fileArrayAdapter.addAll(fileNavigationCache.get(currPath));
        }
    }

    @Data
    @AllArgsConstructor(suppressConstructorProperties = true)
    private static class ViewHolder {
        /**
         * Icon for differentiating a file or a folder.
         */
        ImageView icon;
        /**
         * Name of the resource.
         */
        TextView name;
        /**
         * Last modified date, from the OS.
         */
        TextView lastModified;
    }

    @Data
    private class HorizontalRecyclerViewAdapter extends RecyclerView.Adapter<
            HorizontalRecyclerViewAdapter.HorizontalTextViewDisplayHolder> {

        private List<String> itemList;
        private int selectedPos = -1;

        public HorizontalRecyclerViewAdapter(List<String> itemList) {
            if (itemList == null) {
                throw new IllegalArgumentException("List of items should not be null.");
            }
            this.itemList = itemList;
        }

        public boolean addItem (String item) {
            boolean isItemAdded = true;
            if (selectedPos == itemList.size() -1) {
                itemList.add(item);
            } else {
                String clickedFolderName = getFolderName(item);
                String prevFolderName = getFolderName(itemList.get(selectedPos+1));
                if (!clickedFolderName.equals(prevFolderName)) {
                    itemList.subList(selectedPos+1, itemList.size()).clear();
                    notifyItemRangeRemoved(selectedPos + 2, itemList.size() - selectedPos - 1);
                    itemList.add(item);
                    notifyItemRangeChanged(selectedPos + 1, itemList.size());
                } else {
                    isItemAdded = false;
                }
            }
            ++selectedPos;
            return isItemAdded;
        }

        @Override
        public HorizontalTextViewDisplayHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.horizontal_view, parent, false);
            return new HorizontalTextViewDisplayHolder(itemView);
        }

        @Override
        public void onBindViewHolder(HorizontalTextViewDisplayHolder holder, int position) {
            String item = itemList.get(position);
            String folderName = getFolderName(item);

            holder.getTextView().setText(folderName + "    ");
        }

        private String getFolderName(String path) {
            File file = new File(path);
            String folderName = file.getName();
            if(TextUtils.isEmpty(folderName)) {
                folderName = "Root Dir";
            }
            return folderName;
        }


        @Override
        public int getItemCount() {
            return itemList.size();
        }

        @Data
        protected class HorizontalTextViewDisplayHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            TextView textView;

            public HorizontalTextViewDisplayHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                textView = (TextView) itemView.findViewById(R.id.folder_name);
            }

            @Override
            public void onClick(View view) {
                selectedPos = getAdapterPosition();
                String path = itemList.get(selectedPos);

                try {
                    List<FileInfo> files = browserCache.get(path);
                    fileArrayAdapter.clear();
                    fileArrayAdapter.addAll(files);
                } catch (ExecutionException ee) {
                    //TODO:
                    Log.e(TAG, "We got exception", ee);
                }
            }
        }
    }


    private class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        private Drawable mDivider;

        private int type;

        public DividerItemDecoration(Context context, int type) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
            setOrientation(type);
        }

        private void setOrientation(int type) {
            this.type = type;   //draw a triangular by default.
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            if (type == 0) {
                drawAngularSeparator(c, parent);
            }
        }

        private void drawAngularSeparator(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicHeight();

                Paint paint = new Paint();

                paint.setStrokeWidth(8);
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setAntiAlias(true);

                Point a = new Point(top, left);
                Point b = new Point((top+bottom)/2, right);
                Point cPoint = new Point(bottom, left);

                Path path = new Path();
                path.setFillType(Path.FillType.EVEN_ODD);
                path.moveTo(a.x, a.y);
                path.lineTo(b.x, b.y);
                path.moveTo(b.x, b.y);
                path.lineTo(cPoint.x, cPoint.y);
                path.close();

                c.drawPath(path, paint);
            }
        }
    }

    /**
     * This class is the exact replica of FileAdapter and is most likely to remain so.
     * The reason it exists is that there is no way to implement parcelable interface
     * for FileAdapter, and use of serialization is very expensive for use in functions
     * related to onSaveInstanceState.
     */
    @Getter
    @AllArgsConstructor(suppressConstructorProperties = true)
    public static final class FileInfo implements Parcelable {

        public final static String PARAM_NAME = "name";
        public final static String PARAM_PATH = "path";
        public final static String PARAM_LAST_MODIFIED = "last_modified";
        public final static String PARAM_RESOURCE_TYPE = "resource_type";

        private String name;
        private String path;
        private long lastModified;
        private FileAdapter.ResourceType resourceType;

        public FileInfo(FileAdapter fileAdapter) {
            name = fileAdapter.getName();
            path = fileAdapter.getPath();
            lastModified = fileAdapter.getLastModified();
            resourceType = fileAdapter.getResourceType();
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            Bundle bundle = new Bundle();

            bundle.putString(PARAM_NAME, name);
            bundle.putString(PARAM_PATH, path);
            bundle.putLong(PARAM_LAST_MODIFIED, lastModified);
            bundle.putString(PARAM_RESOURCE_TYPE, resourceType.name());

            parcel.writeBundle(bundle);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Parcelable.Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
            @Override
            public FileInfo createFromParcel(Parcel in) {
                Bundle bundle = in.readBundle();

                String name = bundle.getString(PARAM_NAME);
                String path = bundle.getString(PARAM_PATH);
                Long lastModified = bundle.getLong(PARAM_LAST_MODIFIED);
                FileAdapter.ResourceType resourceType = FileAdapter.ResourceType
                        .valueOf(bundle.getString(PARAM_RESOURCE_TYPE));

                return new FileInfo(name, path, lastModified, resourceType);
            }

            @Override
            public FileInfo[] newArray(int size) {
                return new FileInfo[size];
            }
        };

    }

    /**
     * A custom adapter to browse files from other device.
     */
    @Data
    private class FileArrayAdapter extends ArrayAdapter<FileInfo> {

        private List<FileInfo> rows;

        FileArrayAdapter(Context context, int resourceId,
                         List<FileInfo> rows) {
            super(context, resourceId, rows);
            this.rows = rows;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView resourceDescriptorImage;
            TextView resourceName;
            TextView lastModified;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater)parentActivity.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.resource_info, parent, false);
                resourceDescriptorImage = (ImageView) convertView.findViewById(R.id.image_descriptor_resource);
                resourceName = (TextView) convertView.findViewById(R.id.name_resource);
                lastModified = (TextView) convertView.findViewById(R.id.resource_last_modified);

                ViewHolder viewHolder = new ViewHolder(resourceDescriptorImage,
                        resourceName, lastModified);
                convertView.setTag(viewHolder);   //optimization: use in future.
            } else {
                ViewHolder viewHolder = (ViewHolder) convertView.getTag();
                resourceDescriptorImage = viewHolder.getIcon();
                resourceName = viewHolder.getName();
                lastModified = viewHolder.getLastModified();
            }

            //change the data to reflect the new row.
            FileInfo file = rows.get(position);
            resourceName.setText(file.getName());
            SimpleDateFormat simpleDateFormat= new SimpleDateFormat("MMM dd, yyyy");
            lastModified.setText(getString(R.string.last_modified_text) + simpleDateFormat.format(new Date(file.getLastModified())));
            resourceDescriptorImage.setImageResource(ImageLoader.getFileTypeIcon(file.getResourceType()));
            return convertView;
        }
    }
}
