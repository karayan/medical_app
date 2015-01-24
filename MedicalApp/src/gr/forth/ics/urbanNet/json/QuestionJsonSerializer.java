package gr.forth.ics.urbanNet.json;

import java.lang.reflect.Type;
import gr.forth.ics.urbanNet.database.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class QuestionJsonSerializer implements JsonSerializer<Question> {

    @Override
    public JsonElement serialize(Question arg0, Type arg1, JsonSerializationContext arg2) {
	JsonObject root = new JsonObject();
	root.addProperty("id", arg0.getId());
	root.addProperty("answer_index", arg0.getAnswer_id());
	root.addProperty("question_index", arg0.getQuestion_id());
	root.addProperty("duration", arg0.getDuration());
	root.addProperty("question_text", arg0.getQuestion_text());
	root.addProperty("latitude", arg0.getLatitude());
	root.addProperty("longitude", arg0.getLongitude());
	root.addProperty("timestamp", arg0.getTimestamp());
	root.addProperty("answer_text", arg0.getAnswer_text());
	
	return root;
    }
}
