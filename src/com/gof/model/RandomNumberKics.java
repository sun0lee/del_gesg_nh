package com.gof.model;

import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RandomNumberKics extends IrModel {
	
	private int timeIdx;
	
	private int scenIdx;
	
	private int seed;
	
	private double[][] randNum;
	

	public RandomNumberKics(Integer timeIdx, Integer scenIdx, Integer seed) {
		super();
		
		this.timeIdx  = (timeIdx == null ? 1200 : timeIdx);
		this.scenIdx  = (scenIdx == null ? 1000 : scenIdx);
		this.seed     = (seed    == null ? 470  : seed   );
		this.randNum  = new double[this.timeIdx][this.scenIdx];				
	}	
	
	
	public double[][] mersenneTwisterKics() {		
		
	    double JB_TEST_TOL = 5.0;
	    double RUNS_TEST_TOL = 0.04;
//	    double JB_TEST_TOL = 6.0;
//	    double RUNS_TEST_TOL = 0.020;
	    
		GaussianRandomGenerator grg = new GaussianRandomGenerator(new MersenneTwister(Long.valueOf(seed)));		
		this.randNum = new double[this.timeIdx][this.scenIdx];

//		for(int j=0; j<this.scenIdx; j++) {			
//			for(int i=0; i<this.timeIdx; i++) {
//				this.randNum[i][j] = grg.nextNormalizedDouble();				
//			}			
//		}				

		for(int i=0; i<this.timeIdx; i++) {
			for(int j=0; j<this.scenIdx; j++) {
				this.randNum[i][j] = grg.nextNormalizedDouble();
			}
		}
		for(int i=0; i<this.timeIdx; i++) {
			this.randNum[i] = vecToZeroMean(this.randNum[i]);
//			this.randNum[i] = vecToUnitVariance(this.randNum[i]);
		}
		
		for(int k=0; k<1; k++) {
			//TODO:JB Test and re-generation
			for(int i=0; i<this.timeIdx; i++) {
				if(vecJBtest(this.randNum[i]) > JB_TEST_TOL) {
//					log.info("JB {}th, i={}th, {}", k+1, i, vecJBtest(this.randNum[i]));				
					for(int j=0; j<this.scenIdx; j++) {
						this.randNum[i][j] = grg.nextNormalizedDouble();
					}
					this.randNum[i] = vecToZeroMean(this.randNum[i]);
				}
			}
			
			//Runs Test and re-generation
			double[][] randNumT = matTranspose(this.randNum);		
			for(int j=0; j<this.scenIdx; j++) {		
				if(runsTest(randNumT[j]) < RUNS_TEST_TOL) {
//					log.info("Runs {}th, j={}th, {}", k+1, j+1, runsTest(randNumT[j]));
					for(int i=0; i<this.timeIdx; i++) {
						this.randNum[i][j] = grg.nextNormalizedDouble();
					}
				}
			}			
//			for(int i=0; i<this.timeIdx; i++) this.randNum[i] = vecToZeroMean(this.randNum[i]);  //too many adjusting to zero mean results in bad random set 
		}
		for(int i=0; i<this.timeIdx; i++) {			
			this.randNum[i] = vecToZeroMean(this.randNum[i]);
//			this.randNum[i] = vecToUnitVariance(this.randNum[i]);			
		}
		
//		int scenNumGen = (this.scenIdx + 1) / 2;
//		this.randNum   = new double[this.timeIdx][this.scenIdx];
//		
//		for(int i=0; i<this.timeIdx; i++) {
//			for(int j=0; j<scenNumGen; j++) {			
//				
//				double random        = grg.nextNormalizedDouble();
////				random = 0.0;
//				this.randNum[i][j*2] = +random;
//				if(this.scenIdx > j*2 + 1) this.randNum[i][j*2 + 1] = -random;
//			}
//		}
		
		return this.randNum;
	}
	
	
	public double[][] mersenneTwister() {		
		
		GaussianRandomGenerator grg = new GaussianRandomGenerator(new MersenneTwister(Long.valueOf(this.seed)));		
		this.randNum   = new double[this.timeIdx][this.scenIdx];
		
		for(int i=0; i<this.timeIdx; i++) {
			for(int j=0; j<this.scenIdx; j++) {
				this.randNum[i][j] = grg.nextNormalizedDouble();
			}
		}	
		
		for(int i=0; i<this.timeIdx; i++) this.randNum[i] = vecToZeroMean(this.randNum[i]);
		
//		for(int j=0; j<this.scenIdx; j++) {			
//		for(int i=0; i<this.timeIdx; i++) {
//			this.randNum[i][j] = grg.nextNormalizedDouble();				
//		}		
		return this.randNum;		
	}	
	
	
	public double[][] mersenneTwisterFair() {		
		
		GaussianRandomGenerator grg = new GaussianRandomGenerator(new MersenneTwister(Long.valueOf(this.seed)));
		
		int scenNumGen = (this.scenIdx + 1) / 2;
		this.randNum   = new double[this.timeIdx][this.scenIdx];
		
		for(int i=0; i<this.timeIdx; i++) {
			for(int j=0; j<scenNumGen; j++) {			
				
				double random        = grg.nextNormalizedDouble();				
				this.randNum[i][j*2] = +random;
				if(this.scenIdx > j*2 + 1) this.randNum[i][j*2 + 1] = -random;
			}
		}
		return this.randNum;				
	}
		
	
	public double[][] randLinearCongruential() {		

//		double[] randNum = randLinearCongruentDbl((long) Math.pow(2, 31), 65539, 0, seed, this.timeIdx * this.scenIdx);              // for cross-check with R or Excel (considering integer calculation accuracy)
		double[] randNum = randLinearCongruentDbl((long) Math.pow(2, 31), 1103515245, 12345, 800000, this.timeIdx * this.scenIdx);   // for more accurate method
		
		double[] randNumInvCdf = randNumInvCdf(randNum);
//		log.info("Rands' Mean: {}, Rands' Sd: {}", vectMean(randNumInvCdf), vectSd(randNumInvCdf));
		
		this.randNum = new double[this.timeIdx][this.scenIdx];
		
		for(int i=0; i<this.timeIdx; i++) {
			for(int j=0; j<this.scenIdx; j++) {
				this.randNum[i][j] = randNumInvCdf[this.scenIdx*i + j];
			}				
		}
		return this.randNum;
	}
	
}
