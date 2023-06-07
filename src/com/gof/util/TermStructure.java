package com.gof.util;

import org.apache.commons.math3.distribution.NormalDistribution;

public abstract class TermStructure {
	
	public abstract double bond(double t, int order);

	// Bond Price
	public DVector bond(DVector t, int order) {
		return t.map(x -> this.bond(x, order));
	}
	
	public double bond(double t) {
		return bond(t, 0);
	}
	
	public DVector bond(DVector t) {
		return bond(t, 0);
	}
	
	// Spot Rate
	public double spot(double t) {
		double u = Math.max(t, 0.000001);
		return -1.0 * Math.log(bond(u))* 1/u; 
	}
	
//	public double spot(double t) {
//		double u = Math.max(t, 1e-6);
//		return Math.pow(1/bond(u), 1/u)-1;
//	}
	
	public DVector spot(DVector t) {
		return t.map(x -> this.spot(x));
	}
	
	// instantaneous Forward Rate at t from 0  (Continuously Compounded)
	public double forward(double t, int order) {
		if(order==0)
			return -bond(t, 1)/bond(t);
		else if(order==1)
			return 1/bond(t)*(bond(t, 1)*bond(t, 1)/bond(t)+bond(t, 2));
		else
			throw new RuntimeException("��ȿ���� ���� �����Դϴ�.");
	}

	public DVector forward(DVector t, int order) {
		return t.map(x -> this.forward(x, order));
	}
	
	public double forward(double t) {
		return forward(t, 0);
	}
	
	public DVector forward(DVector t) {
		return forward(t, 0);
	}
	
	// Forward Rate between t1 and t2 (continuous Compounded)
	public double forwardBtw(double t1, double t2) {
		if(t1 > t2) {
			throw new RuntimeException("���� t1 <= t2 �����Դϴ�.");
		} else if (t1 == t2) {
			return this.spot(t1);
		} else {
			return (t2 * this.spot(t2) - t1 * this.spot(t1)) / (t2 - t1);
		}
	}
	
	public double convertToAnnnual(double rate) {
		return Math.exp(rate) - 1.0;
	}
	
	public double convertToCont(double rate) {
		return Math.log(1+rate);
	}
	
//	public double forwardBtw(double t1, double t2) {
//		if(t1 > t2) {
//			throw new RuntimeException("���� t1 <= t2 �����Դϴ�.");
//		} else if (t1 == t2) {
//			return Math.exp(forward(t1))-1;
//		} else {
//			return Math.pow(Math.pow(1+this.spot(t2), t2)/Math.pow(1+this.spot(t1), t1), 1/(t2-t1))-1;
//		}
//	}
	// 1M-Forward Rate (Annually Compounded)
	public double forward1M(double t) {
		if(t == 0) {
			return spot(1.0/12.0);
		}
		return forwardBtw(t, t+1./12.);
	}
	
	public DVector forward1M(DVector t) {
		return t.map(x -> this.forward1M(x));
	}
	
	// Forward Swap Rate
	public double fswap(double start, double tenor, double tau) {
		int l = (int)(tenor/tau);
		double term1 = (bond(start) - bond(start+tenor))/tau;
		double term2 = .0;
		for(int i=1; i<=l; i++)
			term2 += bond(start+tau*i);
		return term1/term2;
	}
	
	public double fswap(double start, double tenor) {
		return fswap(start, tenor, 0.25);
	}
	
	// Forward Swap Terms
	public DVector fswapTerms(double start, double tenor, double tau) {
		int l = (int)(tenor/tau);
		DVector t = DVector.createZeroVector(l);
		for(int i=0; i<l; i++) 
			t.setEntry(i, tau*(i+1)+start);
		return t;
	}
	
	public DVector fswapTerms(double start, double tenor) {
		return fswapTerms(start, tenor, 0.25);
	}
	
	// Forward Swap Cash Flow
	public DVector fswapCashFlow(double start, double tenor, double tau) {
		int l = (int)(tenor/tau);
		DVector cf = DVector.createZeroVector(l);
		cf.set(fswap(start, tenor)*tau);
		cf.addToEntry(l-1, 1);
		return cf;
	}
	
	public DVector fswapCashFlow(double start, double tenor) {
		return fswapCashFlow(start, tenor, 0.25);
	}
	
	// Black Receiver Swaption Price
	public double rsBlack(double maturity, double tenor, double blackVol) {
		double term1 = bond(maturity) - bond(maturity+tenor);
		double d1 = 0.5*blackVol*Math.sqrt(maturity);
		double cumProb = (new NormalDistribution()).cumulativeProbability(d1); 
		return term1*(2*cumProb-1);
	}
	
}
