package gr.forth.ics.urbanNet.json;

import gr.forth.ics.urbanNet.database.Query;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class QueryJsonSerializer implements JsonSerializer<Query>{

	@Override
	public JsonElement serialize(Query arg0, Type arg1, JsonSerializationContext arg2) {
		JsonObject root = new JsonObject();
		root.addProperty("id", arg0.getId());
		root.addProperty("androidDelay" , arg0.getAndroidDelay());
		root.addProperty("networkDelay" , arg0.getNetworkDelay());
		root.addProperty("serverDelay" , arg0.getServerDelay());
		return root;
	}

}
