package io.hellaballer.data.pipedream.speech;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechAlternative;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechModel;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechTimestamp;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Transcript;

public class BluMixSpeechRunner {

	public static Map<String, List<Time>> getTimings(File wavInput) {
		Map<String, List<Time>> map = new HashMap<>();
		SpeechToText service = new SpeechToText();
		service.setUsernameAndPassword("7d7bce45-a04b-47ce-8ea6-517bd1ae017d", "0qbUa1eU4hOY");

		RecognizeOptions options = new RecognizeOptions.Builder().timestamps(true).continuous(true)
				.model(SpeechModel.EN_US_BROADBANDMODEL.getName()).build();
		SpeechResults transcript = service.recognize(wavInput, options).execute();
		for (Transcript result : transcript.getResults()) {
			SpeechAlternative alt = result.getAlternatives().get(0);
			for (SpeechTimestamp ts : alt.getTimestamps()) {
				if (map.containsKey(ts.getWord())) {
					map.get(ts.getWord()).add(new Time(ts.getStartTime(), ts.getEndTime()));
				} else {
					List<Time> lst = new ArrayList<>();
					lst.add(new Time(ts.getStartTime(), ts.getEndTime()));
					map.put(ts.getWord(), lst);
				}
			}
		}
		return map;
	}
}
