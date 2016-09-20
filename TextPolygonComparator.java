package com.walmart.ocr.util;

import java.util.Comparator;

import com.google.api.services.vision.v1.model.EntityAnnotation;

public class TextPolygonComparator implements Comparator<EntityAnnotation>{

	@Override
	public int compare(EntityAnnotation o1, EntityAnnotation o2) {
		Integer fisrtWordEndX = o1.getBoundingPoly().getVertices().get(1).getX();
		Integer SecondwordStartX = o2.getBoundingPoly().getVertices().get(0).getX();
		Integer diffX  = SecondwordStartX - fisrtWordEndX;
		if(diffX>0){
			//Find Y diff
			Integer fisrtWordEndY = o1.getBoundingPoly().getVertices().get(0).getY();
			Integer SecondwordStartY = o2.getBoundingPoly().getVertices().get(3).getY();
			Integer diffY =  fisrtWordEndX - SecondwordStartY ;
			
			if(diffY>0){
				return (diffY-diffX);
			}
		}
		return 0;
	}

}
