package gr.forth.ics.urbanNet.json;

import gr.forth.ics.urbanNet.database.Feedback;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * {@link JsonSerializer} used to serialize {@link Feedback} objects in JSON format, in order to send them to the urbanNet server.
 * @author katsarakis
 */
public class FeedbackJsonSerializer implements JsonSerializer<Feedback> {

    @Override
    public JsonElement serialize(Feedback arg0, Type arg1, JsonSerializationContext arg2) {
	JsonObject root = new JsonObject();
	root.addProperty("id", arg0.getId());
	root.addProperty("image", arg0.getImageString());
	root.addProperty("score", arg0.getScore());
	root.addProperty("timestamp", arg0.getTimestamp());
	root.addProperty("longitude", arg0.getLongitude());
	root.addProperty("latitude", arg0.getLatitude());
	root.addProperty("altitude", arg0.getAltitude());
	root.addProperty("accuracy", arg0.getAccuracy());
	root.addProperty("water_type", arg0.getWaterType());
	root.addProperty("complaints", arg0.getComplaints());
	root.addProperty("red", toInt(arg0.isRed()));
	root.addProperty("green", toInt(arg0.isGreen()));
	root.addProperty("black", toInt(arg0.isBlack()));
	root.addProperty("bitter", toInt(arg0.isBitter()));
	root.addProperty("sweet", toInt(arg0.isSweet()));
	root.addProperty("salty", toInt(arg0.isSalty()));
	root.addProperty("chlorine", toInt(arg0.isChlorine()));
	root.addProperty("chemical", toInt(arg0.isChemical()));
	root.addProperty("gasoline", toInt(arg0.isGasoline()));
	root.addProperty("sewer", toInt(arg0.isSewer()));
	root.addProperty("l_pressure", toInt(arg0.isLpressure()));
	root.addProperty("n_pressure", toInt(arg0.isNpressure()));
	root.addProperty("h_pressure", toInt(arg0.isHpressure()));
	root.addProperty("f_particles", toInt(arg0.isFparticles()));
	root.addProperty("sand", toInt(arg0.isSand()));
	root.addProperty("milky", toInt(arg0.isMilky()));
	root.addProperty("rusty", toInt(arg0.isRusty()));
	root.addProperty("animal", toInt(arg0.isAnimal()));
	root.addProperty("plant", toInt(arg0.isPlant()));
	root.addProperty("water_name", arg0.getWatername());
	return root;
    }

    private int toInt(boolean b) {
	return b ? 1 : 0;
    }
}
