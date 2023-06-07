package com.gof.util;

public class MathUtil {
	 
	 
	 public static  double[][] multi(double[][] leftMatrix,  double[][] rightMatrix) {
		 int rowNum = leftMatrix.length;
		 int colNum = rightMatrix[0].length;
		 
		 int effectCol = rightMatrix.length;
		 
		 if( leftMatrix[0].length >  rightMatrix.length) {
//			 logger.info("LeftMatrix size is shortened to Matrix Multiply");
		 }
		 
		 double[][] rstMatrix = new double[rowNum][colNum];
		 double temp =0.0;
		 
		 for(int i= 0; i<rowNum; i++) {
	    	for(int j =0; j<colNum; j++) {
//	    		logger.info("TM : {},{},{}", i,j, rightMatrix[i][j]);
	    		for(int k =0; k< effectCol; k++) {
	    			temp = temp + leftMatrix[i][k] * rightMatrix[k][j] ;
	    		}
//	    		logger.info("Multi : {},{},{}", temp);
	    		rstMatrix[i][j] =temp;
	    		temp =0.0;
	    	}
		  }
		 
		  return rstMatrix;
	 }

}
