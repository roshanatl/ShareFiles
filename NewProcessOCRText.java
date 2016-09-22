package com.walmart.ocr.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Vertex;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class NewProcessOCRText {

	public static void main(String[] args) {
		//Get the TextAnotate google vision response from JSON.
		AnnotateImageResponse annotateImageResponse = createTextEAResponse();
		List<EntityAnnotation> textAnnos = annotateImageResponse.getTextAnnotations();
		String fullText =  textAnnos.get(0).getDescription();
		//System.out.println(fullText);
		
		
		
		String processedText=processX(fullText, annotateImageResponse);
		System.out.println("***************---Text based on Location of words --********");
		System.out.println(processedText);
		
		//Sort based on X & put in map of word:Location.
				System.out.println("***********Sort based on X *************");
				Map<String,BoundingPoly> wordLocationMap = new LinkedHashMap<String, BoundingPoly>();
				Collections.sort(textAnnos, new Comparator<EntityAnnotation>() {

					@Override
					public int compare(EntityAnnotation o1, EntityAnnotation o2) {
						if(null!=o1.getBoundingPoly() && null!=o2.getBoundingPoly() ){
						return o1.getBoundingPoly().getVertices().get(0).getX().compareTo(o2.getBoundingPoly().getVertices().get(0).getX());
						}
						return 0;
					}
				});
				//System.out.println(textAnnos);
				System.out.println("*********End of Sort based on X ***************");
				textAnnos.remove(0);
				//System.out.println(textAnnos);
				for(EntityAnnotation ea :textAnnos){
					
					//System.out.println(ea);
					wordLocationMap.put(ea.getDescription().trim(), ea.getBoundingPoly());
				}
				//End of Sort based on X
		
		//Iterate the Sentences & find its Location using 1st word .
				List<String> sentences = Arrays.asList(fullText.split("\n"));
				Map<String,BoundingPoly> sentenceLocationMap = new LinkedHashMap<String, BoundingPoly>();
				System.out.println("***********Sort Sentences on X*************");
				for(String sentence:sentences){
					//System.out.println(sentence);
					String firstWord=sentence;
					int spacePos = sentence.indexOf(" ");
					if( spacePos != -1){
						firstWord = sentence.substring(0,spacePos);
					}
					sentenceLocationMap.put(sentence,wordLocationMap.get(firstWord.trim()));
				}
				
				
				System.out.println("***********End of Sort Sentences on X *************");
				
				
				for (Map.Entry<String, BoundingPoly> entry : sentenceLocationMap.entrySet()) {
					System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
				}
				
				String arrangedString = arrangeSentences(processedText,sentenceLocationMap);
				//System.out.println("***********Joined Sentences *************");
				//System.out.println(arrangedString);

	}

private static String arrangeSentences(String textWithPosition,
			Map<String, BoundingPoly> sentenceLocationMap) {
	
	/*
	 *  textWithPosition  -->has sentences arranged using tab 
	 *  & white spaces .Each line has one sentence.
	 *  
	 *  sentenceLocationMap --> has sentence & its location from Google Vision.
	 */
	
	// Split the word blocks based on new line.

	List<String> wordBlockList = Arrays.asList(textWithPosition.split("\n"));
	String fisrtString=wordBlockList.get(0);
	char firstChar= fisrtString.trim().charAt(0);
	int noOfSpaces = fisrtString.indexOf(firstChar);
	int wordCount =0 ;
	
	StringBuilder joiner =new StringBuilder();
	for(String sentence:wordBlockList){
		char firstChar1= sentence.trim().charAt(0);
		int noOfSpaces1 = sentence.indexOf(firstChar1);
		int diffSpace=noOfSpaces1-noOfSpaces;
		if(diffSpace<0)
		{
			diffSpace=diffSpace*-1;
		}
		if(diffSpace <5){
			System.out.println(sentence);
		}
		wordCount++;
		if(wordBlockList.size()==wordCount){
			wordCount =0;
			
		}
	
	}
			
		return joiner.toString();
	}

private static String sortX(String processedText, AnnotateImageResponse annotateImageResponse) {
		
		// Split the word blocks based on new line.

		List<String> wordBlockList = Arrays.asList(processedText.split("\n"));
		
		Map<String, BoundingPoly> wordLocationMap = new LinkedHashMap<String, BoundingPoly>();
		List<EntityAnnotation> worldList = annotateImageResponse.getTextAnnotations();
		// Remove the first Word as it is full text in image,
		worldList.remove(0);
		// Form a Map of words & its Location
		for (EntityAnnotation word : worldList) {
			wordLocationMap.put(word.getDescription(), word.getBoundingPoly());
				}

		// Initialize the local variables.
		String startWord = null;
		
		for (String wordBlock : wordBlockList) {
			if (!wordBlock.isEmpty()) {

				// System.out.println("wordBlock : " + wordBlock);
				if (wordBlock.indexOf(" ") == -1) {
					startWord = wordBlock;
				} else {
					startWord = wordBlock.substring(0, wordBlock.indexOf(" "));
				}
				// System.out.println("startWord : " + startWord);

				BoundingPoly locationOfWord = wordLocationMap.get(startWord);
				wordLocationMap.put(wordBlock, locationOfWord);
			}
		}
		System.out.println("********************************************");
		System.out.println("wordLocationMap : " + wordLocationMap);
		System.out.println("********************************************");
		Map<String, BoundingPoly>  sortedwordLocationMap = sortByStartX(wordLocationMap);
		
		System.out.println("sortedwordLocationMap : " + sortedwordLocationMap);
		System.out.println("********************************************");
		
		StringBuilder sortedString = new StringBuilder(); 
		for(Map.Entry<String, BoundingPoly> entry : sortedwordLocationMap.entrySet()){
			sortedString.append(entry.getKey());
			sortedString.append("\n");
		}
		return sortedString.toString();
	}
	
	private static Map<String, BoundingPoly> sortByStartX(Map<String, BoundingPoly> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, BoundingPoly>> list =
                new LinkedList<Map.Entry<String, BoundingPoly>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, BoundingPoly>>() {
            public int compare(Map.Entry<String, BoundingPoly> o1,
                               Map.Entry<String, BoundingPoly> o2) {
            	if(null!=o1.getValue() && null != o2.getValue()){
            		return (o2.getValue().getVertices().get(0).getX()).compareTo(o1.getValue().getVertices().get(0).getX());
            	}
            	else{
            		return 0;
            	}
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, BoundingPoly> sortedMap = new LinkedHashMap<String, BoundingPoly>();
        for (Map.Entry<String, BoundingPoly> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        /*
        //classic iterator example
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }*/


        return sortedMap;
    }

	private static String  processX(String string, AnnotateImageResponse annotateImageResponse) {
		StringBuilder imageTestBuilder = new StringBuilder();

		List<EntityAnnotation> worldList = annotateImageResponse.getTextAnnotations();
		// Remove the first Word as it is full text in image,
		worldList.remove(0);
		Map<String, BoundingPoly> wordLocationMap = new HashMap<String, BoundingPoly>();
		// Form a Map of words & its Location
		for (EntityAnnotation word : worldList) {
			wordLocationMap.put(word.getDescription(), word.getBoundingPoly());
		}

		// Split the word blocks based on new line.

		List<String> wordBlockList = Arrays.asList(string.split("\n"));
		
		//Initialize the local variables.
		String startWord = null;
		Integer prevStartX = 0;
		Integer prexStartY = 0;
		int prevNoOfTabs = 0;

		boolean isNewLine = true;
		for (String wordBlock : wordBlockList) {
			if (!wordBlock.isEmpty()) {

				//System.out.println("wordBlock : " + wordBlock);
				if (wordBlock.indexOf(" ") == -1) {
					startWord = wordBlock;
				} else {
					startWord = wordBlock.substring(0, wordBlock.indexOf(" "));
				}
				//System.out.println("startWord : " + startWord);

				BoundingPoly locationOfWord = wordLocationMap.get(startWord);
				if (null != locationOfWord) {
					Integer startX = locationOfWord.getVertices().get(0).getX();
					Integer startY = locationOfWord.getVertices().get(1).getY();
					//System.out.println("BoundingPoly : " + locationOfWord);
					//System.out.println("StartX : " + startX);

					if (prevStartX != 0) {
						Integer diffInX = startX - prevStartX;
						Integer diffInY = startY - prexStartY;
						if (diffInY > 5 || diffInY < -5) {
							//System.out.println("New Line -- DiffX :" + diffInY);
							imageTestBuilder.append("\n");
							isNewLine = true;
						}
					}

					int noOfTabs = startX / 30;
					//System.out.println("noOfTabs : " + noOfTabs);
					if (!isNewLine) {
						noOfTabs = (noOfTabs - prevNoOfTabs)/2;
					}
					for (int i = 0; i < noOfTabs; i++) {
						imageTestBuilder.append("  ");

					}

					imageTestBuilder.append(wordBlock);
					prevStartX = startX;
					prexStartY = startY;
					isNewLine = false;
//					System.out.println("********************************************");
//					System.out.println(imageTestBuilder.toString());
//					System.out.println("********************************************");
				}
			}
		}
		//System.out.println("********************************************");
		//System.out.println(imageTestBuilder.toString());
		//System.out.println("********************************************");
		return imageTestBuilder.toString();
	}
	private static AnnotateImageResponse createTextEAResponse() {
		String productCreateJson;
		AnnotateImageResponse annotateImageResponse = null;
		try {
			productCreateJson = Resources.toString(
					Resources.getResource("GVision.json"), Charsets.UTF_8);
			JSONObject jsonObj = new JSONObject(productCreateJson);
			Map<String, Object> jsonMap = JsonstringToMap
					.jsonString2Map(jsonObj.toString());
			// System.out.println(jsonMap.get("textAnnotations"));
			JSONArray textJsonArray = new JSONArray(jsonMap.get(
					"textAnnotations").toString());
			List<EntityAnnotation> textEAList = new ArrayList<EntityAnnotation>();
			for (int i = 0; i < textJsonArray.length(); i++) {
				EntityAnnotation ea = new EntityAnnotation();
				ea.setDescription(textJsonArray.getJSONObject(i)
						.get("description").toString());
				ea.setBoundingPoly(CreateBoundingPolicy(textJsonArray
						.getJSONObject(i).get("boundingPoly").toString()));
				// System.out.println(textJsonArray.getJSONObject(i));
				textEAList.add(ea);
			}

			annotateImageResponse = new AnnotateImageResponse();
			annotateImageResponse.setTextAnnotations(textEAList);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return annotateImageResponse;
	}

	@JsonCreator
	public static BoundingPoly CreateBoundingPolicy(String jsonString) {

		BoundingPoly ea = new BoundingPoly();
		try {
			Map<String, Object> jsonMap = JsonstringToMap
					.jsonString2Map(jsonString);
			JSONArray textJsonArray = new JSONArray(jsonMap.get(
					"vertices").toString());
			List<Vertex> vertexList = new ArrayList<Vertex>();
			for (int i = 0; i < textJsonArray.length(); i++) {
			
				Vertex vertex = new Vertex();
				vertex.setX(Integer.parseInt(textJsonArray.getJSONObject(i).get("x").toString()));
				vertex.setY(Integer.parseInt(textJsonArray.getJSONObject(i).get("y").toString()));
				vertexList.add(vertex);
			}
			ea.setVertices(vertexList);
			
			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		

		return ea;
	}

}
