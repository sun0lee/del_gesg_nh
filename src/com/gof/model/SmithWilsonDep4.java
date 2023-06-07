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
public class SmithWilsonDep4 extends IrModel {
	
	private final static double           ltfrEpsilon       = 0.0001;
	
	private int                           prjYear;
	private int                           prjInterval       = 1;
	private char                          prjTimeUnit       = TIME_UNIT_MONTH;
	private LocalDate[]                   prjDate;
	private double[]                      prjYearFrac;	                         
	                                      
	private double                        ltfr;
	private double                        ltfrCont;
	private int                           ltfrT;	
	                                      
	private double                        alphaMaxInit      = 1.000;
	private double                        alphaMinInit      = 0.001;
	private int                           alphaItrNum       = 100;
	private double                        lastLiquidPoint   = 20;	                                                        

	private double                        alphaApplied      = 0.0;
	private double                        kappaApplied      = 0.0;
	private double                        alphaMaxApplied   = 0.0;
	private double                        alphaMinApplied   = 0.0;	
	private double                        alphaPp           = 1.0;
	private double                        alphaDpp          = 0.0;
	private double                        alphaFwd          = 0.0;               
	private double                        targetFwd         = 0.0;
	
	private RealMatrix                    zetaColumn;
	
	
	public SmithWilsonDep4(LocalDate baseDate, List<IrCurveSpot> irCurveHisList,                boolean isRealNumber, double ltfr, int ltfrT, int prjYear) {
		this(baseDate, irCurveHisList, CMPD_MTD_DISC, isRealNumber, ltfr, ltfrT, prjYear, 1, 100, 1);		
	}	

	public SmithWilsonDep4(LocalDate baseDate, List<IrCurveSpot> irCurveHisList, char cmpdType, boolean isRealNumber, double ltfr, int ltfrT, int prjYear, int dayCountBasis) {
		this(baseDate, irCurveHisList, cmpdType     , isRealNumber, ltfr, ltfrT, prjYear, 1, 100, dayCountBasis);		
	}
	
	public SmithWilsonDep4(LocalDate baseDate, List<IrCurveSpot> irCurveHisList, char cmpdType, boolean isRealNumber, double ltfr, int ltfrT, int prjYear, int prjInterval, int alphaItrNum, int dayCountBasis) {				
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
	
	
	public SmithWilsonDep4(LocalDate baseDate, Map<Double, Double> termStructure,                boolean isRealNumber, double ltfr, int ltfrT, int prjYear) {
		this(baseDate, termStructure,  CMPD_MTD_DISC, isRealNumber, ltfr, ltfrT, prjYear, 1, 100, 1);		
	}

	public SmithWilsonDep4(LocalDate baseDate, Map<Double, Double> termStructure, char cmpdType, boolean isRealNumber, double ltfr, int ltfrT, int prjYear, int dayCountBasis) {
		this(baseDate, termStructure,  cmpdType     , isRealNumber, ltfr, ltfrT, prjYear, 1, 100, dayCountBasis);		
	}	
	
	public SmithWilsonDep4(LocalDate baseDate,  Map<Double, Double> termStructure, char cmpdType, boolean isRealNumber, double ltfr, int ltfrT, int prjYear, int prjInterval, int alphaItrNum, int dayCountBasis) {
				
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
		resultList.addAll(this.swProjectionList(this.smithWilsonAlphaFinding()));
		
		return resultList;
	}
	
	
	private void setSwAttributes() {
		
		this.tenorDate     = new LocalDate[this.tenor.length];
		this.tenorYearFrac = new double[this.tenor.length];
		int yearToMonth    = (this.timeUnit == TIME_UNIT_YEAR) ? 12 : 1;
		
		double toRealScale = this.isRealNumber ? 1 : 0.01;
		this.ltfrCont = irDiscToCont(toRealScale * this.ltfr);
						
		for(int i=0; i<this.tenor.length; i++) {			
			this.tenorDate[i] = this.baseDate.plusMonths((long) Math.round(this.tenor[i] * yearToMonth));
			this.tenorYearFrac[i] = getTimeFactor(this.baseDate,  this.tenorDate[i],  this.dayCountBasis);
			this.iRateBase[i] = (this.cmpdType == CMPD_MTD_DISC) ? irDiscToCont(toRealScale*this.iRateBase[i]) : toRealScale*this.iRateBase[i];
		}		
//		log.info("{}, {}, {}", this.tenorDate, this.tenorYearFrac, this.iRateBase);
//		log.info("{}, {}, {}", this.tenorDate.length, this.tenorYearFrac.length, this.iRateBase.length);
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
	
	
	private double smithWilsonAlphaFinding() {
		
		for(int i=0; i<this.alphaItrNum; i++) {
			
			if(i==0) {
				this.alphaMaxApplied = this.alphaMaxInit;
				this.alphaMinApplied = this.alphaMinInit;	
			}			
			this.alphaApplied  = round(0.5 * (this.alphaMaxApplied + this.alphaMinApplied), DECIMAL_DIGIT);
			this.smithWilsonZeta(this.alphaApplied, i);			
//			log.info("i:{}, alphaMax:{}, alphaMin:{}, alphaOpt: {}", i, alphaMaxApplied, alphaMinApplied, round(0.5 * (alphaMaxApplied + alphaMinApplied), DECIMAL_DIGIT));	
		}
		log.info("alphaOpt: {}", round(0.5 * (alphaMaxApplied + alphaMinApplied), DECIMAL_DIGIT));
		return round(0.5 * (this.alphaMaxApplied + this.alphaMinApplied), DECIMAL_DIGIT);
	}
	
	
	private void smithWilsonZeta(double alpha, int itrNum) {
		
		RealMatrix tenorCol = MatrixUtils.createColumnRealMatrix(this.tenorYearFrac);
		RealMatrix weight   = MatrixUtils.createRealMatrix(smithWilsonWeight(this.tenorYearFrac, this.tenorYearFrac, alpha, this.ltfrCont));
		RealMatrix invWeight = MatrixUtils.inverse(weight);
		
//		if(itrNum == 0) log.info("weight: {}, {}, {}, {}", this.tenorYearFrac, alpha, this.ltfrCont, weight);
		
		double[] pVal = new double[this.tenorYearFrac.length];
		double[] mean = new double[this.tenorYearFrac.length];
		double[] loss = new double[this.tenorYearFrac.length];
		double[] sinh = new double[this.tenorYearFrac.length];
		
		for(int i=0; i<loss.length; i++) {
			pVal[i] = zeroBondUnitPrice(this.iRateBase[i], this.tenorYearFrac[i]);
			mean[i] = zeroBondUnitPrice(this.ltfrCont, this.tenorYearFrac[i]);
			loss[i] = smithWilsonLoss(this.iRateBase[i], this.tenorYearFrac[i], this.ltfrCont);
			sinh[i] = Math.sinh(alpha * this.tenorYearFrac[i]);
		}
		
		RealMatrix lossCol = MatrixUtils.createColumnRealMatrix(loss);
		RealMatrix zetaCol = invWeight.multiply(lossCol);		
		RealMatrix sinhCol = MatrixUtils.createColumnRealMatrix(sinh);
		RealMatrix qMatDiag = MatrixUtils.createRealDiagonalMatrix(mean);
		
		double kappaNum = tenorCol.transpose().multiply(qMatDiag).multiply(zetaCol).scalarMultiply(alpha).scalarAdd(1.0).getEntry(0,0);
		double kappaDenom = sinhCol.transpose().multiply(qMatDiag).multiply(zetaCol).getEntry(0,0);
		this.kappaApplied = kappaNum / (Math.abs(kappaDenom) < ZERO_DOUBLE ? 1.0 : kappaDenom);		
		
		this.alphaPp  = Math.exp(-this.ltfrCont * this.ltfrT) * (kappaNum - Math.exp(-alpha * this.ltfrT) * kappaDenom);
		this.alphaDpp = -this.ltfrCont * this.alphaPp + Math.exp(-this.ltfrCont * this.ltfrT) * alpha * Math.exp(-alpha * this.ltfrT) * kappaDenom;
		this.alphaFwd = -1 / this.alphaPp * this.alphaDpp;		

		if(itrNum == 0) {				
			this.alphaPp   = Math.exp(-this.ltfrCont * this.lastLiquidPoint) * (kappaNum - Math.exp(-alpha * this.lastLiquidPoint) * kappaDenom);
			this.alphaDpp  = -this.ltfrCont * this.alphaPp + Math.exp(-this.ltfrCont * this.lastLiquidPoint) * alpha * Math.exp(-alpha * this.lastLiquidPoint) * kappaDenom;
			double tempFwd = -1 / this.alphaPp * this.alphaDpp;			
			
			if(tempFwd > this.ltfrCont) this.targetFwd = Math.log(Math.exp(this.ltfrCont) + ltfrEpsilon);
			else this.targetFwd = Math.log(Math.exp(this.ltfrCont) - ltfrEpsilon);			
		}
			
		if(this.targetFwd < this.ltfrCont) {
			if(this.alphaFwd < this.targetFwd) this.alphaMinApplied = alpha;
			else this.alphaMaxApplied = alpha;						
		}
		else {
			if(this.alphaFwd < this.targetFwd) this.alphaMaxApplied = alpha;
			else this.alphaMinApplied = alpha;				
		}
		this.zetaColumn = zetaCol;		
	}
	
	//TODO:
	private List<SmithWilsonRslt> swProjectionList(double alpha) {
		
		List<SmithWilsonRslt> swResultlList = new ArrayList<SmithWilsonRslt>();			
		this.smithWilsonZeta(alpha, this.alphaItrNum);
		
		double[] df = new double[this.prjYearFrac.length];
		for(int i=0; i<df.length; i++) df[i] = zeroBondUnitPrice(this.ltfrCont,  this.prjYearFrac[i]);
		
		RealMatrix weightPrjTenor = MatrixUtils.createRealMatrix(smithWilsonWeight(this.prjYearFrac, this.tenorYearFrac, alpha, this.ltfrCont));		
		RealMatrix dfCol = MatrixUtils.createColumnRealMatrix(df);
		RealMatrix sigmaCol = weightPrjTenor.multiply(this.zetaColumn);
		RealMatrix priceCol = dfCol.add(sigmaCol);
		
		//log.info("{}", priceCol.getEntry(1,0)) : column index = 1
		double[] priceZcb = new double[this.prjYearFrac.length];
		double[] spotCont = new double[this.prjYearFrac.length];
		double[] fwdCont  = new double[this.prjYearFrac.length];	

		/**
		 * for FSS Excel Validation (i.e. fwdDisc[0] = f(0, 1, 1)), fwdDisc[719]
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
		 * for fwdDisc[0] = f(0, 0, 1) = r(0, 1)), fwdDisc[720]
		 */
		for(int i=0; i<this.prjYearFrac.length-1; i++) {			
			priceZcb[i] = priceCol.getEntry(i,0);
			spotCont[i] = -1.0 / this.prjYearFrac[i] * Math.log(priceZcb[i]);
			fwdCont[i]  = (i > 0) ? (spotCont[i] * prjYearFrac[i] - spotCont[i-1] * prjYearFrac[i-1]) / (prjYearFrac[i] - prjYearFrac[i-1]) : spotCont[i];
			
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
