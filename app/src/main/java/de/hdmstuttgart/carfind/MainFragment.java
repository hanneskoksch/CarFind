package de.hdmstuttgart.carfind;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    RVAdapter rvAdapter;
    AppDatabase database;
    RecyclerView recyclerView;

    List<Car> carlist = new ArrayList<>();


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    // Callback to register swiped items in recycler view list
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition();

            switch (direction){
                case ItemTouchHelper.LEFT:
                    // delete swiped car
                    Car deletedCar = carlist.get(position);
                    database.carDao().delete(deletedCar);
                    carlist.remove(deletedCar);
                    rvAdapter.notifyItemRemoved(position);
                    Snackbar snackbar = Snackbar.make(recyclerView, deletedCar.carModel, Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.delete_undo), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // restore deleted car
                                    database.carDao().insert(deletedCar);
                                    carlist.add(position, deletedCar);
                                    rvAdapter.notifyItemInserted(position);
                                }
                            });
                    snackbar.show();
                    snackbar.addCallback(new Snackbar.Callback() {

                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            // also delete the image after assuring the car will not be restored
                            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                try {
                                    File file  = new File(deletedCar.filepath);
                                    file.delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onShown(Snackbar snackbar) {
                        }
                    });

                    break;
                case ItemTouchHelper.RIGHT:
                    break;
            }
        }


        /**
         * Method draws rectangle from the right side of the list/the screen to the right side of the swiped item.
         */
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                // Get RecyclerView item from the ViewHolder
                View itemView = viewHolder.itemView;

                Paint p = new Paint();
                if (dX > 0) {
                    // yet not used case for right swipe

                    // Draw Rect with varying right side, equal to displacement dX
                    c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                            (float) itemView.getBottom(), p);
                } else {
                    // set color for negative displacement
                    TypedValue typedValue = new TypedValue();
                    // set background color of swiped item to secondary color
                    getContext().getTheme().resolveAttribute(R.attr.colorSecondary, typedValue, true);
                    int color = typedValue.data;
                    p.setColor(color);

                    // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                            (float) itemView.getRight(), (float) itemView.getBottom(), p);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        reloadList();
    }



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = Room.databaseBuilder(getContext().getApplicationContext(),
                AppDatabase.class, "carDb")
                .allowMainThreadQueries()
                .build();

        carlist = database.carDao().getAll();

        recyclerView = view.findViewById(R.id.homeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        rvAdapter = new RVAdapter();
        recyclerView.setAdapter(rvAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> implements Filterable {


        @Override
        public RVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_itemlist, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RVAdapter.ViewHolder holder, int position) {
            holder.showCar(carlist.get(position));
        }

        @Override
        public int getItemCount() {
            return carlist.size();
        }

        @Override
        public Filter getFilter() {
            return filter;
        }

        private Filter filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Car> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(database.carDao().getAll());
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    database.carDao().getAll().forEach(car -> {
                        if (car.carModel.toLowerCase().contains(filterPattern) || car.licencePlate.toLowerCase().contains(filterPattern)) {
                            filteredList.add(car);
                        }
                    });
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                carlist.clear();
                carlist.addAll((List)results.values);
                notifyDataSetChanged();
            }
        };

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView carModel, licencePlate, level, spot, annotation;
            public ImageView thumbnail;
            public Car car;
            public Context context;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.context = itemView.getContext();
                this.carModel = itemView.findViewById(R.id.carModel);
                this.licencePlate = itemView.findViewById(R.id.licence_plate);
                this.level = itemView.findViewById(R.id.level);
                this.spot = itemView.findViewById(R.id.spot);
                this.thumbnail = itemView.findViewById(R.id.thumbnail);


                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), AddCarActivity.class);
                        intent.putExtra("UID", car.uid);
                        startActivity(intent);
                    }
                });
            }

            /**
             * Publishes current car data to the labels
             * @param car the current car of the rv-adapter
             */
            public void showCar(Car car) {
                this.car = car;

                carModel.setText(car.carModel);
                licencePlate.setText(car.licencePlate);

                level.setText(getString(R.string.level_label));
                level.append(": " + car.level);
                spot.setText(getString(R.string.parking_spot_label));
                spot.append(": " + car.spot);

                // don't show level label and pipe if there is no level saved
                if (car.level.equals("")){
                    level.setVisibility(View.GONE);
                    itemView.findViewById(R.id.pipe).setVisibility(View.GONE);
                }

                // set image thumbnail or if there is no image, set default icon
                if (car.filepath != null) {
                    if (!car.filepath.contains("drawable")) {
                        try {
                            // display image with correct orientation
                            ExifInterface exif = null;
                            try {
                                exif = new ExifInterface(car.filepath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED);

                            switch(orientation) {
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    Glide.with(context)
                                            .load(car.filepath)
                                            .transform(new RotateTransformation(90f))
                                            .fitCenter()
                                            .into(thumbnail);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    Glide.with(context)
                                            .load(car.filepath)
                                            .transform(new RotateTransformation(180f))
                                            .fitCenter()
                                            .into(thumbnail);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    Glide.with(context)
                                            .load(car.filepath)
                                            .transform(new RotateTransformation(270f))
                                            .fitCenter()
                                            .into(thumbnail);
                                    break;
                                case ExifInterface.ORIENTATION_NORMAL:
                                default:
                                    Glide.with(context)
                                            .load(car.filepath)
                                            .fitCenter()
                                            .into(thumbnail);
                            }
                        } catch (Exception e) {
                            thumbnail.setImageResource(R.drawable.ic_baseline_directions_car_24);
                            e.printStackTrace();
                        }
                    } else {
                        int imageResource = getResources().getIdentifier(car.filepath, null, getActivity().getPackageName());
                        Drawable image = ContextCompat.getDrawable(context, imageResource);
                        Glide.with(context)
                                .load(image)
                                .fitCenter()
                                .into(thumbnail);
                    }
                } else {
                    thumbnail.setImageResource(R.drawable.ic_baseline_directions_car_24);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
               @Override
               public boolean onMenuItemActionExpand(MenuItem item) {
                   // focus search widget on expand
                   searchView.onActionViewExpanded();
                   return true;
               }

               @Override
               public boolean onMenuItemActionCollapse(MenuItem item) {
                   // reset carlist after closing search widget
                   reloadList();

                   // hide keyboard after closing search widget
                   InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                   imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                   return true;
               }
           });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                rvAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sample_data) {
            AddSampleDataHelper helper = new AddSampleDataHelper(database);
            helper.createSampleData();
            reloadList();
            return true;
        }

        if (id == R.id.action_clear_list) {
            carlist.forEach(car -> {
                try {
                    new File(car.filepath).delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            database.carDao().delete();
            carlist.clear();
            rvAdapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void reloadList() {
        carlist.clear();
        carlist.addAll(database.carDao().getAll());
        rvAdapter.notifyDataSetChanged();
    }
}