package gr.forth.ics.urbanNet.json;

import gr.forth.ics.urbanNet.database.Feedback;
import gr.forth.ics.urbanNet.database.SceintFeedback;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * {@link JsonSerializer} used to serialize {@link Feedback} objects in JSON format, in order to send them to the urbanNet server.
 * @author katsarakis
 */
public class SceintFeedbackJsonSerializer implements JsonSerializer<SceintFeedback> {

    @Override
    public JsonElement serialize(SceintFeedback arg0, Type arg1, JsonSerializationContext arg2) {
	JsonObject root = new JsonObject();
	root.addProperty("id", arg0.getId());
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
	root.addProperty("l_pressure", toInt(arg0.isLpressure()));
	root.addProperty("n_pressure", toInt(arg0.isNpressure()));
	root.addProperty("h_pressure", toInt(arg0.isHpressure()));
	root.addProperty("f_particles", toInt(arg0.isFparticles()));
	root.addProperty("sand", toInt(arg0.isSand()));
	root.addProperty("milky", toInt(arg0.isMilky()));
	root.addProperty("rusty", toInt(arg0.isRusty()));
	root.addProperty("water_name", arg0.getWatername());
	root.addProperty("turbidity", arg0.getTurbidity());
	root.addProperty("temperature", arg0.getTemperature());
	root.addProperty("br", arg0.getBr());
	root.addProperty("ph", arg0.getPh());
	root.addProperty("cl", arg0.getCl());
	root.addProperty("na", arg0.getNa());
	root.addProperty("K", arg0.getK());
	root.addProperty("mg", arg0.getMg2());
	root.addProperty("no", arg0.getNo3());
	root.addProperty("a", arg0.getA());
	root.addProperty("do", arg0.getDO());
	root.addProperty("cod", arg0.getCOD());
	root.addProperty("bod", arg0.getBOD());
	root.addProperty("acidity", arg0.getAcidity());
	root.addProperty("p", arg0.getP());
	root.addProperty("n", arg0.getN());
	root.addProperty("h", arg0.getH());
	root.addProperty("c", arg0.getC());
	root.addProperty("ca", arg0.getCa());
	root.addProperty("ephemeroptera", toInt(arg0.isEphemeroptera()));
	root.addProperty("plecoptera", toInt(arg0.isPlecoptera()));
	root.addProperty("mollusca", toInt(arg0.isMollusca()));
	root.addProperty("trichoptera", toInt(arg0.isTrichoptera()));
	root.addProperty("escherichia_coli", toInt(arg0.isEcoli()));
	root.addProperty("coliform_bacteria", toInt(arg0.isCbacteria()));
	root.addProperty("cryptosporidium", toInt(arg0.isCryptosporidium()));
	root.addProperty("giardia_lamblia", toInt(arg0.isGlamblia()));
	return root;
    }

    private int toInt(boolean b) {
	return b ? 1 : 0;
    }
}
