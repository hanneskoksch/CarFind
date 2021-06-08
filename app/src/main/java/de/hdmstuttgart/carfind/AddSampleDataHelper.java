package de.hdmstuttgart.carfind;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Helper class for the purpose of creating sample data to add to the list. Only for demonstration purposes.
 */
public class AddSampleDataHelper {

    private AppDatabase database;

    public AddSampleDataHelper(AppDatabase database) {
        this.database = database;
    }

    public void createSampleData() {
        List<String> carNames = getNames();

        carNames.forEach(name -> {
            String filename = name.replace(' ', '_').replace('-', '_').toLowerCase();
            Car car = new Car(name, createLicencePlate(), createLevel(), createSpot(),"",null,null, "drawable/" + filename);
            database.carDao().insert(car);
        });
    }

    public List<String> getNames() {
        List<String> carNames = new ArrayList<String>();
        carNames.add("Honda Civic");
        carNames.add("Nissan Micra");
        carNames.add("Ford Fiesta");
        carNames.add("VW Golf");
        carNames.add("VW Passat");
        carNames.add("Ford Focus");
        carNames.add("BMW i3");
        carNames.add("Tesla Model S");
        carNames.add("Tesla Model 3");
        carNames.add("Tesla Model X");
        carNames.add("VW Polo");
        carNames.add("Audi A3");
        carNames.add("Audi A4");
        carNames.add("VW Golf Variant");
        carNames.add("Opel Corsa");
        carNames.add("Audi A5");
        carNames.add("Mazda 6");
        carNames.add("Lexus NX");
        carNames.add("Lexus RX");
        carNames.add("Mazda 3");
        carNames.add("Mercedes A-Klasse");
        carNames.add("Mercedes B-Klasse");
        carNames.add("Mercedes-Maybach S 560");
        carNames.add("Dacia Duster");
        carNames.add("Dacia Dokker");
        carNames.add("Dacia Logan");
        return carNames;
    }

    public String createLicencePlate() {
        String licencePlate = "";
        Random rnd = new Random();
        for (int i = 0; i < 2; i++) {
            licencePlate += (char) ('a' + rnd.nextInt(26));
        }
        licencePlate += " ";
        licencePlate += (char) ('a' + rnd.nextInt(26));
        licencePlate += rnd.nextInt(999);
        return licencePlate;
    }

    public String createSpot() {
        Random rnd = new Random();
        return String.valueOf(rnd.nextInt(999));
    }

    public String createLevel() {
        Random rnd = new Random();
        if (rnd.nextBoolean()){
            return String.valueOf(rnd.nextInt(12));
        } else {
            return "";
        }
    }

}
