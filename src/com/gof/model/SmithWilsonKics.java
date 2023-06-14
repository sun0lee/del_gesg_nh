package com.gof.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import com.gof.entity.IrCurveSpot;
import com.gof.model.entity.SmithWilsonRslt;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class SmithWilsonKics extends IrModel {
	
	private final static double           ltfrEpsilon       = 0.0001;
	
	private int                           prjYear;
	private int                           prjInterval       = 1;
	private char                          prjTimeUnit       = TIME_UNIT_MONTH;
	private LocalDate[]                   prjDate;
	private double[]                      prjYearFrac;	                         
	                                      
	private double                        ltfr;	
	private double                        ltfrCont;
	private int                           ltfrT;	
	private double                        lastLiquidPoint   = 20;
	
	private double                        alphaApplied      = 0.0;
	private double                        alphaDApplied     = 0.0;
	private int                           alphaItrNum       = 100;
	private double                        kappaApplied      = 0.0;
	
	private double                        alphaPp           = 1.0;
	private double                        alphaDpp          = 0.0;
	private double                        alphaFwd          = 0.0;   
	private double                        alphaFwdT         = 0.0;
	private double                        targetFwd         = 0.0;
	
	private RealMatrix                    zetaColumn;
	
	
	public SmithWilsonKics(LocalDate baseDate, List<IrCurveSpot> irCurveHisList,                boolean isRealNumber, double ltfr, int ltfrT, int prjYear) {
		this(baseDate, irCurveHisList, CMPD_MTD_DISC, isRealNumber, ltfr, ltfrT, prjYear, 1, 100, 1);		
	}	

	public SmithWilsonKics(LocalDate baseDate, List<IrCurveSpot> irCurveHisList, char cmpdType, boolean isRealNumber, double ltfr, int ltfrT, int prjYear, int dayCountBasis) {
		this(baseDate, irCurveHisList, cmpdType     , isRealNumber, ltfr, ltfrT, prjYear, 1, 100, dayCountBasis);		
	}
	
	public SmithWilsonKics(LocalDate baseDate, List<IrCurveSpot> irCurveHisList, char cmpdType, boolean isRealNumber, double ltfr, int ltfrT, int prjYear, int prjInterval, int alphaItrNum, int dayCountBasis) {				
		super();		
		this.baseDate = baseDate;		
		this.setTermStructureBase(irCurveHisList);
		this.setLastLiquidPoint(this.tenor[this.tenor.length-1]);
		this.cmpdType = cmpdType;
		this.isRealNumber = isRealNumber;
		this.ltfr = ltfr;
		this.ltfrT = ltfrT;
		this.prjYear = prjYear;		
		this.prjInterval = prjInterval;
		this.alphaItrNum = alphaItrNum;
		this.dayCountBasis = dayCountBasis;
		this.setSwAttributes();
		this.setProjectionTenor();
	}		
	
	
	public SmithWilsonKics(LocalDate baseDate, Map<Double, Double> termStructure,                boolean isRealNumber, double ltfr, int ltfrT, int prjYear) {
		this(baseDate, termStructure,  CMPD_MTD_DISC, isRealNumber, ltfr, ltfrT, prjYear, 1, 100, 1);		
	}

	public SmithWilsonKics(LocalDate baseDate, Map<Double, Double> termStructure, char cmpdType, boolean isRealNumber, double ltfr, int ltfrT, int prjYear, int dayCountBasis) {
		this(baseDate, termStructure,  cmpdType     , isRealNumber, ltfr, ltfrT, prjYear, 1, 100, dayCountBasis);		
	}	
	
	public SmithWilsonKics(LocalDate baseDate,  Map<Double, Double> termStructure, char cmpdType, boolean isRealNumber, double ltfr, int ltfrT, int prjYear, int prjInterval, int alphaItrNum, int dayCountBasis) {
				
		super();		
		this.baseDate = baseDate;		
		this.setTermStructureBase(termStructure);
		this.setLastLiquidPoint(this.tenor[this.tenor.length-1]);
		this.cmpdType = cmpdType;
		this.isRealNumber = isRealNumber;
		this.ltfr = ltfr;
		this.ltfrT = ltfrT;
		this.prjYear = prjYear;		
		this.prjInterval = prjInterval;
		this.alphaItrNum = alphaItrNum;
		this.dayCountBasis = dayCountBasis;
		this.setSwAttributes();
		this.setProjectionTenor();
	}
	

	public List<SmithWilsonRslt> getSmithWilsonResultList() {
		
		List<SmithWilsonRslt> resultList = new ArrayList<SmithWilsonRslt>();		
		resultList.addAll(this.swProjectionList());
		
		return resultList;
	}
	
	
	private void setSwAttributes() {
		
		this.tenorDate     = new LocalDate[this.tenor.length];
		this.tenorYearFrac = new double[this.tenor.length];
		int yearToMonth    = (this.timeUnit == TIME_UNIT_YEAR) ? 12 : 1;
		
		double toRealScale = this.isRealNumber ? 1.0 : 0.01;
		this.ltfrCont = irDiscToCont(toRealScale * this.ltfr);
		
		for(int i=0; i<this.tenor.length; i++) {			
			this.tenorDate[i] = this.baseDate.plusMonths((long) Math.round(this.tenor[i] * yearToMonth));
			this.tenorYearFrac[i] = getTimeFactor(this.baseDate,  this.tenorDate[i],  this.dayCountBasis);
			this.iRateBase[i] = (this.cmpdType == CMPD_MTD_DISC) ? irDiscToCont(toRealScale*this.iRateBase[i]) : toRealScale*this.iRateBase[i];
//			this.iRateBase[i] = round(this.iRateBase[i], this.decimalDigit);
		}		
//		log.info("{}, {}, {}", this.tenorDate, this.tenorYearFrac, this.iRateBase);
	}
	
	
	private void setProjectionTenor() {
		
		int monthToYear = (this.prjTimeUnit == TIME_UNIT_MONTH) ? 12 : 1;
		int yearToMonth = (this.prjTimeUnit == TIME_UNIT_YEAR)  ? 12 : 1;
		int prjNum      = this.prjYear * monthToYear / ((this.prjInterval > 0) ? this.prjInterval : 1);
		
		this.prjDate      = new LocalDate[prjNum + 1];
		this.prjYearFrac  = new double[prjNum + 1];
		
		for(int i=0; i<this.prjDate.length; i++) {
			
			this.prjDate[i] = this.baseDate.plusMonths((long) Math.round((i+1) * this.prjInterval * yearToMonth));
			this.prjYearFrac[i] = getTimeFactor(this.baseDate,  this.prjDate[i],  this.dayCountBasis);
//			log.info("prjDate: {}, prjYearFrac: {}", prjDate[i] ,prjYearFrac[i]);
		}		
	}
	
	
	private void smithWilsonAlphaFinding() {
		
		for(int i=0; i<this.alphaItrNum; i++) {
			
			if(i==0) {
				this.alphaApplied  = 0.0001;
				this.alphaDApplied = (1.0 - 0.0001) / 4.0;
			}			
			if(i==1) {
				this.alphaApplied  = (1.0 + 0.0001) / 2.0;
			}
			
			RealMatrix tenorCol = MatrixUtils.createColumnRealMatrix(this.tenorYearFrac);
			RealMatrix weight   = MatrixUtils.createRealMatrix(smithWilsonWeight(this.tenorYearFrac, this.tenorYearFrac, this.alphaApplied, this.ltfrCont));
			RealMatrix invWeight = MatrixUtils.inverse(weight);
			
//			if(i == 0) log.info("weight: {}, {}, {}, {}", this.tenorYearFrac, alpha, this.ltfrCont, weight);
			
			double[] pVal = new double[this.tenorYearFrac.length];
			double[] mean = new double[this.tenorYearFrac.length];
			double[] loss = new double[this.tenorYearFrac.length];
			double[] sinh = new double[this.tenorYearFrac.length];
			
			for(int j=0; j<loss.length; j++) {
				pVal[j] = zeroBondUnitPrice(this.iRateBase[j], this.tenorYearFrac[j]);
				mean[j] = zeroBondUnitPrice(this.ltfrCont, this.tenorYearFrac[j]);
				loss[j] = smithWilsonLoss(this.iRateBase[j], this.tenorYearFrac[j], this.ltfrCont);
				sinh[j] = Math.sinh(this.alphaApplied * this.tenorYearFrac[j]);
			}
			
			RealMatrix lossCol = MatrixUtils.createColumnRealMatrix(loss);
			RealMatrix zetaCol = invWeight.multiply(lossCol);		
			RealMatrix sinhCol = MatrixUtils.createColumnRealMatrix(sinh);
			RealMatrix qMatDiag = MatrixUtils.createRealDiagonalMatrix(mean);
			
			double kappaNum = tenorCol.transpose().multiply(qMatDiag).multiply(zetaCol).scalarMultiply(this.alphaApplied).scalarAdd(1.0).getEntry(0,0);
			double kappaDenom = sinhCol.transpose().multiply(qMatDiag).multiply(zetaCol).getEntry(0,0);
			this.kappaApplied = kappaNum / (Math.abs(kappaDenom) < ZERO_DOUBLE ? 1.0 : kappaDenom);
			
			this.alphaPp  = Math.exp(-this.ltfrCont * this.ltfrT) * (kappaNum - Math.exp(-this.alphaApplied * this.ltfrT) * kappaDenom);
			this.alphaDpp = -this.ltfrCont * this.alphaPp + Math.exp(-this.ltfrCont * this.ltfrT) * this.alphaApplied * Math.exp(-this.alphaApplied * this.ltfrT) * kappaDenom;
			this.alphaFwd = -1 / this.alphaPp * this.alphaDpp;		

			this.zetaColumn = zetaCol;
			
			if(i==0) {	
				if(Math.abs(Math.exp(this.ltfrCont) - Math.exp(this.alphaFwd)) < ltfrEpsilon) {
					break;
				}
				else if(this.alphaFwd > this.ltfrCont) {
					this.alphaFwdT = Math.log(Math.exp(this.ltfrCont) + ltfrEpsilon);
				}
				else {
					this.alphaFwdT = Math.log(Math.exp(this.ltfrCont) - ltfrEpsilon);
				}
			}
			else {
				if(this.alphaFwdT < this.ltfrCont) {
					if(this.alphaFwd < this.alphaFwdT) {
						this.alphaApplied = this.alphaApplied + this.alphaDApplied;
					}
					else {
						this.alphaApplied = this.alphaApplied - this.alphaDApplied;
					}					
				}
				else {
					if(this.alphaFwd < this.alphaFwdT) {
						this.alphaApplied = this.alphaApplied - this.alphaDApplied;
					}
					else {
						this.alphaApplied = this.alphaApplied + this.alphaDApplied;
					}					
				}
				this.alphaDApplied *= 0.5;	
			}			
//			if(i<10) log.info("2nd ITR: {}, ALPHA: {}, ALPHA_D: {}", i+1, this.alphaApplied, this.alphaDApplied);
		}		
	}
	
	
	//TODO:
	private List<SmithWilsonRslt> swProjectionList() {
		
		List<SmithWilsonRslt> swResultlList = new ArrayList<SmithWilsonRslt>();			
		this.smithWilsonAlphaFinding();
//		log.info("AlphaOpt: {}, Error: {}", this.alphaApplied, Math.abs(this.alphaFwd - this.ltfrCont));
//		log.info("{}", this.zetaColumn);
		
		double[] df = new double[this.prjYearFrac.length];
		for(int i=0; i<df.length; i++) df[i] = zeroBondUnitPrice(this.ltfrCont,  this.prjYearFrac[i]);
		
		RealMatrix weightPrjTenor = MatrixUtils.createRealMatrix(smithWilsonWeight(this.prjYearFrac, this.tenorYearFrac, this.alphaApplied, this.ltfrCont));		
		RealMatrix dfCol = MatrixUtils.createColumnRealMatrix(df);		
		RealMatrix sigmaCol = weightPrjTenor.multiply(this.zetaColumn);
		RealMatrix priceCol = dfCol.add(sigmaCol);
		
		//log.info("{}", priceCol.getEntry(1,0)) : column index = 1
		double[] priceZcb = new double[this.prjYearFrac.length];
		double[] spotCont = new double[this.prjYearFrac.length];
		double[] fwdCont  = new double[this.prjYearFrac.length];	

		/**
		 * for FSS Excel Validation (i.e. fwdDisc[0] = f(0, 1, 1)), fwdDisc[719] =0.051900547578169
		 */
//		for(int i=0; i<this.prjYearFrac.length; i++) {			
//			priceZcb[i] = priceCol.getEntry(i,0);
//			spotCont[i] = -1.0 / this.prjYearFrac[i] * Math.log(priceZcb[i]);
//		}
//		
//		for(int i=0; i<this.prjYearFrac.length-1; i++) {			
//			fwdCont[i]  = (spotCont[i+1] * this.prjYearFrac[i+1] - spotCont[i] * this.prjYearFrac[i]) / (this.prjYearFrac[i+1] - this.prjYearFrac[i]);			
//			
//			SmithWilsonRslt swResult = new SmithWilsonRslt();
//			
//			swResult.setBaseDate(baseDate.toString());
//			swResult.setResultType("Smith-Wilson");
//			swResult.setScenType("1");
//			swResult.setMatCd(String.format("%s%04d",  this.prjTimeUnit, (i+1) * this.prjInterval ));
//			swResult.setMatTerm(round(this.prjYearFrac[i]));
//			swResult.setDcntFactor(priceZcb[i]);
//			swResult.setSpotCont(round(spotCont[i]));
//			swResult.setFwdCont(round(fwdCont[i]));
//			swResult.setSpotDisc(round(irContToDisc(spotCont[i])));
//			swResult.setFwdDisc(round(irContToDisc(fwdCont[i])));			
//			
//			swResultlList.add(swResult);
//		}
		
		/**
		 * for fwdDisc[0] = f(0, 0, 1) = r(0, 1)), fwdDisc[719] = 0.0518994483
		 */
		boolean flag =false; 
		double tempFwdCont =0.0;
		double tempspotCont =0.0;
		int idx =0;
		for(int i=0; i<this.prjYearFrac.length-1; i++) {			
			priceZcb[i] = priceCol.getEntry(i,0);
			
//			20221019 수정 : Smith Wilson 의 추정이 비정상인 경우, 음의 가격 예외처리
			if(Math.abs(this.alphaFwd - this.ltfrCont) > 0.01 && flag) {
				fwdCont[i]  =0.0;
				if(flag) {
					if(i < 720 ) {
						spotCont[i] = tempspotCont +  (i-idx) * ( this.ltfrCont - tempFwdCont) / ( 720 - idx ) ;			//  target rate linera interpol
						priceZcb[i] = Math.exp(spotCont[i] * -1.0 * this.prjYearFrac[i]);
					}
					else {
						spotCont[i] =  this.ltfrCont;
					}
				}
				else {
					spotCont[i] = -1.0 *  Math.log(priceZcb[i]) / this.prjYearFrac[i] ;
					fwdCont[i]  = (i > 0) ? (spotCont[i] * prjYearFrac[i] - spotCont[i-1] * prjYearFrac[i-1]) / (prjYearFrac[i] - prjYearFrac[i-1]) : spotCont[i];
				}
			}
//			20221019 수정 : Smith Wilson 의 추정이 정상적인 경우임			
			else {
				spotCont[i] = -1.0 *  Math.log(priceZcb[i]) / this.prjYearFrac[i] ;
				fwdCont[i]  = (i > 0) ? (spotCont[i] * prjYearFrac[i] - spotCont[i-1] * prjYearFrac[i-1]) / (prjYearFrac[i] - prjYearFrac[i-1]) : spotCont[i];
				
				
				if(priceZcb[i] < 0 || fwdCont[i]> 0.15 || spotCont[i]> 0.15 ) {
					flag = true;
					tempspotCont =spotCont[i];
					tempFwdCont = spotCont[i];
					idx =i;
				}
			}
			
			SmithWilsonRslt swResult = new SmithWilsonRslt();
			
			swResult.setBaseDate(baseDate.toString());
			swResult.setResultType("Smith-Wilson");
			swResult.setScenType("1");
			swResult.setMatCd(String.format("%s%04d",  this.prjTimeUnit, (i+1) * this.prjInterval ));
			swResult.setMatTerm(round(this.prjYearFrac[i]));
			swResult.setDcntFactor(priceZcb[i]);
			swResult.setSpotCont(round(spotCont[i]));
			swResult.setFwdCont(round(fwdCont[i]));
			swResult.setSpotDisc(round(irContToDisc(spotCont[i])));
			swResult.setFwdDisc(round(irContToDisc(fwdCont[i])));			
			
//			log.info("swResult : {}, {}, {}", spotCont[i], priceZcb[i], swResult.toString());
			swResultlList.add(swResult);
		}
		
		return swResultlList;
	}	
			
	
	private double[][] smithWilsonWeight(double[] prjYearFrac, double[] tenorYearFrac, double alpha, double ltfrCont) {
		
		double[][] weight = new double[prjYearFrac.length][tenorYearFrac.length];
		double min, max;
		
		for(int i=0; i<prjYearFrac.length; i++) {
			for(int j=0; j<tenorYearFrac.length; j++) {
				
				min = Math.min(prjYearFrac[i], tenorYearFrac[j]);
				max = Math.max(prjYearFrac[i], tenorYearFrac[j]);
				//weight[i][j] = Math.exp(-ltfrCont * (prjYearFrac[i] + tenorYearFrac[j])) * (alpha * min - 0.5 * Math.exp(-alpha*max) * (Math.exp(alpha*min) - Math.exp(-alpha*min)));
				weight[i][j] = Math.exp(-ltfrCont * (prjYearFrac[i] + tenorYearFrac[j])) * (alpha * min - Math.exp(-alpha*max) * Math.sinh(alpha*min));				
			}
		}
		return weight;
	}
	
		
	private double smithWilsonLoss(double rateCont, double mat, double ltfrCont) {
		return zeroBondUnitPrice(rateCont, mat) - zeroBondUnitPrice(ltfrCont, mat);
	}
		
}
