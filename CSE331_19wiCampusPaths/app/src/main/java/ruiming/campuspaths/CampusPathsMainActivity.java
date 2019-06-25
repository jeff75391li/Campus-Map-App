package ruiming.campuspaths;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import hw5.Edge;
import hw8.CampusPathsModel;
import hw8.Coordinate;

import java.io.*;
import java.util.*;

/**
 * Main activity for the campus paths application.
 * It allows users to select two buildings on the campus map
 * and displays the shortest path between them.
 */
public class CampusPathsMainActivity extends AppCompatActivity {

    // Scale down all coordinates on the map
    private static final float SCALE = 0.25f;

    /* The model that stores the data of campus paths and buildings */
    private CampusPathsModel model;
    /* The drawing view of the image view */
    private DrawView view;
    /* The spinner for the starting building */
    private Spinner startBuildingSpinner;
    /* The spinner for the ending building */
    private Spinner endBuildingSpinner;
    /* The abbreviated name of the starting building */
    private String start;
    /* The abbreviated name of the ending building */
    private String end;
    /* Whether the starting building is selected */
    private boolean startSelected = false;
    /* Whether the ending building is selected */
    private boolean endSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Initialize the model
            InputStream buildingsInputStream = this.getResources()
                    .openRawResource(R.raw.campus_buildings_new);
            InputStream pathsInputStream = this.getResources()
                    .openRawResource(R.raw.campus_paths);
            model = new CampusPathsModel(buildingsInputStream, pathsInputStream);

            // Get all the widgets
            Button routeButton = (Button) findViewById(R.id.routeButton);
            Button resetButton = (Button) findViewById(R.id.resetButton);
            view = (DrawView) findViewById(R.id.imageView);
            startBuildingSpinner = (Spinner) findViewById(R.id.startBuildings);
            endBuildingSpinner = (Spinner) findViewById(R.id.endBuildings);

            routeButton.setOnClickListener(findRouteButtonClick);
            resetButton.setOnClickListener(resetButtonClick);

            view.setOnTouchListener(mapTouched);

            // Set up the spinners so that they hold the abbreviated names of all campus buildings
            setupSpinners();

            startBuildingSpinner.setOnItemSelectedListener(startBuildingSelected);
            endBuildingSpinner.setOnItemSelectedListener(endBuildingSelected);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Fills the spinners with the abbreviated names of all campus buildings.
     *
     * @spec.modifies startBuildingSpinner
     * @spec.modifies endBuildingSpinner
     * @spec.effects Fills the startBuildingSpinner with the abbreviated names
     * of all campus buildings.
     * @spec.effects Fills the endBuildingSpinner with the abbreviated names
     * of all campus buildings.
     */
    private void setupSpinners() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<String>());
        Map<String, String> shortNameToLongName = model.getAllBuildings();
        // Add empty string so that the spinners will display nothing at the beginning
        adapter.add("Select a building");
        // Add all abbreviated building names
        for (String shortName : shortNameToLongName.keySet()) {
            adapter.add(shortName);
        }
        startBuildingSpinner.setAdapter(adapter);
        endBuildingSpinner.setAdapter(adapter);
    }

    // It listens when users click on the "FIND ROUTE" button
    private View.OnClickListener findRouteButtonClick = new View.OnClickListener() {
        public void onClick(View v) {
            // Have to select both buildings before finding paths
            if (startSelected && endSelected) {
                if (!start.equals(end)) {
                    // Get the shortest path from 'start' to 'end'
                    List<Edge<Coordinate, Double>> path = model.findPath(start, end);
                    // Draw the shortest path on the map
                    view.drawPath(path);
                } else {
                    // If two selected buildings are the same, print out a warning
                    Toast.makeText(getApplicationContext(),
                            "You must select two different buildings!",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // If not all buildings are selected, print out a warning
                Toast.makeText(getApplicationContext(), "You must select both buildings!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    // It listens when users click on the "RESET" button
    private View.OnClickListener resetButtonClick = new View.OnClickListener() {
        public void onClick(View v) {
            // Reset the spinners
            startBuildingSpinner.setSelection(0);
            endBuildingSpinner.setSelection(0);
            // Reset the map, which means clearing all the markings and paths
            view.resetMap();
        }
    };

    // It listens when users select a building as the starting position
    private Spinner.OnItemSelectedListener startBuildingSelected = new Spinner
            .OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
            start = (String) startBuildingSpinner.getItemAtPosition(position);
            // If users select nothing, we clear out the one that we have drawn before
            if (start == "Select a building") {
                startSelected = false;
                // When we unselect the start point, we also unselect the endpoint
                // since we always want users to determine the start point first
                if (endSelected) {
                    view.clearEndMarking();
                    endBuildingSpinner.setSelection(0);
                    endSelected = false;
                }
                view.clearStartMarking();
            } else {
                startSelected = true;
                view.drawStartMarking(model.getCoordinate(start));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
            // do nothing
        }
    };

    // It listens when users select a building as the ending position
    private Spinner.OnItemSelectedListener endBuildingSelected = new Spinner
            .OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
            end = (String) endBuildingSpinner.getItemAtPosition(position);
            // If users select nothing, we clear out the one that we have drawn before
            if (end == "Select a building") {
                endSelected = false;
                view.clearEndMarking();
            } else if (!startSelected) {
                // We want users to select the start point first
                endBuildingSpinner.setSelection(0);
                Toast.makeText(getApplicationContext(),
                        "You have to select the start point first!",
                        Toast.LENGTH_SHORT).show();
            } else {
                endSelected = true;
                view.drawEndMarking(model.getCoordinate(end));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
            // do nothing
        }
    };

    // It listens when users touch on the map to select an endpoint
    private View.OnTouchListener mapTouched = new View.OnTouchListener() {
        // The x and y coordinate users have touched
        float x;
        float y;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // When touch down on the map, record the x and y coordinate
                    x = event.getX();
                    y = event.getY();
                case MotionEvent.ACTION_UP:
                    // Since we only allow users to select an endpoint, we must make sure
                    // they have already selected the start point.
                    if (!startSelected) {
                        Toast.makeText(getApplicationContext(),
                                "You have to select the start point first!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // When users lift up their fingers, determine the building they touched
                        Map<String, String> shortToLong = model.getAllBuildings();
                        int pos = 1;
                        // Check each building to see if the touched point is within the range of the building
                        for (String bldShortName : shortToLong.keySet()) {
                            Coordinate coord = model.getCoordinate(bldShortName);
                            if (x < coord.getX() * SCALE + 15f && x > coord.getX() * SCALE - 15f
                                    && y < coord.getY() * SCALE + 15f && y > coord.getY() * SCALE - 15f) {
                                // Let the selection of the spinner match what they touched
                                endBuildingSpinner.setSelection(pos);
                                endSelected = true;
                                view.drawEndMarking(coord);
                                break;
                            }
                            pos++;
                        }
                    }
                    break;
            }
            return false;
        }
    };
}
