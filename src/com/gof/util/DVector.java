package com.gof.util;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class DVector {
	private double[] data;

	// Initialization with data
	public DVector(List<Double> data) {
		int n = data.size();
		this.data = new double[n];
		for (int i=0; i<n; i++)
				this.data[i] = data.get(i);
	}
		
		
	// Initialization with data
	public DVector(double[] data) {
		int n = data.length;
		this.data = new double[n];
		for (int i=0; i<n; i++) {
				this.data[i] = data[i];
		}		
	}
	
	// Set Entry
	public void setEntry(int i, double value) {
		this.data[i] = value;
	}
	
	// Set
	public void set(double value) {
		int n = this.data.length;
		for(int i=0; i<n; i++)
			this.data[i] = value;
	}
	
	// Add to Entry
	public void addToEntry(int i, double value) {
		this.data[i] += value;
	}
	
	// Get Copy
	public DVector copy() {
		return new DVector(this.data.clone());
	}
	
	// Get Entry
	public double getEntry(int i) {
		return this.data[i];
	}
	
	// Get Data
	public double[] getData() {
		return this.data.clone();
	}
	
	// Get Subvector
	public DVector getSubvector(int r, int s) {
		int n = s-r;
		double[] w = new double[n];
		for(int i=0; i<n; i++)
			w[i] = this.data[r+i];
		return new DVector(w);
	}
	
	// Set Subvector
	public void setSubvector(int r, DVector v) {
		int n = v.getDimension();
		for(int i=0; i<n; i++)
			this.data[r+i] = v.getEntry(i);
	}
	
	// Insert Element
	public void insertElement(int k, double value) {
		int n = this.data.length;
		double[] w = new double[n+1];
		for(int i=0; i<k; i++)
			w[i] = this.data[i];
		w[k] = value;
		for(int i=k+1; i<n+1; i++)
			w[i] = this.data[i-1];
		this.data = w;
	}
	
	// Delete Element
	public void deleteElement(int k) {
		int n = this.data.length;
		double[] w = new double[n-1];
		for(int i=0; i<k; i++)
			w[i] = this.data[i];
		for(int i=k; i<n-1; i++)
			w[i] = this.data[i+1];
		this.data = w;
	}
	
	// Get Dimension
	public int getDimension() {
		return this.data.length;
	}
	
	// Diagonal Matrix of Vector
	public DMatrix diag() {
		int n = this.data.length;
		double[][] B = new double[n][n];
		for(int i=0; i<n; i++)
			B[i][i] = this.data[i];
		return new DMatrix(B);
	}
	
	// Vector Addition
	public DVector add(DVector v) {
		int n = v.getData().length;
		double[] w = new double[n];
		for(int i=0; i<n; i++)
				w[i] = this.data[i] + v.getEntry(i);
		return new DVector(w);
	}
	
	// Vector Subtraction
	public DVector subtract(DVector v) {
		return this.add(v.scalarMultiply(-1.));
	}
	
	// Power of Vector
	public DVector power(int p) {
		return this.map(x -> Math.pow(x, p));
	}
	
	// Scalar Multiplication of Vector
	public DVector scalarMultiply(double c) {
		return this.map(x -> x*c);
	}
	
	// Scalar Addition of Vector
	public DVector scalarAdd(double c) {
		return this.map(x -> x+c);
	}
	
	// Element-Wise Vector Multiplication
	public DVector eleMultiply(DVector v) {
		int n = v.data.length;
		double[] w = new double[n];
		for(int i=0; i<n; i++)
				w[i] = this.data[i] * v.getEntry(i); 
		return new DVector(w);
	}
	
	// Element-Wise Vector Division
	public DVector eleDivide(DVector v) {
		int n = v.data.length;
		double[] w = new double[n];
		for(int i=0; i<n; i++)
				w[i] = this.data[i] / v.getEntry(i); 
		return new DVector(w);
	}
	
	// Print Vector
	public void print() {
		int n = this.data.length;
		System.out.println("===print===");
		for (int i=0; i<n; i++)
			System.out.print(this.data[i]+" ");
		System.out.println();
	}
	
	// Dot Product
	public double dotProduct(DVector v) {
		int n = this.data.length;
		double value = 0.;
		for (int i=0; i<n; i++) {
			value += this.data[i] * v.getEntry(i);
		}		
		return value;
	}
	
	// Outer Product
	public DMatrix outerProduct(DVector v) {
		int m = this.data.length;
		int n = v.getData().length;
		double[][] B = new double[m][n];
		for(int i=0; i<m; i++)
			for(int j=0; j<n; j++)
				B[i][j] = this.data[i] * v.getEntry(j);
		return new DMatrix(B);
	}
	
	// Norm
	public double getNorm() {
		return Math.pow(this.dotProduct(this), 0.5);
	}
	
	// Distance
	public double getDistance(DVector v) {
		return this.subtract(v).getNorm();
	}
	
	// Map
	public DVector map(UnaryOperator<Double> fn) {
		int n = this.data.length;
		double[] v = new double[n];
		for(int i=0; i<n; i++)
			v[i] = fn.apply(this.data[i]);
		return new DVector(v);
	}
	
	// Binary Map
	public DVector binaryMap(DVector v, BinaryOperator<Double> fn) {
		int n = this.data.length;
		double[] w = new double[n];
		for(int i=0; i<n; i++)
			w[i] = fn.apply(this.data[i], v.getEntry(i));
		return new DVector(w);
	}
	
	// Cumulative Map
	public DVector cumulativeMap(BinaryOperator<Double> fn) {
		int n = this.data.length;
		double[] v = new double[n];
		v[0] = this.data[0];
		for(int i=1; i<n; i++)
			v[i] = fn.apply(v[i-1], this.data[i]);
		return new DVector(v);
	}
	
	// Cumulative Sum
	public DVector cumulativeSum() {
		return this.cumulativeMap((x, y) -> x+y); 
	}
	
	// Cumulative Product
	public DVector cumulativeProduct() {
		return this.cumulativeMap((x, y) -> x*y); 
	}
	
	// Increment
	public DVector	increment() { 
		int n = this.data.length;
		double[] v = new double[n];
		v[0] = this.data[0];
		for(int i=1; i<n; i++)
			v[i] = this.data[i] - this.data[i-1];
		return new DVector(v);
	}

	// Growth
	public DVector growth() { 
		int n = this.data.length;
		double[] v = new double[n];
		v[0] =  this.data[0];
		for(int i=1; i<n; i++)
			v[i] = this.data[i] / this.data[i-1];
		return new DVector(v);
	}
	
	// Transpose
	public DMatrix transpose() {
		int n = this.data.length;
		double[][] B = new double[1][n];
		for (int i=0; i<n; i++)
				B[0][i] = this.data[i];
		return new DMatrix(B);
	}
	
	// Sum
	public double sum() {
		int n = this.data.length;
		double value = 0.;
		for(int i=0; i<n; i++)
			value += this.data[i];
		return value;
	}
	
	// Mean
	public double mean() {
		return this.sum()/this.data.length;
	}
	
	// Standard Deviation
	public double std() {
		int n = this.getDimension();
		double mean = this.mean();
		double value = 0.;
		for(int i=0; i<n; i++)
			value += Math.pow(this.data[i] - mean, 2);
		value /= n-1;
		return Math.sqrt(value);
	}
	
	// Concatenate
	public DVector concatenate(DVector v) {
		int n = this.data.length;
		int m = v.getDimension();
		double[] w = new double[n+m];
		for(int i=0; i<n; i++)
			w[i] = this.data[i];
		for(int i=0; i<m; i++)
			w[m+i] = v.getEntry(i);
		return new DVector(w);
	}
	
//	// double to String
//	public StringVector toStringVector() {
//		int n = this.data.length;
//		String[] v = new String[n];
//		for(int i=0; i<n; i++)
//			v[i] = String.valueOf(this.data[i]);
//		return new StringVector(v);
//	}
	
	// Vector to Matrix
	public DMatrix toMatrix(int n) {
		int m = this.data.length/n;
		if(this.data.length%n != 0)
			throw new RuntimeException("��ȿ���� ���� �� �����Դϴ�.");
		double[][] A = new double[m][n];
		for(int i=0; i<m; i++)
			for(int j=0; j<n; j++)
				A[i][j] = this.data[n*i+j];
		return new DMatrix(A);
	}
	
	// Rank
	public DVector rank() {
		int n = this.data.length;
		int[] v = new int[n];
		for(int i=0; i<n; i++) {
			v[i] = 0;
			for(int j=0; j<n; j++)
				if(this.data[i] < this.data[j]) v[i]++;
		}
		
		double[] w = new double[n];
		for(int i=0; i<n; i++) {
			for(int j=0; j<n; j++) {
				if(v[i] == j) w[i] = j;
			}
		}
		return new DVector(w);
	}
	
	
	/* Vector Utilities */
	// Vector of 1...n
	public static DVector createRangeVector(int n) {
		double[] v = new double[n];
		for(int i=0; i<n; i++)
			v[i] = i;
		return new DVector(v);
		
	}
	
	// Unit Vector
	public static DVector createUnitVector(int i, int n) {
		double[] v = new double[n];
		v[i] = 1.;
		return new DVector(v);
	}
	
	// Zero Vector of n	
	public static DVector createZeroVector(int n) {
		return new DVector(new double[n]);
	}
	
	// Vector of Ones of n
	public static DVector createOneVector(int n) {
		double[] v = new double[n];
		for(int i=0; i<n; i++)
				v[i] = 1.;
		
		
		return new DVector(v);
	}
	
}
