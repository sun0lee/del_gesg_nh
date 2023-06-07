package com.gof.util;

import java.util.List;

import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class SplineInterpolator {
	
//	public static void main(String[] args) throws Exception {
//		
//		List<Double> x1 = Arrays.asList(1.0, 3.0, 6.0, 9.0, 12.0, 15.0, 18.0);
////		List<Double> y1 = Arrays.asList(8.32575525847206E-5, 0.004373849294047508, 0.02323125310585315, 0.05819259986573744, 0.1369099839550322, 0.26480061696879076, 1.0);
////		List<Double> y1 = Arrays.asList(8.99371493260902E-5, 0.004509255025704291, 0.02338356574615245, 0.058258648720474454, 0.1369270518428403, 0.2648055714352242, 1.0);
//		List<Double> y1 = Arrays.asList(9.665698762257082E-5, 0.004635624553604842, 0.023517819224865524, 0.05831480099259618, 0.1369408998533855, 0.2648094244169797, 1.0);
//
//		for(int i=1; i<=18; i++) log.info("{}, {}", i, SplineInterpolator.createMonotoneCubicSpline(x1,y1).interpolate(i));
//	}	
	
	private final static double ZERO_TOLERANCE = 0.00000000001;	
	private final static int    INTERP_TYPE_CONSTANT = 0;
	private final static int    INTERP_TYPE_LINEAR   = 1;
	private final static int    EXTRAP_TYPE_CONSTANT = 0;
	private final static int    EXTRAP_TYPE_LINEAR   = 1;		
		
	private final List<Double> mX;
	private final List<Double> mY;
	private final double[] mM;
	
	private SplineInterpolator(List<Double> x, List<Double> y, double[] m) {
		mX = x;
		mY = y;
		mM = m;
	}
	

	/**
	 * Creates a monotone cubic spline from a given set of control points.
	 * The spline is guaranteed to pass through each control point exactly. Moreover, assuming the control points are
	 * monotonic (Y is non-decreasing or non-increasing) then the interpolated values will also be monotonic.
	 * This function uses the Fritsch-Carlson method for computing the spline parameters.
	 * http://en.wikipedia.org/wiki/Monotone_cubic_interpolation
	 * @param x   The X component of the control points, strictly increasing.
	 * @param y   The Y component of the control points
	 * @return
	 * @throws IllegalArgumentException if the X or Y arrays are null, have different lengths or have fewer than 2 values.
	 */	
	//for more information...  https://priuss.tistory.com/7	
	//must compare this...     https://stackoverflow.com/questions/37519503/how-to-use-cubic-spline-interpolation-to-get-a-curve-in-java

	public static SplineInterpolator createMonotoneCubicSpline(List<Double> x, List<Double> y) {

		if (x == null || y == null || x.size() != y.size() || x.size() < 2) {
			throw new IllegalArgumentException("There must be at least two control points and the arrays must be of equal length.");
		}

		final int n = x.size();
		double[] d = new double[n-1]; // could optimize this out
		double[] m = new double[n];
		

		// Compute slopes of secant lines between successive points...    	
     	// d[i]: dy/dx
		for (int i=0; i<n-1; i++) {

			double h = x.get(i+1) - x.get(i);
			if (h <= 0) {
				throw new IllegalArgumentException("The control points must all have strictly increasing X values.");
			}
			d[i] = (y.get(i+1) - y.get(i)) / h;			
			//log.info("{}", d[i]);
		}			

		// Initialize the tangents as the average of the secants.
		m[0] = d[0];

		for (int i=1; i<n-1; i++) {
			
			if(Math.signum(d[i-1]*d[i]) < 0) m[i] = 0;   // not exist in original source          
			else m[i] = (d[i-1] + d[i]) * 0.5;
		}
		m[n-1] = d[n-2];		
		//for(int i=0; i<n; i++) log.info("{}", m[i]);
		
		// Update the tangents to preserve monotonicity.
		for (int i=0; i<n-1; i++) {

			if (Math.abs(d[i]) < ZERO_TOLERANCE) {       // successive Y values are equal
				m[i]   = 0;
				m[i+1] = 0;
			} 
			else {				
				double a = m[i]   / d[i];
				double b = m[i+1] / d[i];

				double h = (double) Math.hypot(a, b);
				//log.info("{}, {}, {}, {}", i, m[i], m[i+1], h);

				if (h > 3.0) {
					double t = 3.0 / h;
					m[i]     = t * a * d[i];
					m[i+1]   = t * b * d[i];
				}
			}
		}
		return new SplineInterpolator(x, y, m);
	}

	/**
	 * Interpolates the value of Y = f(X) for given X. Clamps X to the domain of the spline.
	 * 
	 * @param x  The X value.
	 * @return The interpolated Y = f(X) value.
	 */
	public double interpolate(double x) {

		// Handle the boundary cases.
		final int n = mX.size();

		if (Double.isNaN(x)) return x;		
		if (x <= mX.get(0)) return mY.get(0);
		if (x >= mX.get(n-1)) return mY.get(n-1);

		// Find the index 'i' of the last point with smaller X.
		// We know this will be within the spline due to the boundary tests.

		int i = 0;

		while (x >= mX.get(i+1)) {			
			i += 1;
			if (x == mX.get(i)) return mY.get(i);
		}

		// Perform cubic Hermite spline interpolation.
		double h = mX.get(i+1) - mX.get(i);
		double t = (x - mX.get(i)) / h;

		return (mY.get(i) * (1+2*t) + h * mM[i]*t) * (1-t) * (1-t) + (mY.get(i+1) * (3-2*t) + h * mM[i+1] * (t-1)) * t * t;
	}
	
	@Override
	public String toString() {

		StringBuilder str = new StringBuilder();
		final int n = mX.size();

		str.append("[");

		for (int i = 0; i < n; i++) {
			if (i != 0) {
				str.append(", ");
			}

			str.append("(").append(mX.get(i));
			str.append(", ").append(mY.get(i));
			str.append(": ").append(mM[i]).append(")");
		}

		str.append("]");

		return str.toString();
	}
	

	public static SplineInterpolator createCubeInterpolator(List<Double> x, List<Double> y) {		
		return new SplineInterpolator(x, y, new double[x.size()]);		
	}	

	
	public double interpolate3(double x) throws Exception {    	    	
    	
		AkimaSplineInterpolator asi = new AkimaSplineInterpolator();
		PolynomialSplineFunction psf = asi.interpolate(mX.stream().mapToDouble(Double::doubleValue).toArray(), mY.stream().mapToDouble(Double::doubleValue).toArray());
		
		log.info("{}, {}", psf.getKnots(), psf.getPolynomials());
		
		
		return interpolate2(mX.toArray(new Double[0]), mY.toArray(new Double[mY.size()]), x,  EXTRAP_TYPE_CONSTANT, INTERP_TYPE_LINEAR);
    	
    }    
	
	
	public static SplineInterpolator createLinearInterpolator(List<Double> x, List<Double> y) {		
		return new SplineInterpolator(x, y, new double[x.size()]);		
	}	
	
		
	public double interpolate2(double x) throws Exception {    	    	
    	return interpolate2(mX.toArray(new Double[0]), mY.toArray(new Double[mY.size()]), x,  EXTRAP_TYPE_CONSTANT, INTERP_TYPE_LINEAR);
    	
    }
    
    
    private static double interpolate2(Double[] idxs, Double[] values, double idx, int extrapType, int interpType) throws Exception {    	
       
        if(idxs.length != values.length) throw new Exception( "The length of indexes and values must be equal!");
        //if(idxs.length < 2) throw new Exception( "The length of indexes must be 2 at least!");
        
        double idxFirst = idxs[0];
        double idxLast = idxs[idxs.length-1];
        double idxNearby = 0;
        double value = 0.0;
        
        if(idx < idxFirst) {
        	
        	switch(extrapType) {
        	
        	    case EXTRAP_TYPE_CONSTANT: 
        		    value = values[0];
        		    break;
        	    case EXTRAP_TYPE_LINEAR:
        	    	idxNearby = idxs[1];
        	    	value = (values[1] - values[0]) / (idxNearby - idxFirst) * (idx - idxFirst) + values[0];
        	    	break;
        	    default: throw new Exception("Undefined Extraplotation Type!");        	    
        	}        	
        }
        else if(idx > idxLast) {
        	
        	switch(extrapType) {
        	
	    	    case EXTRAP_TYPE_CONSTANT: 
	    		    value = values[values.length-1];	    		    
	    		    break;
	    	    case EXTRAP_TYPE_LINEAR:
	    	    	idxNearby = idxs[idxs.length-2];
        	    	value = (values[values.length-1] - values[values.length-2]) / (idxLast - idxNearby) * (idx - idxNearby) / values[values.length-2];
	    	    	break;
	    	    default: throw new Exception("Undefined Extraplotation Type!");	    	    	
        	}        	
    	}
        else {        	
        	
        	switch(interpType) {        	
        	
        	    case INTERP_TYPE_CONSTANT:        	    	
        	    	
        	    	for(int i=1; i<idxs.length; i++) {
		                if(Math.abs(idx - idxs[i]) < ZERO_TOLERANCE) {            	   
		                    value = values[i];
		                    break;
		                }
		                else if(idx < idxs[i]) {
		                    value = values[i-1];
		                    break;
		                }        	    		
        	    	}        	
        	    	break;
        	    	
        	    case INTERP_TYPE_LINEAR:        	    	
        	    	
        	    	for(int i=1; i<idxs.length; i++) {         	   
        	    		if(Math.abs(idx - idxs[i]) < ZERO_TOLERANCE) {
		                    value = values[i];
		                    break;
		                }
		                else if(idx < idxs[i]) {
		                    value = (values[i]-values[i-1]) / (idxs[i]-idxs[i-1]) * (idx-idxs[i-1]) + values[i-1];
		                    break;
		                }
        	    	}
        	    	break;
        	    	
        	    default: throw new Exception("Undefined Interpolation Type!");
        	}        	
        }        
        return value;            
    }
	
	
	public static double round(double number, int decimalDigit) {
		if(decimalDigit < 0) return Math.round(number);
		return Double.parseDouble(String.format("%." + decimalDigit + "f",  number));
	}	
	
}
