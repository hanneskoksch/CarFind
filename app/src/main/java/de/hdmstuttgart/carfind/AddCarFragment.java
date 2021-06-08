package de.hdmstuttgart.carfind;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddCarFragment extends Fragment {

    private AppDatabase database;

    private Context context;
    private View view;

    private EditText editCarModel, editLicencePlate, editLevel, editSpot, editAnnotation;

    private ImageView imageView, cameraIcon;
    private String currentPhotoPath;
    private String backUpPhotoPath;

    private LinearLayout locationFrame;
    private TextView locationTextView;
    private String longitude, latitude;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private boolean editMode = false;
    private Car car;
    private int uid;

    // permission codes
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int LOCATION_PERM_CODE = 105;


    public AddCarFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_add_car, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        this.view = view;
        this.context = view.getContext();

        editCarModel = (EditText) view.findViewById(R.id.carModel);
        editLicencePlate = (EditText) view.findViewById(R.id.licence_plate);
        editLevel = (EditText) view.findViewById(R.id.level);
        editSpot = (EditText) view.findViewById(R.id.spot);
        editAnnotation = (EditText) view.findViewById(R.id.annotation);

        imageView = view.findViewById(R.id.imageView);
        cameraIcon = view.findViewById(R.id.cameraIcon);

        locationFrame = view.findViewById(R.id.locationFrame);
        locationTextView = view.findViewById(R.id.location);

        database = Room.databaseBuilder(context,
                AppDatabase.class, "carDb")
                .allowMainThreadQueries()
                .build();

        uid = getActivity().getIntent().getIntExtra("UID", 0);

        // check how to interpret the start intent
        if (uid == 0) {

            // --> Add car
            getActivity().setTitle(R.string.add_car_activity_name);

            // asking for location permission to enable localization while editing the inputs
            askLocationPermission();

            cameraIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    askCameraPermission();
                }
            });

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    askCameraPermission();
                }
            });

        } else {

            // --> Edit car
            getActivity().setTitle(R.string.edit_car_activity_name);


            car = database.carDao().getPrimaryKey(uid);
            currentPhotoPath = car.filepath;

            // Set data to editTexts and make them not focusable until user presses editButton in options menu
            editCarModel.setText(car.carModel);
            editCarModel.setFocusable(false);
            editLicencePlate.setText(car.licencePlate);
            editLicencePlate.setFocusable(false);
            editLevel.setText(car.level);
            editLevel.setFocusable(false);
            editSpot.setText(car.spot);
            editSpot.setFocusable(false);
            editAnnotation.setText(car.annotation);
            editAnnotation.setFocusable(false);

            // hide progress animation
            view.findViewById(R.id.progress).setVisibility(View.GONE);

            if (car.longitude != null){
                view.findViewById(R.id.locationIcon).setVisibility(View.VISIBLE);
                locationTextView.setText(getString(R.string.start_navigation));

                locationFrame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMap(Uri.parse("geo:" + car.latitude + "," + car.longitude + "?q=" + car.latitude + "," + car.longitude + "(" + car.carModel + ")"));
                    }
                });

            } else {
                view.findViewById(R.id.noLocation).setVisibility(View.VISIBLE);
                locationTextView.setText(getString(R.string.no_location));
            }

            if (car.filepath == null){
                view.findViewById(R.id.imageView).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.cameraIcon).setVisibility(View.VISIBLE);

            } else {
                view.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                view.findViewById(R.id.cameraIcon).setVisibility(View.INVISIBLE);

                // This if-case is necessary to make image loading from sample data possible.
                // Could be removed if no sample data should be added.
                // Since loading from @drawable, there's no orientation check needed
                if (!car.filepath.contains("drawable")){

                    // display image with correct orientation
                    ExifInterface exif = null;
                    try {
                        exif = new ExifInterface(currentPhotoPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    switch(orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            Glide.with(this)
                                    .load(currentPhotoPath)
                                    .transform(new RotateTransformation(90f))
                                    .centerCrop()
                                    .into(imageView);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            Glide.with(this)
                                    .load(currentPhotoPath)
                                    .transform(new RotateTransformation(180f))
                                    .centerCrop()
                                    .into(imageView);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            Glide.with(this)
                                    .load(currentPhotoPath)
                                    .transform(new RotateTransformation(270f))
                                    .centerCrop()
                                    .into(imageView);
                            break;
                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            Glide.with(this)
                                    .load(currentPhotoPath)
                                    .centerCrop()
                                    .into(imageView);
                    }
                } else {
                    int imageResource = getResources().getIdentifier(car.filepath, null, getActivity().getPackageName());
                    Drawable image = ContextCompat.getDrawable(context, imageResource);
                    Glide.with(context)
                            .load(image)
                            .centerCrop()
                            .into(imageView);
                }
            }

        }
    }


    /**
     * Starts location intent with given location data
     * @param geoLocation location of the intent
     */
    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        startActivity(intent);
    }


    /**
     * Gets all the input from the editTexts and the filepath to the image and stores it as car object in the database.
     */
    private void safeCar() {
        String carModel = editCarModel.getText().toString();
        String licencePlate = editLicencePlate.getText().toString();
        String level = editLevel.getText().toString();
        String spot = editSpot.getText().toString();
        String annotation = editAnnotation.getText().toString();

        // check if required fields are filled out
        if (checkUserEntry(carModel,licencePlate,spot)) {
            if (uid == 0) {
                Car newCar = new Car(carModel, licencePlate, level, spot, annotation, longitude, latitude, currentPhotoPath);
                database.carDao().insert(newCar);
            } else {
                car.carModel = carModel;
                car.licencePlate = licencePlate;
                car.level = level;
                car.spot = spot;
                car.annotation = annotation;

                // delete old image that was saved in database
                try {
                    new File(car.filepath).delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                car.filepath = currentPhotoPath;

                database.carDao().update(car);
            }
            getActivity().finish();
        } else {
            Toast.makeText(context, getActivity().getString(R.string.error_not_filled_out), Toast.LENGTH_LONG).show();
        }
    }

    // checks if the given strings are not empty
    boolean checkUserEntry(String model, String licence, String spot){
        if (model.equals("") || licence.equals("") || spot.equals("")){
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.edit:
                startEdit();
                break;

            case android.R.id.home:
                getActivity().finish();
                break;

            case R.id.save:
                safeCar();
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Turns editText to focusable and sets onclicklisteners to enable editing.
     */
    private void startEdit() {
        editMode = true;
        editCarModel.setFocusableInTouchMode(true);
        editLicencePlate.setFocusableInTouchMode(true);
        editLevel.setFocusableInTouchMode(true);
        editSpot.setFocusableInTouchMode(true);
        editAnnotation.setFocusableInTouchMode(true);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermission();
            }
        });

        view.findViewById(R.id.cameraIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermission();
            }
        });
        getActivity().invalidateOptionsMenu();
    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        if(uid == 0){
            menu.findItem(R.id.save).setVisible(true);
            menu.findItem(R.id.edit).setVisible(false);
        } else {
            if (editMode) {
                // replace edit icon by safe icon
                menu.findItem(R.id.save).setVisible(true);
                menu.findItem(R.id.edit).setVisible(false);
            }
        }
    }


    private void  askLocationPermission() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERM_CODE);
        } else {
            getLocation();
        }
    }

    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    /**
     * Checks if the user has granted necessary permissions and if so, starts the corresponding methods
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERM_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(context, getActivity().getString(R.string.toast_missing_camera_permission), Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == LOCATION_PERM_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                view.findViewById(R.id.progress).setVisibility(View.GONE);
                view.findViewById(R.id.noLocation).setVisibility(View.VISIBLE);
                locationTextView.setText(getActivity().getString(R.string.no_location));

                // provide possibility to once again aks for location permission
                locationFrame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        askLocationPermission();
                    }
                });

            }
        }
    }


    @SuppressLint("MissingPermission") // gets only called, when permission was granted
    private void getLocation() {
        locationManager = (LocationManager) context.getSystemService(Activity.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    /**
     * Gets called after the Camera-Intent has taken a photo and displays it in the appropriate ImageView depending on its orientation
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == Activity.RESULT_CANCELED) {
            // delete empty file
            try {
                new File(currentPhotoPath).delete();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (car != null){
                try {
                    if(new File(backUpPhotoPath).length() != 0){
                        //set old photo if its not deleted
                        currentPhotoPath = backUpPhotoPath;
                    } else {
                        // set old filepath if it is not a new car
                        currentPhotoPath = car.filepath;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (backUpPhotoPath != null){
                    // set old filepath if it is a new car
                    currentPhotoPath = backUpPhotoPath;
                } else {
                    // no filepath needed
                    currentPhotoPath = null;
                }
            }
        } else {

            if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                imageView.setVisibility(View.VISIBLE);
                view.findViewById(R.id.cameraIcon).setVisibility(View.GONE);

                if(car != null){
                    // if car has already been saved and previous picture wont be needed,
                    // but only if previous picture is not the same as saved in database
                    if (backUpPhotoPath != null && !backUpPhotoPath.equals(car.filepath)){
                        try {
                            new File(backUpPhotoPath).delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // first saving of the car and previous picture won't be needed
                    if (backUpPhotoPath != null ){
                        try {
                            new File(backUpPhotoPath).delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(currentPhotoPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        Glide.with(this)
                                .load(currentPhotoPath)
                                .transform(new RotateTransformation(90f))
                                .fitCenter()
                                .into(imageView);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        Glide.with(this)
                                .load(currentPhotoPath)
                                .transform(new RotateTransformation(180f))
                                .fitCenter()
                                .into(imageView);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        Glide.with(this)
                                .load(currentPhotoPath)
                                .transform(new RotateTransformation(270f))
                                .fitCenter()
                                .into(imageView);
                        break;
                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        Glide.with(this)
                                .load(currentPhotoPath)
                                .fitCenter()
                                .into(imageView);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Method creates file with unique name for a new photo using a date-timestamp.
     * @return file to save the image in
     * @throws IOException Error occurred while creating the File
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        MediaStore.Images a = new MediaStore.Images();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save the path of the previous image for later usage
        if (currentPhotoPath != null) {
            backUpPhotoPath = currentPhotoPath;
        }

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    /**
     * Create and invoke intent to take a picture.
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context,
                        "de.hdmstuttgart.carfind.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onDestroy() {
        // prevent the locationListener from giving updates to a closed activity
        if (locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
        super.onDestroy();
    }




    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            view.findViewById(R.id.progress).setVisibility(View.GONE);
            view.findViewById(R.id.locationIcon).setVisibility(View.VISIBLE);

            // refreshes the current location until onDestroy gets called
            longitude = String.valueOf(location.getLongitude());
            latitude = String.valueOf(location.getLatitude());

            locationTextView.setText(getActivity().getString(R.string.location_found));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }
    }
}