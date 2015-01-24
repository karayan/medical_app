package gr.forth.ics.urbanNet.json;


import gr.forth.ics.urbanNet.database.BatteryMeasurement;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BatteryMeasurementJsonSerializer implements JsonSerializer<BatteryMeasurement> {

    @Override
    public JsonElement serialize(BatteryMeasurement arg0, Type arg1, JsonSerializationContext arg2) {
	JsonObject root = new JsonObject();
	root.addProperty("id", arg0.getId());
	root.addProperty("charging", arg0.getCharging());
	root.addProperty("inSession", arg0.getInSession());
	root.addProperty("newMonRate", arg0.getNewMonitore());
	root.addProperty("oldMonRate", arg0.getOldMonitore());
	root.addProperty("timestamp", arg0.getTimestamp());
	root.addProperty("velocity", arg0.getVelocityEstimation());
	root.addProperty("batteryPct", arg0.getBatteryPct());
	return root;
    }

}
